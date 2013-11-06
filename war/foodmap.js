var singleton = {};

function createInfo() {
	var info = new google.maps.InfoWindow({
		content: 'info',
		disableAutoPan: true,
		size: new google.maps.Size(50, 50)
	});
	
	return info;
}

function getInfo() {
	if (!singleton.info) {
		singleton.info = createInfo();
	}
	
	return singleton.info;
}

function createMap() {
	var map = new google.maps.Map(document.getElementById('map-canvas'), {
		zoom: 8,
		center: new google.maps.LatLng(-34.397, 150.644),
		mapTypeId: google.maps.MapTypeId.ROADMAP
	});
	
	google.maps.event.addListener(map, 'click', function(event) {
		var food = createFood('watermelon.jpg');
		food.setPosition(event.latLng);
		food.setMap(map);
	});
	
	return map;
}

function createFood(url) {
	var food = new google.maps.Marker({
		icon: url,
		title: 'Click to show information about the food.'
	});
	
	google.maps.event.addListener(food, 'click', function() {
		var info = getInfo();
		info.setContent(food.getTitle());
		info.open(food.getMap(), food);
	});
	
	return food;
}

function initialize() {
	createMap();
	
	var ROOT = '/_ah/api';
	gapi.client.load('foodendpoint', 'v1', function() {}, ROOT);
}

google.maps.event.addDomListener(window, 'load', initialize);