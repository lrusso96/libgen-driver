package lrusso96.libgen.driver.core;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Libgen
{
    public static final int DEFAULT_RESULTS_NUMBER = 25;
    public static final Field DEFAULT_SORTING_FIELD = Field.YEAR;
    public static final String DEFAULT_SORTING_MODE = "DESC";
    private Mirror mirror;
    private List<Mirror> mirrors = new LinkedList<>();
    private int maxResultsNumber = DEFAULT_RESULTS_NUMBER;
    private String sorting_field = DEFAULT_SORTING_FIELD.toString();
    private String sorting_mode = DEFAULT_SORTING_MODE;


    public Libgen() throws NoMirrorAvailableException
    {
        initMirrors();
        this.mirror = MirrorHelper.getFirstReachable(mirrors);
    }

    public Libgen(Mirror mirror, boolean unique) throws NoMirrorAvailableException
    {
        if (!unique)
            initMirrors();
        mirrors.add(mirror);
        this.mirror = MirrorHelper.getFirstReachable(mirrors);
    }

    private void initMirrors()
    {
        try
        {
            mirrors.add(new Mirror("http://libgen.is"));
        }
        catch (MalformedURLException ignored) { }
    }

    public URL getDownloadLink(Book book) throws LibgenException, NoMirrorAvailableException
    {
        try
        {
            Document doc = Jsoup.connect(mirror.getDownloadLink(book)).get();
            Elements anchors = doc.getElementsByTag("a");
            for (Element anchor : anchors)
            {
                String text = anchor.text();
                if (text.toLowerCase().equals("get"))
                    return new URL(anchor.attr("href"));
            }
        } catch (IOException e)
        {
            throw new LibgenException(e);
        }
        throw new NoMirrorAvailableException("Error occurred");
    }


    private List<String> getIds(String stuff, String column) throws LibgenException
    {
        int page = 1;
        //reduce number of pages requested!
        int results = maxResultsNumber <= 25 ? 25 : maxResultsNumber <= 50 ? 50 : 100;
        List<String> ids = getIds(stuff, column, page, results);
        while (ids.size() < maxResultsNumber)
        {
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

    private List<String> getIds(String stuff, String column, int page, int results) throws LibgenException
    {
        try {
            List<String> list = new ArrayList<>();
            Document doc = Jsoup.connect(mirror.getUrl() + "/search.php")
                    .data("req", stuff)
                    .data("column", column)
                    .data("res", results + "")
                    .data("sort", sorting_field)
                    .data("sortmode", sorting_mode)
                    .data("page", page + "")
                    .get();

            Elements rows = doc.getElementsByTag("tr");
            for (Element row : rows) {
                String id = row.child(0).text();
                if (StringUtils.isNumeric(id))
                    list.add(id);
            }
            return list;
        }
        catch (IOException e)
        {
            throw new LibgenException(e);
        }
    }

    private List<Book> search(List<String> ids) throws LibgenException, NoBookFoundException
    {
        if (ids.isEmpty())
            throw new NoBookFoundException("Try a new query");
        List<Book> list = new ArrayList<>();
        StringBuilder ids_comma = new StringBuilder();
        for (String id : ids)
            ids_comma.append(",").append(id);
        ids_comma.replace(0, 1, "");

        try
        {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true).build();

            String fields = "Author,Title,MD5,Year,Pages,Language,Filesize,Extension,CoverURL";
            RequestBody formBody = new FormBody.Builder()
                    .add("ids", ids_comma.toString())
                    .add("fields", fields)
                    .build();
            Request req = new Request.Builder()
                    .url(mirror.getUrl() + "/json.php")
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .post(formBody)
                    .build();
            Response resp = client.newCall(req).execute();
            if (resp.body() == null)
                throw new LibgenException("Invalid response");
            String body = resp.body().string();
            body = body.substring(body.indexOf("["), body.lastIndexOf("]") + 1);
            JSONArray response = new JSONArray(body);
            for (int i = 0; i < response.length(); i++)
            {
                JSONObject bookObject = response.getJSONObject(i);
                Book book = new Book();
                book.setId(Integer.parseInt(ids.get(i)));
                book.setAuthor(bookObject.getString(Field.AUTHOR + ""));
                book.setTitle(bookObject.getString(Field.TITLE + ""));
                book.setMD5(bookObject.getString("md5"));
                String o = bookObject.getString(Field.YEAR + "");
                if (NumberUtils.isParsable(o))
                    book.setYear(Integer.parseInt(o));
                o = bookObject.getString("pages");
                if (NumberUtils.isParsable(o))
                    book.setPages(Integer.parseInt(o));
                book.setLanguage(bookObject.getString("language"));
                o = bookObject.getString("filesize");
                if (NumberUtils.isParsable(o))
                    book.setFilesize(Integer.parseInt(o));
                book.setExtension(bookObject.getString("extension"));
                book.setCoverUrl(mirror.getCoverUrl(bookObject.getString("coverurl")));
                list.add(book);
            }
            Collections.sort(list, (b1, b2) ->
            {
                if (sorting_field.equals(Field.YEAR + ""))
                {
                    if (sorting_mode.equals("ASC"))
                        return Integer.compare(b1.getYear(), b2.getYear());
                    return Integer.compare(b2.getYear(), b1.getYear());
                }
                if(sorting_field.equals(Field.TITLE + ""))
                {
                    if (sorting_mode.equals("ASC"))
                        return b1.getTitle().compareTo(b2.getTitle());
                    return b2.getTitle().compareTo(b1.getTitle());
                }

                //never happens
                return b1.getTitle().compareTo(b2.getTitle());
            });

            return list;
        }
        catch (IOException | JSONException e)
        {
            throw new LibgenException(e);
        }
    }

    public List<Book> search(String stuff) throws LibgenException, NoBookFoundException
    {
        return search(getIds(stuff, "def"));
    }

    public List<Book> searchAuthor(String author) throws LibgenException, NoBookFoundException
    {
        return search(getIds(author, Field.AUTHOR + ""));
    }

    public List<Book> searchTitle(String title) throws LibgenException, NoBookFoundException
    {
        return search(getIds(title, Field.AUTHOR + ""));
    }

    public int getMaxResultsNumber()
    {
        return this.maxResultsNumber;
    }

    public void setMaxResultsNumber(int i)
    {
        if (i > 0) this.maxResultsNumber = i;
    }

    public void setSorting(Field field)
    {
        this.sorting_field = field.toString();
    }

    public void setAscendingSort()
    {
        this.sorting_mode = "ASC";
    }

    public void setDecendingSort()
    {
        this.sorting_mode = "DESC";
    }
}