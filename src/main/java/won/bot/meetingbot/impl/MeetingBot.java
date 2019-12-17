package won.bot.meetingbot.impl;

import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.action.impl.LogAction;
import won.bot.framework.eventbot.behaviour.ExecuteWonMessageCommandBehaviour;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.atomlifecycle.AtomCreatedEvent;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.lifecycle.ShutdownEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.CloseFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.filter.impl.AtomUriInNamedListFilter;
import won.bot.framework.eventbot.filter.impl.NotFilter;
import won.bot.framework.eventbot.listener.BaseEventListener;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;
import won.bot.framework.extensions.matcher.MatcherBehaviour;
import won.bot.framework.extensions.matcher.MatcherExtension;
import won.bot.framework.extensions.matcher.MatcherExtensionAtomCreatedEvent;
import won.bot.framework.extensions.serviceatom.ServiceAtomBehaviour;
import won.bot.framework.extensions.serviceatom.ServiceAtomExtension;
import won.bot.meetingbot.action.RespondToMessageAction;
import won.bot.meetingbot.context.MeetingBotContextWrapper;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.util.linkeddata.WonLinkedDataUtils;
import won.protocol.vocabulary.WXCHAT;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Collection;

public class MeetingBot extends EventBot implements MatcherExtension, ServiceAtomExtension {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String OUR_ATOM_NAME = "our-atom";
    private static final String API_TAG = "meetingapi";
    private int registrationMatcherRetryInterval;
    private MatcherBehaviour matcherBehaviour;
    private ServiceAtomBehaviour serviceAtomBehaviour;

    @Override
    public MatcherBehaviour getMatcherBehaviour() {
        return matcherBehaviour;
    }

    @Override
    public ServiceAtomBehaviour getServiceAtomBehaviour() {
        return serviceAtomBehaviour;
    }

    @Override
    protected void initializeEventListeners() {
        logger.debug("initializeEventListeners");
        EventListenerContext ctx = getEventListenerContext();
        if (!(getBotContextWrapper() instanceof MeetingBotContextWrapper)) {
            logger.error(getBotContextWrapper().getBotName() + " does not work without a SkeletonBotContextWrapper");
            throw new IllegalStateException(getBotContextWrapper().getBotName() + " does not work without a " +
                    "SkeletonBotContextWrapper");
        }
        EventBus bus = getEventBus();
        MeetingBotContextWrapper botContextWrapper = (MeetingBotContextWrapper) getBotContextWrapper();
        // register listeners for event.impl.command events used to tell the bot to send
        // messages
        ExecuteWonMessageCommandBehaviour wonMessageCommandBehaviour = new ExecuteWonMessageCommandBehaviour(ctx);
        wonMessageCommandBehaviour.activate();
        // activate ServiceAtomBehaviour
        serviceAtomBehaviour = new ServiceAtomBehaviour(ctx);
        serviceAtomBehaviour.activate();


        bus.subscribe(AtomCreatedEvent.class, event -> {
            AtomCreatedEvent ev = (AtomCreatedEvent) event;
            logger.info("Our atom is @ https://hackathon.matchat.org/owner/#!/post/?postUri={}", ev.getAtomURI());
            getEventListenerContext().getBotContext().appendToNamedAtomUriList(ev.getAtomURI(), OUR_ATOM_NAME);

        });
        // set up matching extension
        // as this is an extension, it can be activated and deactivated as needed
        // if activated, a MatcherExtensionAtomCreatedEvent is sent every time a new
        // atom is created on a monitored node
        matcherBehaviour = new MatcherBehaviour(ctx, "BotSkeletonMatchingExtension", registrationMatcherRetryInterval);
        matcherBehaviour.activate();
        // create filters to determine which atoms the bot should react to
        NotFilter noOwnAtoms = new NotFilter(new AtomUriInNamedListFilter(ctx,
                ctx.getBotContextWrapper().getAtomCreateListName()));
        // filter to prevent reacting to serviceAtom<->ownedAtom events;
        NotFilter noInternalServiceAtomEventFilter = getNoInternalServiceAtomEventFilter();
        NotFilter x = new NotFilter(event -> true);
        BaseEventListener autoResponder = new ActionOnEventListener(ctx, new RespondToMessageAction(ctx));
        bus.subscribe(MessageFromOtherAtomEvent.class, autoResponder);
        bus.subscribe(CloseFromOtherAtomEvent.class, new ActionOnEventListener(ctx, new LogAction(ctx, "received " +
                "close message from remote atom.")));

        bus.subscribe(ConnectFromOtherAtomEvent.class, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener eventListener) throws Exception {
                EventListenerContext ctx = getEventListenerContext();
                logger.info("EVENT: {}", event);
                ConnectFromOtherAtomEvent con = (ConnectFromOtherAtomEvent) event;
                ConnectCommandEvent connectCommandEvent = new ConnectCommandEvent(con.getRecipientSocket(),
                        con.getSenderSocket(), "" + "Hi there, welcome to the meeting bot!\n" + "Just give me at " +
                        "least two addresses separated by a semicolon(;).\n" + "If you want a specific venue category" +
                        " just pass it after a slash(/).\n" + "If you want to search for a category, just write " +
                        "'/Category searchTerm'" + "E.g.: \nFriedrich-Schmidt-Platz 1, 1010 Wien;Museumsplatz 1, 1070" +
                        " Wien;Heldenplatz ," + "1010 " + "Wien/Metro Station");
                ctx.getEventBus().publish(connectCommandEvent);

/*
                CloseCommandEvent close = new CloseCommandEvent(con.getCon(), "Bye!");
                ctx.getEventBus().publish(close);

*/

            }

        });

        // Send new created atoms with a location an invitation
        bus.subscribe(MatcherExtensionAtomCreatedEvent.class, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) throws Exception {
                if (!(event instanceof MatcherExtensionAtomCreatedEvent)) {
                    return;
                }

                MatcherExtensionAtomCreatedEvent e = (MatcherExtensionAtomCreatedEvent) event;
                Dataset atomData = WonLinkedDataUtils.getFullAtomDataset(e.getAtomURI(),
                        getEventListenerContext().getLinkedDataSource());
                final DefaultAtomModelWrapper amw = new DefaultAtomModelWrapper(atomData);
                if (amw.getAllTags().contains(API_TAG)) {
                    logger.info("Found a new atom with tag '{}'. Trying to establish a connection ...", API_TAG);
                    // Open Connection to atom

                    Collection<URI> sockets = WonLinkedDataUtils.getSocketsOfType(e.getAtomURI(),
                            URI.create(WXCHAT.ChatSocketString), getEventListenerContext().getLinkedDataSource());
                    //sockets should have 0 or 1 items
                    if (sockets.isEmpty()) {
                        //did not find a socket of that type
                        logger.error("Could not get chat socket of atomData: {}", atomData);
                        return;
                    }
                    URI target = sockets.iterator().next();
                    logger.info("Found socket: {}", target);

                    Collection<URI> mySockets =
                            WonLinkedDataUtils.getSocketsOfType(botContextWrapper.getServiceAtomUri(),
                                    URI.create(WXCHAT.ChatSocketString),
                                    getEventListenerContext().getLinkedDataSource());
                    //sockets should have 0 or 1 items
                    if (mySockets.isEmpty()) {
                        //did not find a socket of that type
                        logger.error("Could not get chat socket of atomData: {}", atomData);
                        return;
                    }
                    ConnectCommandEvent connectCommandEvent = new ConnectCommandEvent(mySockets.iterator().next(),
                            target, "message");
                    getEventBus().publish(connectCommandEvent);

                }
            }
        });

        bus.subscribe(ShutdownEvent.class, event -> {
            ShutdownEvent shutdownEvent = (ShutdownEvent) event;
            printAtoms(botContextWrapper);
        })
