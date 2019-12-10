package won.bot.meetingbot.foursquare;


public class FSVenueResult extends FSResult {

    private FSVenueResponse response;

    @Override
    public FSVenueResponse getResponse() {
        return response;
    }

    public void setResponse(FSVenueResponse response) {
        this.response = response;
    }
}
