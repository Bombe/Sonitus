/*
 * Sonitus - MetadataStream.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.io;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Map;

import net.pterodactylus.sonitus.data.ContentMetadata;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

/**
 * Wrapper around an {@link InputStream} that can separate metadata out of
 * icecast audio streams.
 * <p/>
 * {@link #read(byte[])} and {@link #read(byte[], int, int)} are implemented
 * using {@link #read()} so wrapping the underlying stream into a {@link
 * BufferedInputStream} is highly recommended.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MetadataStream extends FilterInputStream {

	/** The UTF-8 charset. */
	private static final Charset utf8Charset = Charset.forName("UTF-8");

	/** The interval of the metadata blocks. */
	private final int metadataInterval;

	/** How many bytes of stream are left before a metadata block is expected. */
	private int streamRemaining;

	/** The last parsed metadata. */
	private Optional<ContentMetadata> contentMetadata = Optional.absent();

	/**
	 * Creates a new metadata stream.
	 *
	 * @param inputStream
	 * 		The input stream to parse metadata out of
	 * @param metadataInterval
	 * 		The interval at which metadata blocks are weaved into the stream
	 */
	public MetadataStream(InputStream inputStream, int metadataInterval) {
		super(inputStream);
		this.metadataInterval = metadataInterval;
		this.streamRemaining = metadataInterval;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the last parsed content metadata of this stream.
	 *
	 * @return The last parsed content metadata
	 */
	public Optional<ContentMetadata> getContentMetadata() {
		return contentMetadata;
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Parses the metadata from the given byte array.
	 *
	 * @param metadataBuffer
	 * @return The parsed metadata, or {@link Optional#absent()} if the metadata
	 *         could not be parsed
	 */
	private static Optional<ContentMetadata> parseMetadata(byte[] metadataBuffer) {

		/* the byte array may be padded with NULs. */
		int realLength = metadataBuffer.length;
		while ((realLength > -1) && (metadataBuffer[realLength - 1] == 0)) {
			realLength--;
		}

		try {

			/* decode the byte array as a UTF-8 string. */
			CharsetDecoder utf8Decoder = utf8Charset.newDecoder();
			utf8Decoder.onMalformedInput(CodingErrorAction.REPORT);
			CharBuffer decodedBuffer = CharBuffer.allocate(realLength);
			CoderResult utf8Result = utf8Decoder.decode(ByteBuffer.wrap(metadataBuffer, 0, realLength), decodedBuffer, true);
			utf8Decoder.flush(decodedBuffer);

			/* use latin-1 as fallback if decoding as UTF-8 failed. */
			String metadataString;
			if (utf8Result.isMalformed()) {
				metadataString = new String(metadataBuffer, 0, realLength, "ISO8859-1");
			} else {
				metadataString = decodedBuffer.flip().toString();
			}
			int currentOffset = 0;

			/* metadata has the form of key='value'[;key='value'[…]] */
			Map<String, String> metadataAttributes = Maps.newHashMap();
			while (currentOffset < metadataString.length()) {
				int equalSign = metadataString.indexOf('=', currentOffset);
				if (equalSign == -1) {
					break;
				}
				String key = metadataString.substring(currentOffset, equalSign);
				int semicolon = metadataString.indexOf(';', equalSign);
				if (semicolon == -1) {
					break;
				}
				String value = metadataString.substring(equalSign + 1, semicolon);
				if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\""))) {
					value = value.substring(1, value.length() - 1);
				}
				metadataAttributes.put(key, value);
				currentOffset = semicolon + 1;
			}

			if (!metadataAttributes.containsKey("StreamTitle")) {
				return Optional.absent();
			}

			return Optional.of(new ContentMetadata(metadataAttributes.get("StreamTitle")));

		} catch (UnsupportedEncodingException uee1) {
			/* should never happen. */
			throw new RuntimeException("UTF-8 not supported");
		}
	}

	//
	// INPUTSTREAM METHODS
	//

	@Override
	public int read() throws IOException {
		int data = super.read();
		if (data == -1) {
			return -1;
		}
		if (streamRemaining > 0) {
			--streamRemaining;
		} else if (data == 0) {
			/* 0-byte metadata follows, ignore. */
			streamRemaining = metadataInterval - 1;
			data = super.read();
		} else {
			/* loop until we’ve read all metadata. */
			byte[] metadataBuffer = new byte[data * 16];
			int metadataPosition = 0;
			do {
				int metadataByte = super.read();
				if (metadataByte == -1) {
					return -1;
				}
				metadataBuffer[metadataPosition++] = (byte) metadataByte;
				if (metadataPosition == metadataBuffer.length) {
					/* parse metadata. */
					Optional<ContentMetadata> parsedMetadata = parseMetadata(metadataBuffer);
					if (parsedMetadata.isPresent()) {
						contentMetadata = parseMetadata(metadataBuffer);
					}
					/* reset metadata buffer and position. */
					metadataBuffer = null;
					metadataPosition = 0;
					/* we read one more byte after the loop. */
					streamRemaining = metadataInterval - 1;
				}
			} while (metadataBuffer != null);
			data = super.read();
		}
		return data;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		for (int index = offset; index < (offset + length); ++index) {
			int data = read();
			if (data == -1) {
				return (index > offset) ? (index - offset) : -1;
			}
			buffer[index] = (byte) data;
		}
		return length;
	}

}
