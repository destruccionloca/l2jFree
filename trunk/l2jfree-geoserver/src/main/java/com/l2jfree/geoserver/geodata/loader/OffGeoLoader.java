/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.geoserver.geodata.loader;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import com.l2jfree.geoserver.geodata.GeoEngine;

public class OffGeoLoader extends AbstractGeoLoader
{
	private static final Pattern	PATTERN	= Pattern.compile("[\\d]{2}_[\\d]{2}_conv.dat");

	@Override
	protected byte[][] parse(byte[] data)
	{

		if (data.length <= 393234)
		{ // 18 + ((256 * 256) * (2 * 3)) - it's minimal size of geodata (whole region with flat blocks)
			return null;
		}

		// Indexing geo files, so we will know where each block starts
		int index = 18; // Skip firs 18 bytes, they have nothing with data;

		byte[][] blocks = new byte[65536][]; // 256 * 256

		for (int block = 0, n = blocks.length; block < n; block++)
		{
			short type = makeShort(data[index + 1], data[index]);
			index += 2;

			byte[] geoBlock;
			if (type == 0)
			{

				geoBlock = new byte[2 + 1];

				geoBlock[0] = GeoEngine.BLOCKTYPE_FLAT;
				geoBlock[1] = data[index + 2];
				geoBlock[2] = data[index + 3];

				blocks[block] = geoBlock;
				index += 4;
			}
			else if (type == 0x0040)
			{

				geoBlock = new byte[128 + 1];

				geoBlock[0] = GeoEngine.BLOCKTYPE_COMPLEX;
				System.arraycopy(data, index, geoBlock, 1, 128);

				index += 128;

				blocks[block] = geoBlock;
			}
			else
			{

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				baos.write(GeoEngine.BLOCKTYPE_MULTILEVEL);

				for (int b = 0; b < 64; b++)
				{
					byte layers = (byte) makeShort(data[index + 1], data[index]);

					index += 2;

					baos.write(layers);
					for (int i = 0; i < layers << 1; i++)
					{
						baos.write(data[index++]);
					}
				}

				blocks[block] = baos.toByteArray();
			}
		}

		return blocks;
	}

	protected short makeShort(byte b1, byte b0)
	{
		return (short) (b1 << 8 | b0 & 0xff);
	}

	@Override
	public Pattern getPattern()
	{
		return PATTERN;
	}

	@Override
	public byte[] convert(byte[] data)
	{
		return data;
	}
}