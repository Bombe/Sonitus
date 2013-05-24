/*
 * Sonitus - AudioSink.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data.sink;

import java.io.IOException;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.Sink;
import net.pterodactylus.sonitus.data.Source;

import com.google.common.base.Preconditions;

/**
 * {@link Sink} implementation that uses the JDK’s {@link AudioSystem} to play
 * all {@link Source}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class AudioSink implements Sink {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(AudioSink.class.getName());

	/** The current metadata. */
	private Metadata metadata;

	/** The audio output. */
	private SourceDataLine sourceDataLine;

	//
	// SINK METHODS
	//

	@Override
	public void open(Metadata metadata) throws IOException {
		Preconditions.checkArgument(metadata.encoding().equalsIgnoreCase("PCM"), "source must be PCM-encoded");
		AudioFormat audioFormat = new AudioFormat(metadata.frequency(), 16, metadata.channels(), true, false);
		try {
			sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
			sourceDataLine.open(audioFormat);
			sourceDataLine.start();
		} catch (LineUnavailableException e) {
			/* TODO */
			throw new IOException(e);
		}
	}

	@Override
	public void close() {
		sourceDataLine.stop();
		sourceDataLine.close();
	}

	@Override
	public void metadataUpdated(Metadata metadata) {
		/* ignore. */
	}

	@Override
	public void process(byte[] buffer) {
		sourceDataLine.write(buffer, 0, buffer.length);
		logger.finest(String.format("AudioSink: Wrote %d Bytes.", buffer.length));
	}

}
