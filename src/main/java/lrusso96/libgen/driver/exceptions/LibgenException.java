package lrusso96.libgen.driver.exceptions;

public class LibgenException extends Exception {
    public LibgenException(String message) {
        super(message);
    }

    public LibgenException(Exception e){
        super(e);
    }


}
