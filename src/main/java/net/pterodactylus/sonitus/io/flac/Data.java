/*
 * Sonitus - Data.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.io.flac;

import static com.google.common.io.ByteStreams.readFully;

import java.io.IOException;
import java.io.InputStream;

/**
 * Accessor type that can parse the contents of a {@link MetadataBlock}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Data {

	/** The content of the metadata block. */
	private final byte[] content;

	/**
	 * Creates a new data accessor.
	 *
	 * @param content
	 * 		The content of the metadata block
	 */
	public Data(byte[] content) {
		this.content = content;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the content of this metadata block.
	 *
	 * @return The content of the metadata block
	 */
	public byte[] content() {
		return content;
	}

	//
	// SUBCLASS METHODS
	//

	/**
	 * Parses the given number of bits from the content of this metadata block,
	 * starting at the given byte offset and bit offset (bit 0 being the most
	 * significant bit).
	 *
	 * @param byteOffset
	 * 		The byte offset at which to start reading
	 * @param bitOffset
	 * 		The bit offset at which to start reading
	 * @param numberOfBits
	 * 		The number of bits to parse (should be <= 64)
	 * @return The parsed bits
	 */
	protected long parseBits(int byteOffset, int bitOffset, int numberOfBits) {
		long value = 0;
		int currentByteOffset = byteOffset;
		int currentBitOffset = bitOffset;
		int bitsRemaining = numberOfBits;

		while (bitsRemaining > 0) {
			value = (value << Math.min(8, bitsRemaining)) | ((content[currentByteOffset] & (0xff >>> currentBitOffset)) >> (8 - currentBitOffset - Math.min(bitsRemaining, 8 - currentBitOffset)));
			bitsRemaining -= Math.min(bitsRemaining, 8 - currentBitOffset);
			currentBitOffset = 0;
			currentByteOffset++;
		}

		return value;
	}

	//
	// STATIC METHODS
	//

	/**
	 * Creates a new data accessor from the given input stream.
	 *
	 * @param inputStream
	 * 		The input stream to read the contents of the metadata block from
	 * @param blockType
	 * 		The type of the metadata block
	 * @param length
	 * 		The length of the metadata block
	 * @return The parsed metadata block
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public static Data parse(InputStream inputStream, BlockType blockType, int length) throws IOException {
		byte[] buffer = new byte[length];
		readFully(inputStream, buffer);
		return blockType.createData(buffer);
	}

}
