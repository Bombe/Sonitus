/*
 * Sonitus - ResampleFilter.java - Copyright © 2013 David Roden
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
 * {@link net.pterodactylus.sonitus.data.Filter} implementation that uses {@code
 * sox} to resample a PCM-encoded source.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoxResampleFilter extends ExternalFilter {

	/** The location of the binary. */
	private final String binary;

	/** The final sampling rate. */
	private final int rate;

	/**
	 * Creates a new resample filter.
	 *
	 * @param binary
	 * 		The location of the binary
	 * @param rate
	 */
	public SoxResampleFilter(String binary, int rate) {
		super(String.format("Resample to %s kHz", rate / 1000.0));
		this.binary = binary;
		this.rate = rate;
	}

	//
	// FILTER METHODS
	//

	@Override
	public Metadata metadata() {
		return super.metadata().frequency(rate);
	}

	@Override
	public void open(Metadata metadata) throws IOException {
		checkNotNull(metadata, "metadata must not be null");
		checkArgument(metadata.encoding().equalsIgnoreCase("PCM"), "source must be PCM-encoded");

		super.open(metadata);
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
		/* the input file. */
		parameters.add("--bits").add("16");
		parameters.add("--channels").add(String.valueOf(metadata.channels()));
		parameters.add("--encoding").add("signed-integer");
		parameters.add("--rate").add(String.valueOf(metadata.frequency()));
		parameters.add("--type").add("raw");
		parameters.add("--endian").add("little");
		parameters.add("-");
		/* the output file. */
		parameters.add("--bits").add("16");
		parameters.add("--channels").add(String.valueOf(metadata.channels()));
		parameters.add("--encoding").add("signed-integer");
		parameters.add("--type").add("raw");
		parameters.add("--endian").add("little");
		parameters.add("-");
		/* rate effect. */
		parameters.add("rate").add("-h").add(String.valueOf(rate));
		return parameters.build();
	}

}
