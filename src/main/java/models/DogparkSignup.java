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

package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.UUID;

public class DogparkSignup {

	public static final String KG_1_TO_5 = "1-5 kg";
	public static final String KG_5_TO_10 = "5-10 kg";
	public static final String KG_10_TO_15 = "10-15 kg";
	public static final String KG_15_TO_25 = "15-25 kg";
	public static final String KG_25_TO_40 = "25-40 kg";
	public static final String KG_40_PLUS = "40+ kg";

	public Date arrivalTime;

	public String dogBreed;

	public String dogWeightClass;

	public boolean dogIsMale;

	@JsonIgnore
	public String cancellationCode;

	public DogparkSignup() {
	}

	public DogparkSignup(Date arrivalTime, String dogBreed, String dogWeightClass, boolean dogIsMale) {
		this.arrivalTime = arrivalTime;
		this.dogBreed = dogBreed;
		this.dogWeightClass = dogWeightClass;
		this.dogIsMale = dogIsMale;

		this.cancellationCode = UUID.randomUUID().toString();
	}

}
