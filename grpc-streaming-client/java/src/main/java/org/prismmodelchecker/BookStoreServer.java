package org.prismmodelchecker;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;


import org.prismmodelchecker.BookStoreGrpc.BookStoreImplBase;
import org.prismmodelchecker.BookStoreGrpc.BookStoreStub;
import org.prismmodelchecker.BookStoreGrpc.BookStoreBlockingStub;
import org.prismmodelchecker.BookStoreGrpc.BookStoreFutureStub;
import org.prismmodelchecker.Bookstore.Book;
import org.prismmodelchecker.Bookstore.Cart;
import org.prismmodelchecker.BookStoreGrpc.BookStoreImplBase;
import org.prismmodelchecker.BookStoreGrpc;
import org.prismmodelchecker.BookStoreServer.BookStoreImpl;

public class  BookStoreServer {
    private static final Logger logger = Logger.getLogger(BookStoreServer.class.getName());

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
        final BookStoreServer greetServer = new  BookStoreServer();
        greetServer.start();
        greetServer.server.awaitTermination();
    }
    static class BookStoreImpl extends BookStoreGrpc.BookStoreImplBase {
        @Override
        public StreamObserver<Book> totalCartValue(StreamObserver<Cart> responseObserver) {
            return new StreamObserver<Book>() {

                ArrayList<Book> bookCart = new ArrayList<Book>();
                @Override
                public void onNext(Book book) {
                    logger.info("Searching for book with title starting with: " + book.getName());
                    for (Entry<String, Book> bookEntry : bookMap.entrySet()) {
                        if(bookEntry.getValue().getName().startsWith(book.getName())){
                            logger.info("Found book, adding to cart:....");
                            bookCart.add(bookEntry.getValue());
                        }
                    }
                }

                @Override
                public void onError(Throwable t) {
                    logger.info("Error while reading book stream: " + t);
                }

                @Override
                public void onCompleted() {
                    int cartValue = 0;
                    for (Book book : bookCart) {
                        cartValue += book.getPrice();
                    }
                    responseObserver.onNext(Cart.newBuilder()
                            .setPrice(cartValue)
                            .setBooks(bookCart.size()).build());
                    responseObserver.onCompleted();
                }
            };
        }

    }
}