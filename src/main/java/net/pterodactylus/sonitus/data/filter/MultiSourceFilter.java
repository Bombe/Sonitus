/*
 * Sonitus - MultiSource.java - Copyright © 2013 David Roden
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

import static com.google.common.base.Preconditions.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.ConnectException;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.Format;
import net.pterodactylus.sonitus.data.ReusableSink;
import net.pterodactylus.sonitus.data.Source;
import net.pterodactylus.sonitus.data.event.SourceFinishedEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * {@link ReusableSink} implementation that supports changing the source without
 * letting the {@link net.pterodactylus.sonitus.data.Sink} know.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MultiSourceFilter implements Filter, ReusableSink {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(MultiSourceFilter.class.getName());

	/** Object used for synchronization. */
	private final Object syncObject = new Object();

	/** The event bus. */
	private final EventBus eventBus;

	/** The connection. */
	private Connection connection;

	@Inject
	public MultiSourceFilter(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public Format format() {
		synchronized (syncObject) {
			return connection.source.format();
		}
	}

	@Override
	public byte[] get(int bufferSize) throws EOFException, IOException {
		byte[] buffer = new byte[bufferSize];
		InputStream inputStream;
		synchronized (syncObject) {
			inputStream = connection.pipedInputStream;
		}
		int read = inputStream.read(buffer);
		return Arrays.copyOf(buffer, read);
	}

	@Override
	public void connect(Source source) throws ConnectException {
		checkNotNull(source, "source must not be null");
		if ((connection != null) && (connection.source != null)) {
			checkArgument(connection.source.format().equals(source.format()), "source’s format must equal this sink’s format");
		}

		if (connection == null) {
			connection = new Connection();
			new Thread(connection).start();
		}
		try {
			connection.source(source);
		} catch (IOException ioe1) {
			throw new ConnectException(ioe1);
		}
	}

	/**
	 * The connection feeds the input from the currently connected source to the
	 * input stream that {@link #get(int)} will get its data from.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private class Connection implements Runnable {

		/** The currently connected source. */
		/* synchronized by syncObject. */
		private Source source;

		/** The input stream that {@link #get(int)} will read from. */
		/* synchronized by syncObject. */
		private PipedInputStream pipedInputStream;

		/** The output stream that the source will be fed into. */
		/* synchronized by syncObject. */
		private PipedOutputStream pipedOutputStream;

		/**
		 * Changes the source of the connection.
		 *
		 * @param source
		 * 		The new source of the connection
		 * @return This connection
		 * @throws IOException
		 * 		if an I/O error occurs
		 */
		public Connection source(Source source) throws IOException {
			synchronized (syncObject) {
				if (this.source != null) {
					eventBus.post(new SourceFinishedEvent(this.source));
				}
				this.source = source;
				pipedInputStream = new PipedInputStream();
				pipedOutputStream = new PipedOutputStream(pipedInputStream);
				syncObject.notifyAll();
			}
			return this;
		}

		@Override
		public void run() {
			while (true) {
				/* wait for source to be set. */
				OutputStream outputStream;
				Source source;
				logger.finest("Entering synchronized block...");
				synchronized (syncObject) {
					logger.finest("Entered synchronized block.");
					source = this.source;
					while (source == null) {
						try {
							logger.finest("Waiting for source to connect...");
							syncObject.wait();
						} catch (InterruptedException ie1) {
							/* ignore, keep waiting. */
						}
						source = this.source;
					}
					outputStream = pipedOutputStream;
				}
				logger.finest("Exited synchronized block.");

				byte[] buffer = null;
				boolean readSuccessful = false;
				while (!readSuccessful) {
					try {
						buffer = source.get(4096);
						logger.finest(String.format("Read %d Bytes.", buffer.length));
						if (buffer.length > 0) {
							readSuccessful = true;
						}
					} catch (IOException e) {
						/* TODO - notify & wait */
					}
				}

				try {
					outputStream.write(buffer);
					logger.finest(String.format("Wrote %d Bytes.", buffer.length));
				} catch (IOException ioe1) {
					/* okay, the sink has died, exit. */
					logger.log(Level.WARNING, "Could not write to pipe!", ioe1);
					break;
				}
			}

			logger.info("Exiting.");
		}

	}

}
