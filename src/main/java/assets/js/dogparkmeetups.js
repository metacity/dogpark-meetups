$(document).ajaxStart(function() {
	$('.spinner').spin({lines: 11, length: 0, width: 5, radius: 10, top: '100px', left: '60px'});
}).ajaxStop(function() {
	$('.spinner').spin(false);
});

// -------------------------------------------------------------------

var dogparkContainer;

$(document).ready(function() {
	
	dogparkContainer = $('#dogpark');
	if (dogparkContainer.length) {
		$('body').click(function(e) {
			dogparkContainer.find('.day a').each(function() {
				if ($('.popover .close').is(e.target) 
					|| 
					 (!$(this).is(e.target)
						&& $(this).has(e.target).length === 0 
						&& $('.popover').has(e.target).length === 0)
					) {
					
					$(this).popover('destroy');
				}
			});
		});
		loadCalendar();
	}
	
	$('#signup-form').submit(validateSignupForm);
});

function loadCalendar() {
	var calendarOptions = {
		translateMonths: [
			'Tammikuu', 'Helmikuu', 'Maaliskuu', 'Huhtikuu', 'Toukokuu', 'Kesäkuu', 
			'Heinäkuu', 'Elokuu', 'Syyskuu', 'Lokakuu', 'Marraskuu', 'Joulukuu'
		],
		onDayClick: showSignupsPopup
	};

	var now = new Date();
	$.getJSON(window.location.href + '/signups?yearMonth=' + now.getFullYear() + '-' + pad(now.getMonth()+1), function(signups) {
		var calendarEvents = groupByDate(signups);
		calendarOptions.events = calendarEvents;
		console.log(calendarOptions);
		$('#dogpark-calendar').responsiveCalendar(calendarOptions);
	});
}

// Forms the 'events' object like described at http://w3widgets.com/responsive-calendar/#options
function groupByDate(signups) {
	var optionsByDate = {};
	
	$.each(signups, function(i, signup) {
		var arrivalTime = new Date(signup.arrivalTime);
		var dateString = toLocalDateString(arrivalTime);

		// Init the options for the date if necessary
		if (optionsByDate.hasOwnProperty(dateString) === false) {
			optionsByDate[dateString] = {
				badgeClass: 'badge-warning',
				dayEvents: []
			};
		}
		
		optionsByDate[dateString].dayEvents.push({
			name: signup.dogBreed + ' (' + signup.dogWeightClass + ' ' + (signup.dogIsMale ? 'uros' : 'naaras') + ')',
			hour: pad(arrivalTime.getHours()) + ':' + pad(arrivalTime.getMinutes())
		});		
	});
	
	$.each(optionsByDate, function(date, options) {
		options.number = options.dayEvents.length;
	});
	
	return optionsByDate;
}

function toLocalDateString(date) {
	return date.getFullYear() + '-' + pad(date.getMonth()+1) + '-' + pad(date.getDate());
}

function pad(num) {
	return (num < 10 ? '0' : '') + num;
}

function showSignupsPopup(events) {
	var thisDayEvents,
	    key,
	    eventsHtml;

	key = $(this).data('year') + '-'
			+ pad($(this).data('month')) + '-'
			+ pad($(this).data('day'));
	thisDayEvents = events[key] ? events[key].dayEvents : [];

	eventsHtml = '<ul class="list-unstyled">';
	if (thisDayEvents.length > 0) {
		for (var i = 0; i < thisDayEvents.length; ++i) {
			eventsHtml += '<li><b>' + thisDayEvents[i].hour + ':</b> ' + thisDayEvents[i].name + '</li>';
		}
	} else {
		eventsHtml += '<li>Ei lmoittautuneita</li>';
	}
	eventsHtml += '</ul><hr> \
		<div class="centered"> \
			<a href="' + window.location.href + '/signup?date=' + key + '" class="btn btn-primary">Ilmoittaudu</a> \
		</div>';

	$(this).popover({
		trigger: 'manual',
		html: true,
		animation: false,
		placement: 'auto top',
		title: 'Ilmoittautuneet (' + key + ') <button type="button" class="pull-right close" aria-hidden="true">&times;</button>',
		content: eventsHtml,
		container: dogparkContainer
	});
	//dogparkContainer.find('.day a').not(this).popover('destroy');
	$(this).popover('toggle');
	//return false;
}

// Some refactoring needed..
function validateSignupForm(event) {
	var hasErrors = false;
	var timeInput  = $(this).find('#timeOfArrival:first').val().trim().replace('\.', ':');
	
	var re = new RegExp('^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$');
	if (re.test(timeInput) === false) {
		$(this).find('.form-group:first').removeClass('has-success').addClass('has-error');
		hasErrors = true;
	} else {
		$(this).find('.form-group:first').removeClass('has-error').addClass('has-success');
	}
	
	if ($(this).find('#dogbreed:first').val().trim().length === 0) {
		$(this).find('.form-group:eq(1)').removeClass('has-success').addClass('has-error');
		hasErrors = true;
	} else {
		$(this).find('.form-group:eq(1)').removeClass('has-error').addClass('has-success');
	}
	
	if (hasErrors) {
		event.preventDefault();
	}
}