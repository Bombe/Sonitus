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

import static com.google.common.io.ByteStreams.readFully;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

/**
 * Parser and container for information about a FLAC stream.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Stream {

	/** The metadata blocks of the stream. */
	private final List<MetadataBlock> metadataBlocks = Lists.newArrayList();

	/**
	 * Creates a new FLAC stream containing the given metadata blocks.
	 *
	 * @param metadataBlocks
	 * 		The metadata blocks in order of appearance
	 */
	private Stream(List<MetadataBlock> metadataBlocks) {
		this.metadataBlocks.addAll(metadataBlocks);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns all metadata blocks of the given block type, in the order they
	 * appear in this stream.
	 *
	 * @param blockType
	 * 		The block type to get all metadata blocks for
	 * @return The metadata blocks of the given block type
	 */
	public List<MetadataBlock> metadataBlocks(final BlockType blockType) {
		return FluentIterable.from(metadataBlocks).filter(new Predicate<MetadataBlock>() {

			@Override
			public boolean apply(MetadataBlock metadataBlock) {
				return metadataBlock.header().blockType() == blockType;
			}
		}).toList();
	}

	//
	// STATIC METHODS
	//

	/**
	 * Parses the given input stream and returns information about the stream if it
	 * can be successfully parsed as a FLAC stream.
	 *
	 * @param inputStream
	 * 		The input stream containing the FLAC stream
	 * @return The parsed FLAC stream, or {@link Optional#absent()} if no FLAC
	 *         stream could be found at the stream’s current position
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public static Optional<Stream> parse(InputStream inputStream) throws IOException {
		byte[] streamTag = new byte[4];
		readFully(inputStream, streamTag);
		if (!Arrays.equals(streamTag, new byte[] { 'f', 'L', 'a', 'C' })) {
			return Optional.absent();
		}
		List<MetadataBlock> metadataBlocks = Lists.newArrayList();
		while (true) {
			MetadataBlock metadataBlock = MetadataBlock.parse(inputStream);
			metadataBlocks.add(metadataBlock);
			if (metadataBlock.header().isLastMetadataBlock()) {
				break;
			}
		}
		return Optional.of(new Stream(metadataBlocks));
	}

}
