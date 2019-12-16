package won.bot.meetingbot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import won.bot.meetingbot.foursquare.FSLocation;

import java.io.IOException;

public class Venue {
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
    public Venue(String name, String address, String city, String formattedAddress, String mapsLink, String country, String postalCode, double longitude, double latitude) {
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
    public Venue(FSLocation location, String name){
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
    @JsonIgnore
    private String getMapsLink(double latitude, double longitude){
        return "http://maps.google.com/maps?q=" + latitude + "," + longitude + "";
    }
    @JsonIgnore
    public String toJSON() {
        try {
            return mapper.writeValueAsString(this);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public String getMapsLink() {
        return mapsLink;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getCountry() {
        return country;
    }

    public String getPostalCode() {
        return postalCode;
    }
}