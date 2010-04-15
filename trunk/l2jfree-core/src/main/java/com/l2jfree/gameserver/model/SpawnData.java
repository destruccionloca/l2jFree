package com.l2jfree.gameserver.model;

public class SpawnData
{
	public final int npcId;
	public final int x;
	public final int y;
	public final int z;
	public final int heading;
	public final int respawnDelay;
	public final int respawnMinDelay;
	public final int respawnMaxDelay;
	
	public SpawnData(int npcId, int x, int y, int z, int heading, int respawnDelay)
	{
		this.npcId = npcId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
		this.respawnDelay = respawnDelay;
		this.respawnMinDelay = respawnDelay;
		this.respawnMaxDelay = respawnDelay;
	}
	
	public SpawnData(int npcId, int x, int y, int z, int heading, int respawnDelay, int respawnMinDelay, int respawnMaxDelay)
	{
		this.npcId = npcId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
		this.respawnDelay = respawnDelay;
		this.respawnMinDelay = respawnMinDelay;
		this.respawnMaxDelay = respawnMaxDelay;
	}
}
