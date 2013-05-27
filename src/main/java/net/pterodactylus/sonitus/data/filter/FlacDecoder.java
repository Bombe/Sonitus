/*
 * Sonitus - FlacDecoder.java - Copyright © 2013 David Roden
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

import net.pterodactylus.sonitus.data.Metadata;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

/**
 * Decoder {@link net.pterodactylus.sonitus.data.Filter} for FLAC files.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FlacDecoder extends ExternalFilter {

	/** The location of the binary. */
	private final String binary;

	/** Whether to swap the bytes. */
	private boolean swapBytes;

	/**
	 * Creates a new FLAC decoder.
	 *
	 * @param eventBus
	 * 		The event bus
	 * @param binary
	 * 		The location of the binary
	 */
	public FlacDecoder(EventBus eventBus, String binary) {
		super(eventBus, "FLAC Decoder");
		this.binary = binary;
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

		parameters.add("--decode");
		parameters.add("--stdout");
		parameters.add("--silent");
		parameters.add("--output-name=-");
		parameters.add("--force-raw-format");
		if (swapBytes) {
			parameters.add("--endian=little");
		} else {
			parameters.add("--endian=big");
		}
		parameters.add(String.format("--channels=%d", metadata.channels()));
		parameters.add("--bps=16");
		parameters.add(String.format("--sample-rate=%d", metadata.frequency()));
		parameters.add("--sign=signed");

		return parameters.build();
	}

}
