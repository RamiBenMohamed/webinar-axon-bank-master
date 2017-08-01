package org.axonframework.sample.lmg.cqrs.eventhandling.legacy;

/**
 * Created by RAMI on 26/06/2017.
 */
import org.axonframework.common.DateTimeUtils;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.axonframework.serialization.SerializedMessage;
import org.axonframework.serialization.SerializedMetaData;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.SimpleSerializedObject;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Reader that reads EventMessage instances written to the underlying input. Typically, these messages have been written
 * using a {@link EventMessageWriter}. This reader distinguishes between DomainEventMessage and regular EventMessage
 * implementations and will reconstruct an instance implementing that same interface when reading.
 *
 * @author Allard Buijze
 * @since 2.0
 */
public class EventMessageReader {

    private final Serializer serializer;
    private final DataInputStream in;

    /**
     * Creates a new EventMessageReader that reads the data from the given {@code input} and deserializes payload
     * and meta data using the given {@code serializer}.
     *
     * @param input      The input providing access to the written data
     * @param serializer The serializer to deserialize payload and meta data with
     */
    public EventMessageReader(DataInputStream input, Serializer serializer) {
        this.in = input;
        this.serializer = serializer;
    }

    /**
     * Reads an EventMessage from the underlying input. If the written event was a DomainEventMessage, an instance of
     * DomainEventMessage is returned.
     *
     * @param <T> The type of payload expected to be in the returned EventMessage. This is not checked at runtime!
     * @return an EventMessage representing the message originally written, or {@code null} if the stream has
     * reached the end
     * @throws IOException                                                    when an error occurs reading from the
     *                                                                        underlying input
     * @throws java.io.EOFException                                           when the end of the stream was reached
     *                                                                        before the message was entirely read
     * @throws org.axonframework.serialization.UnknownSerializedTypeException if the type of the serialized object
     *                                                                        cannot be resolved to a class
     */
    public <T> EventMessage<T> readEventMessage() throws IOException {
        final int firstByte = in.read();
        if (firstByte == -1) {
            // end of stream
            return null;
        }
        EventMessageType messageType = EventMessageType.fromTypeByte((byte) firstByte);
        String identifier = in.readUTF();
        String timestamp = in.readUTF();
        String aggregateIdentifier = null;
        long sequenceNumber = 0;
        if (messageType == EventMessageType.DOMAIN_EVENT_MESSAGE) {
            aggregateIdentifier = in.readUTF();
            sequenceNumber = in.readLong();
        }
        String payloadType = in.readUTF();
        String payloadRevision = in.readUTF();
        byte[] payload = new byte[in.readInt()];
        in.readFully(payload);
        int metaDataSize = in.readInt();
        byte[] metaData = new byte[metaDataSize];
        in.readFully(metaData);
        SimpleSerializedObject<byte[]> serializedPayload =
                new SimpleSerializedObject<>(payload, byte[].class, payloadType, payloadRevision);
        SerializedMetaData<byte[]> serializedMetaData = new SerializedMetaData<>(metaData, byte[].class);

        if (messageType == EventMessageType.DOMAIN_EVENT_MESSAGE) {
            return new GenericDomainEventMessage<>(null, aggregateIdentifier, sequenceNumber,
                    new SerializedMessage<>(identifier, serializedPayload,
                            serializedMetaData, serializer),
                    () -> DateTimeUtils.parseInstant(timestamp));
        } else {
            return new GenericEventMessage<>(
                    new SerializedMessage<>(identifier, serializedPayload, serializedMetaData, serializer),
                    () -> DateTimeUtils.parseInstant(timestamp));
        }
    }
}