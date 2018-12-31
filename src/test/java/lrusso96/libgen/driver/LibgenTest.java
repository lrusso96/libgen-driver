package lrusso96.libgen.driver;

import lrusso96.libgen.driver.core.*;

import lrusso96.libgen.driver.exceptions.LibgenException;
import lrusso96.libgen.driver.exceptions.NoBookFoundException;
import lrusso96.libgen.driver.exceptions.NoMirrorAvailableException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class LibgenTest
{

    private static final int ENOUGH_MS = 2000;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNoMirrorException() throws NoMirrorAvailableException, MalformedURLException
    {
        thrown.expect(NoMirrorAvailableException.class);
        //should be down!
        new Libgen(new URL("http://libgen.org"), true);
    }

    @Test
    public void testMalformedURLException() throws NoMirrorAvailableException, MalformedURLException
    {
        thrown.expect(MalformedURLException.class);
        new Libgen(new URL("not a url"), false);
    }

    @Test
    public void testNoBookException() throws NoMirrorAvailableException, NoBookFoundException, LibgenException, InterruptedException
    {
        Thread.sleep(ENOUGH_MS);
        String book_title = "Malavaogliaaaaaaa";
        Libgen libgen = new Libgen();
        thrown.expect(NoBookFoundException.class);
        libgen.search(book_title);
    }

    @Test
    public void testBook() throws NoMirrorAvailableException, NoBookFoundException, LibgenException, InterruptedException
    {
        Thread.sleep(ENOUGH_MS);
        String book_title = "promessi sposi";
        Libgen libgen = new Libgen();
        List<Book> books = libgen.search(book_title);
        books.removeIf(book -> !book.getAuthor().toLowerCase().contains("manzoni"));
        assertFalse(books.isEmpty());
        Book book = books.get(0);
        assertTrue(book.toString().toLowerCase().contains(book_title));
        assertFalse(book.getAuthor().isEmpty());
        assertNotEquals(0, book.getId());
        assertFalse(book.getMD5().isEmpty());
        assertFalse(book.getLanguage().isEmpty());
        assertFalse(book.getReadableFilesize().isEmpty());
        assertNotEquals(0, book.getPages());
        assertNotEquals(0, book.getYear());
        assertFalse(book.getExtension().isEmpty());
        assertFalse(book.getCoverUrl().toString().isEmpty());
    }

    @Test
    public void testCustomMirror() throws NoMirrorAvailableException, NoBookFoundException, LibgenException, MalformedURLException, InterruptedException
    {
        Thread.sleep(ENOUGH_MS);
        String book_title = "divina commedia";
        Libgen libgen = new Libgen(new URL("http://libgen.is"), false);
        List<Book> books = libgen.search(book_title);
        assertFalse(books.isEmpty());
    }

    @Test
    public void testMaxResultsNumber() throws NoMirrorAvailableException, InterruptedException
    {
        Thread.sleep(ENOUGH_MS);
        Libgen libgen = new Libgen();
        libgen.setMaxResultsNumber(42);
        assertEquals(42, libgen.getMaxResultsNumber());
        libgen.setMaxResultsNumber(0);
        assertEquals(42, libgen.getMaxResultsNumber());
        libgen.setMaxResultsNumber(Libgen.DEFAULT_RESULTS_NUMBER);
        assertEquals(Libgen.DEFAULT_RESULTS_NUMBER, libgen.getMaxResultsNumber());
    }

    @Test
    public void testSearchWithoutMax() throws NoMirrorAvailableException, NoBookFoundException, LibgenException, InterruptedException
    {
        Thread.sleep(ENOUGH_MS);
        String query = "platone";
        Libgen libgen = new Libgen();
        libgen.setMaxResultsNumber(Integer.MAX_VALUE);
        List<Book> books = libgen.search(query);
        assertTrue(books.size() > 50);
    }

    @Test
    public void testSearchWithMaxResultsNumber() throws NoMirrorAvailableException, NoBookFoundException, LibgenException, InterruptedException
    {
        Thread.sleep(ENOUGH_MS);
        String author = "platone";
        Libgen libgen = new Libgen();
        libgen.setMaxResultsNumber(4);
        List<Book> books = libgen.searchAuthor(author);
        assertEquals(4, books.size());
    }

    @Test
    public void testSearch() throws NoMirrorAvailableException, NoBookFoundException, LibgenException, InterruptedException
    {
        Thread.sleep(ENOUGH_MS);
        String book_title = "apologia Socrate";
        Libgen libgen = new Libgen();
        List<Book> books = libgen.search(book_title);
        assertFalse(books.isEmpty());
    }

    @Test
    public void testSorting() throws NoMirrorAvailableException, NoBookFoundException, LibgenException, InterruptedException
    {
        Thread.sleep(ENOUGH_MS);
        String query = "Camilleri";
        Libgen libgen = new Libgen();
        libgen.setSorting(Field.YEAR);
        libgen.setDecendingSort();
        List<Book> books = libgen.search(query);
        assert(books.get(0).getYear() >= books.get(1).getYear());

        Thread.sleep(ENOUGH_MS);
        libgen.setAscendingSort();
        books = libgen.search(query);
        assert(books.remove(books.size() - 1).getYear() >= books.remove(books.size() - 1).getYear());

        Thread.sleep(ENOUGH_MS);
        libgen.setSorting(Field.TITLE);
        books = libgen.search(query);
        assertNotEquals(books.get(0).getTitle().compareTo(books.get(1).getTitle()), 1);

    }

    @Test
    public void testSearchAuthor() throws NoMirrorAvailableException, NoBookFoundException, LibgenException, InterruptedException
    {
        Thread.sleep(ENOUGH_MS);
        String author = "Plutarco";
        Libgen libgen = new Libgen();
        List<Book> books = libgen.searchAuthor(author);
        assertFalse(books.isEmpty());
    }

    @Test
    public void testDownloadURL() throws NoMirrorAvailableException, NoBookFoundException, LibgenException, InterruptedException
    {
        Thread.sleep(ENOUGH_MS);
        String author = "Platone";
        Libgen libgen = new Libgen();
        Book book = libgen.searchTitle(author).get(0);
        String url = libgen.getDownloadURL(book).toString();
        assertFalse(url.isEmpty());
    }
}
