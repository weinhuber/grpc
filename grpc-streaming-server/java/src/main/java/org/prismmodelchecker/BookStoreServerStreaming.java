package org.prismmodelchecker;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.prismmodelchecker.Bookstore.Book;
import org.prismmodelchecker.Bookstore.BookSearch;
import org.prismmodelchecker.BookStoreGrpc.BookStoreImplBase;
import org.prismmodelchecker.BookStoreGrpc;


public class BookStoreServerStreaming {
    private static final Logger logger = Logger.getLogger(BookStoreServerStreaming.class.getName());

    static Map<String, Book> bookMap = new HashMap<>();
    static {
        bookMap.put("Great Gatsby", Book.newBuilder().setName("Great Gatsby")
                .setAuthor("Scott Fitzgerald")
                .setPrice(300).build());
        bookMap.put("To Kill MockingBird", Book.newBuilder().setName("To Kill MockingBird")
                .setAuthor("Harper Lee")
                .setPrice(400).build());
        bookMap.put("Passage to India", Book.newBuilder().setName("Passage to India")
                .setAuthor("E.M.Forster")
                .setPrice(500).build());
        bookMap.put("The Side of Paradise", Book.newBuilder().setName("The Side of Paradise")
                .setAuthor("Scott Fitzgerald")
                .setPrice(600).build());
        bookMap.put("Go Set a Watchman", Book.newBuilder().setName("Go Set a Watchman")
                .setAuthor("Harper Lee")
                .setPrice(700).build());
    }
    private Server server;

    private void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new BookStoreImpl()).build().start();

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
    public static void main(String[] args) throws IOException, InterruptedException {
        final BookStoreServerStreaming greetServer = new BookStoreServerStreaming();
        greetServer.start();
        greetServer.server.awaitTermination();
    }
    static class BookStoreImpl extends BookStoreGrpc.BookStoreImplBase {
        @Override
        public void first(BookSearch searchQuery, StreamObserver<Book> responseObserver) {
            logger.info("Searching for book with author: " + searchQuery.getAuthor());
            for (Entry<String, Book> bookEntry : bookMap.entrySet()) {
                try {
                    logger.info("Going through more books....");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(bookEntry.getValue().getAuthor().startsWith(searchQuery.getAuthor())){
                    logger.info("Found book with required author: " + bookEntry.getValue().getName()+ ". Sending....");

                    responseObserver.onNext(bookEntry.getValue());
                }
            }
            responseObserver.onCompleted();
        }
    }
}