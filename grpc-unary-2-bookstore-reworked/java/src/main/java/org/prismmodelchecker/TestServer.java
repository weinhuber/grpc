package org.prismmodelchecker;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.prismmodelchecker.proto.BookStoreSearchGrpc;
import org.prismmodelchecker.proto.SecondBookStore.BookSearchInput;
import org.prismmodelchecker.proto.SecondBookStore.Book;
import org.prismmodelchecker.proto.BookStoreSearchGrpc.BookStoreSearchImplBase;


public class TestServer {

    private static final Logger logger = Logger.getLogger(TestServer.class.getName());

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

    static class BookStoreImpl extends BookStoreSearchGrpc.BookStoreSearchImplBase {
        @Override
        public void find(BookSearchInput searchQuery, StreamObserver<Book> responseObserver) {
            logger.info("Searching for book with title: " + searchQuery.getName());
            List<String> matchingBookTitles = bookMap.keySet().stream().filter(title ->
                    title.startsWith(searchQuery.getName().trim())).collect(Collectors.toList());

            Book foundBook = null;
            if(matchingBookTitles.size() > 0) {
                foundBook = bookMap.get(matchingBookTitles.get(0));
            }
            responseObserver.onNext(foundBook);
            responseObserver.onCompleted();
        }
    }

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
        final TestServer server = new TestServer();
        server.start();
        server.server.awaitTermination();
    }


}
