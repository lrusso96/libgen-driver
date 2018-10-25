package lrusso96.libgen.driver.core;

import lrusso96.libgen.driver.exceptions.NoMirrorAvailableException;

import java.util.List;

class MirrorHelper {

    static Mirror getFirstReachable(List<Mirror> mirrors) throws NoMirrorAvailableException
    {
        for(Mirror mirror : mirrors){
            if(mirror.isReachable())
                return mirror;
        }
        throw new NoMirrorAvailableException("Mirrors not reachable: " + mirrors.size());
    }
}
