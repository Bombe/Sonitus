/*
 * Sonitus - MetadataBlock.java - Copyright © 2013 David Roden
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

import java.io.IOException;
import java.io.InputStream;

/**
 * A metadata block.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MetadataBlock {

	/** The header of the metadata block. */
	private final Header header;

	/** The data of the metadata block. */
	private final Data data;

	/**
	 * Creates a new metadata block.
	 *
	 * @param header
	 * 		The header of the metadata block
	 * @param data
	 * 		The data of the metadata block
	 */
	MetadataBlock(Header header, Data data) {
		this.header = header;
		this.data = data;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the header of this metadata block.
	 *
	 * @return The header of this metadata block
	 */
	public Header header() {
		return header;
	}

	/**
	 * Returns the data of this metadata block.
	 *
	 * @return The data of this metadata block
	 */
	public Data data() {
		return data;
	}

	//
	// STATIC METHODS
	//

	/**
	 * Parses the metadata block from the current position of the given input
	 * stream.
	 *
	 * @param inputStream
	 * 		The input stream to parse the metadata block from
	 * @return The parsed metadata block
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public static MetadataBlock parse(InputStream inputStream) throws IOException {
		Header header = Header.parse(inputStream);
		Data data = Data.parse(inputStream, header.blockType(), header.length());
		return new MetadataBlock(header, data);
	}

}
