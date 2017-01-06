package com.buy.plugin.rabbitmq.publisher;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.buy.plugin.rabbitmq.Message;

/**
 * A message publisher publishes messages to a broker.
 */
public interface MessagePublisher {

    /**
     * Publishes a message to the broker using no delivery options.
     * 
     * @param message The message to publish
     * @throws IOException if the message could not be published
     * @throws TimeoutException 
     */
    void publish(Message message) throws IOException, TimeoutException;

    /**
     * Publishes a message to the broker using the given delivery options.
     * 
     * @param message The message to publish
     * @param deliveryOptions The delivery options
     * @throws IOException if the message could not be published
     * @throws TimeoutException 
     */
    void publish(Message message, DeliveryOptions deliveryOptions) throws IOException, TimeoutException;

    /**
     * Publishes messages to the broker using no delivery options.
     *
     * @param messages The list of messages to publish
     * @throws IOException if the message could not be published
     * @throws TimeoutException 
     */
    void publish(List<Message> messages) throws IOException, TimeoutException;

    /**
     * Publishes messages to the broker using the given delivery options.
     *
     * @param messages The list of messages to publish
     * @param deliveryOptions The delivery options
     * @throws IOException if the message could not be published
     * @throws TimeoutException 
     */
    void publish(List<Message> messages, DeliveryOptions deliveryOptions) throws IOException, TimeoutException;
    
    /**
     * Closes the publisher by closing its underlying channel.
     * 
     * @throws IOException if the channel cannot be closed correctly. Usually occurs when the channel 
     * is already closing or is already closed.
     * @throws TimeoutException 
     */
    void close() throws IOException, TimeoutException;
}