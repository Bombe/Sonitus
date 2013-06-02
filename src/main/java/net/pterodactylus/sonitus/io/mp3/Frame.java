/*
 * Sonitus - Frame.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.io.mp3;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;

/**
 * A single MPEG audio frame.
 * <p/>
 * This uses information from <a href="http://mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm">mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm</a>.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Frame {

	/** The MPEG audio version. */
	public enum MpegAudioVersion {

		/** Verstion 2.5. */
		VERSION_2_5,

		/** Reserved. */
		RESERVED,

		/** Version 2. */
		VERSION_2,

		/** Version 1. */
		VERSION_1

	}

	/** The MPEG layer description. */
	public enum LayerDescription {

		/** Reserved. */
		RESERVED,

		/** Layer III. */
		LAYER_3,

		/** Layer II. */
		LAYER_2,

		/** Layer I. */
		LAYER_1

	}

	/** The channel mode. */
	public enum ChannelMode {

		/* Stereo. */
		STEREO,

		/** Joint stereo. */
		JOINT_STEREO,

		/** Dual-channel stereo. */
		DUAL_CHANNEL,

		/** Single channel, aka mono. */
		SINGLE_CHANNEL

	}

	/** Mode of emphasis. */
	public enum Emphasis {

		/** No emphasis. */
		NONE,

		/** 50/15 ms. */
		_50_15_MS,

		/** Reserved. */
		RESERVED,

		/** CCIT J.17. */
		CCIT_J_17

	}

	/** Bitrate table. */
	private static final Supplier<Map<MpegAudioVersion, Map<LayerDescription, Map<Integer, Integer>>>> bitrateSupplier = Suppliers.memoize(new Supplier<Map<MpegAudioVersion, Map<LayerDescription, Map<Integer, Integer>>>>() {

		@Override
		public Map<MpegAudioVersion, Map<LayerDescription, Map<Integer, Integer>>> get() {
			ImmutableMap.Builder<MpegAudioVersion, Map<LayerDescription, Map<Integer, Integer>>> mpegAudioVersionMapBuilder = ImmutableMap.builder();

			/* MPEG 1. */
			ImmutableMap.Builder<LayerDescription, Map<Integer, Integer>> mpeg1Builder = ImmutableMap.builder();

			/* Layer 1. */
			ImmutableMap.Builder<Integer, Integer> bitrates = ImmutableMap.builder();
			bitrates.put(0, 0).put(1, 32).put(2, 64).put(3, 96).put(4, 128).put(5, 160).put(6, 192).put(7, 224);
			bitrates.put(8, 256).put(9, 288).put(10, 320).put(11, 352).put(12, 384).put(13, 416).put(14, 448).put(15, -1);
			mpeg1Builder.put(LayerDescription.LAYER_1, bitrates.build());

			/* MPEG 1, Layer 2 bitrates. */
			bitrates = ImmutableMap.builder();
			bitrates.put(0, 0).put(1, 32).put(2, 48).put(3, 56).put(4, 64).put(5, 80).put(6, 96).put(7, 112);
			bitrates.put(8, 128).put(9, 160).put(10, 192).put(11, 224).put(12, 256).put(13, 320).put(14, 384).put(15, -1);
			mpeg1Builder.put(LayerDescription.LAYER_2, bitrates.build());

			/* MPEG 1, Layer 3 bitrates. */
			bitrates = ImmutableMap.builder();
			bitrates.put(0, 0).put(1, 32).put(2, 40).put(3, 48).put(4, 56).put(5, 64).put(6, 80).put(7, 96);
			bitrates.put(8, 112).put(9, 128).put(10, 160).put(11, 192).put(12, 224).put(13, 256).put(14, 320).put(15, -1);
			mpeg1Builder.put(LayerDescription.LAYER_3, bitrates.build());
			mpegAudioVersionMapBuilder.put(MpegAudioVersion.VERSION_1, mpeg1Builder.build());

			/* MPEG 2 & 2.5. */
			ImmutableMap.Builder<LayerDescription, Map<Integer, Integer>> mpeg2Builder = ImmutableMap.builder();

			/* Layer 1. */
			bitrates = ImmutableMap.builder();
			bitrates.put(0, 0).put(1, 32).put(2, 48).put(3, 56).put(4, 64).put(5, 80).put(6, 96).put(7, 112);
			bitrates.put(8, 128).put(9, 144).put(10, 160).put(11, 176).put(12, 192).put(13, 224).put(14, 256).put(15, -1);
			mpeg2Builder.put(LayerDescription.LAYER_1, bitrates.build());

			/* Layer 2 & 3. */
			bitrates = ImmutableMap.builder();
			bitrates.put(0, 0).put(1, 8).put(2, 16).put(3, 24).put(4, 32).put(5, 40).put(6, 48).put(7, 56);
			bitrates.put(8, 64).put(9, 80).put(10, 96).put(11, 112).put(12, 128).put(13, 144).put(14, 160).put(15, -1);
			mpeg2Builder.put(LayerDescription.LAYER_2, bitrates.build());
			mpeg2Builder.put(LayerDescription.LAYER_3, bitrates.build());

			mpegAudioVersionMapBuilder.put(MpegAudioVersion.VERSION_2, mpeg2Builder.build());
			mpegAudioVersionMapBuilder.put(MpegAudioVersion.VERSION_2_5, mpeg2Builder.build());

			return mpegAudioVersionMapBuilder.build();
		}
	});

	/** Sampling rate table. */
	private static final Supplier<Map<MpegAudioVersion, Map<Integer, Integer>>> samplingRateSupplier = Suppliers.memoize(new Supplier<Map<MpegAudioVersion, Map<Integer, Integer>>>() {

		@Override
		public Map<MpegAudioVersion, Map<Integer, Integer>> get() {
			ImmutableMap.Builder<MpegAudioVersion, Map<Integer, Integer>> mpegAudioVersions = ImmutableMap.builder();

			/* MPEG 1. */
			ImmutableMap.Builder<Integer, Integer> samplingRates = ImmutableMap.builder();
			samplingRates.put(0, 44100).put(1, 48000).put(2, 32000).put(3, 0);
			mpegAudioVersions.put(MpegAudioVersion.VERSION_1, samplingRates.build());

			/* MPEG 2. */
			samplingRates = ImmutableMap.builder();
			samplingRates.put(0, 22050).put(1, 24000).put(2, 16000).put(3, 0);
			mpegAudioVersions.put(MpegAudioVersion.VERSION_2, samplingRates.build());

			/* MPEG 2.5. */
			samplingRates = ImmutableMap.builder();
			samplingRates.put(0, 11025).put(1, 12000).put(2, 8000).put(3, 0);
			mpegAudioVersions.put(MpegAudioVersion.VERSION_2_5, samplingRates.build());

			return mpegAudioVersions.build();
		}
	});

	/** The decoded MPEG audio version ID. */
	private final int mpegAudioVersionId;

	/** The decoded layer description. */
	private final int layerDescription;

	/** The decoded protection bit. */
	private final int protectionBit;

	/** The decoded bitrate index. */
	private final int bitrateIndex;

	/** The deocded sampling rate frequency index. */
	private final int samplingRateFrequencyIndex;

	/** The decoded padding bit. */
	private final int paddingBit;

	/** The decoded private bit. */
	private final int privateBit;

	/** The decoded channel mode. */
	private final int channelMode;

	/** The deocded mode extension. */
	private final int modeExtension;

	/** The decoded copyright bit. */
	private final int copyrightBit;

	/** The deocded original bit. */
	private final int originalBit;

	/** The decoded emphasis mode. */
	private final int emphasis;

	/** The content of the frame. */
	private final byte[] content;

	/**
	 * Creates a new frame from the given values.
	 *
	 * @param mpegAudioVersionId
	 * 		The MPEG audio version ID
	 * @param layerDescription
	 * 		The layer description
	 * @param protectionBit
	 * 		The protection bit
	 * @param bitrateIndex
	 * 		The bitrate index
	 * @param samplingRateFrequencyIndex
	 * 		The sampling rate frequency index
	 * @param paddingBit
	 * 		The padding bit
	 * @param privateBit
	 * 		The private bit
	 * @param channelMode
	 * 		The channel mode
	 * @param modeExtension
	 * 		The mode extension
	 * @param copyrightBit
	 * 		The copyright bit
	 * @param originalBit
	 * 		The original bit
	 * @param emphasis
	 * 		The emphasis
	 */
	private Frame(int mpegAudioVersionId, int layerDescription, int protectionBit, int bitrateIndex, int samplingRateFrequencyIndex, int paddingBit, int privateBit, int channelMode, int modeExtension, int copyrightBit, int originalBit, int emphasis, byte[] content) {
		this.mpegAudioVersionId = mpegAudioVersionId;
		this.layerDescription = layerDescription;
		this.protectionBit = protectionBit;
		this.bitrateIndex = bitrateIndex;
		this.samplingRateFrequencyIndex = samplingRateFrequencyIndex;
		this.paddingBit = paddingBit;
		this.privateBit = privateBit;
		this.channelMode = channelMode;
		this.modeExtension = modeExtension;
		this.copyrightBit = copyrightBit;
		this.originalBit = originalBit;
		this.emphasis = emphasis;
		this.content = content;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the MPEG audio version.
	 *
	 * @return The MPEG audio version
	 */
	public MpegAudioVersion mpegAudioVersion() {
		return MpegAudioVersion.values()[mpegAudioVersionId];
	}

	/**
	 * Returns the layer description.
	 *
	 * @return The layer description
	 */
	public LayerDescription layerDescription() {
		return LayerDescription.values()[layerDescription];
	}

	/**
	 * Returns the protection bit.
	 *
	 * @return {@code true} if the protection bit is set, {@code false} otherwise
	 */
	public boolean protectionBit() {
		return protectionBit != 0;
	}

	/**
	 * Returns the bitrate of this frame.
	 *
	 * @return The bitrate of this frame (in kbps)
	 */
	public int bitrate() {
		return bitrateSupplier.get().get(mpegAudioVersion()).get(layerDescription()).get(bitrateIndex);
	}

	/**
	 * Returns the sampling rate of the audio data in this frame.
	 *
	 * @return The sample rate (in Hertz)
	 */
	public int samplingRate() {
		return samplingRateSupplier.get().get(mpegAudioVersion()).get(samplingRateFrequencyIndex);
	}

	/**
	 * Returns the padding bit.
	 *
	 * @return {@code true} if the padding bit is set, {@code false} otherwise
	 */
	public boolean paddingBit() {
		return paddingBit != 0;
	}

	/**
	 * Returns the private bit.
	 *
	 * @return {@code true} if the private bit is set, {@code false} otherwise
	 */
	public boolean privateBit() {
		return privateBit != 0;
	}

	/**
	 * Returns the channel mode.
	 *
	 * @return The channel mode
	 */
	public ChannelMode channelMode() {
		return ChannelMode.values()[channelMode];
	}

	/* TODO - mode extension. */

	/**
	 * Returns the copyright bit.
	 *
	 * @return {@code true} if the copyright bit is set, {@code false} otherwise
	 */
	public boolean copyrightBit() {
		return copyrightBit != 0;
	}

	/**
	 * Returns the original bit.
	 *
	 * @return {@code true} if the original bit is set, {@code false} otherwise
	 */
	public boolean originalBit() {
		return originalBit != 0;
	}

	/**
	 * Returns the emphasis.
	 *
	 * @return The emphasis
	 */
	public Emphasis emphasis() {
		return Emphasis.values()[emphasis];
	}

	/**
	 * Returns the content of this frame.
	 *
	 * @return The content of this frame
	 */
	public byte[] content() {
		return content;
	}

	//
	// STATIC METHODS
	//

	/**
	 * Returns whether the data beginning at the given offset is an MPEG audio
	 * frame.
	 *
	 * @param buffer
	 * 		The buffer in which the data is stored
	 * @param offset
	 * 		The beginning of the data to parse
	 * @param length
	 * 		The length of the data to parse
	 * @return {@code true} if the data at the given offset is an MPEG audio frame
	 */
	public static boolean isFrame(byte[] buffer, int offset, int length) {
		return (length > 3) && (((buffer[offset] & 0xff) == 0xff) && ((buffer[offset + 1] & 0xe0) == 0xe0));
	}

	/**
	 * Calculates the frame length in bytes for the frame starting at the given
	 * offset in the given buffer. This method should only be called for a buffer
	 * and an offset for which {@link #isFrame(byte[], int, int)} returns {@code
	 * true}.
	 *
	 * @param buffer
	 * 		The buffer storing the frame
	 * @param offset
	 * 		The offset of the frame
	 * @return The length of the frame in bytes, or {@code -1} if the frame length
	 *         can not be calculated
	 */
	public static int getFrameLength(byte[] buffer, int offset) {
		MpegAudioVersion mpegAudioVersion = MpegAudioVersion.values()[(buffer[offset + 1] & 0x18) >>> 3];
		LayerDescription layerDescription = LayerDescription.values()[(buffer[offset + 1] & 0x06) >>> 1];
		int bitrate = bitrateSupplier.get().get(mpegAudioVersion).get(layerDescription).get((buffer[offset + 2] & 0xf0) >>> 4) * 1000;
		int samplingRate = samplingRateSupplier.get().get(mpegAudioVersion).get((buffer[offset + 2] & 0x0c) >>> 2);
		int paddingBit = (buffer[offset + 2] & 0x02) >>> 1;
		if (layerDescription == LayerDescription.LAYER_1) {
			return (12 * bitrate / samplingRate + paddingBit) * 4;
		} else if ((layerDescription == LayerDescription.LAYER_2) || (layerDescription == LayerDescription.LAYER_3)) {
			return 144 * bitrate / samplingRate + paddingBit;
		}
		return -1;
	}

	/**
	 * Tries to create an MPEG audio from the given data.
	 *
	 * @param buffer
	 * 		The buffer in which the data is stored
	 * @param offset
	 * 		The offset at which to look for a frame
	 * @param length
	 * 		The length of the data in the buffer
	 * @return The frame if it could be parsed, or {@link Optional#absent()} if no
	 *         frame could be found
	 */
	public static Optional<Frame> create(byte[] buffer, int offset, int length) {
		if (isFrame(buffer, offset, length)) {
			int mpegAudioVersionId = (buffer[offset + 1] & 0x18) >>> 3;
			int layerDescription = (buffer[offset + 1] & 0x06) >>> 1;
			int protectionBit = buffer[offset + 1] & 0x01;
			int bitrateIndex = (buffer[offset + 2] & 0xf0) >>> 4;
			int samplingRateFrequencyIndex = (buffer[offset + 2] & 0x0c) >>> 2;
			int paddingBit = (buffer[offset + 2] & 0x02) >>> 1;
			int privateBit = buffer[offset + 2] & 0x01;
			int channelMode = (buffer[offset + 3] & 0xc0) >> 6;
			int modeExtension = (buffer[offset + 3] & 0x60) >> 4;
			int copyright = (buffer[offset + 3] & 0x08) >> 3;
			int original = (buffer[offset + 3] & 0x04) >> 2;
			int emphasis = buffer[offset + 3] & 0x03;
			int frameLength = getFrameLength(buffer, offset);
			return Optional.of(new Frame(mpegAudioVersionId, layerDescription, protectionBit, bitrateIndex, samplingRateFrequencyIndex, paddingBit, privateBit, channelMode, modeExtension, copyright, original, emphasis, Arrays.copyOfRange(buffer, 4, frameLength)));
		}
		return Optional.absent();
	}

}
