package lrusso96.libgen.driver;

import lrusso96.libgen.driver.core.*;

import lrusso96.libgen.driver.exceptions.LibgenException;
import lrusso96.libgen.driver.exceptions.NoBookFoundException;
import lrusso96.libgen.driver.exceptions.NoMirrorAvailableException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.MalformedURLException;
import java.util.List;

import static org.junit.Assert.*;

public class LibgenTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNoMirrorException() throws NoMirrorAvailableException, MalformedURLException {
        thrown.expect(NoMirrorAvailableException.class);
        new Libgen(new Mirror("http://libgen.io"), true);
    }

    @Test
    public void testMalformedURLException() throws NoMirrorAvailableException, MalformedURLException {
        thrown.expect(MalformedURLException.class);
        new Libgen(new Mirror("not a url"), false);
    }

    @Test
    public void testNoBookException() throws NoMirrorAvailableException, NoBookFoundException, LibgenException {
        String book_title = "Malavaogliaaaaaaa";
        Libgen libgen = new Libgen();
        thrown.expect(NoBookFoundException.class);
        libgen.search(book_title);
    }

    @Test
    public void testBook() throws NoMirrorAvailableException, NoBookFoundException, LibgenException {
        String book_title = "Promessi sposi";
        Libgen libgen = new Libgen();
        List<Book> books = libgen.search(book_title);
        books.removeIf(book -> !book.getAuthor().toLowerCase().contains("manzoni"));
        assertFalse(books.isEmpty());
    }

    @Test
    public void testCustomMirror() throws NoMirrorAvailableException, NoBookFoundException, LibgenException, MalformedURLException {
        String book_title = "divina commedia";
        Libgen libgen = new Libgen(new Mirror("http://libgen.is"), false);
        List<Book> books = libgen.search(book_title);
        assertFalse(books.isEmpty());
    }

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