/*        bus.subscribe(ConnectFromOtherAtomEvent.class, x, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) {
                logger.debug("ConnectFromOtherAtomEvent");
                EventListenerContext ctx = getEventListenerContext();
                ConnectFromOtherAtomEvent connectFromOtherAtomEvent = (ConnectFromOtherAtomEvent) event;
                try {
                    String message = "Hello i am the MeetingBot! Please tell me two locations and I'll tell you a
                    venue.";
                    final ConnectCommandEvent connectCommandEvent = new ConnectCommandEvent(
                                    connectFromOtherAtomEvent.getRecipientSocket(),
                                    connectFromOtherAtomEvent.getSenderSocket(), message);
                    ctx.getEventBus().subscribe(ConnectCommandSuccessEvent.class, new ActionOnFirstEventListener(ctx,
                                    new CommandResultFilter(connectCommandEvent), new BaseEventBotAction(ctx) {
                                        @Override
                                        protected void doRun(Event event, EventListener executingListener) {
                                            ConnectCommandResultEvent connectionMessageCommandResultEvent =
                                            (ConnectCommandResultEvent) event;
                                            if (!connectionMessageCommandResultEvent.isSuccess()) {
                                                logger.error("Failure when trying to open a received Request: "
                                                                + connectionMessageCommandResultEvent.getMessage());
                                            } else {
                                                logger.info(
                                                                "Add an established connection " +
                                                                                connectCommandEvent.getLocalSocket()
                                                                                + " -> "
                                                                                + connectCommandEvent.getTargetSocket()
                                                                                +
                                                                                " to the botcontext ");
                                                botContextWrapper.addConnectedSocket(
                                                                connectCommandEvent.getLocalSocket(),
                                                                connectCommandEvent.getTargetSocket());
                                            }
                                        }
                                    }));
                    ctx.getEventBus().publish(connectCommandEvent);
                } catch (Exception te) {
                    logger.error(te.getMessage(), te);
                }
            }
        })*/;
        // listen for the MatcherExtensionAtomCreatedEvent
        //bus.subscribe(MatcherExtensionAtomCreatedEvent.class, new MatcherExtensionAtomCreatedAction(ctx));
        bus.subscribe(CloseFromOtherAtomEvent.class, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener executingListener) {
                EventListenerContext ctx = getEventListenerContext();
                CloseFromOtherAtomEvent closeFromOtherAtomEvent = (CloseFromOtherAtomEvent) event;
                URI targetSocketUri = closeFromOtherAtomEvent.getSocketURI();
                URI senderSocketUri = closeFromOtherAtomEvent.getTargetSocketURI();
                logger.info("Remove a closed connection " + senderSocketUri + " -> " + targetSocketUri + " from the " +
                        "botcontext ");
                botContextWrapper.removeConnectedSocket(senderSocketUri, targetSocketUri);
            }
        });
    }

    private void printAtoms(MeetingBotContextWrapper botContextWrapper) {
        botContextWrapper.getConnectedSockets().forEach((uri, uris) -> {
            logger.debug("URI in connected sockets: {}", uri);
        });
    }

    // bean setter, used by spring
    public void setRegistrationMatcherRetryInterval(final int registrationMatcherRetryInterval) {
        this.registrationMatcherRetryInterval = registrationMatcherRetryInterval;
    }

}
