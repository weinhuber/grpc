package org.prismmodelchecker;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.prismmodelchecker.BookStoreGrpc.BookStoreImplBase;
import org.prismmodelchecker.BookStoreGrpc.BookStoreStub;
import org.prismmodelchecker.BookStoreGrpc.BookStoreBlockingStub;
import org.prismmodelchecker.BookStoreGrpc.BookStoreFutureStub;
import org.prismmodelchecker.Bookstore.Book;
import org.prismmodelchecker.Bookstore.Cart;
import org.prismmodelchecker.BookStoreGrpc.BookStoreImplBase;
import org.prismmodelchecker.BookStoreGrpc;
import org.prismmodelchecker.BookStoreServer.BookStoreImpl;

public class BookStoreClient {
    private static final Logger logger = Logger.getLogger(BookStoreClient.class.getName());
    private final BookStoreStub stub;
    private boolean serverResponseCompleted = false;
    StreamObserver<Book> streamClientSender;

    public BookStoreClient(Channel channel) {
        stub = BookStoreGrpc.newStub(channel);
    }
    public StreamObserver<Cart> getServerResponseObserver(){
        StreamObserver<Cart> observer = new StreamObserver<Cart>(){
            @Override
            public void onNext(Cart cart) {
                logger.info("Order summary:" + "\nTotal number of Books:" + cart.getBooks() +
                        "\nTotal Order Value:" + cart.getPrice());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.log(Level.WARNING, "RPC failed: {0}", throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                //logger.info("Server: Done reading orderreading cart");
                serverResponseCompleted = true;
            }
        };
        return observer;
    }
    public void addBook(String book) {
        logger.info("Adding book with title starting with: " + book);
        Book request = Book.newBuilder().setName(book).build();

        if(streamClientSender == null) {
            streamClientSender = stub.totalCartValue(getServerResponseObserver());
        }
        try {
            streamClientSender.onNext(request);
        }
        catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }
    public void completeOrder() {
        logger.info("Done, waiting for server to create order summary...");
        if(streamClientSender != null);
        streamClientSender.onCompleted();
    }

    public static void main(String[] args) throws Exception {
        String serverAddress = "localhost:50051";
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();
        try {
            BookStoreClient client = new BookStoreClient(channel);
            String bookName = "";

            while(true) {
                // degenerate example...here we're just spaming requests to the server with the same book name
                // while true, ask user for input
                // if input is EXIT, then call client.completeOrder() and break
                // else call client.addBook(bookName)

                // Using Console to input data from user
                // Enter data using BufferReader
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(System.in));

                // Reading data using readLine
                bookName = reader.readLine();

                // Printing the read line
                System.out.println(bookName);

                if(bookName.equals("EXIT")) {
                    client.completeOrder();
                    break;
                } else {
                    client.addBook(bookName);
                }
            }

            while(!client.serverResponseCompleted) {
                Thread.sleep(2000);
            }

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}