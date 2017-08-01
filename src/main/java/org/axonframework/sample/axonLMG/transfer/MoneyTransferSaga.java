package org.axonframework.sample.axonLMG.transfer;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.saga.EndSaga;
import org.axonframework.eventhandling.saga.SagaEventHandler;
import org.axonframework.eventhandling.saga.SagaLifecycle;
import org.axonframework.eventhandling.saga.StartSaga;
import org.axonframework.sample.axonLMG.LoggingCallback;
import org.axonframework.sample.axonLMG.coreapi.*;
import org.axonframework.sample.axonbank.coreapi.CancelMoneyTransferCommand;
import org.axonframework.sample.axonbank.coreapi.CompleteMoneyTransferCommand;
import org.axonframework.sample.axonbank.coreapi.DepositMoneyCommand;
import org.axonframework.sample.axonbank.coreapi.MoneyDepositedEvent;
import org.axonframework.sample.axonbank.coreapi.MoneyTransferCancelledEvent;
import org.axonframework.sample.axonbank.coreapi.MoneyTransferCompletedEvent;
import org.axonframework.sample.axonbank.coreapi.MoneyTransferRequestedEvent;
import org.axonframework.sample.axonbank.coreapi.MoneyWithdrawnEvent;
import org.axonframework.sample.axonbank.coreapi.WithdrawMoneyCommand;
import org.axonframework.spring.stereotype.Saga;

import javax.inject.Inject;

import static org.axonframework.eventhandling.saga.SagaLifecycle.end;

@Saga
public class MoneyTransferSaga {

    @Inject
    private transient CommandGateway commandGateway;

    private String targetAccount;
    private String transferId;

    @StartSaga
    @SagaEventHandler(associationProperty = "transferId")
    public void on(MoneyTransferRequestedEvent event) {
        targetAccount = event.getTargetAccount();
        transferId = event.getTransferId();
        SagaLifecycle.associateWith("transactionId", transferId);
        commandGateway.send(new WithdrawMoneyCommand(event.getSourceAccount(), transferId, event.getAmount()),
                            new CommandCallback<WithdrawMoneyCommand, Object>() {
                                @Override
                                public void onSuccess(CommandMessage<? extends WithdrawMoneyCommand> commandMessage, Object result) {

                                }

                                @Override
                                public void onFailure(CommandMessage<? extends WithdrawMoneyCommand> commandMessage, Throwable cause) {
                                    commandGateway.send(new CancelMoneyTransferCommand(event.getTransferId()));
                                }
                            });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void on(MoneyWithdrawnEvent event) {
        commandGateway.send(new DepositMoneyCommand(targetAccount, event.getTransactionId(), event.getAmount()),
                            LoggingCallback.INSTANCE);
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void on(MoneyDepositedEvent event) {
        commandGateway.send(new CompleteMoneyTransferCommand(transferId),
                            LoggingCallback.INSTANCE);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "transferId")
    public void on(MoneyTransferCompletedEvent event) {
    }

    @SagaEventHandler(associationProperty = "transferId")
    public void on(MoneyTransferCancelledEvent event) {
        end();
    }
}
