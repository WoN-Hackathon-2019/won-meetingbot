package won.bot.meetingbot.model.foursquare;


import java.util.List;

public class FSCategoryResponse extends FSResponse {

    private List<FSCategory> categories;

    public List<FSCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<FSCategory> categories) {
        this.categories = categories;
    }
}
