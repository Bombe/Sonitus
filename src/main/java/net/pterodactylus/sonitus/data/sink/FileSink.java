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

import net.pterodactylus.sonitus.data.AbstractControlledComponent;
import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.Sink;
import net.pterodactylus.sonitus.data.event.MetadataUpdated;

import com.google.common.eventbus.EventBus;

/**
 * {@link net.pterodactylus.sonitus.data.Sink} that writes all received data
 * into a file.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FileSink extends AbstractControlledComponent implements Sink {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(FileSink.class.getName());

	/** The event bus. */
	private final EventBus eventBus;

	/** The path of the file to write to. */
	private final String path;

	/** The output stream writing to the file. */
	private FileOutputStream fileOutputStream;

	/** The current metadata. */
	private Metadata metadata;

	/**
	 * Creates a new file sink that will write to the given path.
	 *
	 * @param eventBus
	 * 		The event bus
	 * @param path
	 * 		The path of the file to write to
	 */
	public FileSink(EventBus eventBus, String path) {
		this.eventBus = eventBus;
		this.path = path;
	}

	//
	// CONTROLLED METHODS
	//

	@Override
	public String name() {
		return path;
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
		fileOutputStream = new FileOutputStream(path);
		metadataUpdated(metadata);
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
		this.metadata = metadata;
		fireMetadataUpdated(metadata);
		eventBus.post(new MetadataUpdated(this, metadata));
	}

	@Override
	public void process(byte[] buffer) throws IOException {
		fileOutputStream.write(buffer);
		logger.finest(String.format("FileSink: Wrote %d Bytes.", buffer.length));
	}

}
