package won.bot.meetingbot.model.foursquare;


import java.util.List;

public class FSCategory {

    /*
    {
                  "id":"52f2ab2ebcbc57f1066b8b4a",
                  "name":"Tunnel",
                  "pluralName":"Tunnels",
                  "shortName":"Tunnel",
                  "icon":{
                     "prefix":"https:\/\/ss3.4sqi.net\/img\/categories_v2\/travel\/default_",
                     "suffix":".png"
                  },
                  "categories":[

                  ]
               }
     */

    private String id;
    private String name;
    private String pluralName;
    private String shortName;
    private List<FSCategory> categories;

    public FSCategory() {
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPluralName() {
        return pluralName;
    }

    public void setPluralName(String pluralName) {
        this.pluralName = pluralName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FSCategory{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", pluralName='").append(pluralName).append('\'');
        sb.append(", shortName='").append(shortName).append('\'');
        sb.append(", categories=").append(categories);
        sb.append('}');
        return sb.toString();
    }
}
