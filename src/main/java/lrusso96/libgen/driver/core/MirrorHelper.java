package lrusso96.libgen.driver.core;

import lrusso96.libgen.driver.exceptions.NoMirrorAvailableException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

class MirrorHelper
{
    private static boolean isReachable(URL url)
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

    static URL getFirstReachable(List<URL> urls) throws NoMirrorAvailableException
    {
        for (URL url : urls)
        {
            if (isReachable(url))
                return url;
        }
        throw new NoMirrorAvailableException("# urls tested: " + urls.size());
    }

    static URL getCoverUrl(URL url, String cover)
    {
        if (cover.isEmpty())
            return null;
        try
        {
            if (cover.startsWith("http"))
                return new URL(cover);
            return new URL(url.toString() + "/covers/" + cover);
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }
}
