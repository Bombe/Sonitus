/*
 * Sonitus - SourceFinishedEvent.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data.event;

import net.pterodactylus.sonitus.data.Source;

/**
 * Event that is sent to an {@link com.google.common.eventbus.EventBus} when
 * a {@link Source} is no longer in use.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SourceFinishedEvent {

	/** The source that is no longer in use. */
	private final Source source;

	/**
	 * Creates a new source finished event.
	 *
	 * @param source
	 * 		The source that is no longer in use
	 */
	public SourceFinishedEvent(Source source) {
		this.source = source;
	}

	/**
	 * Returns the source that is no longer in use
	 *
	 * @return The source that is no longer in use
	 */
	public Source source() {
		return source;
	}

}
