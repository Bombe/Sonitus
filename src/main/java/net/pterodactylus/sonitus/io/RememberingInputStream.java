/*
 * Sonitus - RememberingInputStream.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

/**
 * Wrapper around an {@link InputStream} that remembers all bytes that have been
 * read from the wrapped input stream. The remembered bytes can be retrieved
 * from this stream as another input stream, suitable for use with {@link
 * java.io.SequenceInputStream}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class RememberingInputStream extends FilterInputStream {

	/** The buffer for the read bytes. */
	private final ByteArrayOutputStream rememberBuffer = new ByteArrayOutputStream();

	/**
	 * Creates a new remembering input stream.
	 *
	 * @param inputStream
	 * 		The input stream to remember
	 */
	public RememberingInputStream(InputStream inputStream) {
		super(inputStream);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns an input stream that repeats the originally wrapped stream,
	 * including all bytes that have already been read.
	 *
	 * @return A new input stream with the original content
	 */
	public InputStream remembered() {
		return new SequenceInputStream(new ByteArrayInputStream(rememberBuffer.toByteArray()), in);
	}

	//
	// INPUTSTREAM METHODS
	//

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public int read() throws IOException {
		int read = super.read();
		if (read != -1) {
			rememberBuffer.write(read);
		}
		return read;
	}

	@Override
	public int read(byte[] bytes) throws IOException {
		int read = super.read(bytes);
		if (read != -1) {
			rememberBuffer.write(bytes, 0, read);
		}
		return read;
	}

	@Override
	public int read(byte[] bytes, int offset, int length) throws IOException {
		int read = super.read(bytes, offset, length);
		if (read != -1) {
			rememberBuffer.write(bytes, offset, read);
		}
		return read;
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark()/reset() not supported");
	}

	/**
	 * {@inheritDoc} <p> This method disallows seeking and always returns {@code
	 * 0}. </p>
	 */
	@Override
	public long skip(long l) throws IOException {
		return 0;
	}

}
