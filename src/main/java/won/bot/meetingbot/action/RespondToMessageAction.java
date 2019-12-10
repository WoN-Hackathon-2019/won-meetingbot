package won.bot.meetingbot.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.ConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.MessageEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.meetingbot.context.MeetingBotContextWrapper;
import won.bot.meetingbot.foursquare.*;
import won.protocol.message.WonMessage;
import won.protocol.message.builder.WonMessageBuilder;
import won.protocol.util.WonRdfUtils;

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
            map.put(category.getName(), category.getId());
        }

        return map;
    }

    @Override
    protected void doRun(final Event event, EventListener executingListener) throws Exception {
        if (event instanceof ConnectionSpecificEvent) {
            handleMessageEvent((ConnectionSpecificEvent) event);
        }
    }

    private void handleMessageEvent(final ConnectionSpecificEvent messageEvent) {
        getEventListenerContext().getTaskScheduler().schedule(() -> {
            String message = null;
            if (messageEvent instanceof MessageEvent) {
                message = createMessage(
                        extractTextMessageFromWonMessage(((MessageEvent) messageEvent).getWonMessage()));
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

                WonMessage wonMessage = WonMessageBuilder
                        .connectionMessage()
                        .sockets()
                        .sender(senderSocket)
                        .recipient(targetSocket)
                        .content()
                        .text(message)
                        .build();
                ctx.getWonMessageSender().prepareAndSendMessage(wonMessage);

                /*getEventListenerContext().getWonMessageSender()
                        .prepareAndSendMessage(createWonMessage(connectionUri, message)); */

            } catch (Exception e) {
                logger.warn("could not send message via connection {}", connectionUri, e);
            }
        }, new Date(System.currentTimeMillis() + this.millisTimeoutBeforeReply));
    }

    //Given a Won Message returns the Text message from it
    private String extractTextMessageFromWonMessage(WonMessage wonMessage) {
        if (wonMessage == null)
            return null;
        return WonRdfUtils.MessageUtils.getTextMessage(wonMessage);
    }

    //Given a message containing lat and longitude of locations split by "/" and ";" returns a Message containing
    //informations regarding the best matching venue, else an error message
    private String createMessage(String inMessage) {
        if (inMessage == null) {
            return "no message found";
        } else {
            String[] parts = inMessage.split("/");
            String[] locationStrings = parts[0].split(";");
            String[] categories = parts[1].split(";");
            logger.info("Searching for '{}'", Arrays.toString(categories));
            ArrayList<String> filteredCategories = filterCategories(categories);
            String filteredCategoriesString = createCategoriesString(filteredCategories);
            double[][] locations = new double[locationStrings.length][2];
            for (int i = 0; i < locationStrings.length; i++) {
                locations[i] = parseLocationString(locationStrings[i]);
            }
            try {
                double[] interpolLocation = interpolateLocations(locations);
                //return locationsToString(interpolLocation[0],interpolLocation[1]);
                return coordinatesToHood(interpolLocation[0], interpolLocation[1], filteredCategoriesString);
            } catch (Exception e) {
                logger.error("Message could not be created! " + e.getMessage());
                return e.getMessage() + " or equals null";
            }
        }
    }

    private String createCategoriesString(ArrayList<String> filteredCategories) {
        StringBuilder sb = new StringBuilder();
        for(String category : filteredCategories){
            sb.append(category).append(',');
        }
        if(sb.length() > 0)
            return sb.toString().substring(0, sb.length()-1);
        return "";
    }

    private ArrayList<String> filterCategories(String[] categories) {
        ArrayList<String> filtered = new ArrayList<>();
        for(String category : categories){
            if(categoryMap.containsKey(category)){
                filtered.add(categoryMap.get(category));
            }
        }
        return filtered;
    }

    private String coordinatesToHood(double longitude, double latitude, String filteredCategoriesString) throws Exception {
        int range = 50;
        int i = 1;
        String coordinates = locationsToString(longitude, latitude);
        while (range <= 100000) {
            FSVenueResult request = new FSRequestBuilder("https://api.foursquare.com/v2/venues/search")
                    .withParameter("ll", coordinates)
                    .withParameter("range", Integer.toString(range))
                    .withParameter("categoryId", filteredCategoriesString)
                    .executeForObject(FSVenueResult.class);

            //TODO: check if this null check really catches no returned results
            if (request != null){
                if (request.getMeta() != null){
                    if(request.getMeta().getCode() == 200){
                       String name = request.getResponse().getVenues().get(0).getName()+"\n";
                       String address = request.getResponse().getVenues().get(0).getLocation().getFormattedAddress().toString();
                       return name+"\n"+address;
                    }
                }
            }
            range += 50 * i++;
        }
        throw new Exception("could not find Venues");
    }

    private String locationsToString(double longitude, double latitude) {
        return longitude + "," + latitude;
    }
    /*
    private String locationsToString(double[][] locations){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locations.length; i++) {
            sb.append("Location ").append(i).append(": ").append(locations[i][0]).append(",").append(locations[i][1]).append("\n");
        }
        return sb.toString();
    }*/

    //first latitude then longitude
    //Returns an double[2] array containing latitude and longitude as doubles
    private double[] parseLocationString(String locationString) {
        String[] latlng = locationString.split(",");
        String lat = latlng[0];
        String lng = latlng[1];
        return new double[]{Double.parseDouble(lat), Double.parseDouble(lng)};
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

}