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

import static com.google.common.base.Preconditions.*;

import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import net.pterodactylus.sonitus.data.ConnectException;
import net.pterodactylus.sonitus.data.Connection;
import net.pterodactylus.sonitus.data.Format;
import net.pterodactylus.sonitus.data.Sink;
import net.pterodactylus.sonitus.data.Source;

/**
 * {@link Sink} implementation that uses the JDK’s {@link javax.sound.sampled.AudioSystem} to play all {@link
 * net.pterodactylus.sonitus.data.Source}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class AudioSink implements Sink {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(AudioSink.class.getName());

	@Override
	public void connect(Source source) throws ConnectException {
		checkNotNull(source, "source must not be null");
		checkState(source.format().encoding().equalsIgnoreCase("PCM"), "source must be PCM-encoded");

		final Format sourceFormat = source.format();
		AudioFormat audioFormat = new AudioFormat(sourceFormat.frequency(), 16, sourceFormat.channels(), true, false);
		try {
			final SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
			sourceDataLine.open(audioFormat);
			sourceDataLine.start();
			new Thread(new Connection(source) {
				@Override
				protected int bufferSize() {
					return sourceFormat.channels() * sourceFormat.frequency() * 2;
				}

				@Override
				protected void feed(byte[] buffer) {
					sourceDataLine.write(buffer, 0, buffer.length);
					logger.finest(String.format("AudioSink: Wrote %d Bytes.", buffer.length));
				}

				@Override
				protected void finish() {
					sourceDataLine.stop();
				}
			}).start();
		}
		catch (LineUnavailableException lue1) {
			throw new ConnectException(lue1);
		}
	}

}
