/*
 * Sonitus - RememberingInputStreamTest.java - Copyright © 2013 David Roden
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

import com.google.common.io.ByteStreams;
import org.testng.annotations.Test;

/**
 * Test case for {@link RememberingInputStream}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class RememberingInputStreamTest {

	/**
	 * Tests {@link RememberingInputStream#remembered()}.
	 *
	 * @throws IOException
	 * 		if an I/O error occurs
	 */
	@Test
	public void test() throws IOException {
		RememberingInputStream rememberingInputStream;
		byte[] randomData = generateData(System.currentTimeMillis(), 1048576);
		InputStream inputStream = new ByteArrayInputStream(randomData);
		byte[] readBytes;

		rememberingInputStream = new RememberingInputStream(inputStream);
		readBytes = new byte[524288];
		ByteStreams.readFully(rememberingInputStream, readBytes);
		assertThat(readBytes, is(Arrays.copyOfRange(randomData, 0, 524288)));

		rememberingInputStream = new RememberingInputStream(rememberingInputStream.remembered());
		readBytes = new byte[131072];
		ByteStreams.readFully(rememberingInputStream, readBytes);
		assertThat(readBytes, is(Arrays.copyOfRange(randomData, 0, 131072)));

		rememberingInputStream = new RememberingInputStream(rememberingInputStream.remembered());
		readBytes = new byte[1048576];
		ByteStreams.readFully(rememberingInputStream, readBytes);
		assertThat(readBytes, is(Arrays.copyOfRange(randomData, 0, 1048576)));
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Generates random data.
	 *
	 * @param seed
	 * 		The seed for the random number generator
	 * @param length
	 * 		The length of the data to generate
	 * @return The generated random data
	 */
	private byte[] generateData(long seed, int length) {
		Random random = new Random(seed);
		byte[] buffer = new byte[length];
		random.nextBytes(buffer);
		return buffer;
	}

}
