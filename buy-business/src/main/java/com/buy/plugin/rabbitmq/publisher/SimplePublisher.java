package com.buy.plugin.rabbitmq.publisher;

import com.buy.plugin.rabbitmq.Message;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * A simple publisher sends messages to a broker in a fire-and-forget manner.
 * </p>
 */
public class SimplePublisher extends DiscretePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePublisher.class);

    public SimplePublisher(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    /**
     * {@inheritDoc}
     * @throws TimeoutException 
     */
    @Override
    public void publish(Message message, DeliveryOptions deliveryOptions) throws IOException, TimeoutException {
        for (int attempt = 1; attempt <= DEFAULT_RETRY_ATTEMPTS; attempt++) {
            if (attempt > 1) {
                LOGGER.info("Attempt {} to send message", attempt);
            }

            try {
                Channel channel = provideChannel();
                message.publish(channel, deliveryOptions,null);
                return;
            } catch (IOException e) {
                handleIoException(attempt, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * @throws TimeoutException 
     */
    @Override
    public void publish(List<Message> messages, DeliveryOptions deliveryOptions) throws IOException, TimeoutException {
        for (Message message : messages) {
            publish(message, deliveryOptions);
        }
    }
}
