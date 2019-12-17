package won.bot.meetingbot.foursquare;


import java.io.IOException;

public class FoursquareRunner {

    public static void main(String[] args) throws IOException {

        FSCategoryResult result =
                new FSRequestBuilder("https://api.foursquare.com/v2/venues/categories").executeForObject(FSCategoryResult.class);

        System.out.println(result.getResponse().getCategories());

        String xxx = new FSRequestBuilder("https://api.foursquare.com/v2/venues/search").withParameter("ll", "40" +
                ".74224,-73.99386").executeForObject(String.class);
        System.out.println(xxx);

        FSVenueResult venues = new FSRequestBuilder("https://api.foursquare.com/v2/venues/search").withParameter("ll"
                , "48.194530,16.369823").executeForObject(FSVenueResult.class);
        // 48.210159,16.355502;48.202251,16.361638/Metro Station
        // 33.671543,-39.285743;33.671543,-39.285743/Metro Station
        System.out.println(venues.getMeta().getCode());
        System.out.println(venues.getResponse());
        System.out.println(venues.getResponse().getVenues());
        System.out.println(venues.getResponse().getVenues().get(0));

    }

}
