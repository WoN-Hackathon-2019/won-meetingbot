package won.bot.meetingbot.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

public class RequestMessage {
    private String[] categories;
    private double[][] locations;

    @JsonIgnore
    public static RequestMessage parseJSON(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonString, RequestMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public void setLocations(double[][] locations) {
        this.locations = locations;
    }

    public String[] getCategories() {
        return categories;
    }

    public double[][] getLocations() {
        return locations;
    }
}
