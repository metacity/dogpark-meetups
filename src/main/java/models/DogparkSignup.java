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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "dogpark_signup")
@JsonIgnoreProperties(
		{"id", "arrivalTime", "dogBreed", "dogWeightClass", "dogIsMale", "cancellationCode", "dogpark"}
)
public class DogparkSignup implements DayEvent {

	public static final String TABLE_NAME = "dogpark_signup";

	public static final String COL_ARRIVAL_TIME = "arrival_time";
	public static final String COL_DOG_BREED = "dog_breed";
	public static final String COL_DOG_WEIGHT_CLASS = "dog_weight_class";
	public static final String COL_DOG_IS_MALE = "dog_is_male";
	public static final String COL_CANCELLATION_CODE = "cancellation_code";
	public static final String COL_DOGPARK_ID = "dogpark_id";

	@Id
	private Long id;

	@Column(name = COL_ARRIVAL_TIME, nullable = false)
	private Timestamp arrivalTime;

	@Column(name = COL_DOG_BREED)
	private String dogBreed;

	@Enumerated(EnumType.STRING)
	@Column(name = COL_DOG_WEIGHT_CLASS, nullable = false)
	private DogWeightClass dogWeightClass;

	@Column(name = COL_DOG_IS_MALE, nullable = false)
	private Boolean dogIsMale;

	@Column(name = COL_CANCELLATION_CODE, nullable = false)
	private String cancellationCode;

	@ManyToOne
	@JoinColumn(name = COL_DOGPARK_ID, nullable = false)
	private Dogpark dogpark;

	public DogparkSignup() {
		this.cancellationCode = UUID.randomUUID().toString();
	}

	public DogparkSignup(Timestamp arrivalTime, String dogBreed, DogWeightClass dogWeightClass, Boolean dogIsMale, Dogpark dogpark) {
		this.arrivalTime = arrivalTime;
		this.dogBreed = dogBreed;
		this.dogWeightClass = dogWeightClass;
		this.dogIsMale = dogIsMale;
		this.dogpark = dogpark;

		this.cancellationCode = UUID.randomUUID().toString();
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

	public DogWeightClass getDogWeightClass() {
		return dogWeightClass;
	}

	public void setDogWeightClass(DogWeightClass dogWeightClass) {
		this.dogWeightClass = dogWeightClass;
	}

	public Boolean isDogIsMale() {
		return dogIsMale;
	}

	public void setDogIsMale(Boolean dogIsMale) {
		this.dogIsMale = dogIsMale;
	}

	public String getCancellationCode() {
		return cancellationCode;
	}

	public void setCancellationCode(String cancellationCode) {
		this.cancellationCode = cancellationCode;
	}

	public Dogpark getDogpark() {
		return dogpark;
	}

	public void setDogpark(Dogpark dogpark) {
		this.dogpark = dogpark;
	}

	@Override
	public String getDate() {
		LocalDateTime ldt = LocalDateTime.ofInstant(arrivalTime.toInstant(), ZoneOffset.UTC);
		return ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}

	@Override
	public String getHour() {
		LocalDateTime ldt = LocalDateTime.ofInstant(arrivalTime.toInstant(), ZoneOffset.UTC);
		return ldt.format(DateTimeFormatter.ofPattern("HH:mm"));
	}

	@Override
	public String getName() {
		return dogBreed + " (" + dogWeightClass.toFormatted()
				+ " " + (isDogIsMale() ? "uros" : "naaras") + ")";
	}

	public static enum DogWeightClass {
		KG_1_TO_5("1-5 kg"),
		KG_5_TO_10("5-10 kg"),
		KG_10_TO_15("10-15 kg"),
		KG_15_TO_25("15-25 kg"),
		KG_25_TO_40("25-40 kg"),
		KG_40_PLUS("40+ kg");

		private final String formattedString;

		private DogWeightClass(String formattedString) {
			this.formattedString = formattedString;
		}

		public String toFormatted() {
			return formattedString;
		}
	}

}
