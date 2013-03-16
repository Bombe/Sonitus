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
	private static final Logger logger = Logger.getLogger(ExternalFilter.class.getName());

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
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						drainInputStream(processError);
					} catch (IOException ioe1) {
						/* ignore, just let the thread exit. */
					}
					logger.finest("ExternalFilter: Reading stderr finished.");
				}
			}).start();
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						ByteStreams.copy(processOutput, pipedOutputStream);
					} catch (IOException ioe1) {
						/* okay, just exit. */
					}
					logger.finest("ExternalFilter: Reading stdout finished.");
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

	//
	// STATIC METHODS
	//

	private static void drainInputStream(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[4096];
		int read;
		while ((read = inputStream.read(buffer)) != -1) {
			logger.finest(String.format("ExternalFilter: Drained %d Bytes.", read));
			/* do nothing, just read the damn thing. */
		}
	}

}
