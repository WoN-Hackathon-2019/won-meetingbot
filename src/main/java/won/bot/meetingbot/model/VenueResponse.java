package won.bot.meetingbot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import won.bot.meetingbot.model.foursquare.FSLocation;

import java.io.IOException;

public class VenueResponse {
    private String name;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String formattedAddress;
    private String mapsLink;
    private double longitude;
    private double latitude;
    private ObjectMapper mapper = new ObjectMapper();

    @JsonIgnore
    public VenueResponse(String name, String address, String city, String formattedAddress, String mapsLink, String country,
                         String postalCode, double longitude, double latitude) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.formattedAddress = formattedAddress;
        this.mapsLink = mapsLink;
        this.longitude = longitude;
        this.latitude = latitude;
        this.country = country;
        this.postalCode = postalCode;
    }

    @JsonIgnore
    public VenueResponse(FSLocation location, String name) {
        this.name = name;
        this.latitude = location.getLat();
        this.longitude = location.getLng();
        this.address = location.getAddress();
        this.city = location.getCity();
        this.formattedAddress = String.join("\n", location.getFormattedAddress());
        this.country = location.getCountry();
        this.postalCode = location.getPostalCode();
        this.mapsLink = getMapsLink(latitude, longitude);
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @JsonIgnore
    private String getMapsLink(double latitude, double longitude) {
        return "http://maps.google.com/maps?q=" + latitude + "," + longitude + "";
    }

    public String getMapsLink() {
        return mapsLink;
    }

    public String getName() {
        return name;
    }

    public String getPostalCode() {
        return postalCode;
    }

    @JsonIgnore
    public String toJSON() {
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
