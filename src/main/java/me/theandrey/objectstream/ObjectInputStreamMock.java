package me.theandrey.objectstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * Заглушка класса {@link ObjectInputStream}
 */
public final class ObjectInputStreamMock extends ObjectInputStream {

    public ObjectInputStreamMock(InputStream in) throws IOException {
        throw new SecurityException("Not available due security reasons");
    }

    public ObjectInputStreamMock() throws IOException, SecurityException {
        throw new SecurityException("Not available due security reasons");
    }
}
