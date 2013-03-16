/*
 * Sonitus - FileSink.java - Copyright © 2013 David Roden
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.ConnectException;
import net.pterodactylus.sonitus.data.Connection;
import net.pterodactylus.sonitus.data.Sink;
import net.pterodactylus.sonitus.data.Source;

import com.google.common.base.Preconditions;

/**
 * {@link Sink} that writes all received data into a file.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FileSink implements Sink {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(FileSink.class.getName());

	/** The path of the file to write to. */
	private final String path;

	/**
	 * Creates a new file sink that will write to the given path.
	 *
	 * @param path
	 * 		The path of the file to write to
	 */
	public FileSink(String path) {
		this.path = path;
	}

	@Override
	public void connect(Source source) throws ConnectException {
		Preconditions.checkNotNull(source, "source must not be null");

		try {
			final FileOutputStream fileOutputStream = new FileOutputStream(path);
			new Thread(new Connection(source) {

				@Override
				protected int bufferSize() {
					return 65536;
				}

				@Override
				protected void feed(byte[] buffer) throws IOException {
					fileOutputStream.write(buffer);
					logger.finest(String.format("FileSink: Wrote %d Bytes.", buffer.length));
				}

				@Override
				protected void finish() throws IOException {
					fileOutputStream.close();
				}
			}).start();
		} catch (FileNotFoundException fnfe1) {
			throw new ConnectException(fnfe1);
		}
	}

	@Override
	public void metadataUpdated() {
		/* ignore. */
	}

}
