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

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.ConnectException;
import net.pterodactylus.sonitus.data.Connection;
import net.pterodactylus.sonitus.data.Format;
import net.pterodactylus.sonitus.data.Sink;
import net.pterodactylus.sonitus.data.Source;
import net.pterodactylus.sonitus.io.InputStreamDrainer;

import com.google.common.io.BaseEncoding;
import com.google.common.io.Closeables;

/**
 * {@link Sink} implementation that delivers all incoming data to an Icecast2
 * server.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Icecast2Sink implements Sink {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(Icecast2Sink.class.getName());

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

	/**
	 * Creates a new Icecast2 sink.
	 *
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
	public Icecast2Sink(String server, int port, String password, String mountPoint, String serverName, String serverDescription, String genre, boolean publishServer) {
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
	// SINK METHODS
	//

	@Override
	public void connect(Source source) throws ConnectException {
		checkNotNull(source, "source must not be null");

		try {
			logger.info(String.format("Icecast2Sink: Connecting to %s:%d...", server, port));
			final Socket socket = new Socket(server, port);
			logger.info("Icecast2Sink: Connected.");
			final OutputStream socketOutputStream = socket.getOutputStream();
			final InputStream socketInputStream = socket.getInputStream();

			sendLine(socketOutputStream, String.format("SOURCE /%s ICE/1.0", mountPoint));
			sendLine(socketOutputStream, String.format("Authorization: Basic %s", generatePassword(password)));
			sendLine(socketOutputStream, String.format("Content-Type: %s", getContentType(source.format())));
			sendLine(socketOutputStream, String.format("ICE-Name: %s", serverName));
			sendLine(socketOutputStream, String.format("ICE-Description: %s", serverDescription));
			sendLine(socketOutputStream, String.format("ICE-Genre: %s", genre));
			sendLine(socketOutputStream, String.format("ICE-Public: %d", publishServer ? 1 : 0));
			sendLine(socketOutputStream, "");
			socketOutputStream.flush();

			new Thread(new InputStreamDrainer(socketInputStream)).start();
			new Thread(new Connection(source) {

				private long counter;

				@Override
				protected int bufferSize() {
					return 4096;
				}

				@Override
				protected void feed(byte[] buffer) throws IOException {
					socketOutputStream.write(buffer);
					socketOutputStream.flush();
					counter += buffer.length;
					logger.finest(String.format("Wrote %d Bytes.", counter));
				}

				@Override
				protected void finish() throws IOException {
					Closeables.close(socketOutputStream, true);
					Closeables.close(socket, true);
				}
			}).start();
		} catch (IOException ioe1) {
			throw new ConnectException(ioe1);
		}
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
	 * @throws IOException
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
	 * @throws UnsupportedEncodingException
	 * 		if the UTF-8 encoding is not supported (which can never happen)
	 */
	private static String generatePassword(String password) throws UnsupportedEncodingException {
		return BaseEncoding.base64().encode(("source:" + password).getBytes("UTF-8"));
	}

	/**
	 * Returns a MIME type for the given format. Currently only Vorbis, MP3, and
	 * PCM formats are recognized.
	 *
	 * @param format
	 * 		The format to get a MIME type for
	 * @return The MIME type of the format
	 */
	private static String getContentType(Format format) {
		switch (format.encoding().toUpperCase()) {
			case "VORBIS":
				return "audio/ogg";
			case "MP3":
				return "audio/mpeg";
			case "PCM":
				return "audio/vnd.wave";
			default:
				return "application/octet-stream";
		}
	}

}