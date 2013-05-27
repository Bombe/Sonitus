/*
 * Sonitus - Icecast2Sink.java - Copyright © 2013 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sonitus.data.sink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.Sink;
import net.pterodactylus.sonitus.data.event.MetadataUpdated;
import net.pterodactylus.sonitus.io.InputStreamDrainer;

import com.google.common.eventbus.EventBus;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Closeables;

/**
 * {@link net.pterodactylus.sonitus.data.Sink} implementation that delivers all
 * incoming data to an Icecast2 server.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Icecast2Sink implements Sink {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(Icecast2Sink.class.getName());

	/** The event bus. */
	private final EventBus eventBus;

	/** The server name. */
	private final String server;

	/** The port number on the server. */
	private final int port;

	/** The source password. */
	private final String password;

	/** The stream mount point (without leading slash). */
	private final String mountPoint;

	/** The name of the server. */
	private final String serverName;

	/** The description of the server. */
	private final String serverDescription;

	/** The genre of the server. */
	private final String genre;

	/** Whether to publish the server. */
	private final boolean publishServer;

	/** The output stream to the server. */
	private OutputStream socketOutputStream;

	/** The current metadata. */
	private Metadata metadata;

	/**
	 * Creates a new Icecast2 sink.
	 *
	 * @param eventBus
	 * 		The event bus
	 * @param server
	 * 		The hostname of the server
	 * @param port
	 * 		The port number of the server
	 * @param password
	 * 		The source password
	 * @param mountPoint
	 * 		The stream mount point
	 * @param serverName
	 * 		The name of the server
	 * @param serverDescription
	 * 		The description of the server
	 * @param genre
	 * 		The genre of the server
	 * @param publishServer
	 * 		{@code true} to publish the server in a public directory, {@code false} to
	 * 		not publish it
	 */
	public Icecast2Sink(EventBus eventBus, String server, int port, String password, String mountPoint, String serverName, String serverDescription, String genre, boolean publishServer) {
		this.eventBus = eventBus;
		this.server = server;
		this.port = port;
		this.password = password;
		this.mountPoint = mountPoint;
		this.serverName = serverName;
		this.serverDescription = serverDescription;
		this.genre = genre;
		this.publishServer = publishServer;
	}

	//
	// CONTROLLED METHODS
	//

	@Override
	public String name() {
		return String.format("icecast://%s:%d/%s", server, port, mountPoint);
	}

	@Override
	public Metadata metadata() {
		return metadata;
	}

	@Override
	public List<Controller<?>> controllers() {
		return Collections.emptyList();
	}

	//
	// SINK METHODS
	//

	@Override
	public void open(Metadata metadata) throws IOException {
		logger.info(String.format("Connecting to %s:%d...", server, port));
		Socket socket = new Socket(server, port);
		logger.info("Connected.");
		socketOutputStream = socket.getOutputStream();
		InputStream socketInputStream = socket.getInputStream();

		sendLine(socketOutputStream, String.format("SOURCE /%s ICE/1.0", mountPoint));
		sendLine(socketOutputStream, String.format("Authorization: Basic %s", generatePassword(password)));
		sendLine(socketOutputStream, String.format("Content-Type: %s", getContentType(metadata)));
		sendLine(socketOutputStream, String.format("ICE-Name: %s", serverName));
		sendLine(socketOutputStream, String.format("ICE-Description: %s", serverDescription));
		sendLine(socketOutputStream, String.format("ICE-Genre: %s", genre));
		sendLine(socketOutputStream, String.format("ICE-Public: %d", publishServer ? 1 : 0));
		sendLine(socketOutputStream, "");
		socketOutputStream.flush();

		new Thread(new InputStreamDrainer(socketInputStream)).start();

		metadataUpdated(metadata);
	}

	@Override
	public void close() {
		try {
			Closeables.close(socketOutputStream, true);
		} catch (IOException e) {
			/* will never throw. */
		}
	}

	@Override
	public void metadataUpdated(final Metadata metadata) {
		this.metadata = metadata;
		new Thread(new Runnable() {

			@Override
			public void run() {
				String metadataString = String.format("%s (%s)", metadata.title(), "Sonitus");
				logger.info(String.format("Updating metadata to %s", metadataString));

				Socket socket = null;
				OutputStream socketOutputStream = null;
				try {
					socket = new Socket(server, port);
					socketOutputStream = socket.getOutputStream();

					sendLine(socketOutputStream, String.format("GET /admin/metadata?pass=%s&mode=updinfo&mount=/%s&song=%s HTTP/1.0", password, mountPoint, URLEncoder.encode(metadataString, "UTF-8")));
					sendLine(socketOutputStream, String.format("Authorization: Basic %s", generatePassword(password)));
					sendLine(socketOutputStream, String.format("User-Agent: Mozilla/Sonitus"));
					sendLine(socketOutputStream, "");
					socketOutputStream.flush();

					new InputStreamDrainer(socket.getInputStream()).run();
				} catch (IOException ioe1) {
					logger.log(Level.WARNING, "Could not update metadata!", ioe1);
				} finally {
					try {
						Closeables.close(socketOutputStream, true);
						if (socket != null) {
							socket.close();
						}
					} catch (IOException ioe1) {
						/* ignore. */
					}
				}
			}
		}).start();
		eventBus.post(new MetadataUpdated(this, metadata));
	}

	@Override
	public void process(byte[] buffer) throws IOException {
		socketOutputStream.write(buffer);
		socketOutputStream.flush();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Sends the given line, followed by CR+LF, to the given output stream,
	 * encoding the complete line as UTF-8.
	 *
	 * @param outputStream
	 * 		The output stream to send the line to
	 * @param line
	 * 		The line to send
	 * @throws java.io.IOException
	 * 		if an I/O error occurs
	 */
	private static void sendLine(OutputStream outputStream, String line) throws IOException {
		outputStream.write((line + "\r\n").getBytes("UTF-8"));
	}

	/**
	 * Generates the Base64-encoded authorization information from the given
	 * password. A fixed username of “source” is used.
	 *
	 * @param password
	 * 		The password to encode
	 * @return The encoded password
	 * @throws java.io.UnsupportedEncodingException
	 * 		if the UTF-8 encoding is not supported (which can never happen)
	 */
	private static String generatePassword(String password) throws UnsupportedEncodingException {
		return BaseEncoding.base64().encode(("source:" + password).getBytes("UTF-8"));
	}

	/**
	 * Returns a MIME type for the given metadata. Currently only Vorbis, MP3, PCM,
	 * Ogg Vorbis, Opus, and FLAC formats are recognized.
	 *
	 * @param metadata
	 * 		The metadata to get a MIME type for
	 * @return The MIME type of the metadata
	 */
	private static String getContentType(Metadata metadata) {
		String encoding = metadata.encoding();
		if ("Vorbis".equalsIgnoreCase(encoding)) {
			return "audio/ogg";
		}
		if ("MP3".equalsIgnoreCase(encoding)) {
			return "audio/mpeg";
		}
		if ("PCM".equalsIgnoreCase(encoding)) {
			return "audio/vnd.wave";
		}
		if ("Vorbis".equalsIgnoreCase(encoding)) {
			return "application/ogg";
		}
		if ("Opus".equalsIgnoreCase(encoding)) {
			return "audio/ogg; codecs=opus";
		}
		if ("FLAC".equalsIgnoreCase(encoding)) {
			return "audio/flac";
		}
		return "application/octet-stream";
	}

}
