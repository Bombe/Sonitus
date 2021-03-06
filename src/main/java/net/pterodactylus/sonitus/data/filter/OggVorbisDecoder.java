/*
 * Sonitus - OggVorbisDecoder.java - Copyright © 2013 David Roden
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import net.pterodactylus.sonitus.data.Metadata;

import com.google.common.collect.ImmutableList;

/**
 * Ogg Vorbis decoder that uses {@code oggdec} (from the {@code vorbis-tools}
 * package) to decode the Ogg Vorbis stream.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class OggVorbisDecoder extends ExternalFilter {

	/** The location of the binary. */
	private final String binary;

	/** Whether to swap bytes. */
	private boolean swapBytes;

	/**
	 * Creates a new Ogg Vorbis decoder.
	 *
	 * @param binary
	 * 		The location of the binary
	 */
	public OggVorbisDecoder(String binary) {
		super("Ogg Vorbis Decoder");
		this.binary = binary;
	}

	/**
	 * Sets whether to swap bytes on the decoded output.
	 *
	 * @param swapBytes
	 * 		{@code true} to swap bytes on the decoded output, {@code false} otherwise
	 * @return This Ogg Vorbis decoder
	 */
	public OggVorbisDecoder swapBytes(boolean swapBytes) {
		this.swapBytes = swapBytes;
		return this;
	}

	//
	// FILTER METHODS
	//

	@Override
	public void open(Metadata metadata) throws IOException {
		checkNotNull(metadata, "metadata must not be null");
		checkArgument(metadata.encoding().equalsIgnoreCase("Vorbis"), "source must be Vorbis-encoded");

		super.open(metadata.encoding("PCM"));
	}

	//
	// EXTERNALFILTER METHODS
	//

	@Override
	protected String binary(Metadata metadata) {
		return binary;
	}

	@Override
	protected Iterable<String> parameters(Metadata metadata) {
		ImmutableList.Builder<String> parameters = ImmutableList.builder();
		parameters.add("-R");
		if (swapBytes) {
			parameters.add("-e").add("1");
		}
		parameters.add("-o").add("-");
		parameters.add("-");
		return parameters.build();
	}

}
