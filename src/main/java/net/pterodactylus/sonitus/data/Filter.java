/*
 * Sonitus - Filter.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data;

/**
 * A filter processes an input to produce an output.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Filter {

	/**
	 * Returns whether this filter understands the given format.
	 *
	 * @param format
	 * 		The format to check for
	 * @return {@code true} if this filter understands the given format, {@code
	 *         false} otherwise
	 */
	boolean understands(Format format);

	/**
	 * Returns whether this filter can produce the given format.
	 *
	 * @param format
	 * 		The format to check for
	 * @return {@code true} if this filter can produce the given format, {@code
	 *         false} otherwise
	 */
	boolean produces(Format format);

}
