/*
 * Sonitus - ExternalFilter.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sonitus.data.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import net.pterodactylus.sonitus.data.Metadata;
import net.pterodactylus.sonitus.io.InputStreamDrainer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * {@link net.pterodactylus.sonitus.data.Filter} implementation that runs its
 * {@link net.pterodactylus.sonitus.data.Source} through an external program.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class ExternalFilter extends BasicFilter {

	/** The logger. */
	private final Logger logger = Logger.getLogger(getClass().getName());

	/** The external process. */
	private Process process;

	/**
	 * Creates a new external filter with the given name.
	 *
	 * @param name
	 * 		The name of the filter
	 */
	protected ExternalFilter(String name) {
		super(name);
	}

	//
	// FILTER METHODS
	//

	@Override
	public void open(Metadata metadata) throws IOException {
		process = Runtime.getRuntime().exec(Iterables.toArray(ImmutableList.<String>builder().add(binary(metadata)).addAll(parameters(metadata)).build(), String.class));
		InputStream processError = process.getErrorStream();
		new Thread(new InputStreamDrainer(processError)).start();
		super.open(metadata);
	}

	@Override
	public void close() {
		process.destroy();
	}

	//
	// BASICFILTER METHODS
	//

	@Override
	protected InputStream createInputStream() throws IOException {
		return process.getInputStream();
	}

	@Override
	protected OutputStream createOutputStream() throws IOException {
		return process.getOutputStream();
	}

	//
	// SUBCLASS METHODS
	//

	/**
	 * Returns the location of the binary to execute.
	 *
	 * @param metadata
	 * 		The metadata being processed
	 * @return The location of the binary to execute
	 */
	protected abstract String binary(Metadata metadata);

	/**
	 * Returns the parameters for the binary.
	 *
	 * @param metadata
	 * 		The metadata being processed
	 * @return The parameters for the binary
	 */
	protected abstract Iterable<String> parameters(Metadata metadata);

}
