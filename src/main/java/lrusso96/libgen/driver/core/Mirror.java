package lrusso96.libgen.driver.core;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Mirror
{
    private URL url;

    public Mirror(String url) throws MalformedURLException
    {
        this.url = new URL(url);
    }

    URL getUrl()
    {
        return url;
    }

    boolean isReachable()
    {
        try
        {
            Jsoup.connect(url.toString()).get();
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }

    String getDownloadLink(Book book)
    {
        return "http://lib1.org/_ads/" + book.getMD5();
    }

    String getCoverUrl(String cover_url)
    {
        return url.toString() + "/covers/" + cover_url;
    }


}