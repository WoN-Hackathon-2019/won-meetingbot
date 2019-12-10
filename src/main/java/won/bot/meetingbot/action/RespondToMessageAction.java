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
import won.protocol.message.WonMessage;
import won.protocol.message.builder.WonMessageBuilder;
import won.protocol.util.WonRdfUtils;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Date;

/**
 * Listener that responds to open and message events with automatic messages.
 * Can be configured to apply a timeout (non-blocking) before sending messages.
 */
public class RespondToMessageAction extends BaseEventBotAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private long millisTimeoutBeforeReply = 0;

    public RespondToMessageAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    public RespondToMessageAction(final EventListenerContext eventListenerContext,
                                  final long millisTimeoutBeforeReply) {
        super(eventListenerContext);
        this.millisTimeoutBeforeReply = millisTimeoutBeforeReply;
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
            double[][] locations = new double[locationStrings.length][2];
            for (int i = 0; i < locationStrings.length; i++) {
                locations[i] = parseLocationString(locationStrings[i]);
            }
            try {
                double[] interpolLocation = interpolateLocations(locations);
                return locationsToString(interpolLocation[0],interpolLocation[1]);
            } catch (Exception e) {
                return e.getMessage()+" or equals null";
            }
        }
    }

    private  String coordinatesToHood(double longitude, double latitude){
        return "";
    }
    private String locationsToString(double longitude, double latitude){
        return "Location: "+longitude+","+latitude;
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
    private double[] parseLocationString(String locationString){
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