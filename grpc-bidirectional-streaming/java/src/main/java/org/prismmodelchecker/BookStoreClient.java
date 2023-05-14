package org.prismmodelchecker;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.prismmodelchecker.BookStoreGrpc.BookStoreImplBase;
import org.prismmodelchecker.Bookstore.Book;
import org.prismmodelchecker.Bookstore.Cart;
import org.prismmodelchecker.BookStoreServer.BookStoreImpl;
import org.prismmodelchecker.BookStoreGrpc;


public class BookStoreClient {
    private static final Logger logger = Logger.getLogger(BookStoreClient.class.getName());
    private final BookStoreGrpc.BookStoreStub stub;
    private boolean serverIntermediateResponseCompleted = true;
    private boolean serverResponseCompleted = false;

    StreamObserver<Book> streamClientSender;

    public BookStoreClient(Channel channel) {
        stub = BookStoreGrpc.newStub(channel);
    }
    public StreamObserver<Cart> getServerResponseObserver(){
        StreamObserver<Cart> observer = new StreamObserver<Cart>(){
            @Override
            public void onNext(Cart cart) {
                logger.info("Order summary:" +
                        "\nTotal number of Books:" + cart.getBooks() +
                        "\nTotal Order Value:" + cart.getPrice());

                serverIntermediateResponseCompleted = true;
            }
            @Override
            public void onError(Throwable t) {
                logger.info("Error while reading response fromServer: " + t);
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
            streamClientSender =stub.liveCartValue(getServerResponseObserver());
        }
        try {
            streamClientSender.onNext(request);
        }
        catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }
    public void completeOrder() {
        logger.info("Done, waiting for server to create ordersummary...");
        if(streamClientSender != null); {
            streamClientSender.onCompleted();
        }
    }
    public static void main(String[] args) throws Exception {
        String serverAddress = "localhost:50051";
        ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                .usePlaintext()
                .build();
        try {
            BookStoreClient client = new
                    BookStoreClient(channel);
            String bookName = "";

            while(true) {
                if(client.serverIntermediateResponseCompleted ==true) {
                    System.out.println("Type book name to be added to the cart....");

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(System.in));

                    // Reading data using readLine
                    bookName = reader.readLine();

                    if(bookName.equals("EXIT")) {
                        client.completeOrder();
                        break;
                    }
                    client.serverIntermediateResponseCompleted = false;
                    client.addBook(bookName);
                    Thread.sleep(500);
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