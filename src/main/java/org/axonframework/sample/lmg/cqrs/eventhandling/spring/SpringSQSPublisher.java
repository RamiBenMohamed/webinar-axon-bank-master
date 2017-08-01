package org.axonframework.sample.lmg.cqrs.eventhandling.spring;

/**
 * Created by RAMI on 26/06/2017.
 */
    import com.amazonaws.ClientConfiguration;
    import com.amazonaws.Protocol;
    import com.amazonaws.auth.AWSCredentialsProvider;
    import com.amazonaws.regions.Region;
    import com.amazonaws.regions.Regions;
    import com.amazonaws.services.sns.AmazonSNSClient;
    import com.amazonaws.services.sns.model.PublishRequest;
    import com.amazonaws.services.sns.model.PublishResult;

    import org.axonframework.sample.lmg.cqrs.eventhandling.*;

        import org.apache.commons.codec.binary.Base32;
        import org.axonframework.common.AxonConfigurationException;
        import org.axonframework.common.Registration;
        import org.axonframework.eventhandling.EventMessage;
        import org.axonframework.messaging.SubscribableMessageSource;
        import org.axonframework.messaging.unitofwork.CurrentUnitOfWork;
        import org.axonframework.messaging.unitofwork.UnitOfWork;
        import org.axonframework.serialization.Serializer;
      //  import org.slf4j.Logger;
        //import org.slf4j.LoggerFactory;
        import org.springframework.beans.BeansException;
        import org.springframework.beans.factory.InitializingBean;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.context.ApplicationContext;
        import org.springframework.context.ApplicationContextAware;
    import org.springframework.context.annotation.ComponentScan;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.stereotype.Component;


    import javax.annotation.PostConstruct;
    import java.io.IOException;
        import java.util.List;
        import java.util.Map;
        import java.util.concurrent.TimeoutException;

/**
 * EventBusTerminal implementation that uses an AMQP 0.9 compatible Message Broker to dispatch event messages. All
 * outgoing messages are sent to a configured Exchange, which defaults to "{@code Axon.EventBus}".
 * <p>
 * This terminal does not dispatch Events internally, as it relies on each event processor to listen to it's own AMQP Queue.
 *
 * @author Allard Buijze
 * @since 3.0
 */
@Component
public class SpringSQSPublisher implements InitializingBean, ApplicationContextAware {

   // private static final Logger logger = LoggerFactory.getLogger(SpringSQSPublisher.class);
    private static final String DEFAULT_EXCHANGE_NAME = "Axon.EventBus";

    private final SubscribableMessageSource<EventMessage<?>> messageSource;



    @Autowired
    coreConfiguration LaConfig;
    private AmazonSNSClient snsClient;
    private boolean isDurable = true;
    private SQSMessageConverter messageConverter;
    private ApplicationContext applicationContext;
    private Serializer serializer;
    private RoutingKeyResolver routingKeyResolver;
    private Registration eventBusRegistration;

    /**
     * Initialize this instance to publish message as they are published on the given {@code messageSource}.
     *
     * @param messageSource The component providing messages to be publishes
     */

