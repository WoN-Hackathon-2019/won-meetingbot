package won.bot.meetingbot.action;

import at.apf.easycli.CliEngine;
import at.apf.easycli.annotation.Command;
import at.apf.easycli.impl.EasyEngine;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.ConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.MessageEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.meetingbot.Venue;
import won.bot.meetingbot.context.MeetingBotContextWrapper;
import won.bot.meetingbot.foursquare.*;
import won.bot.meetingbot.impl.RequestMessage;
import won.bot.meetingbot.openstreetmap.OSMLocation;
import won.protocol.message.WonMessage;
import won.protocol.message.builder.WonMessageBuilder;
import won.protocol.util.WonRdfUtils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Listener that responds to open and message events with automatic messages.
 * Can be configured to apply a timeout (non-blocking) before sending messages.
 */
public class RespondToMessageAction extends BaseEventBotAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private long millisTimeoutBeforeReply = 0;
    //maps categoryName to Id;
    private HashMap<String, String> categoryMap;

    public RespondToMessageAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
        categoryMap = loadCategoryMap();
    }

    public RespondToMessageAction(final EventListenerContext eventListenerContext,
                                  final long millisTimeoutBeforeReply) {
        super(eventListenerContext);
        this.millisTimeoutBeforeReply = millisTimeoutBeforeReply;
        categoryMap = loadCategoryMap();
    }

    public void addCategoryToMap(HashMap<String, String> map, FSCategory category) {
        if (category == null) {
            return;
        }
        map.put(category.getName(), category.getId());
        if (category.getCategories() == null) {
            return;
        }
        for (FSCategory subCategory : category.getCategories()) {
            addCategoryToMap(map, subCategory);
        }
    }

    private String coordinatesToHood(double longitude, double latitude, String filteredCategoriesString, boolean jsonFlag) throws Exception {
        int range = 50;
        int i = 1;
        String coordinates = locationsToString(longitude, latitude);
        while (range <= 100000) {
            FSVenueResult request;
            if (filteredCategoriesString == null){
                request =
                        new FSRequestBuilder("https://api.foursquare.com/v2/venues/search").withParameter("ll",
                                coordinates).withParameter("range", Integer.toString(range)).executeForObject(FSVenueResult.class);
            }else {
                request =
                        new FSRequestBuilder("https://api.foursquare.com/v2/venues/search").withParameter("ll",
                                coordinates).withParameter("range", Integer.toString(range)).withParameter("categoryId",
                                filteredCategoriesString).executeForObject(FSVenueResult.class);
            }
            //TODO: check if this null check really catches no returned results
            if (request != null) {
                if (request.getMeta() != null) {
                    if (request.getMeta().getCode() == 200) {
                        if (request.getResponse().getVenues().size() > 0) {
                            String name = request.getResponse().getVenues().get(0).getName() + "\n";
                            FSLocation location = request.getResponse().getVenues().get(0).getLocation();
                            String address =
                                    location.getFormattedAddress().toString();
                            String link =
                                    "(http://maps.google.com/maps?q=" + location.getLat() + "," + location.getLng() + ")";
                            Venue venue = new Venue(location, name);
                            System.out.println(venue.toJSON());
                            if (jsonFlag) {
                                return venue.toJSON();
                            }
                            return "We suggest you meet here:\n" + name + "\n" + address + link;
                        }

                    }
                }
            }
            range += 50 * i++;
        }
        return "Sorry! We could not find any venues near the given locations...";
    }

    private String createCategoriesString(ArrayList<String> filteredCategories) {
        StringBuilder sb = new StringBuilder();
        for (String category : filteredCategories) {
            sb.append(category).append(',');
        }
        logger.info("XXX: {}, sb.length: {}, sb: '{}'", filteredCategories, sb.length(), sb.toString());

        if (sb.length() > 0) return sb.toString().substring(0, sb.length() - 1);
        return "";
    }

    //Given a message containing lat and longitude of locations split by "/" and ";" returns a Message containing
    //informations regarding the best matching venue, else an error message
    private String createMessage(String inMessage) {
        boolean jsonFlag = false;
        CliEngine engine = new EasyEngine();
        engine.register(new Object() {
            @Command("/json")
            String json(String message) {
                RequestMessage m = RequestMessage.parseJSON(message);
                String outMessage;
                try {
                    double[] interpolLoc = interpolateLocations(m.getLocations());
                    String filteredCategoriesString = createCategoriesString(filterCategories(m.getCategories()));
                    outMessage = coordinatesToHood(interpolLoc[0], interpolLoc[1], filteredCategoriesString, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    outMessage = "error in /json handling";
                }
                return outMessage;
            }

            @Command("/Category")
            String categories(String search) {
                final StringBuilder builder = new StringBuilder();
                categoryMap.keySet().forEach(s -> {
                    if (s.toLowerCase().contains(search.toLowerCase())) {
                        builder.append(s);
                        builder.append("\n");
                    }
                });

                return builder.toString();
            }
            @Command("/test")
            String testMessage() {
                StringBuilder message = new StringBuilder("Tried following testcases:\n");
                String[] testcases = {
                        "Schottentor; Museumsquartier/Metro Station", //Herrengasse
                        "48.215476, 16.364149;48.202577, 16.361445/Metro Station", //Herrengasse
                        "/Category \"Metro Station\"", //works
                        "Rathhaus Wien; Secession;Blockfabrik",
                        "Stadtpark;Schwedenplatz;Landstraße/Metro Station",//Landstraße
                        "Stadtpark;Schwedenplatz;Landstraße;Herrengasse/Metro Station" //Stubentor
                };
                for (int i = 0; i < testcases.length; i++) {
                    message.append("\n<b>Testcase:</b> ").append(i+1).append("\n")
                            .append("Request: ").append(testcases[i]).append("\n")
                            .append("Result: \n")
                            .append(createMessage(testcases[i]))
                            .append("\n\n\n");
                }
                return message.toString();
            };
        });
        if (inMessage == null) {
            return "no message found";
        } else {
            if(inMessage.charAt(0) == '/'){
                try {
                    return (String) engine.parse(inMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String[] parts = inMessage.split("/");
            String[] locationStrings = parts[0].split(";");
            for (int i = 0; i < locationStrings.length ; i++) {
                while (locationStrings[i].charAt(0) == ' '){
                    locationStrings[i] = locationStrings[i].substring(1);
                }
                if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(locationStrings[i].substring(0, 1))) {
                    OSMLocation locationOfAddress = OSMLocation.getLocationForAddress(locationStrings[i]);
                    try {
                        locationStrings[i] = locationOfAddress.getLat() + "," + locationOfAddress.getLon();
                    }catch (NullPointerException e){
                        return "OOPSIE Something went wrong" + e.getMessage();
                    }
                }
            }
            logger.debug("LocationStrings: [{},{}]", locationStrings[0], locationStrings[1]);
            String filteredCategoriesString= "";
            if (parts.length > 2 ) {
                return "Message could not be parsed. \nUse ',' to Split longitude and latitude Coordinates\n" +
                        "';' to Split different coordinates and '/' to split between coordinates and category";
            }else if (parts.length ==2) {
                String[] categories = parts[1].split(";");
                logger.info("Searching for '{}'", Arrays.toString(categories));
                ArrayList<String> filteredCategories = filterCategories(categories);
                filteredCategoriesString = createCategoriesString(filteredCategories);
            }
            double[][] locations = new double[locationStrings.length][2];
            for (int i = 0; i < locationStrings.length; i++) {
                locations[i] = parseLocationString(locationStrings[i]);
            }

            try {
                double[] interpolLocation = interpolateLocations(locations);
                //return locationsToString(interpolLocation[0],interpolLocation[1]);
                String message = coordinatesToHood(interpolLocation[0], interpolLocation[1], filteredCategoriesString, jsonFlag);
                logger.info("Sending message: \"{}\"", message);
                return message;
            } catch (Exception e) {
                logger.error("Message could not be created! " + e.getMessage());
                return e.getMessage() + " or equals null";
            }
        }
    }

    @Override
    protected void doRun(final Event event, EventListener executingListener) throws Exception {
        if (event instanceof ConnectionSpecificEvent) {
            handleMessageEvent((ConnectionSpecificEvent) event);
        }
    }

    //Given a Won Message returns the Text message from it
    private String extractTextMessageFromWonMessage(WonMessage wonMessage) {
        if (wonMessage == null) return null;
        return WonRdfUtils.MessageUtils.getTextMessage(wonMessage);
    }

    private ArrayList<String> filterCategories(String[] categories) {
        ArrayList<String> filtered = new ArrayList<>();
        for (String category : categories) {
            if (category.charAt(0) == ' '){
                category = category.substring(1);
            }
            if (categoryMap.containsKey(category)) {
                filtered.add(categoryMap.get(category));
            }
        }
        return filtered;
    }

    private void handleMessageEvent(final ConnectionSpecificEvent messageEvent) {
        getEventListenerContext().getTaskScheduler().schedule(() -> {
            String message = null;
            if (messageEvent instanceof MessageEvent) {
                message =
                        createMessage(extractTextMessageFromWonMessage(((MessageEvent) messageEvent).getWonMessage()));
            } else {
                message = createMessage(null);
            }
            URI connectionUri = messageEvent.getConnectionURI();
            logger.debug("sending message " + message);
            URI senderSocket = messageEvent.getSocketURI();
            URI targetSocket = messageEvent.getTargetSocketURI();
            try {
                EventListenerContext ctx = getEventListenerContext();

                MeetingBotContextWrapper botContextWrapper = (MeetingBotContextWrapper) ctx.getBotContextWrapper();

                WonMessage wonMessage =
                        WonMessageBuilder.connectionMessage().sockets().sender(senderSocket).recipient(targetSocket).content().text(message).build();
                ctx.getWonMessageSender().prepareAndSendMessage(wonMessage);

            } catch (Exception e) {
                logger.warn("could not send message via connection {}", connectionUri, e);
            }
        }, new Date(System.currentTimeMillis() + this.millisTimeoutBeforeReply));
    }

    //Given an Array of Locations [latitude][longitude] returns the weighted center of those locations.
    private double[] interpolateLocations(double[][] locations) throws Exception {
        double sumLat = 0;
        double sumLong = 0;
        int i;
        for (i = 0; i < locations.length; i++) {
            if (locations[i].length != 2) {
                throw new Exception("Input does not match Expectations (length of 2)");
            }
            sumLat += locations[i][0];
            sumLong += locations[i][1];
        }
        return new double[]{sumLat / i, sumLong / i};
    }

    private HashMap<String, String> loadCategoryMap() {
        FSCategoryResult result =
                new FSRequestBuilder("https://api.foursquare.com/v2/venues/categories").executeForObject(FSCategoryResult.class);

        HashMap<String, String> map = new HashMap<>();
        if (result == null || result.getMeta() == null || result.getMeta().getCode() != 200) {
            logger.error("Could not load categories from foursquare!");
            return map;
        }

        for (FSCategory category : result.getResponse().getCategories()) {
            addCategoryToMap(map, category);
        }

        logger.info("We have {} categories", map.size());
        return map;
    }

    private String locationsToString(double longitude, double latitude) {
        return longitude + "," + latitude;
    }

    //first latitude then longitude
    //Returns an double[2] array containing latitude and longitude as doubles
    private double[] parseLocationString(String locationString) {
        locationString = locationString.replace(" ","");
        String[] latlng = locationString.split(",");
        String lat = latlng[0];
        String lng = latlng[1];
        return new double[]{Double.parseDouble(lat), Double.parseDouble(lng)};
    }

}