/*
 * Sonitus - AbstractFilter.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

/**
 * Abstract {@link Filter} implementation that takes care of managing {@link
 * MetadataListener}s and pipes its input to its output.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class AbstractFilter implements Filter {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(AbstractFilter.class.getName());

	/** The name of this filter. */
	private final String name;

	/** The list of metadata listeners. */
	private final List<MetadataListener> metadataListeners = Lists.newCopyOnWriteArrayList();

	/** The current metadata. */
	private final AtomicReference<Metadata> metadata = new AtomicReference<Metadata>();

	/** The input stream from which to read. */
	private InputStream inputStream;

	/** The output stream to which to write. */
	private OutputStream outputStream;

	/**
	 * Creates a new abstract filter.
	 *
	 * @param name
	 * 		The name of the filter
	 */
	protected AbstractFilter(String name) {
		this.name = name;
	}

	//
	// LISTENER MANAGEMENT
	//

	@Override
	public void addMetadataListener(MetadataListener metadataListener) {
		metadataListeners.add(metadataListener);
	}

	@Override
	public void removeMetadataListener(MetadataListener metadataListener) {
		metadataListeners.remove(metadataListener);
	}

	//
	// FILTER METHODS
	//

	@Override
	public String name() {
		return name;
	}

	@Override
	public List<Controller<?>> controllers() {
		return Collections.emptyList();
	}

	@Override
	public Metadata metadata() {
		return metadata.get();
	}

	@Override
	public void metadataUpdated(Metadata metadata) {
		if (metadata.equals(this.metadata.get())) {
			return;
		}
		this.metadata.set(metadata);
		fireMetadataUpdated(metadata);
	}

	@Override
	public void open(Metadata metadata) throws IOException {
		metadataUpdated(metadata);
		inputStream = createInputStream();
		outputStream = createOutputStream();
	}

	@Override
	public void close() {
		try {
			Closeables.close(outputStream, true);
			Closeables.close(inputStream, true);
		} catch (IOException e) {
			/* won’t throw. */
		}
	}

	@Override
	public void process(DataPacket dataPacket) throws IOException {
		if (dataPacket.metadata().isPresent() && !dataPacket.metadata().get().equalsIgnoreComment(this.metadata.get())) {
			metadataUpdated(dataPacket.metadata().get());
		}
		logger.finest(String.format("Writing %d bytes to %s...", dataPacket.buffer().length, name()));
		outputStream.write(dataPacket.buffer());
		outputStream.flush();
	}

	@Override
	public DataPacket get(int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int read = inputStream.read(buffer);
		if (read == -1) {
			throw new EOFException();
		}
		return new DataPacket(metadata(), Arrays.copyOf(buffer, read));
	}

	//
	// EVENT METHODS
	//

	/**
	 * Notifies all registered metadata listeners that the metadata has changed.
	 *
	 * @param metadata
	 * 		The new metadata
	 */
	protected void fireMetadataUpdated(Metadata metadata) {
		for (MetadataListener metadataListener : metadataListeners) {
			metadataListener.metadataUpdated(this, metadata);
		}
	}

	//
	// SUBCLASS METHODS
	//

	/**
	 * Creates the input stream from which {@link net.pterodactylus.sonitus.data.Pipeline}
	 * will read the audio data. If you override this, you have to override {@link
	 * #createOutputStream()}, too!
	 *
	 * @return The input stream to read from
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	protected InputStream createInputStream() throws IOException {
		return new PipedInputStream();
	}

	/**
	 * Creates the output stream to which {@link net.pterodactylus.sonitus.data.Pipeline}
	 * will write the audio data. If you override this, you have to override {@link
	 * #createInputStream()}, too!
	 *
	 * @return The output stream to write to
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	protected OutputStream createOutputStream() throws IOException {
		return new PipedOutputStream((PipedInputStream) inputStream);
	}

}
