package lrusso96.libgen.driver.core;

import lrusso96.libgen.driver.exceptions.LibgenException;
import lrusso96.libgen.driver.exceptions.NoBookFoundException;
import lrusso96.libgen.driver.exceptions.NoMirrorAvailableException;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Libgen {
    private Mirror mirror;
    private List<Mirror> mirrors = new LinkedList<>();;

    private void initMirrors() {
        try {
            mirrors.add(new Mirror("http://libgen.is"));
        } catch (MalformedURLException ignored) {
        }
    }

    public Libgen() throws NoMirrorAvailableException {
        initMirrors();
        this.mirror = MirrorHelper.getFirstReachable(mirrors);
    }

    public Libgen(Mirror mirror, boolean unique) throws NoMirrorAvailableException {
        if(!unique)
            initMirrors();
        mirrors.add(mirror);
        this.mirror = MirrorHelper.getFirstReachable(mirrors);
    }

    public URL getDownloadLink(Book book) throws LibgenException, NoMirrorAvailableException {
        try {
            Document doc = Jsoup.connect(mirror.getDownloadLink(book)).get();
            Elements anchors = doc.getElementsByTag("a");
            for (Element anchor : anchors) {
                String text = anchor.text();
                if (text.toLowerCase().equals("get"))
                    return new URL(anchor.attr("href"));
            }
        } catch (IOException e) {
            throw new LibgenException(e);
        }
        throw new NoMirrorAvailableException("Error occurred");
    }

    private List<String> getIds(String stuff, String column) throws LibgenException {
        try {
            List<String> list = new ArrayList<>();
            Document doc = Jsoup.connect(mirror.getUrl() + "/search.php")
                    .data("req", stuff)
                    .data("column", column)
                    .get();
            Elements rows = doc.getElementsByTag("tr");
            for (Element row : rows) {
                String id = row.child(0).text();
                if (StringUtils.isNumeric(id))
                    list.add(id);
            }
            return list;
        } catch (IOException e) {
            throw new LibgenException(e);
        }
    }

    private List<Book> search(List<String> ids) throws LibgenException, NoBookFoundException{
        if(ids.isEmpty())
                throw new NoBookFoundException("Try a new query");
        List<Book> list = new ArrayList<>();
        StringBuilder ids_comma = new StringBuilder();
        for(String id : ids)
            ids_comma.append(",").append(id);
        ids_comma.replace(0,1,"");

        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS).retryOnConnectionFailure(true)
                    .build();

            String fields = "Author,Title,MD5,Year,Pages,Language,Filesize,Extension";
            RequestBody formBody = new FormBody.Builder()
                    .add("ids", ids_comma.toString()).add("fields", fields).build();
            Request req = new Request.Builder().url(mirror.getUrl() + "/json.php").header("Accept","application/json")
                    .header("Content-Type","application/x-www-form-urlencoded").post(formBody).build();
            Response resp = client.newCall(req).execute();
            if(resp.body()==null)
                throw new LibgenException("Invalid response");
            String body = resp.body().string();
            JSONArray response = new JSONArray(body);
            for(int i = 0; i < response.length(); i++){
                JSONObject bookObject = response.getJSONObject(i);
                Book book = new Book();
                book.setId(Integer.parseInt(ids.get(i)));
                book.setAuthor(bookObject.getString("author"));
                book.setTitle(bookObject.getString("title"));
                book.setMD5(bookObject.getString("md5"));
                String o = bookObject.getString("year");
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
                list.add(book);
            }
            return list;
        } catch (IOException e) {
            throw new LibgenException(e);
        }
    }

    public List<Book> search(String stuff) throws LibgenException, NoBookFoundException{
        return search(getIds(stuff, "def"));
    }

    public List<Book> searchAuthor(String author) throws LibgenException, NoBookFoundException{
        return search(getIds(author, "author"));
    }

    public List<Book> searchTitle(String title) throws LibgenException, NoBookFoundException{
        return search(getIds(title, "title"));
    }
}
