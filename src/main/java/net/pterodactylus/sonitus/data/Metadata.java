/*
 * Sonitus - Metainfo.java - Copyright © 2013 David Roden
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

import com.google.common.base.Optional;

/**
 * Metadata contains information about a source, e.g. the name of the content,
 * the artist performing it, dates, comments, URLs, etc. The {@link Format},
 * however, is not part of the metadata because a {@link Source} already exposes
 * it.
 * <p/>
 * Metadata, once created, is immutable.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Metadata {

	/** The artist performing the content. */
	private final Optional<String> artist;

	/** The name of the content. */
	private final Optional<String> name;

	/** Creates empty metadata. */
	public Metadata() {
		this(null, null);
	}

	/**
	 * Creates metadata with the given attributes.
	 *
	 * @param artist
	 * 		The artist performing the content (may be {@code null})
	 * @param name
	 * 		The name of the content (may be {@code null})
	 */
	private Metadata(String artist, String name) {
		this.artist = Optional.fromNullable(artist);
		this.name = Optional.fromNullable(name);
	}

	/**
	 * Returns the artist, if any.
	 *
	 * @return The artist, or {@link Optional#absent()}
	 */
	public Optional<String> artist() {
		return artist;
	}

	/**
	 * Returns new metadata with the same attributes as this metadata, except for
	 * the artist.
	 *
	 * @param artist
	 * 		The new artist
	 * @return New metadata with a changed artist
	 */
	public Metadata artist(String artist) {
		return new Metadata(artist, this.artist.orNull());
	}

	/**
	 * Returns the name of the content, if any.
	 *
	 * @return The name, or {@link Optional#absent()}
	 */
	public Optional<String> name() {
		return name;
	}

	/**
	 * Returns new metadata with the same attributes as this metadata, except for
	 * the name.
	 *
	 * @param name
	 * 		The new name
	 * @return New metadata with a changed name
	 */
	public Metadata name(String name) {
		return new Metadata(name, this.name.orNull());
	}

}
