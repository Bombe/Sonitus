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

import static javax.sound.sampled.FloatControl.Type.VOLUME;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import net.pterodactylus.sonitus.data.Controller;
import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.data.Sink;
import net.pterodactylus.sonitus.data.Source;
import net.pterodactylus.sonitus.data.controller.Fader;
import net.pterodactylus.sonitus.data.controller.Switch;
import net.pterodactylus.sonitus.io.IntegralWriteOutputStream;

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

	/** The volume fader. */
	private final Fader volumeFader;

	/** The “mute” switch. */
	private final Switch muteSwitch;

	/** The current metadata. */
	private Metadata metadata;

	/** The audio output. */
	private SourceDataLine sourceDataLine;

	/** A buffered output stream to ensure correct writing to the source data line. */
	private OutputStream sourceDataLineOutputStream = new IntegralWriteOutputStream(new OutputStream() {

		@Override
		public void write(int b) throws IOException {
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			if (sourceDataLine != null) {
				sourceDataLine.write(b, off, len);
			}
		}
	}, 1024);

	/** Creates a new audio sink. */
	public AudioSink() {
		volumeFader = new Fader("Volume") {

			@Override
			protected void valueSet(Double value) {
				if (sourceDataLine != null) {
					FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(VOLUME);
					volumeControl.setValue((float) (value * volumeControl.getMaximum()));
				}
			}
		};
		muteSwitch = new Switch("Mute") {

			private float previousValue;

			@Override
			protected void valueSet(Boolean value) {
				if (sourceDataLine != null) {
					FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(VOLUME);
					if (value) {
						previousValue = volumeControl.getValue();
						volumeControl.setValue(0);
					} else {
						volumeControl.setValue(previousValue);
					}
				}
			}
		};
	}

	//
	// CONTROLLED METHODS
	//

	@Override
	public String name() {
		return "Audio Output";
	}

	@Override
	public Metadata metadata() {
		return metadata;
	}

	@Override
	public List<Controller<?>> controllers() {
		return Arrays.<Controller<?>>asList(volumeFader, muteSwitch);
	}

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
		logger.info(String.format("Now playing %s.", metadata));
		this.metadata = metadata;
	}

	@Override
	public void process(byte[] buffer) throws IOException {
		sourceDataLineOutputStream.write(buffer);
		logger.finest(String.format("AudioSink: Wrote %d Bytes.", buffer.length));
	}

}
