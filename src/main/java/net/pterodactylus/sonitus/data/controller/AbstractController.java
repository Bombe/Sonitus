/*
 * Sonitus - AbstractController.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data.controller;

import net.pterodactylus.sonitus.data.Controller;

/**
 * Base {@link Controller} implementation that does housekeeping for the common
 * values. Additional functionality (or an arbitrary mapping for the values of a
 * controller) can be added in subclasses.
 * <p/>
 * This implementation is not thread-safe.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class AbstractController implements Controller {

	/** The minimum value of this controller. */
	private final int minimum;

	/** The maximum value of this controller. */
	private final int maximum;

	/** Whether this controller has a “center” position. */
	private final boolean centered;

	/** The current value of this controller. */
	private int value;

	/**
	 * Creates a new abstract controller.
	 *
	 * @param minimum
	 * 		The minimum value of the controller
	 * @param maximum
	 * 		The maximum value of the controller
	 * @param centered
	 * 		{@code true} if this controller has a “center” position, {@code false}
	 * 		otherwise
	 * @param currentValue
	 * 		The current value of this controller
	 */
	public AbstractController(int minimum, int maximum, boolean centered, int currentValue) {
		this.minimum = minimum;
		this.maximum = maximum;
		this.centered = centered;
		value = currentValue;
	}

	//
	// CONTROLLER METHODS
	//

	@Override
	public int minimum() {
		return minimum;
	}

	@Override
	public int maximum() {
		return maximum;
	}

	@Override
	public boolean centered() {
		return centered;
	}

	@Override
	public int value() {
		return value;
	}

	@Override
	public void value(int value) {
		int newValue = Math.min(maximum, Math.max(minimum, value));
		if (newValue != this.value) {
			this.value = newValue;
			valueSet(newValue);
		}
	}

	//
	// SUBCLASS METHODS
	//

	/**
	 * Adjusts the controller. This method is called from {@link #value(int)} if
	 * the new value is different from the current value. Also, the value is
	 * clamped to fit within the range of this controller.
	 *
	 * @param value
	 * 		The new value
	 */
	protected abstract void valueSet(int value);

}
