package org.prismmodelchecker.bookstore;



import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.prismmodelchecker.bookstore.Bookstore.Book;
import org.prismmodelchecker.bookstore.BookStoreServerUnary.BookStoreImpl;
import org.prismmodelchecker.bookstore.*;
import org.prismmodelchecker.bookstore.Bookstore.BookSearch;
import org.prismmodelchecker.bookstore.BookStoreGrpc.BookStoreImplBase;
import org.prismmodelchecker.bookstore.BookStoreGrpc.*;
import org.prismmodelchecker.bookstore.BookStoreGrpc;

public class BookStoreClientUnary {
    private static final Logger logger = Logger.getLogger(BookStoreClientUnary.class.getName());
    private final BookStoreGrpc.BookStoreBlockingStub blockingStub;

    public BookStoreClientUnary(Channel channel) {
        blockingStub = BookStoreGrpc.newBlockingStub(channel);
    }
    public void getBook(String bookName) {
        logger.info("Querying for book with title: " + bookName);
        BookSearch request = BookSearch.newBuilder().setName(bookName).build();

        Book response;
        try {
            response = blockingStub.first(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Got following book from server: " + response.toString());
    }
    public static void main(String[] args) throws Exception {
        String bookName = "To Kill";
        String serverAddress = "localhost:50051";

        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();

        try {
            BookStoreClientUnary client = new
                    BookStoreClientUnary(channel);
            client.getBook(bookName);
        } finally {
            channel.shutdownNow().awaitTermination(5,
                    TimeUnit.SECONDS);
        }
    }
}