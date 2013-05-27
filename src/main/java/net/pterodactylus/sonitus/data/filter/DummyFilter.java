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

package net.pterodactylus.sonitus.data.filter;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.event.MetadataUpdated;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Closeables;

/**
 * Dummy {@link Filter} implementation that pipes its input to its output.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DummyFilter implements Filter {

	/** The name of this filter. */
	private final String name;

	/** The event bus. */
	private final EventBus eventBus;

	/** The input stream from which to read. */
	private InputStream inputStream;

	/** The output stream to which to write. */
	private OutputStream outputStream;

	/** The current metadata. */
	private Metadata metadata;

	/**
	 * Creates a new dummy filter with the given name.
	 *
	 * @param eventBus
	 * 		The event bus
	 * @param name
	 * 		The name of the filter
	 */
	public DummyFilter(EventBus eventBus, String name) {
		this.eventBus = eventBus;
		this.name = name;
	}

	//
	// CONTROLLED METHODS
	//

	@Override
	public String name() {
		return name;
	}

	@Override
	public List<Controller<?>> controllers() {
		return Collections.emptyList();
	}

	//
	// FILTER METHODS
	//

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
	public Metadata metadata() {
		return metadata;
	}

	@Override
	public void metadataUpdated(Metadata metadata) {
		this.metadata = metadata;
		eventBus.post(new MetadataUpdated(this, metadata));
	}

	@Override
	public void process(byte[] buffer) throws IOException {
		outputStream.write(buffer);
		outputStream.flush();
	}

	@Override
	public byte[] get(int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int read = inputStream.read(buffer);
		if (read == -1) {
			throw new EOFException();
		}
		return Arrays.copyOf(buffer, read);
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
