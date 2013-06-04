/*
 * Sonitus - StreamSource.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data.source;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.AbstractFilter;
import net.pterodactylus.sonitus.data.ContentMetadata;
import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.FormatMetadata;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.io.MetadataStream;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

/**
 * {@link Source} implementation that can download an audio stream from a
 * streaming server.
 * <p/>
 * Currently only “audio/mpeg” (aka MP3) streams are supported.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class StreamSource extends AbstractFilter {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(StreamSource.class.getName());

	/** The URL of the stream. */
	private final String streamUrl;

	/** The name of the station. */
	private final String streamName;

	/** The metadata stream. */
	private final MetadataStream metadataStream;

	/**
	 * Creates a new stream source. This will also connect to the server and parse
	 * the response header for vital information (sampling frequency, number of
	 * channels, etc.).
	 *
	 * @param eventBus
	 * 		The event bus
	 * @param streamUrl
	 * 		The URL of the stream
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	public StreamSource(String streamUrl) throws IOException {
		super(null);
		this.streamUrl = streamUrl;
		URL url = new URL(streamUrl);

		/* set up connection. */
		URLConnection urlConnection = url.openConnection();
		if (!(urlConnection instanceof HttpURLConnection)) {
			throw new IllegalArgumentException("Not an HTTP URL!");
		}
		HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;
		httpUrlConnection.setRequestProperty("ICY-Metadata", "1");

		/* connect. */
		logger.info(String.format("Connecting to %s...", streamUrl));
		httpUrlConnection.connect();

		/* check content type. */
		String contentType = httpUrlConnection.getContentType();
		if (!contentType.startsWith("audio/mpeg")) {
			throw new IllegalArgumentException("Not an MP3 stream!");
		}

		/* get ice-audio-info header. */
		String iceAudioInfo = httpUrlConnection.getHeaderField("ICE-Audio-Info");
		if (iceAudioInfo == null) {
			throw new IllegalArgumentException("No ICE Audio Info!");
		}

		/* parse ice-audio-info header. */
		String[] audioInfos = iceAudioInfo.split(";");
		Map<String, Integer> audioParameters = Maps.newHashMap();
		for (String audioInfo : audioInfos) {
			String key = audioInfo.substring(0, audioInfo.indexOf('=')).toLowerCase();
			int value = Ints.tryParse(audioInfo.substring(audioInfo.indexOf('=') + 1));
			audioParameters.put(key, value);
		}

		/* check metadata interval. */
		String metadataIntervalHeader = httpUrlConnection.getHeaderField("ICY-MetaInt");
		if (metadataIntervalHeader == null) {
			throw new IllegalArgumentException("No Metadata Interval header!");
		}
		Integer metadataInterval = Ints.tryParse(metadataIntervalHeader);
		if (metadataInterval == null) {
			throw new IllegalArgumentException(String.format("Invalid Metadata Interval header: %s", metadataIntervalHeader));
		}

		metadataUpdated(new Metadata(new FormatMetadata(audioParameters.get("ice-channels"), audioParameters.get("ice-samplerate"), "MP3"), new ContentMetadata()));
		metadataStream = new MetadataStream(new BufferedInputStream(httpUrlConnection.getInputStream()), metadataInterval);
		streamName = httpUrlConnection.getHeaderField("ICY-Name");
	}

	//
	// FILTER METHODS
	//

	@Override
	public String name() {
		return streamName;
	}

	@Override
	public List<Controller<?>> controllers() {
		return Collections.emptyList();
	}

	@Override
	public Metadata metadata() {
		Optional<ContentMetadata> streamMetadata = metadataStream.getContentMetadata();
		if (!streamMetadata.isPresent()) {
			return super.metadata();
		}
		metadataUpdated(super.metadata().title(streamMetadata.get().title()));
		return super.metadata();
	}

	@Override
	public byte[] get(int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		metadataStream.read(buffer);
		return buffer;
	}

	//
	// OBJECT METHODS
	//

	@Override
	public String toString() {
		return String.format("StreamSource(%s,%s)", streamUrl, metadata());
	}

}
