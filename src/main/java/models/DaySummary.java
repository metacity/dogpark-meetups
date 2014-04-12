/*
 * The MIT License
 *
 * Copyright 2014 metacity.
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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DaySummary {

	private final String badgeClass = "badge-warning";
	private final List<DayEvent> dayEvents;

	public DaySummary(List<DayEvent> dayEvents) {
		this.dayEvents = dayEvents;
	}

	@JsonProperty("number")
	public int getNumber() {
		return dayEvents.size();
	}

	@JsonProperty("badgeClass")
	public String getBadgeClass() {
		return "badge-warning";
	}

	public List<DayEvent> getDayEvents() {
		return dayEvents;
	}

}
