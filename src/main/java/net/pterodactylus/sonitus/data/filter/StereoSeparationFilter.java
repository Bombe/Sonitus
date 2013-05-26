/*
 * Sonitus - StereoSeparationFilter.java - Copyright © 2013 David Roden
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

import java.util.Arrays;
import java.util.List;

import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.Filter;
import net.pterodactylus.sonitus.data.controller.Knob;

/**
 * {@link Filter} implementation that can reduce the stereo width of a signal,
 * or even reverse the channels.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class StereoSeparationFilter extends AudioProcessingFilter {

	/** The separation knob. */
	private final Knob separationKnob;

	/** Creates a new stereo separation filter. */
	public StereoSeparationFilter() {
		separationKnob = new Knob("Separation", 1.0);
	}

	//
	// CONTROLLED METHODS
	//

	@Override
	public List<Controller<?>> controllers() {
		return Arrays.<Controller<?>>asList(separationKnob);
	}

	//
	// AUDIOPROCESSINGFILTER METHODS
	//

	@Override
	protected int[] processSamples(int[] samples) {
		if (samples.length == 1) {
			return samples;
		}
		int[] processedSamples = new int[samples.length];
		double a = (separationKnob.value() + 1) / 2.0;
		processedSamples[0] = (int) (samples[0] * a + samples[1] * (1 - a));
		processedSamples[1] = (int) (samples[1] * a + samples[0] * (1 - a));
		return processedSamples;
	}

}
