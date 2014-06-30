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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import models.City;
import models.CityList;
import models.Dogpark;
import models.DogparkSignup;
import models.DogparkSignupList;
import ninja.Result;
import ninja.Results;
import ninja.Router;
import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import ninja.params.Param;
import ninja.params.PathParam;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;

//@FilterWith(LatencySimulatorFilter.class)
@Singleton
public class DogparkController {

	public static final String RIAK_HOST = "riak.host";
	public static final String RIAK_PORT = "riak.port";

	public static final String BUCKET_NAME = "dogparkmeetups";
	public static final String KEY_CITIES = "cities";

	public static final String VIEW_404_NOT_FOUND = "views/system/404notFound.ftl.html";

	private final Logger logger;
	private final NinjaProperties properties;
	private final Router router;

	private IRiakClient riakClient;
	private Bucket bucket;

	@Inject
	public DogparkController(Logger logger, NinjaProperties properties, Router router) {
		this.logger = logger;
		this.properties = properties;
		this.router = router;
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

	public Result index() {
		return Results.html();
	}

	public Result dogparkList() throws RiakRetryFailedException {
		CityList cities = bucket.fetch(KEY_CITIES, CityList.class).execute();
		return Results.html().render("cities", cities);
	}

	public Result dogpark(@PathParam("id") String dogparkId) throws RiakRetryFailedException {
		if (dogparkId == null) {
			return Results.notFound().html().template(VIEW_404_NOT_FOUND);
		}

		Optional<Dogpark> dogpark = getDogpark(dogparkId);
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

		final Instant timeLower = YearMonth.parse(yearMonth).atDay(1).minusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC);
		final Instant timeUpper = YearMonth.parse(yearMonth).atDay(1).plusMonths(6).atStartOfDay().toInstant(ZoneOffset.UTC);
		List<DogparkSignup> signups = bucket.fetch(dogparkId, DogparkSignupList.class).execute().stream()
				.filter(signup -> {
					Instant arrival = signup.arrivalTime.toInstant();
					return arrival.isAfter(timeLower) && arrival.isBefore(timeUpper);
				})
				.collect(Collectors.toList());

		return Results.json().render(signups);
	}

	public Result signup(
			@PathParam("id") String dogparkId,
			@Param("date") String date) throws RiakRetryFailedException {

		// Parse to validate
		LocalDate localDate = LocalDate.parse(date);

		Optional<Dogpark> dogpark = getDogpark(dogparkId);
		if (dogpark.isPresent()) {
			return Results.html().render("dogpark", dogpark.get()).render("date", date);
		}
		return Results.notFound().html().template(VIEW_404_NOT_FOUND);
	}

	public Result doSignupPost(
			@PathParam("id") String dogparkId,
			@Param("date") String date,
			@Param("timeOfArrival") String timeOfArrival,
			DogparkSignup newSignup) throws RiakRetryFailedException {

		timeOfArrival = timeOfArrival.replace('.', ':');
		LocalDateTime arrivalTimestamp = LocalDateTime.parse(date + " " + timeOfArrival,
				DateTimeFormatter.ofPattern("yyyy-MM-dd H:m"));

		ZoneId zoneId = ZoneId.of("Europe/Helsinki");
		newSignup.arrivalTime = Date.from(arrivalTimestamp.toInstant(ZonedDateTime.now(zoneId).getOffset()));

		DogparkSignupList signups = bucket.fetch(dogparkId, DogparkSignupList.class).execute();
		signups.add(newSignup);
		bucket.store(dogparkId, signups).execute();

		return Results.redirect("/dogparks/" + dogparkId);
	}

	public Result cancelSignup() {
		return Results.html();
	}

	public Result setupTables() throws RiakException {
		setupKuopio();

		return Results.text().renderRaw("Tables ok");
	}

	private Optional<Dogpark> getDogpark(String dogparkId) throws RiakRetryFailedException {
		Optional<Dogpark> dogpark = bucket.fetch(KEY_CITIES, CityList.class).execute().stream()
				.flatMap(city -> city.dogparks.stream())
				.filter(park -> dogparkId.equalsIgnoreCase(park.id))
				.findFirst();
		return dogpark;
	}

	private void setupKuopio() throws RiakException {
		bucket.delete(KEY_CITIES).execute();
		bucket.keys().forEach(key -> {
				try {
					bucket.delete(key).execute();
				} catch (RiakException riakex) {}
			});

		CityList cities = new CityList();

		City kuopio = new City("kuopio", "Kuopio");
		cities.add(kuopio);

		Dogpark neulamaki = new Dogpark("kuopio-neulamaki", "Neulamäen koirapuisto", 62.887032, 27.609753);
		Dogpark rypysuo = new Dogpark("kuopio-rypysuo", "Rypysuon koirapuisto", 62.918761, 27.636628);
		kuopio.dogparks.addAll(Arrays.asList(neulamaki, rypysuo));

		bucket.store(KEY_CITIES, cities).execute();

		DogparkSignupList neulamakiSignups = new DogparkSignupList();
		neulamakiSignups.add(
				new DogparkSignup(
						Date.from(Instant.now().plus(Duration.ofDays(2))),
						"Seropi",
						DogparkSignup.KG_1_TO_5,
						true
				)
		);
		neulamakiSignups.add(
				new DogparkSignup(
						Date.from(Instant.now().plus(Duration.ofDays(2))),
						"Kääpiosnautseri",
						DogparkSignup.KG_5_TO_10,
						false
				)
		);
		bucket.store(neulamaki.id, neulamakiSignups).execute();


		DogparkSignupList rypysuoSignups = new DogparkSignupList();
		rypysuoSignups.add(
				new DogparkSignup(
						Date.from(Instant.now().plus(Duration.ofDays(5))),
						"Seropi",
						DogparkSignup.KG_40_PLUS,
						true
				)
		);
		bucket.store(rypysuo.id, rypysuoSignups).execute();
	}

}
