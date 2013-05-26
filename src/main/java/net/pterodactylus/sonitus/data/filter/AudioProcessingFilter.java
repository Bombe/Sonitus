/*
 * Sonitus - AudioProcessingFilter.java - Copyright © 2013 David Roden
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

import java.io.IOException;
import java.io.OutputStream;

import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.io.ProcessingOutputStream;

/**
 * {@link Filter} implementation that can process audio samples internally.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class AudioProcessingFilter extends DummyFilter {

	/**
	 * Creates a new audio processing filter with the given name.
	 *
	 * @param name
	 * 		The name of the filter
	 */
	protected AudioProcessingFilter(String name) {
		super(name);
	}

	//
	// DUMMYFILTER METHODS
	//

	@Override
	protected OutputStream createOutputStream() throws IOException {
		OutputStream originalOutputStream = super.createOutputStream();
		ProcessingOutputStream processingOutputStream = new ProcessingOutputStream(originalOutputStream, metadata().channels()) {

			@Override
			protected int[] processSamples(int[] samples) {
				return AudioProcessingFilter.this.processSamples(samples);
			}
		};
		return processingOutputStream;
	}

	//
	// SUBCLASS METHODS
	//

	/**
	 * Called to process the given of channels for a single sample.
	 *
	 * @param samples
	 * 		The channel values of the sample
	 * @return The processed channel values
	 */
	protected abstract int[] processSamples(int[] samples);

}
