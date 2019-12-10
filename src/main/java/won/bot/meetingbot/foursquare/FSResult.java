package won.bot.meetingbot.foursquare;


public class FSResult {

    private FSMeta meta;
    private FSResponse response;


    public FSMeta getMeta() {
        return meta;
    }

    public void setMeta(FSMeta meta) {
        this.meta = meta;
    }

    public FSResponse getResponse() {
        return response;
    }

    public void setResponse(FSResponse response) {
        this.response = response;
    }
}
