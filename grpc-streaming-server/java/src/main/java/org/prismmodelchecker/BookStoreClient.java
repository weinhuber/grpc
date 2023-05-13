package org.prismmodelchecker;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.prismmodelchecker.Bookstore.Book;
import org.prismmodelchecker.Bookstore.BookSearch;
import org.prismmodelchecker.BookStoreGrpc.BookStoreImplBase;
import org.prismmodelchecker.BookStoreGrpc;


public class BookStoreClient {
    private static final Logger logger = Logger.getLogger(BookStoreClient.class.getName());
    private final BookStoreGrpc.BookStoreBlockingStub blockingStub;
    public BookStoreClient(Channel channel) {
        blockingStub = BookStoreGrpc.newBlockingStub(channel);
    }
    public void getBook(String author) {
        logger.info("Querying for book with author: " + author);
        BookSearch request = BookSearch.newBuilder().setAuthor(author).build();
        Iterator<Book> response;
        try {
            response = blockingStub.first(request);
            while(response.hasNext()) {
                logger.info("Found book: " + response.next());
            }
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }
    public static void main(String[] args) throws Exception {
        String authorName = "Har";
        String serverAddress = "localhost:50051";

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();

        try {
            BookStoreClient client = new BookStoreClient(channel);
            client.getBook(authorName);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}