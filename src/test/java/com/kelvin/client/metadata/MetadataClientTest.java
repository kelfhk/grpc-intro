package com.kelvin.client.metadata;

import com.kelvin.client.deadline.DeadlineInterceptor;
import com.kelvin.client.rpctypes.MoneyStreamingResponse;
import com.kelvin.models.Balance;
import com.kelvin.models.BalanceCheckRequest;
import com.kelvin.models.BankServiceGrpc;
import com.kelvin.models.WithdrawRequest;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetadataClientTest {

    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub bankServiceStub;

    @BeforeAll
    public void setup() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(ClientConstants.getClientToken()))
                .usePlaintext()
                .build();

        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        this.bankServiceStub = BankServiceGrpc.newStub(managedChannel);
    }

    @Test
    public void balanceTest() {
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(2)
                .build();

        for (int i = 0; i < 20; i ++) {
            try {
                int random = ThreadLocalRandom.current().nextInt(1, 4);
                System.out.println("Random : " + random);
                Balance balance = this.blockingStub
                        .withCallCredentials(new UserSessionToken("user-secret-" + random + ":regular"))
                        .getBalance(balanceCheckRequest);
                System.out.println(
                        "Received : " + balance.getAmount()
                );
            } catch (StatusRuntimeException e) {
                e.printStackTrace();
            }

        }

    }

    @Test
    public void withdrawTest() {
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder().setAccountNumber(7).setAmount(50).build();
        try {
            this.blockingStub
                    .withDeadline(Deadline.after(2, TimeUnit.SECONDS))
                    .withdraw(withdrawRequest)
                    .forEachRemaining(money -> System.out.println("Received : " + money.getValue()));
        } catch (StatusRuntimeException e) {

        }

    }

    @Test
    public void withdrawAsyncTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder().setAccountNumber(6).setAmount(50).build();
        this.bankServiceStub.withdraw(withdrawRequest, new MoneyStreamingResponse(latch));
        latch.await();
    }

}
