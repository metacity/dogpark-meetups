/*
 * The MIT License
 *
 * Copyright 2014 Mikko Oksa.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package controllers;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.RiakException;
import com.basho.riak.client.RiakFactory;
import com.basho.riak.client.RiakRetryFailedException;
import com.basho.riak.client.bucket.Bucket;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import models.CityList;
import models.Dogpark;
import models.DogparkSignup;
import models.DogparkSignupList;
import ninja.Result;
import ninja.Results;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.params.Param;
import ninja.params.PathParam;
import ninja.postoffice.Mail;
import ninja.postoffice.Postoffice;
import ninja.session.FlashScope;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;

@Singleton
public class DogparkController {

	public static final String RIAK_HOST = "riak.host";
	public static final String RIAK_PORT = "riak.port";

	public static final String BUCKET_NAME = "dogparkmeetups";
	public static final String KEY_CITIES = "cities";

	public static final String VIEW_404_NOT_FOUND = "views/system/404notFound.ftl.html";

	// Guice injected fields
	private final Logger logger;
	private final NinjaProperties properties;
	private final Provider<Mail> mailProvider;
	private final Postoffice postoffice;

	private IRiakClient riakClient;
	private Bucket bucket;

	@Inject
	public DogparkController(
			Logger logger, 
			NinjaProperties properties, 
			Provider<Mail> mailProvider, 
			Postoffice postoffice) {
		
		this.logger = logger;
		this.properties = properties;
		this.mailProvider = mailProvider;
		this.postoffice = postoffice;
	}

	@Start(order = 10)
	public void initRiakClientAndBucket() throws RiakException {
		String host = properties.getWithDefault(RIAK_HOST, "127.0.0.1");
		int port = properties.getIntegerWithDefault(RIAK_PORT, 8087);

		logger.info("Initializing RiakClient on " + host + ":" + port + "...");
		riakClient = RiakFactory.pbcClient(host, port);
		bucket = riakClient.fetchBucket(BUCKET_NAME).execute();
	}

	@Dispose(order = 10)
	public void shutdownRiakClient() {
		if (riakClient != null) {
			logger.info("Shutting down RiakClient...");
			riakClient.shutdown();
		}
	}
	
	public Result allDogparks() throws RiakRetryFailedException {
		CityList cities = bucket.fetch(KEY_CITIES, CityList.class).execute();
		List<Dogpark> dogparks = cities.stream()
				.flatMap(city -> city.dogparks.stream())
				.collect(Collectors.toList());
		return Results.json().render(dogparks);
	}

	public Result dogparkListPage() throws RiakRetryFailedException {
		CityList cities = bucket.fetch(KEY_CITIES, CityList.class).execute();
		return Results.html().render("cities", cities);
	}

	public Result dogparkPage(@PathParam("id") String dogparkId) throws RiakRetryFailedException {
		if (dogparkId == null) {
			return Results.notFound().html().template(VIEW_404_NOT_FOUND);
		}

		Optional<Dogpark> dogpark = getDogparkFromDb(dogparkId);
		if (dogpark.isPresent()) {
			return Results.html().render("dogpark", dogpark.get());
		} else {
			return Results.notFound().html().template(VIEW_404_NOT_FOUND);
		}
	}

	public Result dogparkSignups(
			@PathParam("id") String dogparkId,
			@Param("yearMonth") String yearMonth) throws RiakRetryFailedException {

		if (dogparkId == null) {
			return Results.notFound().json();
		}

		Instant timeLower = YearMonth.parse(yearMonth)
				.atDay(1)
				.minusMonths(1)
				.atStartOfDay()
				.toInstant(ZoneOffset.UTC);
		Instant timeUpper = YearMonth.parse(yearMonth)
				.atDay(1)
				.plusMonths(6)
				.atStartOfDay()
				.toInstant(ZoneOffset.UTC);
		
		DogparkSignupList allSignups = bucket.fetch(dogparkId, DogparkSignupList.class).execute();
		if (allSignups != null) { // Might very well be empty
			List<DogparkSignup> signups = allSignups.stream()
				.filter(signup -> {
					Instant arrival = signup.arrivalTime.toInstant();
					return arrival.isAfter(timeLower) && arrival.isBefore(timeUpper);
				})
				.peek(signup -> signup.cancellationCode = null)
				.collect(Collectors.toList());
			return Results.json().render(signups);
			
		} else {
			return Results.json().render(Collections.emptyList());
		}
		
	}

	public Result signupPage(
			@PathParam("id") String dogparkId,
			@Param("date") String date) throws RiakRetryFailedException {

		// Parse to validate
		LocalDate localDate = LocalDate.parse(date.trim());

		Optional<Dogpark> dogpark = getDogparkFromDb(dogparkId);
		if (dogpark.isPresent()) {
			return Results.html().render("dogpark", dogpark.get()).render("date", date);
		}
		return Results.notFound().html().template(VIEW_404_NOT_FOUND);
	}

	public Result doSignupPost(
			FlashScope flashScope,
			@PathParam("id") String dogparkId,
			@Param("date") String date,
			@Param("timeOfArrival") String timeOfArrival,
			@Param("email") String email,
			DogparkSignup newSignup) throws RiakRetryFailedException {
		
		Optional<Dogpark> dogpark = getDogparkFromDb(dogparkId);
		if (!dogpark.isPresent()) {
			return Results.notFound().html().template(VIEW_404_NOT_FOUND);
		}
		
		// Escape against HTML 
		Escaper htmlEscaper = HtmlEscapers.htmlEscaper();
		newSignup.dogBreed = htmlEscaper.escape(newSignup.dogBreed);
		newSignup.dogName = htmlEscaper.escape(newSignup.dogName);
		newSignup.dogWeightClass = htmlEscaper.escape(newSignup.dogWeightClass);
		
		// Generate UUID cancellation code
		newSignup.generateCancellationCode();

		// Parse and set arrival time
		timeOfArrival = timeOfArrival.trim().replace('.', ':');
		LocalDateTime arrivalTimestamp = LocalDateTime.parse(date + " " + timeOfArrival,
				DateTimeFormatter.ofPattern("yyyy-MM-dd H:m"));
		ZoneId zoneId = ZoneId.of("Europe/Helsinki");
		newSignup.arrivalTime = Date.from(arrivalTimestamp.toInstant(ZonedDateTime.now(zoneId).getOffset()));

		// Add to signups list
		DogparkSignupList signups = bucket.fetch(dogparkId, DogparkSignupList.class).execute();
		if (signups == null) {
			signups = new DogparkSignupList();
		}
		signups.add(newSignup);
		bucket.store(dogparkId, signups).execute();
		
		// If wanted, send cancellation code via email
		if (!Strings.isNullOrEmpty(email)) {
			try {
				sendCancellationCodeViaEmail(email, dogpark.get(), newSignup);
			} catch (Exception ex) {
				logger.error(ex.toString());
			}
		}

		flashScope.put("signupSuccessfulMsg", getCancellationCodeMessage(dogpark.get(), newSignup));
		return Results.redirect("/dogparks/" + dogparkId);
	}

	public Result doCancelSignupPost(
			FlashScope flashScope,
			@Param("cancellationCode") String publicCancellationCode) throws RiakRetryFailedException {
		
		boolean cancelSuccessful = false;
		
		String decoded;
		try {
			decoded = new String(
					Base64.getDecoder().decode(publicCancellationCode),
					StandardCharsets.ISO_8859_1
			);
		} catch (IllegalArgumentException iaex) {
			decoded = "";
		}
		
		String[] parts = decoded.split(":");
		if (parts.length >= 2) {
			String dogparkId = parts[0];
			String cancellationCode = parts[1];

			DogparkSignupList signups = bucket.fetch(dogparkId, DogparkSignupList.class).execute();
			if (signups != null) {
				Optional<DogparkSignup> cancellableSignup = signups.stream()
						.filter(signup -> signup.cancellationCode.equals(cancellationCode))
						.findFirst();
				if (cancellableSignup.isPresent()) {
					signups.remove(cancellableSignup.get());
					bucket.store(dogparkId, signups).execute();
					cancelSuccessful = true;
					
					Optional<Dogpark> dogpark = getDogparkFromDb(dogparkId);
					flashScope.put("cancellationMsg", getCancellationSuccessMsg(dogpark.get(), cancellableSignup.get()));
				}
			}
		}

		if (cancelSuccessful) {
			flashScope.success("cancel.success");
		} else {
			flashScope.error("cancel.invalidCancellationCode");
		}
		return Results.redirect("/cancel");
	}

	private Optional<Dogpark> getDogparkFromDb(String dogparkId) throws RiakRetryFailedException {
		Optional<Dogpark> dogpark = bucket.fetch(KEY_CITIES, CityList.class).execute().stream()
				.flatMap(city -> city.dogparks.stream())
				.filter(park -> dogparkId.equalsIgnoreCase(park.id))
				.findFirst();
		return dogpark;
	}
	
	private void sendCancellationCodeViaEmail(String email, Dogpark dogpark, DogparkSignup signup) throws Exception {
		Mail mail = mailProvider.get();
		mail.setSubject("Peruutuskoodi");
		mail.addTo(email);
		mail.setFrom("koirapuistomiitit@gmail.com");
		mail.setCharset("utf-8");		
		mail.setBodyText(getCancellationCodeMessage(dogpark, signup));
		postoffice.send(mail);
	}
	
	private static String formPublicCancellationCode(String dogparkId, DogparkSignup signup) {
		String rawCode = dogparkId + ":" + signup.cancellationCode;
		String result = Base64.getEncoder().encodeToString(rawCode.getBytes(StandardCharsets.ISO_8859_1));
		return result;
	}
	
	private static String getCancellationCodeMessage(Dogpark dogpark, DogparkSignup signup) {
		String publicCancellationCode = formPublicCancellationCode(dogpark.id, signup);
		return "Peruutuskoodisi koirapuiston \"" + dogpark.name + "\" ilmoittautumiseen \"" 
				+ signup.toString() + "\" on: " + publicCancellationCode;
	}
	
	private static String getCancellationSuccessMsg(Dogpark dogpark, DogparkSignup signup) {
		String publicCancellationCode = formPublicCancellationCode(dogpark.id, signup);
		return "Peruutettu koirapuiston \"" + dogpark.name + "\" ilmoittautuminen \"" 
				+ signup.toString() + "\".";
	}
	
}
