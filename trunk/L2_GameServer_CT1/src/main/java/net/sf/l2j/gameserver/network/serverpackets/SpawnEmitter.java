package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class SpawnEmitter extends L2GameServerPacket
{

    public SpawnEmitter(L2PcInstance client, L2NpcInstance npc)
    {
        _npcId = npc.getObjectId();
        _clientId = client.getObjectId();
    }

    protected final void writeImpl()
    {
        writeC(0xfe);
        writeH(0x5d);
        writeD(_npcId);
        writeD(_clientId);
        writeD(0x00);
    }

    public String getType()
    {
        return "SpawnEmitter";
    }

    private int _npcId;
    private int _clientId;
}
