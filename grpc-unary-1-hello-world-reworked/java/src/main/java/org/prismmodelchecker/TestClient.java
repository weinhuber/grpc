package org.prismmodelchecker;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

// function
import org.prismmodelchecker.proto.Service1Grpc;

// types
import org.prismmodelchecker.proto.TestingMyProto.ClientInputStuff;
import org.prismmodelchecker.proto.TestingMyProto.ServerOutputStuff;


public class TestClient {
    private static final Logger logger = Logger.getLogger(TestClient.class.getName());
    private final Service1Grpc.Service1BlockingStub blockingStub;

    public TestClient(Channel channel) {
        blockingStub = Service1Grpc.newBlockingStub(channel);
    }

    public void sendStuff2Server(String greeting, String username) {
        logger.info("Sending greeting to server: " + greeting + " for name: " + username);
        ClientInputStuff request = ClientInputStuff.newBuilder().setName(username).setText(greeting).build();
        logger.info("Sending to server: " + request);
        ServerOutputStuff response;
        try {
            response = blockingStub.function1(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Got following from the server: " + response.getMessage());
    }

    public static void main(String[] args) throws Exception {

        String serverAddress = "localhost:50051";
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();
        try {
            TestClient client = new TestClient(channel);
            client.sendStuff2Server("Hello", "Prism");
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

}
