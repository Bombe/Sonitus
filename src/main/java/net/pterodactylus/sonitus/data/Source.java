package net.pterodactylus.sonitus.data;

import java.io.IOException;

/**
 * A source produces an audio stream and accompanying metadata.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Source {

	/**
	 * Returns the metadata of the audio stream.
	 *
	 * @return The metadata of the audio stream
	 */
	Metadata metadata();

	/**
	 * Retrieves data from the audio stream.
	 *
	 * @param bufferSize
	 * 		The maximum amount of bytes to retrieve from the audio stream
	 * @return A buffer filled with up to {@code bufferSize} bytes of data; the
	 *         returned buffer may contain less data than requested but will not
	 *         contain excess elements
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	byte[] get(int bufferSize) throws IOException;

}
