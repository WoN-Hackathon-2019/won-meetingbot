package won.bot.meetingbot.foursquare;


import java.util.List;

public class FSVenueResponse extends FSResponse {

    private List<FSVenue> venues;

    public List<FSVenue> getVenues() {
        return venues;
    }

    public void setVenues(List<FSVenue> venues) {
        System.out.println("X");
        this.venues = venues;
    }
}
