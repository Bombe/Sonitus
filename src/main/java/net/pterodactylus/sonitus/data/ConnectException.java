/*
 * Sonitus - ConnectException.java - Copyright © 2013 David Roden
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
 * Exception that signals an error when {@link Sink#connect(Source) connecting}
 * a {@link Source} to a {@link Sink}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ConnectException extends Exception {

	/** Creates a new connect exception. */
	public ConnectException() {
		super();
	}

	/**
	 * Creates a new connect exception.
	 *
	 * @param message
	 * 		The message of the exception
	 */
	public ConnectException(String message) {
		super(message);
	}

	/**
	 * Creates a new connect exception.
	 *
	 * @param cause
	 * 		The cause of the exception
	 */
	public ConnectException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new connect exception.
	 *
	 * @param message
	 * 		The message of the exception
	 * @param cause
	 * 		The cause of the exception
	 */
	public ConnectException(String message, Throwable cause) {
		super(message, cause);
	}

}
