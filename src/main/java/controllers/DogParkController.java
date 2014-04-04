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
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.logging.Logger;
import models.City;
import models.DogPark;
import ninja.Result;
import ninja.Results;
import ninja.params.Param;


@Singleton
public class DogParkController {

	private final Logger logger;
	private final EbeanServer ebeanServer;

	@Inject
	public DogParkController(Logger logger, EbeanServer ebeanServer) {
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

	public Result dogpark(@Param("id") Long dogParkId) {
		if (dogParkId == null) {
			return Results.html().template("views/system/404notFound.ftl.html");
		}

		DogPark dogPark = ebeanServer.find(DogPark.class, dogParkId);
		if (dogPark != null) {
			return Results.html().render("dogPark", dogPark);
		} else {
			return Results.html().template("views/system/404notFound.ftl.html");
		}
	}

	@Transactional
	public Result setupTables() {
		City kuopio = new City("Kuopio");
		List<DogPark> kuopioDogParks = Lists.newArrayList(
				new DogPark("Neulamäen koirapuisto", 62.887032, 27.609753, kuopio),
				new DogPark("Rypysuon koirapuisto", 62.918761, 27.636628, kuopio)
		);
		ebeanServer.save(kuopio);
		ebeanServer.save(kuopioDogParks);
		return Results.text().renderRaw("Tables ok");
	}

}
