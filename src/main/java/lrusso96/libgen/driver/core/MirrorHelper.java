package lrusso96.libgen.driver.core;

import lrusso96.libgen.driver.exceptions.NoMirrorAvailableException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class MirrorHelper {

    private static final Logger LOGGER = Logger.getLogger( MirrorHelper.class.getName() );

    private MirrorHelper() {
    }

    static URI getFirstReachable(List<URI> uris) throws NoMirrorAvailableException {
        for (URI uri : uris) {
            try {
                Jsoup.connect(uri.toString()).get();
                return uri;
            } catch (IOException e) {
                LOGGER.log( Level.WARNING, "{0} not reachable", uri);
            }
        }
        throw new NoMirrorAvailableException("# uris tested: " + uris.size());
    }

    public static URI getCoverUri(URI uri, String cover) {
        if (cover.isEmpty())
            return null;
        try {
            if (cover.startsWith("http"))
                return new URI(cover);
            return new URI(uri.toString() + "/covers/" + cover);
        } catch (URISyntaxException e) {
            LOGGER.log( Level.WARNING, "no cover available for {0}", cover);
            return null;
        }
    }
}
