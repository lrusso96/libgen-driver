package lrusso96.libgen.driver.core;

import lrusso96.libgen.driver.exceptions.NoMirrorAvailableException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

class MirrorHelper {
    private static boolean isReachable(URI uri) {
        try {
            Jsoup.connect(uri.toString()).get();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    static URI getFirstReachable(List<URI> uris) throws NoMirrorAvailableException {
        for (URI uri : uris) {
            if (isReachable(uri))
                return uri;
        }
        throw new NoMirrorAvailableException("# uris tested: " + uris.size());
    }

    static URI getCoverUri(URI uri, String cover) {
        if (cover.isEmpty())
            return null;
        try {
            if (cover.startsWith("http"))
                return new URI(cover);
            return new URI(uri.toString() + "/covers/" + cover);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
