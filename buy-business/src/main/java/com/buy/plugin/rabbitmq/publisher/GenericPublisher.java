package com.buy.plugin.rabbitmq.publisher;

import com.buy.plugin.rabbitmq.Message;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Publishes messages with certain different levels of {@link PublisherReliability}.
 */
public class GenericPublisher implements MessagePublisher {

    MessagePublisher publisher;

    /**
     * <p>Initializes the publisher with a certain level of reliability. All messages
     * sent by the producer are sent with this level of reliability. Uses the given
     * connection factory to establish connections.</p>
     *
     * @see SimplePublisher
     * @see ConfirmedPublisher
     * @see TransactionalPublisher
     *
     * @param connectionFactory The connection factory
     * @param reliability The reliability level
     */
    public GenericPublisher(ConnectionFactory connectionFactory, PublisherReliability reliability) {
        if (reliability == PublisherReliability.CONFIRMED) {
            publisher = new ConfirmedPublisher(connectionFactory);
        } else if (reliability == PublisherReliability.TRANSACTIONAL) {
            publisher = new TransactionalPublisher(connectionFactory);
        } else {
            publisher = new SimplePublisher(connectionFactory);
        }
    }

    /**
     * {@inheritDoc}
     * @throws TimeoutException 
     */
    @Override
    public void publish(Message message) throws IOException, TimeoutException {
        publish(message, DeliveryOptions.NONE);
    }

    /**
     * {@inheritDoc}
     * @throws TimeoutException 
     */
    @Override
    public void publish(List<Message> messages) throws IOException, TimeoutException {
        publish(messages, DeliveryOptions.NONE);
    }

    /**
     * {@inheritDoc}
     * @throws TimeoutException 
     */
    @Override
    public void publish(Message message, DeliveryOptions deliveryOptions)
            throws IOException, TimeoutException {
        publisher.publish(message, deliveryOptions);
    }

    /**
     * {@inheritDoc}
     * @throws TimeoutException 
     */
    @Override
    public void publish(List<Message> messages, DeliveryOptions deliveryOptions) throws IOException, TimeoutException {
        publisher.publish(messages, deliveryOptions);
    }

    /**
     * {@inheritDoc}
     * @throws TimeoutException 
     */
    @Override
    public void close() throws IOException, TimeoutException {
        publisher.close();
    }

}