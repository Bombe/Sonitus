/*
 * Sonitus - Mp3Parser.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.io.mp3;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Optional;
import com.google.common.io.ByteStreams;

/**
 * A parser for MP3 files. It can recognize (and skip) ID3v2 header tags and
 * MPEG audio frames.
 * <p/>
 * This uses information from <a href="http://mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm">mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm</a>.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Parser {

	/** The input stream to parse. */
	private final InputStream inputStream;

	/** The complete ID3v2 tag. */
	private final byte[] id3Tag;

	/** The current read buffer. */
	private final byte[] buffer = new byte[4];

	/**
	 * Creates a new parser.
	 *
	 * @param inputStream
	 * 		The input stream to parse
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public Parser(InputStream inputStream) throws IOException {
		this.inputStream = inputStream;
		readFully(inputStream, buffer, 0, 3);
		if ((buffer[0] == 'I') && (buffer[1] == 'D') && (buffer[2] == '3')) {
			readFully(inputStream, buffer, 0, 3);
			byte[] lengthBuffer = new byte[4];
			readFully(inputStream, lengthBuffer, 0, 4);
			int headerLength = (lengthBuffer[0] << 21) | (lengthBuffer[1] << 14) | (lengthBuffer[2] << 7) | lengthBuffer[3];
			id3Tag = new byte[headerLength + 10];
			System.arraycopy(new byte[] { 'I', 'D', '3', buffer[0], buffer[1], buffer[2], lengthBuffer[0], lengthBuffer[1], lengthBuffer[2], lengthBuffer[3] }, 0, id3Tag, 0, 10);
			readFully(inputStream, id3Tag, 10, headerLength);
			readFully(inputStream, buffer, 0, 3);
		} else {
			id3Tag = null;
		}
	}

	/**
	 * Returns the ID3v2 tag.
	 *
	 * @return The ID3v2 tag, or {@link Optional#absent()} if there is no ID3v2
	 *         tag
	 */
	public Optional<byte[]> getId3Tag() {
		return Optional.fromNullable(id3Tag);
	}

	/**
	 * Returns the next frame.
	 *
	 * @return The next frame
	 * @throws IOException
	 * 		if an I/O error occurs, or EOF is reached
	 */
	public Frame nextFrame() throws IOException {
		while (true) {
			int r = inputStream.read();
			if (r == -1) {
				throw new EOFException();
			}
			System.arraycopy(buffer, 1, buffer, 0, 3);
			buffer[3] = (byte) r;
			Optional<Frame> frame = Frame.create(buffer, 0, 4);
			if (frame.isPresent()) {
				return frame.get();
			}
		}
	}

	//
	// STATIC METHODS
	//

	/**
	 * Reads exactly {@code length} bytes from the given input stream, throwing an
	 * {@link EOFException} if there are not enough bytes left in the stream.
	 *
	 * @param inputStream
	 * 		The input stream to read from
	 * @param buffer
	 * 		The buffer in which to read
	 * @param offset
	 * 		The offset at which to start writing into the buffer
	 * @param length
	 * 		The amount of bytes to read
	 * @throws IOException
	 * 		if an I/O error occurs, or EOF is reached
	 */
	private static void readFully(InputStream inputStream, byte[] buffer, int offset, int length) throws IOException {
		if (ByteStreams.read(inputStream, buffer, offset, length) < length) {
			throw new EOFException();
		}
	}

}
