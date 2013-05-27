/*
 * Sonitus - VolumeFilter.java - Copyright © 2013 David Roden
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
import net.pterodactylus.sonitus.data.controller.Fader;
import net.pterodactylus.sonitus.data.controller.Switch;

import com.google.common.eventbus.EventBus;

/**
 * Internal {@link Filter} implementation that can reduce the volume of the
 * signal.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class VolumeFilter extends AudioProcessingFilter {

	/** The volume fader. */
	private final Fader volumeFader;

	/** The mute switch. */
	private final Switch muteSwitch;

	/**
	 * Creates a new volume filter.
	 *
	 * @param eventBus
	 * 		The event bus
	 */
	public VolumeFilter(EventBus eventBus) {
		super(eventBus, "Volume");
		volumeFader = new Fader("Volume", 1.0);
		muteSwitch = new Switch("Mute", false);
	}

	//
	// CONTROLLED METHODS
	//

	@Override
	public List<Controller<?>> controllers() {
		return Arrays.<Controller<?>>asList(volumeFader, muteSwitch);
	}

	//
	// AUDIOPROCESSINGFILTER METHODS
	//

	@Override
	protected int[] processSamples(int[] samples) {
		int[] processedSamples = new int[samples.length];
		double volumeFactor = volumeFader.value();
		for (int channel = 0; channel < samples.length; ++channel) {
			processedSamples[channel] = muteSwitch.value() ? 0 : (int) (samples[channel] * volumeFactor);
		}
		return processedSamples;
	}

}
