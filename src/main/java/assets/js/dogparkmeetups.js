$(document).ajaxStart(function() {
	$('.spinner').spin({lines: 11, length: 0, width: 5, radius: 10, top: '100px', left: '60px'});
}).ajaxStop(function() {
	$('.spinner').spin(false);
});

// -------------------------------------------------------------------

var navTab;
var dogparkListContainer;
var dogparkContainer;

var calendarOptions;

$(document).ready(function() {
	navTab = $('#nav-tab');
	dogparkListContainer = $('#dogpark-list-container');
	dogparkContainer = $('#dogpark-container');
		
	$('body').click(function(e) {
		 dogparkContainer.find('.day a').each(function() {
			if ($('.popover .close').is(e.target) || (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0)) {
				$(this).popover('destroy');
			}
		});
	});

	navTab.find('a').click(function(e) {
		e.preventDefault();
		$(this).tab('show');
	});
	
	dogparkContainer.on('submit', '.signup-form', function(e) {
		e.preventDefault();
		var dogparkId = $(this).data('dogparkId');
		$.post('dogparks/' + dogparkId + '/signups', $(this).serialize(), function(signup) {
			var day = calendarOptions.events[signup.date];
			if (!day) {
				day = {
					number: 0,
					badgeClass: 'badge-warning',
					dayEvents: []
				};
			}
			day.dayEvents.push(signup);
			day.number = day.dayEvents.length;
			calendarOptions.events[signup.date] = day;
			dogparkContainer.find('.responsive-calendar').responsiveCalendar('edit', calendarOptions.events);
		}, 'json');
	});

	navTab.find('a[href=#dogpark-list]').on('shown.bs.tab', loadDogParkList);
	dogparkListContainer.on('click', 'a', loadDogPark);
});

function loadDogParkList() {
	dogparkListContainer.html("");
	$.get('dogparks', function(dogparkList) {
		dogparkListContainer.html(dogparkList);
	});
}

function loadDogPark() {
	var dogparkId = $(this).data('dogparkId');
	var dogparkName = $(this).text();

	navTab.find('a[href=#dogpark]').tab('show');
	$('#dogpark > h3').html('<i class="glyphicon glyphicon-tree-deciduous"></i> ' + dogparkName);

	dogparkContainer.html("");
	calendarOptions = {
		translateMonths: [
			'Tammikuu', 'Helmikuu', 'Maaliskuu', 'Huhtikuu', 'Toukokuu', 'Kesäkuu', 
			'Heinäkuu', 'Elokuu', 'Syyskuu', 'Lokakuu', 'Marraskuu', 'Joulukuu'
		],
		onDayClick: function(events) {
			var thisDayEvents,
				key;

			key = $(this).data('year')+ '-' 
					+ addLeadingZero($(this).data('month')) + '-' 
					+ addLeadingZero($(this).data('day'));
			thisDayEvents = events[key] ? events[key].dayEvents : [];

			var eventsHtml = '<ul class="list-unstyled">';
			if (thisDayEvents.length > 0) {
				for (var i = 0; i < thisDayEvents.length; ++i) {
					eventsHtml += '<li><b>' + thisDayEvents[i].hour + ':</b> ' + thisDayEvents[i].name + '</li>';
				}
			} else {
				eventsHtml += '<li>Ei lmoittautuneita</li>';
			}
			eventsHtml += '</ul><hr> \
					<form role="form" class="signup-form" data-dogpark-id="' + dogparkId + '">\
						<input name="date" value="' + key + '" type="hidden">\
						<div class="form-group"> \
							<label for="timeOfArrival">Saapumisaika <small>(esim. 13:30 tai 14.45)</small></label> \
								<input type="text" class="form-control" name="timeOfArrival" id="timeOfArrival" placeholder="Syötä kellonaika"> \
						</div> \
						<div class="form-group"> \
							<label for="dogbreed">Koiran rotu</label> \
								<input type="text" class="form-control" name="dogBreed" id="dogbreed" placeholder="Syötä rotu"> \
						</div> \
						<div class="form-group"> \
							<label for="dogweightclass">Koiran paino</label> \
							<select class="form-control" name="dogWeightClass" id="dogweightclass"> \
								<option value="KG_1_TO_5">1-5 kg</option> \
								<option value="KG_5_TO_10">5-10 kg</option> \
								<option value="KG_10_TO_15">10-15 kg</option> \
								<option value="KG_15_TO_25">15-25 kg</option> \
								<option value="KG_25_TO_40">25-40 kg</option> \
								<option value="KG_40_PLUS">40+ kg</option> \
							</select> \
						</div> \
						<div class="form-group"> \
							<label class="radio-inline"> \
								<input type="radio" name="dogIsMale" id="dogIsMale1" value="true" checked> \
								Uros \
							</label> \
							<label class="radio-inline"> \
								<input type="radio" name="dogIsMale" id="dogIsMale1" value="false"> \
								Narttu \
							</label> \
						</div> \
						<div class="centered"><button type="submit" class="btn btn-primary">Ilmoittaudu!</button></div> \
					</form>';

			$(this).popover({
				trigger: 'manual',
				html: true,
				animation: false,
				placement: 'auto top',
				title: 'Ilmoittautuneet (' + key + ') <button type="button" class="pull-right close" aria-hidden="true">&times;</button>',
				content: eventsHtml,
				container: dogparkContainer
			});
			dogparkContainer.find('.day a').not(this).popover('destroy');
			$(this).popover('toggle');
			return false;
		}
	};
	var dogparkHtml;
	
	$.when(
			$.get('dogparks/' + dogparkId, function(dogpark) {
				dogparkHtml = dogpark;
			}),
			$.getJSON('dogparks/' + dogparkId + '/signups?year=' + new Date().getFullYear() + '&month=' + (new Date().getMonth()+1), function(signupEvents) {
				calendarOptions.events = signupEvents;
			})
		).then(function() {
			dogparkContainer.html(dogparkHtml);
			dogparkContainer.find('.responsive-calendar').responsiveCalendar(calendarOptions);
		});
}

function addLeadingZero(num) {
    if (num < 10) {
      return "0" + num;
    } else {
      return "" + num;
    }
  }