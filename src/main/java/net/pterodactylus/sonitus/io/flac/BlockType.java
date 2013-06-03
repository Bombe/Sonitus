/*
 * Sonitus - BlockType.java - Copyright © 2013 David Roden
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

/**
 * The type of a metadata block.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public enum BlockType {

	/** A STREAMINFO block. */
	STREAMINFO {
		@Override
		public Data createData(byte[] content) {
			return new StreamInfo(content);
		}
	},

	/** A PADDING block. */
	PADDING,

	/** An APPLICATION block. */
	APPLICATION,

	/** A SEEKTABLE block. */
	SEEKTABLE,

	/** A VORBIS_COMMENT block. */
	VORBIS_COMMENT,

	/** A CUESHEET block. */
	CUESHEET,

	/** A PICTURE block. */
	PICTURE,

	/** A RESERVED block. */
	RESERVED,

	/** An INVALID block. */
	INVALID;

	//
	// ACTIONS
	//

	/**
	 * Creates a {@link Data} object from the given byte array. Block type
	 * enumeration values can override this to return specialized parser objects.
	 *
	 * @param content
	 * 		The content of the metadata block
	 * @return The metadata block as a data object
	 */
	public Data createData(byte[] content) {
		return new Data(content);
	}

	//
	// STATIC METHODS
	//

	/**
	 * Creates a block type from the given block type number.
	 *
	 * @param blockType
	 * 		The block type number
	 * @return The parsed block type
	 */
	public static BlockType valueOf(int blockType) {
		if ((blockType >= 0) && (blockType <= 6)) {
			return values()[blockType];
		}
		if ((blockType > 6) && (blockType < 127)) {
			return RESERVED;
		}
		return INVALID;
	}

}