    public SpringSQSPublisher(SubscribableMessageSource<EventMessage<?>> messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Subscribes this publisher to the messageSource provided during initialization.
     */
    public void start() {
        eventBusRegistration = messageSource.subscribe(this::send);
    }

    /**
     * Shuts down this component and unsubscribes it from its messageSource.
     */
    public void shutDown() {
        if (eventBusRegistration != null) {
            eventBusRegistration.cancel();
        }
    }

    /**
     * Sends the given {@code events} to the configured AMQP Exchange. It takes the current Unit of Work into account
     * when available. Otherwise, it simply publishes directly.
     *
     * @param events the events to publish on the AMQP Message Broker
     */
  
    protected void send(List<? extends EventMessage<?>> events) {

        AWSCredentialsProvider aws = LaConfig.getCredentialProvider();
        ClientConfiguration cC = LaConfig.getClientConfiguration();
        cC.setProtocol(Protocol.HTTPS);
        String Topic = LaConfig.getSNSTopic();
        try {
            snsClient = new AmazonSNSClient(aws,cC);
            snsClient.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));

            for (EventMessage event : events) {
                SQSMessage sqsMessage = messageConverter.createSQSMessage(event);
                Base32 base32 = new Base32();
                String encoded = new String(base32.encode(sqsMessage.toString().getBytes()));
                //    logger.info("***************messager converted****************");
                //String decoded new String(base32.decode(encoded.getBytes());
                PublishRequest publishRequest = new PublishRequest(Topic, encoded);
                PublishResult publishResult= snsClient.publish(publishRequest);
              //  logger.info("***************messager published****************");
            }
            if (CurrentUnitOfWork.isStarted()) {
                UnitOfWork<?> unitOfWork = CurrentUnitOfWork.get();
            }
        }
        catch(Exception e){

          //  logger.info("cant sen messeage to the topic sns");

        }
    }







    @Override
    public void afterPropertiesSet() throws Exception {

        if (messageConverter == null) {
            if (serializer == null) {
                serializer = applicationContext.getBean(Serializer.class);
            }
            if (routingKeyResolver == null) {
                Map<String, RoutingKeyResolver> routingKeyResolverCandidates = applicationContext.getBeansOfType(
                        RoutingKeyResolver.class);
                if (routingKeyResolverCandidates.size() > 1) {
                    throw new AxonConfigurationException("No MessageConverter was configured, but none can be created "
                            + "using autowired properties, as more than 1 "
                            + "RoutingKeyResolver is present in the "
                            + "ApplicationContent");
                } else if (routingKeyResolverCandidates.size() == 1) {
                    routingKeyResolver = routingKeyResolverCandidates.values().iterator().next();
                } else {
                    routingKeyResolver = new PackageRoutingKeyResolver();
                }
            }
            messageConverter = new DefaultSQSMessageConverter(serializer, routingKeyResolver, isDurable);
        }
    }





    /**
     * Sets the ConnectionFactory providing the Connections and Channels to send messages on. The SpringAMQPPublisher
     * does not cache or reuse connections. Providing a ConnectionFactory instance that caches connections will prevent
     * new connections to be opened for each invocation to {@link #send(List)}
     * <p>
     * Defaults to an autowired Connection Factory.
     *
     * @param connectionFactory The connection factory to set
     */


    /**
     * Sets the Message Converter that creates AMQP Messages from Event Messages and vice versa. Setting this property
     * will ignore the "durable", "serializer" and "routingKeyResolver" properties, which just act as short hands to
     * create a DefaultAMQPMessageConverter instance.
     * <p>
     * Defaults to a DefaultAMQPMessageConverter.
     *
     * @param messageConverter The message converter to convert AMQP Messages to Event Messages and vice versa.
     */
    public void setMessageConverter(SQSMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Whether or not messages should be marked as "durable" when sending them out. Durable messages suffer from a
     * performance penalty, but will survive a reboot of the Message broker that stores them.
     * <p>
     * By default, messages are durable.
     * <p>
     * Note that this setting is ignored if a {@link
     * #setMessageConverter(SQSMessageConverter) MessageConverter} is provided.
     * In that case, the message converter must add the properties to reflect the required durability setting.
     *
     * @param durable whether or not messages should be durable
     */
    public void setDurable(boolean durable) {
        isDurable = durable;
    }

    /**
     * Sets the serializer to serialize messages with when sending them to the Exchange.
     * <p>
     * Defaults to an autowired serializer, which requires exactly 1 eligible serializer to be present in the
     * application context.
     * <p>
     * This setting is ignored if a {@link
     * #setMessageConverter(SQSMessageConverter) MessageConverter} is configured.
     *
     * @param serializer the serializer to serialize message with
     */
    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    /**
     * Sets the RoutingKeyResolver that provides the Routing Key for each message to dispatch. Defaults to a {@link
     * PackageRoutingKeyResolver}, which uses the package name of the message's
     * payload as a Routing Key.
     * <p>
     * This setting is ignored if a {@link
     * #setMessageConverter(SQSMessageConverter) MessageConverter} is configured.
     *
     * @param routingKeyResolver the RoutingKeyResolver to use
     */
    public void setRoutingKeyResolver(RoutingKeyResolver routingKeyResolver) {
        this.routingKeyResolver = routingKeyResolver;
    }






    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
