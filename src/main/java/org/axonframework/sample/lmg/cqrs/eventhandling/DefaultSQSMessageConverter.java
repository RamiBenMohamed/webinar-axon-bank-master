package org.axonframework.sample.lmg.cqrs.eventhandling;

/**
 * Created by RAMI on 26/06/2017.
 */
import org.axonframework.common.Assert;
import org.axonframework.common.DateTimeUtils;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.*;

import java.util.*;

import static org.axonframework.serialization.MessageSerializer.serializePayload;

/**
 * Default implementation of the AMQPMessageConverter interface. This implementation will suffice in most cases. It
 * passes all meta-data entries as headers (with 'axon-metadata-' prefix) to the message. Other message-specific
 * attributes are also added as meta data. The message payload is serialized using the configured serializer and passed
 * as the message body.0102202,
 *
 * @author Allard Buijze
 */
public class DefaultSQSMessageConverter implements SQSMessageConverter {

    private final Serializer serializer;
    private final RoutingKeyResolver routingKeyResolver;
    private final boolean durable;

    /**
     * Initializes the AMQPMessageConverter with the given {@code serializer}, using a {@link
     * PackageRoutingKeyResolver} and requesting durable dispatching.
     *
     * @param serializer The serializer to serialize the Event Message's payload with
     */
    public DefaultSQSMessageConverter(Serializer serializer) {
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
    public DefaultSQSMessageConverter(Serializer serializer, RoutingKeyResolver routingKeyResolver, boolean durable) {
        Assert.notNull(serializer, () -> "Serializer may not be null");
        Assert.notNull(routingKeyResolver, () -> "RoutingKeyResolver may not be null");
        this.serializer = serializer;
        this.routingKeyResolver = routingKeyResolver;
        this.durable = durable;
    }

    @Override
    public SQSMessage createSQSMessage(EventMessage<?> eventMessage) {
        SerializedObject<byte[]> serializedObject = serializePayload(eventMessage, serializer, byte[].class);
        String routingKey = routingKeyResolver.resolveRoutingKey(eventMessage);
        Map<String, Object> headers = new HashMap<>();
        eventMessage.getMetaData().forEach((k, v) -> headers.put("axon-metadata-" + k, v));
        headers.put("axon-message-id", eventMessage.getIdentifier());
        headers.put("axon-message-type", serializedObject.getType().getName());
        headers.put("axon-message-revision", serializedObject.getType().getRevision());
        headers.put("axon-message-timestamp", eventMessage.getTimestamp().toString());
        if (eventMessage instanceof DomainEventMessage) {
            headers.put("axon-message-aggregate-id", ((DomainEventMessage) eventMessage).getAggregateIdentifier());
            headers.put("axon-message-aggregate-seq", ((DomainEventMessage) eventMessage).getSequenceNumber());
            headers.put("axon-message-aggregate-type", ((DomainEventMessage) eventMessage).getType());
        }

        return new SQSMessage(serializedObject.getData(), routingKey, false, false);
    }

    @Override
    public Optional<EventMessage<?>> readSQSMessage(byte[] messageBody, Map<String, Object> headers) {
        if (!headers.keySet().containsAll(Arrays.asList("axon-message-id", "axon-message-type"))) {
            return Optional.empty();
        }
        Map<String, Object> metaData = new HashMap<>();
        headers.forEach((k, v) -> {
            if (k.startsWith("axon-metadata-")) {
                metaData.put(k.substring("axon-metadata-".length()), v);
            }
        });
        SimpleSerializedObject<byte[]> serializedMessage = new SimpleSerializedObject<>(messageBody, byte[].class,
                Objects.toString(headers.get("axon-message-type")),
                Objects.toString(headers.get("axon-message-revision"), null));
        SerializedMessage<?> message = new SerializedMessage<>(Objects.toString(headers.get("axon-message-id")),
                new LazyDeserializingObject<>(serializedMessage, serializer),
                new LazyDeserializingObject<>(MetaData.from(metaData)));
        String timestamp = Objects.toString(headers.get("axon-message-timestamp"));
        if (headers.containsKey("axon-message-aggregate-id")) {
            return Optional.of(new GenericDomainEventMessage<>(Objects.toString(headers.get("axon-message-aggregate-type")),
                    Objects.toString(headers.get("axon-message-aggregate-id")),
                    (Long) headers.get("axon-message-aggregate-seq"),
                    message, () -> DateTimeUtils.parseInstant(timestamp)));
        } else {
            return Optional.of(new GenericEventMessage<>(message, () -> DateTimeUtils.parseInstant(timestamp)));
        }
    }

}

