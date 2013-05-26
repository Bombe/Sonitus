/*
 * Sonitus - SampleOutputStream.java - Copyright © 2013 David Roden
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
 * {@link OutputStream} wrapper that is aware of channels and samples and can
 * process samples before forwarding them to the wrapped output stream.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class ProcessingOutputStream extends FilterOutputStream {

	/** The number of channels. */
	private final int channels;

	/** The current sample’s channel values. */
	private int[] currentSamples;

	/** The index of the current channel. */
	private int currentSampleIndex;

	/** The byte ofset of the current sample’s current channel. */
	private int currentOffset;

	/** The current sample’s channel value. */
	private int currentSample;

	/**
	 * Creates a new processing output stream. Sample values are always assumed to
	 * be 16 bits wide little-ending signed integers.
	 *
	 * @param outputStream
	 * 		The output stream to wrap
	 * @param channels
	 * 		The number of channels
	 */
	public ProcessingOutputStream(OutputStream outputStream, int channels) {
		super(outputStream);
		this.channels = channels;
		currentSamples = new int[channels];
	}

	//
	// OUTPUTSTREAM METHODS
	//

	@Override
	public void write(int data) throws IOException {
		currentSample = ((currentSample >> 8) & 0xff) | (data << 8);
		if (++currentOffset == 2) {
			currentOffset = 0;
			currentSamples[currentSampleIndex++] = currentSample;
			currentSample = 0;
			if (currentSampleIndex == channels) {
				currentSampleIndex = 0;
				int[] newSamples = processSamples(currentSamples);
				for (int sampleIndex = 0; sampleIndex < newSamples.length; ++sampleIndex) {
					super.write(newSamples[sampleIndex] & 0xff);
					super.write(newSamples[sampleIndex] >> 8);
				}
			}
		}
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		write(buffer, 0, buffer.length);
	}

	@Override
	public void write(byte[] buffer, int offset, int length) throws IOException {
		for (int index = 0; index < length; ++index) {
			write(buffer[offset + index]);
		}
	}

	//
	// SUBCLASS METHODS
	//

	/**
	 * Processes the given sample.
	 *
	 * @param samples
	 * 		The channel values for a single sample
	 * @return The processed sample’s channel values
	 */
	protected abstract int[] processSamples(int[] samples);

}
