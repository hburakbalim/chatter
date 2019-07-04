package chatter.messaging.bus;

import chatter.messaging.cache.ChatterConfCache;
import chatter.messaging.cache.OnlineUser;
import chatter.messaging.event.Event;
import chatter.messaging.event.EventHandler;
import chatter.messaging.exception.MessageBusException;
import chatter.messaging.hazelcast.HazelcastInstanceProvider;
import chatter.messaging.model.ConnectedUserModel;
import chatter.messaging.model.MessageEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Component
public class MessagingBus extends EventHandler {

    private OnlineUser onlineUser;
    private ChatterConfCache chatterConfCache;

    public MessagingBus(OnlineUser onlineUser, ChatterConfCache chatterConfCache, HazelcastInstanceProvider hazelcastInstanceProvider) {
        super(hazelcastInstanceProvider);
        this.onlineUser = onlineUser;
        this.chatterConfCache = chatterConfCache;
    }

    @Override
    public void handle(Event event) throws MessageBusException {
        MessageEvent  messageEvent = (MessageEvent) event.getEventPayload();
        ConnectedUserModel connectedUserModel = onlineUser.get(messageEvent.getUserId());

        try {
            Socket client = connectedUserModel.getClient();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
            objectOutputStream.writeObject(messageEvent.getMessage());
        } catch (IOException exception) {
            throw new MessageBusException("Exception while write message:" + connectedUserModel, exception);
        }
    }

    @Override
    public void register() {
        super.onRegister(chatterConfCache.getMessageTopicName());
    }
}
