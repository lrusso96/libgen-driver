package lrusso96.libgen.driver.core;

import lrusso96.libgen.driver.core.model.Book;
import lrusso96.libgen.driver.exceptions.LibgenException;
import lrusso96.libgen.driver.exceptions.NoBookFoundException;
import lrusso96.libgen.driver.exceptions.NoMirrorAvailableException;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static lrusso96.libgen.driver.core.MirrorHelper.getCoverUri;

public class Libgen {
    public static final int DEFAULT_RESULTS_NUMBER = 25;
    private static final Field DEFAULT_SORTING_FIELD = Field.YEAR;
    private static final String DEFAULT_SORTING_MODE = "DESC";
    private URI mirror;
    private List<URI> mirrors = new LinkedList<>();
    private int maxResultsNumber = DEFAULT_RESULTS_NUMBER;
    private String sorting_field = DEFAULT_SORTING_FIELD.toString();
    private String sorting_mode = DEFAULT_SORTING_MODE;

    private static final Logger LOGGER = Logger.getLogger( Libgen.class.getName());

    public Libgen() throws NoMirrorAvailableException {
        initMirrors();
        this.mirror = MirrorHelper.getFirstReachable(mirrors);
    }

    public Libgen(URI mirror, boolean unique) throws NoMirrorAvailableException {
        if (!unique)
            initMirrors();
        mirrors.add(mirror);
        this.mirror = MirrorHelper.getFirstReachable(mirrors);
    }

    private void initMirrors() {
        String available = "http://93.174.95.27/";
        try {
            mirrors.add(new URI(available));
        } catch (URISyntaxException e) {
            LOGGER.log( Level.WARNING, "{0} not well formatted", available);
        }
    }

    public void loadDownloadURI(Book book) throws LibgenException, NoMirrorAvailableException {
        if (book.getDownload() != null)
            return;
        try {
            Document doc = Jsoup.connect("http://93.174.95.29/_ads/" + book.getMD5()).get();
            Elements anchors = doc.getElementsByTag("a");
            for (Element anchor : anchors) {
                if (anchor.text().equalsIgnoreCase("get")) {
                    book.setDownload(new URI(anchor.attr("href")));
                    return;
                }
            }
        } catch (IOException e) {
            throw new LibgenException(LibgenException.DEFAULT_MSG);
        } catch (URISyntaxException e) {
            LOGGER.log( Level.WARNING, e.getMessage());
        }
        throw new NoMirrorAvailableException("no download uri available");
    }

    private List<String> getIds(String stuff, String column) throws LibgenException {
        int page = 1;
        //reduce number of pages requested
        int results = 25;
        if(maxResultsNumber > 25)
            results = 50;
        if(maxResultsNumber > 50)
            results = 100;

        List<String> ids = getIds(stuff, column, page, results);
        while (ids.size() < maxResultsNumber) {
            page++;
            List<String> new_ids = getIds(stuff, column, page, results);
            if (new_ids.isEmpty())
                break;
            ids.addAll(new_ids);
        }
        if (maxResultsNumber < ids.size())
            return ids.subList(0, maxResultsNumber);

        return ids;
    }

    private List<String> getIds(String stuff, String column, int page, int results) throws LibgenException {
        try {
            List<String> list = new ArrayList<>();
            Document doc = Jsoup.connect(mirror + "/search.php")
                    .data("req", stuff)
                    .data("column", column)
                    .data("res", Integer.toString(results))
                    .data("sort", sorting_field)
                    .data("sortmode", sorting_mode)
                    .data("page", Integer.toString(page))
                    .get();
            Elements rows = doc.getElementsByTag("tr");
            for (Element row : rows) {
                String id = row.child(0).text();
                if (StringUtils.isNumeric(id))
                    list.add(id);
            }
            return list;
        } catch (IOException e) {
            throw new LibgenException(LibgenException.DEFAULT_MSG);
        }
    }

