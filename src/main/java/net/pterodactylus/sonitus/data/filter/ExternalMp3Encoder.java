/*
 * Sonitus - ExternalMp3Encoder.java - Copyright © 2013 David Roden
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

import net.pterodactylus.sonitus.data.ConnectException;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.Source;

import com.google.common.base.Preconditions;

/**
 * Basic {@link ExternalFilter} implementation that verifies that the connected
 * source is PCM-encoded and that returns an MP3-encoded metadata.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class ExternalMp3Encoder extends ExternalFilter {

	@Override
	public Metadata metadata() {
		return super.metadata().encoding("MP3");
	}

	@Override
	public void connect(Source source) throws ConnectException {
		Preconditions.checkNotNull(source, "source must not be null");
		Preconditions.checkState(source.metadata().encoding().equalsIgnoreCase("PCM"), "source must be PCM-encoded");

		super.connect(source);
	}

}
