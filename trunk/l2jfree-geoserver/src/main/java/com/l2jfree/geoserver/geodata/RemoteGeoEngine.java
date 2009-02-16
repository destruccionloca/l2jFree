package com.l2jfree.geoserver.geodata;

import java.util.logging.Logger;

public class RemoteGeoEngine extends GeoEngine
{
	private static final Logger	log				= Logger.getLogger(RemoteGeoEngine.class.getName());

	private byte[][][][]		localGeo		= new byte[geodataSizeX][geodataSizeY][][];

	@Override
	protected byte[] getGeoBlockFromGeoCoords(int geoX, int geoY)
	{
		int ix = geoX >> 11;
		int iy = geoY >> 11;

		if (ix < 0 || ix >= geodataSizeX || iy < 0 || iy >= geodataSizeY)
			return null;

		int blockX = getBlock(geoX);
		int blockY = getBlock(geoY);
		int blockIndex = getBlockIndex(blockX, blockY);

		byte[] localBlock = getLocalBlock(ix, iy, blockIndex);
		if (localBlock != null)
			return localBlock;

		byte[][] region = geodata[ix][iy];

		if (region == null)
			return null;

		return region[blockIndex];
	}

	private byte[] getLocalBlock(int ix, int iy, int blockIndex)
	{
		byte[][] region = localGeo[ix][iy];

		if (region == null)
			return null;

		return region[blockIndex];
	}

	@Override
	protected void copyBlock(int ix, int iy, int blockIndex)
	{
		byte[][] region = localGeo[ix][iy];

		if (region == null)
		{
			region = new byte[65536][];
			localGeo[ix][iy] = region;
		}

		byte[] block = region[blockIndex];

		if (block == null)
		{
			byte[][] superRegion = geodata[ix][iy];

			if (superRegion == null || superRegion.length == 0)
				return;

			byte[] superBlock = superRegion[blockIndex];

			if (superBlock == null)
				return;

			byte blockType = superBlock[0];

			switch (blockType)
			{
			case BLOCKTYPE_FLAT:
				block = new byte[129];
				block[0] = BLOCKTYPE_COMPLEX;
				for (int i = 1; i < 129; i += 2)
				{
					block[i] = superBlock[1];
					block[i + 1] = superBlock[2];
				}
				region[blockIndex] = block;
				break;
			case BLOCKTYPE_COMPLEX:
			case BLOCKTYPE_MULTILEVEL:
				block = new byte[superBlock.length];
				System.arraycopy(superBlock, 0, block, 0, superBlock.length);
				region[blockIndex] = block;
				break;
			default:
				log.severe("GeoEngine: Invalid BlockType");
			}
		}
	}
}