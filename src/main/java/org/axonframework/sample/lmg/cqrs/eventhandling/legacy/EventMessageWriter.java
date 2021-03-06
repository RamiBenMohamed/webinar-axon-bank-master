package org.axonframework.sample.lmg.cqrs.eventhandling.legacy;

/**
 * Created by RAMI on 26/06/2017.
 */


import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.serialization.MessageSerializer;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.Serializer;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Writer that writes Event Messages onto a an OutputStream. The format of the message makes them suitable to be read
 * back in using a {@link EventMessageReader}. This writer distinguishes between DomainEventMessage and plain
 * EventMessage when writing. The reader will reconstruct an aggregate implementation for the same message type (i.e.
 * DomainEventMessage or EventMessage).
 *
 * @author Allard Buijze
 * @since 2.0
 */
public class EventMessageWriter {

    private final MessageSerializer serializer;
    private final DataOutput out;

    /**
     * Creates a new EventMessageWriter writing data to the specified underlying {@code output}.
     *
     * @param output     the underlying output
     * @param serializer The serializer to deserialize payload and metadata with
     */
    public EventMessageWriter(DataOutput output, Serializer serializer) {
        this.out = output;
        this.serializer = new MessageSerializer(serializer);
    }

    /**
     * Writes the given {@code eventMessage} to the underling output.
     *
     * @param eventMessage the EventMessage to write to the underlying output
     * @throws IOException when any exception occurs writing to the underlying stream
     */
    public void writeEventMessage(EventMessage eventMessage) throws IOException {
        if (DomainEventMessage.class.isInstance(eventMessage)) {
            out.writeByte(EventMessageType.DOMAIN_EVENT_MESSAGE.getTypeByte());
        } else {
            out.writeByte(EventMessageType.EVENT_MESSAGE.getTypeByte());
        }
        out.writeUTF(eventMessage.getIdentifier());
        out.writeUTF(eventMessage.getTimestamp().toString());
        if (eventMessage instanceof DomainEventMessage) {
            DomainEventMessage domainEventMessage = (DomainEventMessage) eventMessage;
            out.writeUTF(domainEventMessage.getAggregateIdentifier());
            out.writeLong(domainEventMessage.getSequenceNumber());
        }
        SerializedObject<byte[]> serializedPayload = serializer.serializePayload(eventMessage, byte[].class);
        SerializedObject<byte[]> serializedMetaData = serializer.serializeMetaData(eventMessage, byte[].class);

        out.writeUTF(serializedPayload.getType().getName());
        String revision = serializedPayload.getType().getRevision();
        out.writeUTF(revision == null ? "" : revision);
        out.writeInt(serializedPayload.getData().length);
        out.write(serializedPayload.getData());
        out.writeInt(serializedMetaData.getData().length);
        out.write(serializedMetaData.getData());
    }
}