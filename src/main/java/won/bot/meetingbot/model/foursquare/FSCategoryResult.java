package won.bot.meetingbot.model.foursquare;


public class FSCategoryResult extends FSResult {

    private FSCategoryResponse response;

    @Override
    public FSCategoryResponse getResponse() {
        return response;
    }

    public void setResponse(FSCategoryResponse response) {
        this.response = response;
    }
}
