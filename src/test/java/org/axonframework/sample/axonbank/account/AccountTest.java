package org.axonframework.sample.axonbank.account;

import org.axonframework.sample.axonLMG.account.Account;
import org.axonframework.sample.axonLMG.coreapi.*;
import org.axonframework.sample.axonbank.coreapi.AccountCreatedEvent;
import org.axonframework.sample.axonbank.coreapi.CreateAccountCommand;
import org.axonframework.sample.axonbank.coreapi.DepositMoneyCommand;
import org.axonframework.sample.axonbank.coreapi.MoneyDepositedEvent;
import org.axonframework.sample.axonbank.coreapi.MoneyWithdrawnEvent;
import org.axonframework.sample.axonbank.coreapi.WithdrawMoneyCommand;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Before;
import org.junit.Test;

public class AccountTest {

    private FixtureConfiguration<Account> fixture;

    @Before
    public void setUp() throws Exception {
        fixture = new AggregateTestFixture<>(Account.class);
    }

    @Test
    public void testAccountCreated() throws Exception {
        fixture.givenNoPriorActivity()
                .when(new CreateAccountCommand("1234", 1000))
                .expectEvents(new AccountCreatedEvent("1234", 1000));
    }

    @Test
    public void testWithdrawReasonableAmount() throws Exception {
        fixture.given(new AccountCreatedEvent("1234", 1000))
                .when(new WithdrawMoneyCommand("1234", "tx1", 100))
                .expectEvents(new MoneyWithdrawnEvent("1234", "tx1", 100, -100));
    }

    @Test
    public void testWithdrawUnreasonableAmount() throws Exception {
        fixture.given(new AccountCreatedEvent("1234", 1000))
                .when(new WithdrawMoneyCommand("1234", "tx1", 1010))
                .expectNoEvents();
    }

    @Test
    public void testDepositMoney() {
        fixture.given(new AccountCreatedEvent("1234", 1000),
                      new MoneyDepositedEvent("1234", "tx1", 250, 250))
                .when(new DepositMoneyCommand("1234", "tx1", 500))
                .expectEvents(new MoneyDepositedEvent("1234", "tx1", 500, 750));
    }
}
