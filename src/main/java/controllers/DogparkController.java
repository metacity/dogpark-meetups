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

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.annotation.Transactional;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import models.City;
import models.DayEvent;
import models.DaySummary;
import models.Dogpark;
import models.DogparkSignup;
import ninja.Result;
import ninja.Results;
import ninja.params.Param;
import ninja.params.PathParam;

//@FilterWith(LatencySimulatorFilter.class)
@Singleton
public class DogparkController {

	private final Logger logger;
	private final EbeanServer ebeanServer;

	@Inject
	public DogparkController(Logger logger, EbeanServer ebeanServer) {
		this.logger = logger;
		this.ebeanServer = ebeanServer;
	}

	public Result index() {
		return Results.html();
	}

	public Result dogparkList() {
		List<City> cities = ebeanServer.find(City.class).fetch(City.FETCH_DOGPARKS).findList();
		return Results.html().render("cities", cities);
	}

	public Result dogpark(@PathParam("id") Long dogparkId) {
		if (dogparkId == null) {
			return Results.html().template("views/system/404notFound.ftl.html");
		}

		Dogpark dogpark = ebeanServer.find(Dogpark.class, dogparkId);
		if (dogpark != null) {
			return Results.html().render("dogpark", dogpark);
		} else {
			return Results.html().template("views/system/404notFound.ftl.html");
		}
	}

	public Result dogparkSignups(
			@PathParam("id") Long dogparkId,
			@Param("year") Integer year,
			@Param("month") Integer month) {

		if (dogparkId == null) {
			return Results.notFound().json();
		}

		LocalDateTime dateLowerBound = LocalDateTime.of(year, month, 1, 0, 0).minusMonths(1);
		LocalDateTime dateUpperBound = dateLowerBound.plusMonths(6);
		List<DogparkSignup> signups = ebeanServer.find(DogparkSignup.class)
				.where()
					.eq(DogparkSignup.COL_DOGPARK_ID, dogparkId)
					.ge(DogparkSignup.COL_ARRIVAL_TIME, Timestamp.valueOf(dateLowerBound))
					.lt(DogparkSignup.COL_ARRIVAL_TIME, Timestamp.valueOf(dateUpperBound))
				.orderBy(DogparkSignup.COL_ARRIVAL_TIME)
				.findList();
		Map<String, DaySummary> daySummaries = groupByDate(signups);
		return Results.json().render(daySummaries);
	}

	private static Map<String, DaySummary> groupByDate(List<? extends DayEvent> monthSignups) {
		Map<String, List<DayEvent>> daySignups = monthSignups.stream().collect(
			Collectors.groupingBy(DayEvent::getDate)
		);

		Map<String, DaySummary> daySummaries = Maps.newHashMap();
		daySignups.entrySet().stream().forEach((entry) -> {
			daySummaries.put(entry.getKey(), new DaySummary(entry.getValue()));
		});

		return daySummaries;
	}

	public Result signup(
			@PathParam("id") Long dogparkId,
			@Param("date") String date,
			@Param("timeOfArrival") String timeOfArrival,
			@Param("dogWeightClass") String weightClass,
			DogparkSignup signup) {

		Dogpark dogpark = new Dogpark();
		dogpark.setId(dogparkId);
		signup.setDogpark(dogpark);

		signup.setDogWeightClass(DogparkSignup.DogWeightClass.valueOf(weightClass));

		timeOfArrival = timeOfArrival.replace('.', ':');
		LocalDateTime arrivalTimestamp = LocalDateTime.parse(date + " " + timeOfArrival,
				DateTimeFormatter.ofPattern("yyyy-MM-dd H:m"));
		signup.setArrivalTime(Timestamp.from(arrivalTimestamp.toInstant(ZoneOffset.UTC)));

		ebeanServer.save(signup);
		return Results.json().render(signup);
	}

	@Transactional
	public Result setupTables() {
		setupKuopio();

		return Results.text().renderRaw("Tables ok");
	}

	private void setupKuopio() {
		City kuopio = new City("Kuopio");

		Dogpark neulamaki = new Dogpark("Neulamäen koirapuisto", 62.887032, 27.609753, kuopio);
		Dogpark rypysuo = new Dogpark("Rypysuon koirapuisto", 62.918761, 27.636628, kuopio);

		List<DogparkSignup> signUps = Arrays.asList(
				new DogparkSignup(
						Timestamp.from(Instant.now().plus(Duration.ofDays(2))),
						"Seropi",
						DogparkSignup.DogWeightClass.KG_1_TO_5,
						true,
						neulamaki
				),
				new DogparkSignup(
						Timestamp.from(Instant.now().plus(Duration.ofDays(2))),
						"Kääpiosnautseri",
						DogparkSignup.DogWeightClass.KG_5_TO_10,
						false,
						neulamaki
				),
				new DogparkSignup(
						Timestamp.from(Instant.now().plus(Duration.ofDays(5))),
						"Seropi",
						DogparkSignup.DogWeightClass.KG_40_PLUS,
						true,
						rypysuo
				)
		);

		ebeanServer.save(kuopio);
		ebeanServer.save(neulamaki);
		ebeanServer.save(rypysuo);
		ebeanServer.save(signUps);
	}

}
