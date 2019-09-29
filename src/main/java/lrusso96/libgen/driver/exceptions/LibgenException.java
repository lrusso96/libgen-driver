package lrusso96.libgen.driver.exceptions;

public class LibgenException extends Exception {
    public final static String DEFAULT_MSG = "invalid response";

    public LibgenException(String message) {
        super(message);
    }
}
