package com.kelvin.client.loadbalancing;

import com.kelvin.models.Balance;
import com.kelvin.models.BalanceCheckRequest;
import com.kelvin.models.BankServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientSideLoadBalancingTest {
    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub bankServiceStub;

    @BeforeAll
    public void setup() {
        List<String> instances = new ArrayList<>();
        instances.add("localhost:6565");
        instances.add("localhost:7575");
        ServiceRegistry.register("bank-service", instances);
        NameResolverRegistry.getDefaultRegistry().register(new TempNameResolverProvider());

        ManagedChannel managedChannel = ManagedChannelBuilder
                .forTarget("http://bank-service")
                .defaultLoadBalancingPolicy("round_robin")
//                .forAddress("localhost", 8585)
                .usePlaintext()
                .build();

        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        this.bankServiceStub = BankServiceGrpc.newStub(managedChannel);
    }

    @Test
    public void balanceTest() throws InterruptedException {
        for (int i = 0; i < 100; i ++) {
            BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(ThreadLocalRandom.current().nextInt(1, 11))
                    .build();
            Balance balance = this.blockingStub.getBalance(balanceCheckRequest);
            System.out.println(
                    "Received : " + balance.getAmount()
            );
        }
    }
}
