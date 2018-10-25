package lrusso96.libgen.driver;

import lrusso96.libgen.driver.core.*;

import lrusso96.libgen.driver.exceptions.LibgenException;
import lrusso96.libgen.driver.exceptions.NoBookFoundException;
import lrusso96.libgen.driver.exceptions.NoMirrorAvailableException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class LibgenTest
{

    @Test
    public void testSearch() throws NoMirrorAvailableException, NoBookFoundException, LibgenException {
        String book_title = "apologia Socrate";
        Libgen libgen = new Libgen();
        List<Book> books = libgen.search(book_title);
        assertFalse(books.isEmpty());
    }

    @Test
    public void testSearchAuthor() throws NoMirrorAvailableException, NoBookFoundException, LibgenException {
        String author = "Plutarco";
        Libgen libgen = new Libgen();
        List<Book> books = libgen.searchAuthor(author);
        assertFalse(books.isEmpty());
    }

    @Test
    public void testDownloadLink() throws NoMirrorAvailableException, NoBookFoundException, LibgenException {
        String author = "Platone";
        Libgen libgen = new Libgen();
        Book book = libgen.searchTitle(author).get(0);
        String url = libgen.getDownloadLink(book).toString();
        assertFalse(url.isEmpty());
    }


}
