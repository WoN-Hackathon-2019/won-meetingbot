package won.bot.meetingbot.model.foursquare;


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
