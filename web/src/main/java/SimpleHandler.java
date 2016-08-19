import lombok.extern.java.Log;

@Log
public class SimpleHandler implements Handler {

    @Override
    public void accept(HttpRequest httpRequest) {
        try {
            writeResponse(httpRequest, "<html><body><h1>Hello, World!</h1></body></html>");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            log.warning(throwable::getMessage);
        }
        log.info("Client processing finished");
    }

    @Override
    public boolean test(HttpRequest httpRequest) {
        return true;
    }
}
