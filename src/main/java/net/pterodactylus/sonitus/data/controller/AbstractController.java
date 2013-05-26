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
public abstract class AbstractController<V extends Comparable<V>> implements Controller<V> {

	/** The name of this controller. */
	private final String name;

	/** The minimum value of this controller. */
	private final V minimum;

	/** The maximum value of this controller. */
	private final V maximum;

	/** Whether this controller has a “center” position. */
	private final boolean centered;

	/** The current value of this controller. */
	private V value;

	/**
	 * Creates a new abstract controller.
	 *
	 * @param name
	 * 		The name of the controller
	 * @param minimum
	 * 		The minimum value of the controller
	 * @param maximum
	 * 		The maximum value of the controller
	 * @param centered
	 * 		{@code true} if this controller has a “center” position, {@code false}
	 * 		otherwise
	 * @param currentValue
	 */
	public AbstractController(String name, V minimum, V maximum, boolean centered, V currentValue) {
		this.name = name;
		this.minimum = minimum;
		this.maximum = maximum;
		this.centered = centered;
		value = currentValue;
	}

	//
	// CONTROLLER METHODS
	//

	@Override
	public String name() {
		return name;
	}

	@Override
	public V minimum() {
		return minimum;
	}

	@Override
	public V maximum() {
		return maximum;
	}

	@Override
	public boolean centered() {
		return centered;
	}

	@Override
	public V value() {
		return value;
	}

	@Override
	public void value(V value) {
		V newValue = (value.compareTo(minimum) < 0) ? minimum : ((value.compareTo(maximum) > 0) ? maximum : value);
		if (newValue.compareTo(this.value) != 0) {
			this.value = newValue;
			valueSet(newValue);
		}
	}

	//
	// SUBCLASS METHODS
	//

	/**
	 * Adjusts the controller. This method is called from {@link
	 * #value(Comparable)} if the new value is different from the current value.
	 * Also, the value is clamped to fit within the range of this controller.
	 * <p/>
	 * This implementation does nothing.
	 *
	 * @param value
	 * 		The new value
	 */
	protected void valueSet(V value) {
		/* do nothing. */
	}

}
