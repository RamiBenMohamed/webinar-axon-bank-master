package org.axonframework.sample.lmg.cqrs.eventhandling;

/**
 * Created by RAMI on 26/06/2017.
 */
import org.axonframework.common.AxonException;

/**
 * Exception indication that an error occurred while publishing an event to an AMQP Broker
 *
 * @author Allard Buijze
 * @since 2.0
 */
public class EventPublicationFailedException extends AxonException {

    private static final long serialVersionUID = 3663633361627495227L;

    /**
     * Initialize the exception using given descriptive {@code message} and {@code cause}
     *
     * @param message A message describing the exception
     * @param cause   The exception describing the cause of the failure
     */
    public EventPublicationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
