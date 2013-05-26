/*
 * Sonitus - IntegralWriteOutputStream.java - Copyright © 2013 David Roden
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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link OutputStream} wrapper that always writes a multiple of a fixed amount
 * of bytes at once.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IntegralWriteOutputStream extends FilterOutputStream {

	/** The current in-process values. */
	private final byte[] buffer;

	/** The next position of the buffer that will be written to. */
	private int bufferPosition;

	/**
	 * Creates a new integral write output stream.
	 *
	 * @param outputStream
	 * 		The output stream to wrap
	 * @param integralSize
	 * 		The number of bytes to write at once
	 */
	public IntegralWriteOutputStream(OutputStream outputStream, int integralSize) {
		super(outputStream);
		buffer = new byte[integralSize];
	}

	//
	// OUTPUTSTREAM METHODS
	//

	@Override
	public void write(int data) throws IOException {
		buffer[bufferPosition++] = (byte) data;
		if (bufferPosition == buffer.length) {
			bufferPosition = 0;
			out.write(buffer);
		}
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		write(buffer, 0, buffer.length);
	}

	@Override
	public void write(byte[] buffer, int offset, int length) throws IOException {
		/* are the some bytes in the current buffer? */
		int sourceOffset = 0;
		if (bufferPosition != 0) {
			int bytesToCopy = Math.min(length, this.buffer.length - bufferPosition);
			System.arraycopy(buffer, offset, this.buffer, bufferPosition, bytesToCopy);
			sourceOffset += bytesToCopy;
			bufferPosition += bytesToCopy;
			if (bufferPosition == this.buffer.length) {
				bufferPosition = 0;
				out.write(this.buffer);
			}
		}

		/* write the largest possible chunk at once. */
		int integralBytesLeft = (int) ((length - sourceOffset) / this.buffer.length) * this.buffer.length;
		if (integralBytesLeft != 0) {
			out.write(buffer, offset + sourceOffset, integralBytesLeft);
		}

		/* are there some bytes left? */
		sourceOffset += integralBytesLeft;
		if (sourceOffset < length) {
			System.arraycopy(buffer, offset + sourceOffset, this.buffer, 0, length - sourceOffset);
			bufferPosition = length - sourceOffset;
		}
	}

}
