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

import com.avaje.ebean.annotation.CacheStrategy;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@CacheStrategy(readOnly = true, warmingQuery = "ORDER BY name")
@Entity
@Table(name = "dogpark")
public class DogPark {

	public static final String TABLE_NAME = "dogpark";

	public static final String COL_NAME = "name";
	public static final String COL_LATITUDE = "latitude";
	public static final String COL_LONGITUDE = "longitude";
	public static final String COL_CITY_ID = "city_id";

	public static final String FETCH_SIGNUPS = "signups";

	@Id
	private Long id;

	@Column(name = COL_NAME, nullable = false)
	private String name;

	@Column(name = COL_LATITUDE, nullable = false)
	private Double latitude;

	@Column(name = COL_LONGITUDE, nullable = false)
	private Double longitude;

	@ManyToOne
	@JoinColumn(name = COL_CITY_ID, nullable = false)
	private City city;

	@OneToMany(cascade = CascadeType.ALL)
	private List<DogParkSignup> signups;

	public DogPark() {
	}

	public DogPark(String name, Double latitude, Double longitude, City city) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.city = city;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public List<DogParkSignup> getSignups() {
		return signups;
	}

	public void setSignUps(List<DogParkSignup> signups) {
		this.signups = signups;
	}

}