    private List<Book> search(List<String> ids) throws LibgenException, NoBookFoundException {
        if (ids.isEmpty())
            throw new NoBookFoundException("Try a new query");
        List<Book> list = new ArrayList<>();

        try {
            String body = searchRequest(ids);
            JSONArray response = new JSONArray(body);
            for (int i = 0; i < response.length(); i++) {
                JSONObject bookObject = response.getJSONObject(i);
                Book book = parseBook(bookObject);
                book.setId(Integer.parseInt(ids.get(i)));
                list.add(book);
            }
            list.sort((b1, b2) ->
            {
                if (sorting_field.equals(Field.YEAR + "")) {
                    if ("ASC".equals(sorting_mode))
                        return Integer.compare(b1.getYear(), b2.getYear());
                    return Integer.compare(b2.getYear(), b1.getYear());
                }
                if (sorting_field.equals(Field.TITLE + "")) {
                    if ("ASC".equals(sorting_mode))
                        return b1.getTitle().compareTo(b2.getTitle());
                    return b2.getTitle().compareTo(b1.getTitle());
                }
                //never happens
                return b1.getTitle().compareTo(b2.getTitle());
            });
            return list;
        } catch (IOException | JSONException | StringIndexOutOfBoundsException e) {
            throw new LibgenException(LibgenException.DEFAULT_MSG);
        }
    }

    private String searchRequest(List<String> ids) throws LibgenException, IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        String fields = "Author,Title,MD5,Year,Pages,Language,Filesize,Extension,CoverURL";
        RequestBody formBody = new FormBody.Builder()
                .add("ids", encodeIds(ids))
                .add("fields", fields)
                .build();
        Request req = new Request.Builder()
                .url(mirror + "/json.php")
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build();
        Response resp = client.newCall(req).execute();
        if (resp.body() == null)
            throw new LibgenException("Invalid response");
        String body = resp.body().string();
        return body.substring(body.indexOf('['), body.lastIndexOf(']') + 1);
    }

    private String encodeIds(List<String> ids) {
        StringBuilder ids_comma = new StringBuilder();
        for (String id : ids)
            ids_comma.append(",").append(id);
        ids_comma.replace(0, 1, "");
        return ids_comma.toString();
    }

    private Book parseBook(JSONObject object) {
        Book book = new Book();
        book.setAuthor(object.getString(Field.AUTHOR + ""));
        book.setTitle(object.getString(Field.TITLE + ""));
        book.setMD5(object.getString("md5"));
        String o = object.getString(Field.YEAR + "");
        if (NumberUtils.isParsable(o))
            book.setYear(Integer.parseInt(o));
        o = object.getString("pages");
        if (NumberUtils.isParsable(o))
            book.setPages(Integer.parseInt(o));
        book.setLanguage(object.getString("language"));
        o = object.getString("filesize");
        if (NumberUtils.isParsable(o))
            book.setFilesize(Integer.parseInt(o));
        book.setExtension(object.getString("extension"));
        book.setCover(getCoverUri(mirror, object.getString("coverurl")));
        return book;
    }

    public List<Book> search(String stuff) throws LibgenException, NoBookFoundException {
        return search(getIds(stuff, "def"));
    }

    public List<Book> searchAuthor(String author) throws LibgenException, NoBookFoundException {
        return search(getIds(author, Field.AUTHOR + ""));
    }

    public List<Book> searchTitle(String title) throws LibgenException, NoBookFoundException {
        return search(getIds(title, Field.AUTHOR + ""));
    }

    public int getMaxResultsNumber() {
        return this.maxResultsNumber;
    }

    public void setMaxResultsNumber(int i) {
        if (i > 0)
            this.maxResultsNumber = i;
    }

    public void setSorting(Field field) {
        this.sorting_field = field.toString();
    }

    public void setAscendingSort() {
        this.sorting_mode = "ASC";
    }

    public void setDescendingSort() {
        this.sorting_mode = "DESC";
    }
}