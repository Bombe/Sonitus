/*
 * Sonitus - ExternalMp3Decoder.java - Copyright © 2013 David Roden
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

import static com.google.common.base.Preconditions.*;

import net.pterodactylus.sonitus.data.ConnectException;
import net.pterodactylus.sonitus.data.Format;
import net.pterodactylus.sonitus.data.Source;

/**
 * Basic {@link ExternalFilter} implementation that verifies that the connected
 * source is MP3-encoded and returns a PCM-encoded stream.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ExternalMp3Decoder extends ExternalFilter {

	/**
	 * Creates a new external MP3 decoder.
	 *
	 * @param binary
	 * 		The binary to execute
	 * @param parameters
	 * 		The parameters for the binary
	 */
	public ExternalMp3Decoder(String binary, Iterable<String> parameters) {
		super(binary, parameters);
	}

	@Override
	public Format format() {
		return super.format().encoding("PCM");
	}

	@Override
	public void connect(Source source) throws ConnectException {
		checkNotNull(source, "source must not be null");
		checkState(source.format().encoding().equalsIgnoreCase("MP3"), "source must be MP3-encoded");

		super.connect(source);
	}

}
