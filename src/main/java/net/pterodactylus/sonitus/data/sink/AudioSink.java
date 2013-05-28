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

import static javax.sound.sampled.BooleanControl.Type.MUTE;
import static javax.sound.sampled.FloatControl.Type.VOLUME;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import net.pterodactylus.sonitus.data.AbstractControlledComponent;
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
public class AudioSink extends AbstractControlledComponent implements Sink {

	/** The logger. */
	private static final Logger logger = Logger.getLogger(AudioSink.class.getName());

	/** The volume fader. */
	private final Fader volumeFader;

	/** The “mute” switch. */
	private final Switch muteSwitch;

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
		super("Audio Output");
		volumeFader = new Fader("Volume") {

			@Override
			protected void valueSet(Double value) {
				/* search for preferred volume control. */
				FloatControl volumeControl = getVolumeControl(sourceDataLine);
				if (volumeControl == null) {
					/* could not find volume control! */
					return;
				}

				volumeControl.setValue((float) (value * volumeControl.getMaximum()));
			}
		};
		muteSwitch = new Switch("Mute") {

			/** The previous value in case we have to emulate the mute control. */
			private float previousValue;

			@Override
			protected void valueSet(Boolean value) {
				/* search for mute control. */
				BooleanControl muteControl = getMuteControl(sourceDataLine);
				if (muteControl != null) {
					muteControl.setValue(value);
					return;
				}

				/* could not find mute control, use volume control! */
				FloatControl volumeControl = getVolumeControl(sourceDataLine);
				if (volumeControl == null) {
					/* no volume control, either? */
					return;
				}

				if (value) {
					previousValue = volumeControl.getValue();
					volumeControl.setValue(0);
				} else {
					volumeControl.setValue(previousValue);
				}
			}

		};
	}

	//
	// CONTROLLED METHODS
	//

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
			metadataUpdated(metadata);
		} catch (LineUnavailableException e) {
			/* TODO */
			sourceDataLine = null;
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
		super.metadataUpdated(metadata);
		logger.fine(String.format("Now playing %s.", metadata));
	}

	@Override
	public void process(byte[] buffer) throws IOException {
		sourceDataLineOutputStream.write(buffer);
		logger.finest(String.format("AudioSink: Wrote %d Bytes.", buffer.length));
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Returns the {@link FloatControl.Type.VOLUME} control.
	 *
	 * @param dataLine
	 * 		The data line to search for the control
	 * @return The control, or {@code null} if no volume control could be found
	 */
	private static FloatControl getVolumeControl(DataLine dataLine) {
		return getControl(dataLine, VOLUME, FloatControl.class);
	}

	/**
	 * Returns the {@link BooleanControl.Type.MUTE} control.
	 *
	 * @param dataLine
	 * 		The data line to search for the control
	 * @return The control, or {@code null} if no mute control could be found
	 */
	private static BooleanControl getMuteControl(DataLine dataLine) {
		return getControl(dataLine, MUTE, BooleanControl.class);
	}

	/**
	 * Searches the given data line for a control of the given type and returns it.
	 * If the given data line is {@code null}, {@code null} is returned.
	 *
	 * @param dataLine
	 * 		The data line to search for a control
	 * @param controlType
	 * 		The type of the control to search
	 * @param controlClass
	 * 		The class of the control
	 * @param <T>
	 * 		The class of the control
	 * @return The control, or {@code null} if no control could be found
	 */
	private static <T> T getControl(DataLine dataLine, Control.Type controlType, Class<T> controlClass) {
		if (dataLine == null) {
			return null;
		}
		Control[] controls = dataLine.getControls();
		for (Control control : controls) {
			if (control.getType().equals(controlType)) {
				return (T) control;
			}
		}
		return null;
	}

}
