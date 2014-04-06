$(document).ajaxStart(function() {
	$('.spinner').spin({lines: 11, length: 0, width: 5, radius: 10, top: '100px', left: '60px'});
}).ajaxStop(function() {
	$('.spinner').spin(false);
});

$(document).ready(function() {
	$('#nav-tab a').click(function(e) {
		e.preventDefault();
		$(this).tab('show');
	});

	$('#nav-tab a[href=#dogpark-list]').on('shown.bs.tab', loadDogParkList);
	$('#dogpark-list-container').on('click', 'a', loadDogPark);
});

function loadDogParkList() {
	var dogparkListContainer = $('#dogpark-list-container');
	dogparkListContainer.html("");
	$.get('dogparks', function(dogparkList) {
		dogparkListContainer.html(dogparkList);
	});
}

function loadDogPark() {
	var dogparkId = $(this).data('dogparkId');
	var dogparkName = $(this).text();

	$('#nav-tab a[href=#dogpark]').tab('show');
	$('#dogpark > h3').html('<i class="glyphicon glyphicon-tree-deciduous"></i> ' + dogparkName);

	var dogparkContainer = $('#dogpark-container');
	dogparkContainer.html("");
	$.get('dogparks/' + dogparkId, function(dogpark) {
		dogparkContainer.html(dogpark);
		dogparkContainer.find('.responsive-calendar').responsiveCalendar({
			translateMonths: [
				'Tammikuu', 'Helmikuu', 'Maaliskuu', 'Huhtikuu', 'Toukokuu', 'Kesäkuu', 
				'Heinäkuu', 'Elokuu', 'Syyskuu', 'Lokakuu', 'Marraskuu', 'Joulukuu'
			],
			events: {  // SAMPLE DATA! Will really be fetched via an API call..
				'2014-04-25': {
					number: 1, 
					badgeClass: 'badge-warning',
					dayEvents: [
						{
							name: 'Seropi (35 kg uros)',
							hour: '13:30'
						}
					]
				},
				'2014-04-30': {
					number: 2, 
					badgeClass: 'badge-warning',
					dayEvents: [
						{
							name: 'Kääpiösnautseri (5-10 kg uros)',
							hour: '14:00'
						},
						{
							name: 'Seropi (5-10 kg narttu)',
							hour: '14:00'
						}
					]
				}
			},
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
						<form role="form"> \
							<div class="form-group"> \
								<label for="timeOfArrival">Saapumisaika <small>(esim. 13:30 tai 14.45)</small></label> \
									<input type="text" class="form-control" name="timeOfArrival" id="timeOfArrival" placeholder="Syötä kellonaika"> \
							</div> \
							<div class="form-group"> \
								<label for="dogbreed">Koiran rotu</label> \
									<input type="text" class="form-control" name="dogBreed" id="dogbreed" placeholder="Syötä rotu"> \
							</div> \
							<div class="form-group"> \
								<label for="dogweight">Koiran paino</label> \
								<select class="form-control" name="dogWeight" id="dogweight"> \
									<option value="1-5">1-5 kg</option> \
									<option value="5-10">5-10 kg</option> \
									<option value="10-15">10-15 kg</option> \
									<option value="15-25">15-25 kg</option> \
									<option value="25-40">25-40 kg</option> \
									<option value="40+">40+ kg</option> \
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
							<div class="centered"><button type="button" class="btn btn-success">Ilmoittaudu!</button></div> \
						</form>';
						
				
				$(this).popover({
					trigger: 'manual',
					html: true,
					animation: false,
					placement: 'auto top',
					title: 'Ilmoittautuneet (' + key + ')',
					content: eventsHtml,
					container: 'body'
				});
				$('.responsive-calendar .day a').not(this).popover('destroy');
				$(this).popover('toggle');
			}
		});
	});
}

function addLeadingZero(num) {
    if (num < 10) {
      return "0" + num;
    } else {
      return "" + num;
    }
  }