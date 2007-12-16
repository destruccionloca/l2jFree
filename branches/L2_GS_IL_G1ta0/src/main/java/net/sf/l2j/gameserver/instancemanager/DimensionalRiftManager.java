/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.DimensionalRift;
import net.sf.l2j.gameserver.model.entity.DimensionalRiftRoom;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.tools.geometry.Point3D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Thanks to L2Fortress and balancer.ru - kombat
 */
public class DimensionalRiftManager
{

    private static Log _log = LogFactory.getLog(DimensionalRiftManager.class.getName());
    private static DimensionalRiftManager _instance;
    private Map<RoomType, Map<Integer, DimensionalRiftRoom>> _rooms;
    private final short DIMENSIONAL_FRAGMENT_ITEM_ID = 7079;

    public static DimensionalRiftManager getInstance()
    {
        if (_instance == null)
        {
            _instance = new DimensionalRiftManager();
            _instance.load();
        }
        return _instance;
    }

    public DimensionalRiftManager() {}

    public Map<RoomType, Map<Integer, DimensionalRiftRoom>> getRooms()
    {
        if (_rooms == null)
            _rooms = new FastMap<RoomType, Map<Integer, DimensionalRiftRoom>>();
        return _rooms;
    }

    public Map<Integer, DimensionalRiftRoom> getRooms(RoomType roomType)
    {
        if (getRooms().get(roomType) == null)
            getRooms().put(roomType, new FastMap<Integer, DimensionalRiftRoom>());
        return getRooms().get(roomType);
    }

    public DimensionalRiftRoom getRoom(RoomType roomType, int roomId)
    {
        return getRooms(roomType).get(roomId);
    }

    public DimensionalRiftRoom getWaitingRoom()
    {
        return getRooms(RoomType.Start).get(1);
    }
    
