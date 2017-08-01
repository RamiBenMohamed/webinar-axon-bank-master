package org.axonframework.sample.lmg.cqrs.eventhandling.legacy;

/**
 * Created by RAMI on 26/06/2017.
 */



import org.axonframework.common.Assert;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.sample.lmg.cqrs.eventhandling.*;
import org.axonframework.serialization.Serializer;

import java.io.*;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the AMQPMessageConverter interface that serializes the Message using Java Serialization, while
 * the payload and meta data of the messages are serialized using the configured Serializer.
 * <p>
 * This class is not the recommended approach, as it doesn't play well with non-axon consumers. It is available for
 * backwards compatibility reasons, as this implementation was the default in Axon 2.
 *
 * @author Allard Buijze
 */
public class JavaSerializationSQSMessageConverter implements SQSMessageConverter {



    private final Serializer serializer;
    private final RoutingKeyResolver routingKeyResolver;
    private final boolean durable;

    /**
     * Initializes the AMQPMessageConverter with the given {@code serializer}, using a {@link
     * PackageRoutingKeyResolver} and requesting durable dispatching.
     *
     * @param serializer The serializer to serialize the Event Message's payload and Meta Data with
     */
    public JavaSerializationSQSMessageConverter(Serializer serializer) {
        this(serializer, new PackageRoutingKeyResolver(), true);
    }

    /**
     * Initializes the AMQPMessageConverter with the given {@code serializer}, {@code routingKeyResolver} and
     * requesting durable dispatching when {@code durable} is {@code true}.
     *
     * @param serializer         The serializer to serialize the Event Message's payload and Meta Data with
     * @param routingKeyResolver The strategy to use to resolve routing keys for Event Messages
     * @param durable            Whether to request durable message dispatching
     */
    public JavaSerializationSQSMessageConverter(Serializer serializer, RoutingKeyResolver routingKeyResolver, boolean durable) {
        Assert.notNull(serializer, () -> "Serializer may not be null");
        Assert.notNull(routingKeyResolver, () -> "RoutingKeyResolver may not be null");
        this.serializer = serializer;
        this.routingKeyResolver = routingKeyResolver;
        this.durable = durable;
    }

    @Override
    public SQSMessage createSQSMessage(EventMessage eventMessage) {
        byte[] body = asByteArray(eventMessage);
        String routingKey = routingKeyResolver.resolveRoutingKey(eventMessage);
        if (durable) {
            return new SQSMessage(body, routingKey, false, false);
        }
        return new SQSMessage(body, routingKey);
    }

    @Override
    public Optional<EventMessage<?>> readSQSMessage(byte[] messageBody, Map<String, Object> headers) {
        try {
            EventMessageReader in = new EventMessageReader(new DataInputStream(new ByteArrayInputStream(messageBody)),
                    serializer);
            return Optional.of(in.readEventMessage());
        } catch (IOException e) {
            // ByteArrayInputStream doesn't throw IOException... anyway...
            throw new EventPublicationFailedException("Failed to deserialize an EventMessage", e);
        }
    }

    private byte[] asByteArray(EventMessage event) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            EventMessageWriter outputStream = new EventMessageWriter(new DataOutputStream(baos), serializer);
            outputStream.writeEventMessage(event);
            return baos.toByteArray();
        } catch (IOException e) {
            // ByteArrayOutputStream doesn't throw IOException... anyway...
            throw new EventPublicationFailedException("Failed to serialize an EventMessage", e);
        }
    }
}
