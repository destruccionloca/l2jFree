package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ExSpawnEmitter extends L2GameServerPacket
{
    public ExSpawnEmitter(int playerObjectId, int npcObjectId)
    {
        _playerObjectId = playerObjectId;
        _npcObjectId = npcObjectId;
    }

    public ExSpawnEmitter(L2PcInstance player, L2NpcInstance npc)
    {
        _playerObjectId = player.getObjectId();
        _npcObjectId = npc.getObjectId();
    }

    protected final void writeImpl()
    {
        writeC(0xfe);
        writeH(0x5d);
        writeD(_npcObjectId);
        writeD(_playerObjectId);
        writeD(0x00);
    }

    public String getType()
    {
        return "SpawnEmitter";
    }

    private int _npcObjectId;
    private int _playerObjectId;
}
