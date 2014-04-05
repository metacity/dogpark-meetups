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
			]
		});
	});
}