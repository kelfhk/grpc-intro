package com.kelvin.server.deadline;

import com.kelvin.server.rpctypes.TransferService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(6565)
                .addService(new DeadlineService())
                .build();

        server.start();

        server.awaitTermination();
    }
}
