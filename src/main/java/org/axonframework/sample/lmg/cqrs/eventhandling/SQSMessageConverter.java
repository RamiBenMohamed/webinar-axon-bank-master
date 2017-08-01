package org.axonframework.sample.lmg.cqrs.eventhandling;

/**
 * Created by RAMI on 26/06/2017.
 */
import org.axonframework.eventhandling.EventMessage;

import java.util.Map;
import java.util.Optional;

/**
 * Interface describing a mechanism that converts AMQP Messages from an Axon Messages and vice versa.
 *
 * @author Allard Buijze
 * @since 2.0
 */
public interface SQSMessageConverter {

    /**
     * Creates an AMQPMessage from given {@code eventMessage}.
     *
     * @param eventMessage The EventMessage to create the AMQP Message from
     * @return an AMQP Message containing the data and characteristics of the Message to send to the AMQP Message
     *         Broker.
     */
    SQSMessage createSQSMessage(EventMessage<?> eventMessage);

    /**
     * Reconstruct an EventMessage from the given {@code messageBody} and {@code headers}. The returned optional
     * resolves to a message if the given input parameters represented a correct event message.
     *
     * @param messageBody The body of the AMQP Message
     * @param headers     The headers attached to the AMQP Message
     * @return The Event Message to publish on the local event processors
     */
    Optional<EventMessage<?>> readSQSMessage(byte[] messageBody, Map<String, Object> headers);
}
