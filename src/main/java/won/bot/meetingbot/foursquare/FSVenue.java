package won.bot.meetingbot.foursquare;


import java.util.List;

public class FSVenue {

    private String id;
    private String name;
    private FSLocation location;
    private List<FSCategory> categories;

    /*
    "id":"51eabef6498e10cf3aea7942",
            "name":"Brooklyn Bridge Park - Pier 2",
            "location":{},
            "categories":[],
            "referralId":"v-1575987152",
            "hasPerk":false
     */

    public List<FSCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<FSCategory> categories) {
        this.categories = categories;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FSLocation getLocation() {
        return location;
    }

    public void setLocation(FSLocation location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FSVenue{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", location=").append(location);
        sb.append(", categories=").append(categories);
        sb.append('}');
        return sb.toString();
    }
}
