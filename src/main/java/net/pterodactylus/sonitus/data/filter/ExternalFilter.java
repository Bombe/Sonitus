/*
 * Sonitus - ExternalFilter.java - Copyright © 2013 David Roden
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
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.ConnectException;
import net.pterodactylus.sonitus.data.Connection;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Format;
import net.pterodactylus.sonitus.data.Source;
import net.pterodactylus.sonitus.io.InputStreamDrainer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;

/**
 * {@link Filter} implementation that runs its {@link Source} through an
 * external program.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class ExternalFilter implements Filter {

	/** The logger. */
	private final Logger logger = Logger.getLogger(getClass().getName());

	/** The format of the source. */
	private Format format;

	/** The input stream that will hold the converted source. */
	private PipedInputStream pipedInputStream;

	//
	// FILTER METHODS

	@Override
	public Format format() {
		return format;
	}

	@Override
	public byte[] get(int bufferSize) throws EOFException, IOException {
		byte[] buffer = new byte[bufferSize];
		int read = pipedInputStream.read(buffer);
		if (read == -1) {
			throw new EOFException();
		}
		return Arrays.copyOf(buffer, read);
	}

	@Override
	public void connect(Source source) throws ConnectException {
		Preconditions.checkNotNull(source, "source must not be null");

		format = source.format();
		try {
			final Process process = Runtime.getRuntime().exec(Iterables.toArray(ImmutableList.<String>builder().add(binary(format)).addAll(parameters(format)).build(), String.class));
			final InputStream processOutput = process.getInputStream();
			final OutputStream processInput = process.getOutputStream();
			final InputStream processError = process.getErrorStream();
			final PipedOutputStream pipedOutputStream = new PipedOutputStream();
			pipedInputStream = new PipedInputStream(pipedOutputStream);
			new Thread(new InputStreamDrainer(processError)).start();
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						ByteStreams.copy(processOutput, pipedOutputStream);
					} catch (IOException ioe1) {
						/* okay, just exit. */
					}
					logger.finest("Reading stdout finished.");
				}
			}).start();
			new Thread(new Connection(source) {

				@Override
				protected int bufferSize() {
					return 4096;
				}

				@Override
				protected void feed(byte[] buffer) throws IOException {
					processInput.write(buffer);
					processInput.flush();
				}

				@Override
				protected void finish() throws IOException {
					processInput.close();
					processOutput.close();
					processError.close();
				}
			}).start();
		} catch (IOException ioe1) {

		}
	}

	//
	// SUBCLASS METHODS
	//

	/**
	 * Returns the location of the binary to execute.
	 *
	 * @param format
	 * 		The format being processed
	 * @return The location of the binary to execute
	 */
	protected abstract String binary(Format format);

	/**
	 * Returns the parameters for the binary.
	 *
	 * @param format
	 * 		The format being processed
	 * @return The parameters for the binary
	 */
	protected abstract Iterable<String> parameters(Format format);

}
