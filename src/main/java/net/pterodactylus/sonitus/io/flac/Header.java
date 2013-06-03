/*
 * Sonitus - Header.java - Copyright © 2013 David Roden
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
 * Header for a {@link MetadataBlock}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Header {

	/** Whether this metadata block is the last metadata block. */
	private final boolean lastMetadataBlock;

	/** The type of the metadata block. */
	private final BlockType blockType;

	/** The length of the metadata block. */
	private final int length;

	/**
	 * Creates a new metadata block header.
	 *
	 * @param lastMetadataBlock
	 * 		{@code true} if this metadata block is the last metadata block in the FLAC
	 * 		stream, {@code false} otherwise
	 * @param blockType
	 * 		The type of the metadata block
	 * @param length
	 * 		The length of the metadata block
	 */
	private Header(boolean lastMetadataBlock, BlockType blockType, int length) {
		this.lastMetadataBlock = lastMetadataBlock;
		this.blockType = blockType;
		this.length = length;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether this metadata block is the last metadata block in the FLAC
	 * {@link Stream}.
	 *
	 * @return {@code true} if this metadata block is last metadata block in the
	 *         FLAC stream, {@code false} otherwise
	 */
	public boolean isLastMetadataBlock() {
		return lastMetadataBlock;
	}

	/**
	 * Returns the type of the metadata block.
	 *
	 * @return The type of the metadata block
	 */
	public BlockType blockType() {
		return blockType;
	}

	/**
	 * Returns the length of the metadata block.
	 *
	 * @return The length of the metadata block
	 */
	public int length() {
		return length;
	}

	//
	// STATIC METHODS
	//

	/**
	 * Parses a metadata block header from the current position of the given input
	 * stream.
	 *
	 * @param inputStream
	 * 		The input stream to parse the header from
	 * @return The parsed header
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public static Header parse(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[4];
		readFully(inputStream, buffer);
		boolean lastMetadataBlock = ((buffer[0] >> 7) & 0x01) != 0;
		BlockType blockType = BlockType.valueOf(buffer[0] & 0x7f);
		int length = ((buffer[1] & 0xff) << 16) | ((buffer[2] & 0xff) << 8) | (buffer[3] & 0xff);
		return new Header(lastMetadataBlock, blockType, length);
	}

}
