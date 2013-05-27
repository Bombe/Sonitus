/*
 * Sonitus - SonitusConfiguration.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Sonitus instance configuration.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
@XStreamAlias("sonitus")
public class SonitusConfiguration {

	/** The executables configuration. */
	@XStreamAlias("executables")
	private ExecutableConfiguration executableConfiguration;

	/**
	 * Returns the executable configuration.
	 *
	 * @return The executable configuration
	 */
	public ExecutableConfiguration getExecutableConfiguration() {
		return executableConfiguration;
	}

	/**
	 * Configuration for the locations of the various native executables used by
	 * Sonitus.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class ExecutableConfiguration {

		/** The location of the LAME binary. */
		@XStreamAlias("lame")
		private String lameLocation;

		/** The location of the oggdec binary. */
		@XStreamAlias("oggdec")
		private String oggdecLocation;

		/** The location of the oggenc binary. */
		@XStreamAlias("oggenc")
		private String oggencLocation;

		/** The location of the flac binary. */
		@XStreamAlias("flac")
		private String flacLocation;

		/** The location of the sox binary. */
		@XStreamAlias("sox")
		private String soxLocation;

		/**
		 * Returns the location of the LAME binary.
		 *
		 * @return The location of the LAME binary
		 */
		public String getLameLocation() {
			return lameLocation;
		}

		/**
		 * Returns the location of the oggdec binary.
		 *
		 * @return The location of the oggdec binary
		 */
		public String getOggdecLocation() {
			return oggdecLocation;
		}

		/**
		 * Returns the location of the oggenc binary.
		 *
		 * @return The location of the oggenc binary
		 */
		public String getOggencLocation() {
			return oggencLocation;
		}

		/**
		 * Returns the location of the flac binary.
		 *
		 * @return The location of the flac binary
		 */
		public String getFlacLocation() {
			return flacLocation;
		}

		/**
		 * Returns the location of the sox binary.
		 *
		 * @return The location of the sox binary
		 */
		public String getSoxLocation() {
			return soxLocation;
		}

	}

}
