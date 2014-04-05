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

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "dogpark_signup")
public class DogParkSignup {

	public static final String TABLE_NAME = "dogpark_signup";

	public static final String COL_ARRIVAL_TIME = "arrival_time";
	public static final String COL_DOG_BREED = "dog_breed";
	public static final String COL_DOG_WEIGHT = "dog_weight";
	public static final String COL_CANCELLATION_CODE = "cancellation_code";
	public static final String COL_DOGPARK_ID = "dogpark_id";

	@Id
	private Long id;

	@Column(name = COL_ARRIVAL_TIME, nullable = false)
	private Timestamp arrivalTime;

	@Column(name = COL_DOG_BREED)
	private String dogBreed;

	@Column(name = COL_DOG_WEIGHT, nullable = false)
	private Integer dogWeight;

	@Column(name = COL_CANCELLATION_CODE, nullable = false)
	private String cancellationCode;

	@ManyToOne
	@JoinColumn(name = COL_DOGPARK_ID, nullable = false)
	private DogPark dogPark;

	public DogParkSignup() {
	}

	public DogParkSignup(Timestamp arrivalTime, String dogBreed, Integer dogWeight, DogPark dogPark) {
		this.arrivalTime = arrivalTime;
		this.dogBreed = dogBreed;
		this.dogWeight = dogWeight;
		this.dogPark = dogPark;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(Timestamp arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public String getDogBreed() {
		return dogBreed;
	}

	public void setDogBreed(String dogBreed) {
		this.dogBreed = dogBreed;
	}

	public Integer getDogWeight() {
		return dogWeight;
	}

	public void setDogWeight(Integer dogWeight) {
		this.dogWeight = dogWeight;
	}

	public DogPark getDogPark() {
		return dogPark;
	}

	public void setDogPark(DogPark dogPark) {
		this.dogPark = dogPark;
	}

}
