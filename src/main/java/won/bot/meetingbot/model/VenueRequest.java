package won.bot.meetingbot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class VenueRequest {
    private String[] categories;
    private double[][] locations;

    @JsonIgnore
    public static VenueRequest parseJSON(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonString, VenueRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public double[][] getLocations() {
        return locations;
    }

    public void setLocations(double[][] locations) {
        this.locations = locations;
    }
}