    private void load()
    {
        File f = new File(Config.DATAPACK_ROOT + "/data/dimensionalRift.xml");
        Document doc = null;

        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                factory.setIgnoringComments(true);
                doc = factory.newDocumentBuilder().parse(f);
            } catch (Exception e)
            {
                _log.fatal("DimensionalRiftManager: Error loading file " + f.getAbsolutePath(), e);
            }
            try
            {
                parseDocument(doc);
            } catch (Exception e)
            {
                _log.fatal("DimensionalRiftManager: Error while pasing file " + f.getAbsolutePath(), e);
            }
        }

        int roomCount = 0;
        int spawnCount = 0;
        for (Map.Entry<RoomType, Map<Integer, DimensionalRiftRoom>> rooms : getRooms().entrySet())
        {
            roomCount += rooms.getValue().size();
            for (Map.Entry<Integer, DimensionalRiftRoom> room : rooms.getValue().entrySet())
            {
                spawnCount += room.getValue().getSpawns().size();
            }
        }
        _log.info("DimensionalRiftManager: Loaded " + getRooms().size() + " room types:" + roomCount + " rooms with "
                + spawnCount + " spawns.");

    }

    protected void parseDocument(Document doc)
    {
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if ("list".equalsIgnoreCase(n.getNodeName()))
            {
                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
                {
                    if ("room".equalsIgnoreCase(d.getNodeName()))
                    {
                        DimensionalRiftRoom riftRoom = parseEntry(d);
                        if (riftRoom != null)
                            getRooms(riftRoom.getRoomType()).put(riftRoom.getId(), riftRoom);
                    }
                }
            } else if ("room".equalsIgnoreCase(n.getNodeName()))
            {
                DimensionalRiftRoom riftRoom = parseEntry(n);
                if (riftRoom != null)
                    getRooms(riftRoom.getRoomType()).put(riftRoom.getId(), riftRoom);
            }
        }
    }

    public void reload()
    {
        for (Map.Entry<RoomType, Map<Integer, DimensionalRiftRoom>> rooms : getRooms().entrySet())
        {
            for (Map.Entry<Integer, DimensionalRiftRoom> room : rooms.getValue().entrySet())
            {
                room.getValue().unspawn();
                room.getValue().getSpawns().clear();
            }
            rooms.getValue().clear();
        }
        getRooms().clear();
        load();
    }

    protected DimensionalRiftRoom parseEntry(Node n)
    {
        int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
        String name = n.getAttributes().getNamedItem("name").getNodeValue();
        
        DimensionalRiftRoom riftRoom = new DimensionalRiftRoom(id, name);
        RoomType roomType = null;
        Node first = n.getFirstChild();
        for (n = first; n != null; n = n.getNextSibling())
            if ("spawn".equalsIgnoreCase(n.getNodeName()))
            {
                int mobId = Integer.parseInt(n.getAttributes().getNamedItem("mobId").getNodeValue());
                int delay = Integer.parseInt(n.getAttributes().getNamedItem("delay").getNodeValue());
                int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
                L2NpcTemplate template = NpcTable.getInstance().getTemplate(mobId);

                for (int i = 0; i < count; i++)
                {
                    if (template != null)
                    {
                        L2Spawn spawn = new L2Spawn(template);
                        spawn.setAmount(1);
                        spawn.setHeading(-1);
                        spawn.setRespawnDelay(delay);
                        riftRoom.getSpawns().add(spawn);
                    } else
                    {
                        _log.error("DimensionalRiftManager: Unknown npc template '" + mobId + "' !");
                    }
                }
            } else if ("teleport".equalsIgnoreCase(n.getNodeName()))
            {
            	 riftRoom.addRestartPoint(RestartType.RestartNormal, parsePoint(n));
            	 
            } else if ("type".equalsIgnoreCase(n.getNodeName()))
            {
            	roomType = RoomType.getRoomTypeEnum(n.getTextContent());
            	
            } else if ("boss".equalsIgnoreCase(n.getNodeName()))
            {
            	riftRoom.setIsBoss(n.getTextContent().equals("1"));
            	
            } else if ("zone".equalsIgnoreCase(n.getNodeName()))
            {
                IZone zone = ZoneManager.parseZone(n);
                riftRoom.setZone(zone);
                ZoneManager.addZone(zone);
            }


        if (roomType == null)
        {
            _log.error("DimensionalRiftManager: Unknown room type !");
            return null;
        }

        riftRoom.setRoomType(roomType);

        for (L2Spawn spawn : riftRoom.getSpawns())
        {
            Location loc = riftRoom.getZone().getRandomLocation();
            spawn.setLocx(loc.getX());
            spawn.setLocy(loc.getY());
            spawn.setLocz(loc.getZ());
            SpawnTable.getInstance().addNewSpawn(spawn, false);
            riftRoom.getSpawns().add(spawn);
        }

        return riftRoom;
    }

    protected Point3D parsePoint(Node n)
    {
        int x = Integer.parseInt(n.getAttributes().getNamedItem("x").getNodeValue());
        int y = Integer.parseInt(n.getAttributes().getNamedItem("y").getNodeValue());
        int z = 0;
        if (n.getAttributes().getNamedItem("z") != null)
            z = Integer.parseInt(n.getAttributes().getNamedItem("z").getNodeValue());

        return new Point3D(x, y, z);
    }

    public void start(L2PcInstance player, RoomType roomType, L2NpcInstance npc)
    {
        boolean canPass = true;
        if (!player.isInParty())
        {
            showHtmlFile(player, "data/html/seven_signs/rift/NoParty.htm", npc);
            return;
        }

        if (player.getParty().getPartyLeaderOID() != player.getObjectId())
        {
            showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
            return;
        }

        if (player.getParty().isInDimensionalRift())
        {
            handleCheat(player, npc);
            return;
        }

        if (player.getParty().getMemberCount() < Config.RIFT_MIN_PARTY_SIZE)
        {
            NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
            html.setFile("data/html/seven_signs/rift/SmallParty.htm");
            html.replace("%npc_name%", npc.getName());
            html.replace("%count%", new Integer(Config.RIFT_MIN_PARTY_SIZE).toString());
            player.sendPacket(html);
            return;
        }

        for (L2PcInstance p : player.getParty().getPartyMembers())
            if (!getWaitingRoom().getZone().checkIfCharacterInZone(p));
                canPass = false;

        if (!canPass)
        {
            showHtmlFile(player, "data/html/seven_signs/rift/NotInWaitingRoom.htm", npc);
            return;
        }

        L2ItemInstance i;
        for (L2PcInstance p : player.getParty().getPartyMembers())
        {
            i = p.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID);

            if (i == null)
            {
                canPass = false;
                break;
            }

            if (i.getCount() > 0)
                if (i.getCount() < getNeededItems(roomType))
                    canPass = false;
        }

        if (!canPass)
        {
            NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
            html.setFile("data/html/seven_signs/rift/NoFragments.htm");
            html.replace("%npc_name%", npc.getName());
            html.replace("%count%", new Integer(getNeededItems(roomType)).toString());
            player.sendPacket(html);
            return;
        }

        for (L2PcInstance p : player.getParty().getPartyMembers())
        {
            i = p.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID);
            p.destroyItem("RiftEntrance", i.getObjectId(), getNeededItems(roomType), null, false);
        }

        new DimensionalRift(player.getParty(), roomType, (byte) Rnd.get(1, 9));
    }

    public void killRift(DimensionalRift d)
    {
        if (d.getTeleportTimerTask() != null)
            d.getTeleportTimerTask().cancel();
        d.setTeleportTimerTask(null);

        if (d.getTeleportTimer() != null)
            d.getTeleportTimer().cancel();
        d.setTeleportTimer(null);

        if (d.getSpawnTimerTask() != null)
            d.getSpawnTimerTask().cancel();
        d.setSpawnTimerTask(null);

        if (d.getSpawnTimer() != null)
            d.getSpawnTimer().cancel();
        d.setSpawnTimer(null);
    }

    private int getNeededItems(RoomType roomType)
    {
        switch (roomType)
        {
        case Recruit:
            return Config.RIFT_ENTER_COST_RECRUIT;
        case Soldier:
            return Config.RIFT_ENTER_COST_SOLDIER;
        case Officer:
            return Config.RIFT_ENTER_COST_OFFICER;
        case Captain:
            return Config.RIFT_ENTER_COST_CAPTAIN;
        case Commander:
            return Config.RIFT_ENTER_COST_COMMANDER;
        case Hero:
            return Config.RIFT_ENTER_COST_HERO;
        default:
            return 999999;
        }
    }

    public void showHtmlFile(L2PcInstance player, String file, L2NpcInstance npc)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
        html.setFile(file);
        html.replace("%npc_name%", npc.getName());
        player.sendPacket(html);
    }

    public void handleCheat(L2PcInstance player, L2NpcInstance npc)
    {
        showHtmlFile(player, "data/html/seven_signs/rift/Cheater.htm", npc);
        if (!player.isGM())
        {
            _log.warn("Player " + player.getName() + "(" + player.getObjectId() + ") was cheating in dimension rift area!");
            Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName()
                    + " tried to cheat in dimensional rift.", Config.DEFAULT_PUNISH);
        }
    }

    public enum RoomType
    {
        Start, Recruit, Soldier, Officer, Captain, Commander, Hero, DimensionalRift;


        public final static RoomType getRoomTypeEnum(String typeName)
        {
            for (RoomType rt : RoomType.values())
                if (rt.toString().equalsIgnoreCase(typeName))
                    return rt;

            return null;
        }

        public final static RoomType getRoomTypeEnum(int id)
        {
            for (RoomType rt : RoomType.values())
                if (rt.ordinal() == id)
                    return rt;

            return null;
        }
    }
}