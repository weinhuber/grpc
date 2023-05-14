package org.prismmodelchecker;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.io.IOException;

import io.grpc.stub.StreamObserver;
import io.grpc.Server;
import io.grpc.ServerBuilder;

// types
import org.prismmodelchecker.proto.TestingMyProto.ClientInputStuff;
import org.prismmodelchecker.proto.TestingMyProto.ServerOutputStuff;

// function
import org.prismmodelchecker.proto.Service1Grpc;

public class TestServer {

    private static final Logger logger = Logger.getLogger(TestServer.class.getName());
    private Server server;
    private void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port).addService(new Service1Impl()).build().start();

        logger.info("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("Shutting down gRPC server");
                try {
                    server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
            }
        });
    }

    static class Service1Impl extends Service1Grpc.Service1ImplBase {
        @Override
        public void function1(ClientInputStuff req, StreamObserver<ServerOutputStuff> responseObserver) {
            logger.info("Got request from client: " + req);
            ServerOutputStuff reply = ServerOutputStuff.newBuilder().setMessage(
                    "Server says " + "\"" + req.getText() + " " + req.getName() + "\""
            ).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final TestServer testServer = new TestServer();
        testServer.start();
        testServer.server.awaitTermination();
    }
}
