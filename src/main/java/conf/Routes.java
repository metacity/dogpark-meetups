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

package conf;

import controllers.DogparkController;
import ninja.AssetsController;
import ninja.Results;
import ninja.Router;
import ninja.application.ApplicationRoutes;

public class Routes implements ApplicationRoutes {

	@Override
	public void init(Router router) {

		router.GET().route("/cancel").with(Results.html().template("views/DogparkController/cancelSignupPage.ftl.html"));
		router.POST().route("/cancel").with(DogparkController.class, "doCancelSignupPost");
		
		router.GET().route("/dogparks/{id}/signup").with(DogparkController.class, "signupPage");
		router.POST().route("/dogparks/{id}/signup").with(DogparkController.class, "doSignupPost");

		router.GET().route("/dogparks/{id}/signups").with(DogparkController.class, "dogparkSignups");

		router.GET().route("/dogparks/{id}").with(DogparkController.class, "dogparkPage");
		router.GET().route("/dogparks").with(DogparkController.class, "dogparkListPage");
		
		router.GET().route("/map").with(Results.html().template("views/DogparkController/map.ftl.html"));
		router.GET().route("/dogparks.json").with(DogparkController.class, "allDogparks");
		router.GET().route("/").with(Results.html().template("views/DogparkController/index.ftl.html"));


		///////////////////////////////////////////////////////////////////////
		// Assets (pictures / javascript)
		///////////////////////////////////////////////////////////////////////
		router.GET().route("/assets/{fileName: .*}").with(AssetsController.class, "serveStatic");
	}

}
