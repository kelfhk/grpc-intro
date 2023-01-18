package com.kelvin.server.rpctypes;

import com.kelvin.models.TransferRequest;
import com.kelvin.models.TransferResponse;
import com.kelvin.models.TransferServiceGrpc;
import io.grpc.stub.StreamObserver;

public class TransferService extends TransferServiceGrpc.TransferServiceImplBase {
    @Override
    public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
        return new TransferStreamingRequest(responseObserver);
    }
}
