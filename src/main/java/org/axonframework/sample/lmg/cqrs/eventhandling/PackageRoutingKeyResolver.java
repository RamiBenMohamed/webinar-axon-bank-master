package org.axonframework.sample.lmg.cqrs.eventhandling;

/**
 * Created by RAMI on 26/06/2017.
 */
import org.axonframework.eventhandling.EventMessage;

/**
 * RoutingKeyResolver implementation that uses the package name of the Message's payload as routing key.
 *
 * @author Allard Buijze
 * @since 2.0
 */
public class PackageRoutingKeyResolver implements RoutingKeyResolver {

    @Override
    public String resolveRoutingKey(EventMessage<?> event) {
        return event.getPayloadType().getPackage().getName();
    }
}
