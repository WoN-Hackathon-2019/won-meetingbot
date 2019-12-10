package won.bot.meetingbot.foursquare;


import java.util.List;

public class FSLocation {

    private String address;
    private String crossStreet;
    private double lat;
    private double lng;
    private int distance;
    private String postalCode;
    private String cc;
    private String city;
    private String state;
    private String country;
    private List<String> formattedAddress;
    /*
    {
               "address":"Furman St",
               "crossStreet":"Brooklyn Bridge Park Greenway",
               "lat":40.69957016220183,
               "lng":-73.99793274204788,
               "labeledLatLngs":[
                  {
                     "label":"display",
                     "lat":40.69957016220183,
                     "lng":-73.99793274204788
                  }
               ],
               "distance":180,
               "postalCode":"11201",
               "cc":"US",
               "city":"Brooklyn",
               "state":"NY",
               "country":"United States",
               "formattedAddress":[
                  "Furman St (Brooklyn Bridge Park Greenway)",
                  "Brooklyn, NY 11201",
                  "United States"
               ]
            }
     */

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCrossStreet() {
        return crossStreet;
    }

    public void setCrossStreet(String crossStreet) {
        this.crossStreet = crossStreet;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<String> getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(List<String> formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FSLocation{");
        sb.append("address='").append(address).append('\'');
        sb.append(", crossStreet='").append(crossStreet).append('\'');
        sb.append(", lat=").append(lat);
        sb.append(", lng=").append(lng);
        sb.append(", distance=").append(distance);
        sb.append(", postalCode='").append(postalCode).append('\'');
        sb.append(", cc='").append(cc).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append(", country='").append(country).append('\'');
        sb.append(", formattedAddress=").append(formattedAddress);
        sb.append('}');
        return sb.toString();
    }
}
