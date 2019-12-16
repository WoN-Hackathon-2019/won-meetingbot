package won.bot.meetingbot.openstreetmap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSMLocation {

    public static final Logger logger = LoggerFactory.getLogger(OSMLocation.class);

    /*
    "place_id": 187720774,
        "licence": "Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright",
        "osm_type": "way",
        "osm_id": 541958600,
        "boundingbox": [
            "48.2271001",
            "48.2272184",
            "13.9114621",
            "13.9116354"
        ],
        "lat": "48.2271593",
        "lon": "13.9115487662299",
        "display_name": "4, Stelzhamerstraße, Bad Schallerbach, Grieskirchen, Oberösterreich, 4701, Österreich",
        "class": "building",
        "type": "yes",
        "importance": 0.42100000000000004
     */

    private String place_id;
    private String lat;
    private String lon;
    private String display_name;

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OSMLocation{");
        sb.append("place_id='").append(place_id).append('\'');
        sb.append(", lat='").append(lat).append('\'');
        sb.append(", lon='").append(lon).append('\'');
        sb.append(", display_name='").append(display_name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static OSMLocation getLocationForAddress(String address) {
        OSMLocation[] result =
                new OSMRequestBuilder("https://nominatim.openstreetmap.org/search?format=json" +
                        "&addressdetails=0&q=Stelzhamerstraße 4, 4701 Bad Schallerbach").executeForObject(OSMLocation[].class);
        if (result.length == 0) {
            logger.error("Could not find location for address: '{}'", address);
            return null;
        }

        return result[0];
    }
}
