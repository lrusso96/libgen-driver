package lrusso96.libgen.driver.core;

public enum Field {
    AUTHOR("author"),
    TITLE("title"),
    YEAR("year")
    ;

    private final String text;

    Field(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}