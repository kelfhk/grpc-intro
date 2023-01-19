package com.kelvin.server.metadata;

import com.google.common.util.concurrent.Uninterruptibles;
import com.kelvin.models.*;
import com.kelvin.server.rpctypes.AccountDatabase;
import com.kelvin.server.rpctypes.CashStreamingRequest;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class MetadataService extends BankServiceGrpc.BankServiceImplBase {
    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        int amount = AccountDatabase.getBalance(accountNumber);

        UserRole userRole = ServerConstants.CTX_USER_ROLE.get();
        UserRole userRole1 = ServerConstants.CTX_USER_ROLE1.get();
        amount = UserRole.PRIME.equals(userRole) ? amount : (amount - 15);

        System.out.println(
                userRole + " : " + userRole1
        );
        Balance balance = Balance.newBuilder()
                .setAmount(amount)
                .build();

        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        int accountNumber = request.getAccountNumber();
        int amount = request.getAmount();
        int balance = AccountDatabase.getBalance(accountNumber);

        if (balance < amount) {
            Status status = Status.FAILED_PRECONDITION.withDescription("Not enough money. You have only " + balance);
            responseObserver.onError(status.asRuntimeException());
            return;
        }

        // all validations passed
        for (int i = 0; i < (amount / 10); i ++) {
            // simulate time-consuming call
            Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
            if (!Context.current().isCancelled()) {
                Money money = Money.newBuilder().setValue(10).build();
                responseObserver.onNext(money);
                System.out.println(
                        "Delivered $10"
                );
                AccountDatabase.deductBalance(accountNumber, 10);
            } else {
                break;
            }

        }
        System.out.println("Completed");

        responseObserver.onCompleted();

    }

    @Override
    public StreamObserver<DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {
        return new CashStreamingRequest(responseObserver);
    }
}
