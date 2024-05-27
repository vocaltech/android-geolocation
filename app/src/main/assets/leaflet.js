function showAndroidToast(msg) {
    Android.showToast(msg);
}

const curPos = JSON.parse(Android.currentPosition());
const curLat = curPos.lat;
const curLng = curPos.lng;
const curTs = curPos.ts;

var map = 'undefined'
let prevZoom = 0;

if (Android.currentZoom() === 0)
    prevZoom = 14;
else
    prevZoom = Android.currentZoom();

if (curLat === 'null' && curLng === 'null') {
    map = L.map('map').fitWorld().zoomIn();
} else {
    map = L.map('map').setView([curLat, curLng], prevZoom);
    map.on('zoomend', function(e) {
        var curZoom = map.getZoom();
        Android.setCurrentZoom(curZoom);
    })
}

L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=pk.eyJ1Ijoidm9jYWxpdHkiLCJhIjoiY2xzNm00ZDlrMWtnazJrcGlmanVsMGw3ZyJ9.M9Il3xlDeI9BaBLMPcB-4Q', {
    maxZoom: 19,
    attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, ' +
        'Imagery Ã‚Â© <a href="https://www.mapbox.com/">Mapbox</a>',
    id: 'mapbox/streets-v11',
    tileSize: 512,
    zoomOffset: -1
}).addTo(map);

//showAndroidToast(Android.userId())

if (curLat !== 'null' && curLng !== 'null') {
    const popupContent =    `Username: user_id<br>
    Last known position: ${curTs}<br>
    Longitude: ${curLng}<br>
    Latitude: ${curLat}`;

    const marker = L.marker([curLat, curLng]);
    marker
        .addTo(map)
        .bindPopup(popupContent);
}
