package org.axonframework.sample.axonLMG;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.config.EventHandlingConfiguration;
import org.axonframework.config.SagaConfiguration;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.sample.axonLMG.account.Account;
import org.axonframework.sample.axonLMG.transfer.LoggingEventHandler;
import org.axonframework.sample.axonLMG.transfer.MoneyTransfer;
import org.axonframework.sample.axonLMG.transfer.MoneyTransferSaga;
import org.axonframework.sample.axonbank.coreapi.CreateAccountCommand;
import org.axonframework.sample.axonbank.coreapi.RequestMoneyTransferCommand;

import java.util.concurrent.ExecutionException;

public class Application {


    public static void main(String[] args) throws InterruptedException, ExecutionException {

        Configuration configuration = DefaultConfigurer.defaultConfiguration()
                .configureAggregate(Account.class)
                .configureAggregate(MoneyTransfer.class)
                .registerModule(SagaConfiguration.subscribingSagaManager(MoneyTransferSaga.class))
                .registerModule(new EventHandlingConfiguration()
                                        .registerEventHandler(c -> new LoggingEventHandler()))
                .configureEmbeddedEventStore(c -> new InMemoryEventStorageEngine())
                .buildConfiguration();

        configuration.start();

        CommandGateway commandGateway = configuration.commandGateway();

        commandGateway.send(new CreateAccountCommand("1234", 1000), LoggingCallback.INSTANCE);
        
        commandGateway.send(new CreateAccountCommand("4321", 1000), LoggingCallback.INSTANCE);
        commandGateway.send(new RequestMoneyTransferCommand("tf1", "1234", "4321", 100), LoggingCallback.INSTANCE);

        configuration.shutdown();
    }
}
