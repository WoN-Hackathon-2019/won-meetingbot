package won.bot.meetingbot.foursquare;


import java.util.List;

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
