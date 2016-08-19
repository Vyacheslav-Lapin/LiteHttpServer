import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Handler extends Consumer<HttpRequest>, AutoCloseable, Predicate<HttpRequest> {

    default void init() {
    }

    @Override
    default void close() throws Exception {
    }

    default void writeResponse(HttpRequest request, String s) throws Throwable {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Server: YarServer/2009-09-09\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + s.length() + "\r\n" +
                "Connection: close\r\n\r\n";
        String result = response + s;
        try (OutputStream outputStream = request.toSrc().getOutputStream()) {
            outputStream.write(result.getBytes());
            outputStream.flush();
        }
    }
}
