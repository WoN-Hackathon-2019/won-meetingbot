package won.bot.skeleton.action;

import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.ConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.MessageEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.extensions.matcher.MatcherExtensionAtomCreatedEvent;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.protocol.exception.WonMessageBuilderException;
import won.protocol.message.WonMessage;
import won.protocol.message.builder.WonMessageBuilder;
import won.protocol.service.WonNodeInformationService;
import won.protocol.util.WonRdfUtils;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Set;

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

                SkeletonBotContextWrapper botContextWrapper = (SkeletonBotContextWrapper) ctx.getBotContextWrapper();

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

    private String extractTextMessageFromWonMessage(WonMessage wonMessage) {
        if (wonMessage == null)
            return null;
        return WonRdfUtils.MessageUtils.getTextMessage(wonMessage);
    }

    private String createMessage(String toEcho) {
        if (toEcho == null) {
            return "auto reply (delay: " + millisTimeoutBeforeReply + " millis)";
        } else {
            return "You said: '" + toEcho + "' (delay: " + millisTimeoutBeforeReply + " millis)";
        }
    }

}