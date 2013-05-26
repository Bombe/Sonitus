package net.pterodactylus.sonitus.data;

import java.io.IOException;

/**
 * A sink is a destination for audio data. It can be played on speakers, it can
 * be written to a file, or it can be sent to a remote streaming server.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Sink extends Controlled {

	/**
	 * Opens this sink using the format parameters of the given metadata.
	 *
	 * @param metadata
	 * 		The metadata of the stream
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	void open(Metadata metadata) throws IOException;

	/** Closes this sink. */
	void close();

	/**
	 * Processes the given buffer of data.
	 *
	 * @param buffer
	 * 		The data to process
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	void process(byte[] buffer) throws IOException;

	/**
	 * Notifies the sink that the metadata of the audio stream has changed. This
	 * method should return as fast as possible, i.e. every heavy lifting should be
	 * done from another thread.
	 *
	 * @param metadata
	 * 		The new metadata
	 */
	void metadataUpdated(Metadata metadata);

}
