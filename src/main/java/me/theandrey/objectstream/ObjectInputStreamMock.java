package me.theandrey.objectstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import org.apache.commons.lang3.SerializationUtils;

/**
 * Заглушка класса {@link ObjectInputStream}
 */
public class ObjectInputStreamMock extends ObjectInputStream {

	public ObjectInputStreamMock(InputStream in) throws IOException {
		throw new SecurityException("Not available due security reasons");
	}

	public ObjectInputStreamMock() throws IOException, SecurityException {
		throw new SecurityException("Not available due security reasons");
	}

	/**
	 * @see SerializationUtils#deserialize(InputStream)
	 */
	public static <T> T deserialize(InputStream stream) {
		throw new SecurityException("Not available due security reasons");
	}

	/**
	 * @see SerializationUtils#deserialize(byte[])
	 */
	public static <T> T deserialize(byte[] bytes) {
		throw new SecurityException("Not available due security reasons");
	}
}
