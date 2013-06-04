/*
 * Sonitus - SourceFinishedListener.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data.source;

import java.util.EventListener;

/**
 * Interface for {@link MultiSource} notifications if a source is finished
 * playing.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface SourceFinishedListener extends EventListener {

	/**
	 * Notifies the listener that the given multi source has finished playing a
	 * source and a new source should be {@link MultiSource#setSource(Source)
	 * set}.
	 *
	 * @param multiSource
	 * 		The multi source that finished playing
	 */
	public void sourceFinished(MultiSource multiSource);

}
