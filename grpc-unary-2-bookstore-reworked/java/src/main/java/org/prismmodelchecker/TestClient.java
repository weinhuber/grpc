package org.prismmodelchecker;


import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import org.prismmodelchecker.proto.BookStoreSearchGrpc;
import org.prismmodelchecker.proto.SecondBookStore.BookSearchInput;
import org.prismmodelchecker.proto.SecondBookStore.Book;

public class TestClient {

    private static final Logger logger = Logger.getLogger(TestClient.class.getName());
    private final BookStoreSearchGrpc.BookStoreSearchBlockingStub blockingStub;

    public TestClient(Channel channel) {
        blockingStub = BookStoreSearchGrpc.newBlockingStub(channel);
    }

    public void askServerForBook(String bookName) {
        logger.info("Querying for book with title: " + bookName);
        BookSearchInput request = BookSearchInput.newBuilder().setName(bookName).build();

        Book book;
        try {
            book = blockingStub.find(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Got following book from server: " + book.toString());
    }

    public static void main(String[] args) throws Exception {
        String bookName = "To Kill";
        String serverAddress = "localhost:50051";

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();

        try {
            TestClient client = new
                    TestClient(channel);
            client.askServerForBook(bookName);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
