/*
 * Sonitus - IntegralWriteOutputStreamTest.java - Copyright © 2013 David Roden
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link IntegralWriteOutputStream}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IntegralWriteOutputStreamTest {

	@Test
	public void testSingleWritesWithSize2() throws IOException {
		OutputStream outputStream = mock(OutputStream.class);

		IntegralWriteOutputStream integralWriteOutputStream = new IntegralWriteOutputStream(outputStream, 2);
		integralWriteOutputStream.write(1);
		integralWriteOutputStream.write(2);
		integralWriteOutputStream.write(3);
		integralWriteOutputStream.write(4);
		integralWriteOutputStream.write(5);

		verify(outputStream, never()).write(anyInt());
		verify(outputStream, times(2)).write((byte[]) any());
	}

	@Test
	public void testLargeWritesWithSize3() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = getOutputStream(3);
		IntegralWriteOutputStream integralWriteOutputStream = new IntegralWriteOutputStream(byteArrayOutputStream, 3);

		byte[] buffer = generateData(18);
		integralWriteOutputStream.write(buffer, 0, 7);
		integralWriteOutputStream.write(buffer, 7, 7);
		integralWriteOutputStream.write(buffer, 14, 4);

		assertThat(byteArrayOutputStream.toByteArray(), is(buffer));
	}

	@Test
	public void testLargeWritesWithSize4() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = getOutputStream(1024);
		IntegralWriteOutputStream integralWriteOutputStream = new IntegralWriteOutputStream(byteArrayOutputStream, 1024);

		byte[] buffer = generateData(32768);
		integralWriteOutputStream.write(buffer, 0, 123);
		integralWriteOutputStream.write(buffer, 123, 768);
		integralWriteOutputStream.write(buffer, 123 + 768, 6285);
		integralWriteOutputStream.write(buffer, 123 + 768 + 6285, 1234);
		integralWriteOutputStream.write(buffer, 123 + 768 + 6285 + 1234, 21111);
		integralWriteOutputStream.write(buffer, 123 + 768 + 6285 + 1234 + 21111, 32768 - (123 + 768 + 6285 + 1234 + 21111));

		assertThat(byteArrayOutputStream.toByteArray(), is(buffer));
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates a {@link ByteArrayOutputStream} that will throw an {@link
	 * IllegalArgumentException} if its {@link OutputStream#write(byte[], int,
	 * int)} method is called with {@code len} not being a multiple of {@code
	 * integralSize}, or if {@link OutputStream#write(int)} is called.
	 *
	 * @param integralSize
	 * 		The number of bytes to write multiples of
	 * @return The created output stream
	 */
	private static ByteArrayOutputStream getOutputStream(final int integralSize) {
		return new ByteArrayOutputStream() {

			@Override
			public synchronized void write(byte[] b, int off, int len) {
				if ((len % integralSize) != 0) {
					throw new IllegalArgumentException(String.format("%d is not a multiple of %d.", len, integralSize));
				}
				super.write(b, off, len);
			}

			@Override
			public synchronized void write(int b) {
				throw new IllegalArgumentException("write(int) called.");
			}

		};
	}

	/**
	 * Generates a random amount of data.
	 *
	 * @param length
	 * 		The length of the data
	 * @return The generated random data
	 */
	private static byte[] generateData(int length) {
		Random random = new Random();
		byte[] buffer = new byte[length];
		random.nextBytes(buffer);
		return buffer;
	}

}
