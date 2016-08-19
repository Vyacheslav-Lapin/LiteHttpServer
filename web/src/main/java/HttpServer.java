import common.functions.ExceptionalSupplier;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Log
public class HttpServer implements Runnable {

    private int port;
    private ServerSocket serverSocket;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Collection<Handler> handlers = new HashSet<>();

    public HttpServer(int port) throws IOException {
        serverSocket = new ServerSocket(
                this.port = port);
    }

    public static void main(String[] args) throws Throwable {
        new HttpServer(8080)
                .setHandler(SimpleHandler::new)
                .run();
    }

    private Runnable getHandlerRunner(HttpRequest httpRequest) {
        return () -> handlers.stream()
                .filter(handler -> handler.test(httpRequest))
                .findFirst()
                .ifPresent(handler -> handler.accept(httpRequest));
    }

    private <T extends Handler> HttpServer setHandler(Supplier<T> handlerSupplier) {
        handlers.add(handlerSupplier.get());
        return this;
    }

    @Override
    public void run() {
        log.info(() -> "Server running. Please, visit http://localhost:" + port);
        //noinspection InfiniteLoopStatement
        while (true)
            executorService.execute(
                    getHandlerRunner(
                            HttpRequest.from(
                                    ExceptionalSupplier.getOrThrowUnchecked(
                                            serverSocket::accept))));
    }
}