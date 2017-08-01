package org.axonframework.sample.lmg.cqrs.eventhandling;

/**
 * Created by RAMI on 26/06/2017.
 */
import org.axonframework.eventhandling.EventMessage;

/**
 * Interface toward a mechanism that provides the AMQP Routing Key for a given EventMessage. AMQP Message Brokers use
 * the routing key to decide which Queues will receive a copy of eah message, depending on the type of Exchange used.
 *
 * @author Allard Buijze
 * @since 2.0
 */
@FunctionalInterface
public interface RoutingKeyResolver {

    /**
     * Returns the Routing Key to use when sending the given {@code eventMessage} to the Message Broker.
     *
     * @param eventMessage The EventMessage to resolve the routing key for
     * @return the routing key for the event message
     */
    String resolveRoutingKey(EventMessage<?> eventMessage);
}

