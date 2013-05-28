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
 * Metadata contains information about a source, e.g. the number of channels,
 * the frequency, the encoding, the name of the content, the artist performing
 * it, dates, comments, URLs, etc.
 * <p/>
 * Metadata, once created, is immutable.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Metadata {

	/** The format metadata. */
	private final FormatMetadata formatMetadata;

	/** The content metadata. */
	private final ContentMetadata contentMetadata;

	/** Creates empty metadata. */
	public Metadata() {
		this(new FormatMetadata(), new ContentMetadata());
	}

	/**
	 * Creates metadata from the given format and content metadata.
	 *
	 * @param formatMetadata
	 * 		The format metadata
	 * @param contentMetadata
	 * 		The content metadata
	 */
	public Metadata(FormatMetadata formatMetadata, ContentMetadata contentMetadata) {
		this.formatMetadata = formatMetadata;
		this.contentMetadata = contentMetadata;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the embedded format metadata.
	 *
	 * @return The format metadata
	 */
	public FormatMetadata format() {
		return formatMetadata;
	}

	/**
	 * Returns the embedded content metadata.
	 *
	 * @return The content metadata
	 */
	public ContentMetadata content() {
		return contentMetadata;
	}

	/**
	 * Returns the number of channels of this metadata.
	 *
	 * @return The number of channels of this metadata
	 */
	public int channels() {
		return formatMetadata.channels();
	}

	/**
	 * Returns a metadata with the same parameters as this metadata and the given
	 * number of channels.
	 *
	 * @param channels
	 * 		The new number of channels
	 * @return A new metadata with the given number of channels
	 */
	public Metadata channels(int channels) {
		return new Metadata(formatMetadata.channels(channels), contentMetadata);
	}

	/**
	 * Returns the sampling frequency of this metadata.
	 *
	 * @return The sampling frequency of this metadata
	 */
	public int frequency() {
		return formatMetadata.frequency();
	}

	/**
	 * Returns a new metadata with the same parameters as this metadata and the
	 * given frequency.
	 *
	 * @param frequency
	 * 		The new frequency
	 * @return A new metadata with the given frequency
	 */
	public Metadata frequency(int frequency) {
		return new Metadata(formatMetadata.frequency(frequency), contentMetadata);
	}

	/**
	 * Returns the encoding of this metadata
	 *
	 * @return The encoding of this metadata
	 */
	public String encoding() {
		return formatMetadata.encoding();
	}

	/**
	 * Returns a new metadata with the same parameters as this metadata and the
	 * given encoding.
	 *
	 * @param encoding
	 * 		The new encoding
	 * @return A new metadata with the given encoding
	 */
	public Metadata encoding(String encoding) {
		return new Metadata(formatMetadata.encoding(encoding), contentMetadata);
	}

	/**
	 * Returns the artist, if any.
	 *
	 * @return The artist, or {@link Optional#absent()}
	 */
	public Optional<String> artist() {
		return contentMetadata.artist();
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
		return new Metadata(formatMetadata, contentMetadata.artist(artist));
	}

	/**
	 * Returns the name of the content, if any.
	 *
	 * @return The name, or {@link Optional#absent()}
	 */
	public Optional<String> name() {
		return contentMetadata.name();
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
		return new Metadata(formatMetadata, contentMetadata.name(name));
	}

	/**
	 * Returns the title of the content.
	 *
	 * @return The title of the content
	 */
	public String title() {
		return contentMetadata.title();
	}

	/**
	 * Returns new metadata with the same attributes as this metadata but with the
	 * title changed to the given title.
	 *
	 * @param title
	 * 		The new title
	 * @return The new metadata
	 */
	public Metadata title(String title) {
		return new Metadata(formatMetadata, contentMetadata.title(title));
	}

	/**
	 * Returns the comment of the content, if any.
	 *
	 * @return The comment of the content
	 */
	public Optional<String> comment() {
		return contentMetadata.comment();
	}

	/**
	 * Returns new metadata with the same attributes as this metadata but with the
	 * comment changed to the given comment.
	 *
	 * @param comment
	 * 		The new comment
	 * @return The new metadata
	 */
	public Metadata comment(String comment) {
		return new Metadata(formatMetadata, contentMetadata.comment(comment));
	}

	/**
	 * Returns the title with the comment appended in parantheses, if a comment has
	 * been set.
	 *
	 * @return The title with the comment appended
	 */
	public String fullTitle() {
		return String.format("%s%s", title(), comment().isPresent() ? String.format(" (%s)", comment().get()) : "");
	}

	//
	// OBJECT METHODS
	//

	@Override
	public int hashCode() {
		return formatMetadata.hashCode() ^ contentMetadata.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Metadata)) {
			return false;
		}
		Metadata metadata = (Metadata) object;
		return formatMetadata.equals(metadata.formatMetadata) && contentMetadata.equals(metadata.contentMetadata);
	}

	@Override
	public String toString() {
		return String.format("%s%s%s", formatMetadata, contentMetadata.toString().length() > 0 ? ": " : "", contentMetadata);
	}

}
