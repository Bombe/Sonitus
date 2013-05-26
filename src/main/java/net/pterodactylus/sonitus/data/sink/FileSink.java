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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.Sink;

/**
 * {@link net.pterodactylus.sonitus.data.Sink} that writes all received data
 * into a file.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FileSink implements Sink {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(FileSink.class.getName());

	/** The path of the file to write to. */
	private final String path;

	private FileOutputStream fileOutputStream;

	/**
	 * Creates a new file sink that will write to the given path.
	 *
	 * @param path
	 * 		The path of the file to write to
	 */
	public FileSink(String path) {
		this.path = path;
	}

	//
	// CONTROLLED METHODS
	//

	@Override
	public List<Controller<?>> controllers() {
		return Collections.emptyList();
	}

	//
	// SINK METHODS
	//

	@Override
	public void open(Metadata metadata) throws IOException {
		fileOutputStream = new FileOutputStream(path);
	}

	@Override
	public void close() {
		try {
			fileOutputStream.close();
		} catch (IOException e) {
			/* ignore. */
		}
	}

	@Override
	public void metadataUpdated(Metadata metadata) {
		/* ignore. */
	}

	@Override
	public void process(byte[] buffer) throws IOException {
		fileOutputStream.write(buffer);
		logger.finest(String.format("FileSink: Wrote %d Bytes.", buffer.length));
	}

}
