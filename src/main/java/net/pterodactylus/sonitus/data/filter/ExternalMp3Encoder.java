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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;

import net.pterodactylus.sonitus.data.Metadata;

/**
 * Basic {@link net.pterodactylus.sonitus.data.filter.ExternalFilter}
 * implementation that verifies that the connected source is PCM-encoded and
 * that returns an MP3-encoded metadata.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class ExternalMp3Encoder extends ExternalFilter {

	/**
	 * Creates a new external MP3 encoder.
	 *
	 * @param name
	 * 		The name of the filter
	 */
	protected ExternalMp3Encoder(String name) {
		super(name);
	}

	@Override
	public Metadata metadata() {
		return super.metadata().encoding("MP3");
	}

	@Override
	public void open(Metadata metadata) throws IOException {
		checkNotNull(metadata, "metadata must not be null");
		checkState(metadata.encoding().equalsIgnoreCase("PCM"), "source must be PCM-encoded");

		super.open(metadata);
	}

}
