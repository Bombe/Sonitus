/*
 * Sonitus - ContentMetadata.java - Copyright © 2013 David Roden
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

import java.util.Arrays;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * The part of the {@link Metadata} that contains information about the content
 * of a {@link Source}, such as the name of the track, the artist, or other
 * information.
 * <p/>
 * Content metadata also contains a “title” which is an amalgamation of all
 * information in the content metadata. If not given, it will be automatically
 * constructed from all other information. If can also be specified manually to
 * override the default.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ContentMetadata {

	/** The artist. */
	private final Optional<String> artist;

	/** The name. */
	private final Optional<String> name;

	/** The all-in-one title. */
	private final String title;

	/** The comment. */
	private final Optional<String> comment;

	/** Creates empty content metadata. */
	public ContentMetadata() {
		this("");
	}

	/**
	 * Creates content metadata containing the given title.
	 *
	 * @param title
	 * 		The title of the metadata
	 * @throws NullPointerException
	 * 		if {@code title} is {@code null}
	 */
	public ContentMetadata(String title) throws NullPointerException {
		this(null, null, title, null);
	}

	/**
	 * Creates content metadata.
	 *
	 * @param artist
	 * 		The artist of the track
	 * @param name
	 * 		The name of the track
	 */
	public ContentMetadata(String artist, String name) {
		this(artist, name, joinStrings(artist, name), null);
	}

	/**
	 * Creates content metadata.
	 *
	 * @param artist
	 * 		The artist of the track (may be null)
	 * @param name
	 * 		The name of the track (may be null)
	 * @param title
	 * 		The title of the track
	 * @param comment
	 * 		The comment of the track
	 * @throws NullPointerException
	 * 		if {@code title} is {@code null}
	 */
	private ContentMetadata(String artist, String name, String title, String comment) throws NullPointerException {
		this.artist = Optional.fromNullable(artist);
		this.name = Optional.fromNullable(name);
		this.title = Preconditions.checkNotNull(title, "title must not be null");
		this.comment = Optional.fromNullable(comment);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the artist of the track, if it has been set.
	 *
	 * @return The artist of the track
	 */
	public Optional<String> artist() {
		return artist;
	}

	/**
	 * Returns the name of the track, if it has been set.
	 *
	 * @return The name of the track
	 */
	public Optional<String> name() {
		return name;
	}

	/**
	 * Returns the title of the track.
	 *
	 * @return The title of the track
	 */
	public String title() {
		return title;
	}

	/**
	 * Returns the comment of the track, if it has been set.
	 *
	 * @return The comment of the track
	 */
	public Optional<String> comment() {
		return comment;
	}

	//
	// ACTIONS
	//

	/**
	 * Creates new content metadata that is a copy of this content metadata but
	 * with the artist changed. The title will be reconstructed from the new artist
	 * and the existing name.
	 *
	 * @param artist
	 * 		The new artist
	 * @return The new content metadata
	 */
	public ContentMetadata artist(String artist) {
		return new ContentMetadata(artist, name().orNull(), joinStrings(artist, name().orNull()), comment.orNull());
	}

	/**
	 * Creates new content metadata that is a copy of this content metadata but
	 * with the name changed. The title will be reconstructed from the existing
	 * artist and the new name.
	 *
	 * @param name
	 * 		The new name
	 * @return The new content metadata
	 */
	public ContentMetadata name(String name) {
		return new ContentMetadata(artist().orNull(), name, joinStrings(artist().orNull(), name), comment.orNull());
	}

	/**
	 * Creates new content metadata that is a copy of this content metadata but
	 * with the title changed.
	 *
	 * @param title
	 * 		The new title
	 * @return The new content metadata
	 */
	public ContentMetadata title(String title) {
		return new ContentMetadata(artist().orNull(), name().orNull(), title, comment.orNull());
	}

	/**
	 * Creates new content metadata that is a copy of this content metadata but
	 * with the comment changed.
	 *
	 * @param comment
	 * 		The comment
	 * @return The new content metadata
	 */
	public ContentMetadata comment(String comment) {
		return new ContentMetadata(artist().orNull(), name().orNull(), title(), comment);
	}

	/**
	 * Returns whether this content metadata object equals the given object if the
	 * comments of this and the given object are ignored.
	 *
	 * @param object
	 * 		The object to compare to this one
	 * @return {@code true} if the given object and this object are equal if the
	 *         comments are ignored, {@code false} otherwise
	 */
	public boolean equalsIgnoreComment(Object object) {
		if (!(object instanceof ContentMetadata)) {
			return false;
		}
		ContentMetadata contentMetadata = (ContentMetadata) object;
		return artist().equals(contentMetadata.artist()) && name().equals(contentMetadata.name()) && title().equals(contentMetadata.title());
	}

	//
	// OBJECT METHODS
	//

	@Override
	public int hashCode() {
		return artist().hashCode() ^ name().hashCode() ^ title().hashCode() ^ comment().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ContentMetadata)) {
			return false;
		}
		ContentMetadata contentMetadata = (ContentMetadata) object;
		return artist().equals(contentMetadata.artist()) && name().equals(contentMetadata.name()) && title().equals(contentMetadata.title()) && comment().equals(contentMetadata.comment());
	}

	@Override
	public String toString() {
		return String.format("%s%s", title(), comment().isPresent() ? String.format(" (%s)", comment().get()) : "");
	}

	//
	// STATIC METHODS
	//

	/**
	 * Joins the given strings, concatenating them with “ - ” and ignoring {@code
	 * null} values.
	 *
	 * @param strings
	 * 		The strings to join
	 * @return The joined strings
	 */
	private static String joinStrings(String... strings) {
		return Joiner.on(" - ").skipNulls().join(Arrays.asList(strings));
	}

}
