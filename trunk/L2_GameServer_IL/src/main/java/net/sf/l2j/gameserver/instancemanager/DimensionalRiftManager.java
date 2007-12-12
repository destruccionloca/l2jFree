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
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
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
import net.sf.l2j.gameserver.model.zone.ZoneRect;
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
    private FastMap<RoomType, FastMap<Byte, DimensionalRiftRoom>> _rooms;
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

    public FastMap<RoomType, FastMap<Byte, DimensionalRiftRoom>> getRooms()
    {
        if (_rooms == null)
            _rooms = new FastMap<RoomType, FastMap<Byte, DimensionalRiftRoom>>();
        return _rooms;
    }

    public FastMap<Byte, DimensionalRiftRoom> getRooms(RoomType roomType)
    {
        if (getRooms().get(roomType) == null)
            getRooms().put(roomType, new FastMap<Byte, DimensionalRiftRoom>());
        return getRooms().get(roomType);
    }

    public DimensionalRiftRoom getRoom(RoomType roomType, byte roomId)
    {
        return getRooms(roomType).get(roomId);
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
                _log.fatal("DimensionalRiftManager: Error in file " + f.getAbsolutePath(), e);
            }
        }

        int roomCount = 0;
        int spawnCount = 0;
        for (Map.Entry<RoomType, FastMap<Byte, DimensionalRiftRoom>> rooms : getRooms().entrySet())
        {
            roomCount += rooms.getValue().size();
            for (Map.Entry<Byte, DimensionalRiftRoom> room : rooms.getValue().entrySet())
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
                            getRooms(riftRoom.getRoomType()).put((byte) riftRoom.getId(), riftRoom);
                    }
                }
            } else if ("item".equalsIgnoreCase(n.getNodeName()))
            {
                DimensionalRiftRoom riftRoom = parseEntry(n);
                _log.info(" " + riftRoom.getZoneName());
                if (riftRoom != null)
                    getRooms(riftRoom.getRoomType()).put((byte) riftRoom.getId(), riftRoom);
            }
        }
    }

    public void reload()
    {
        for (Map.Entry<RoomType, FastMap<Byte, DimensionalRiftRoom>> rooms : getRooms().entrySet())
        {
            for (Map.Entry<Byte, DimensionalRiftRoom> room : rooms.getValue().entrySet())
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
        DimensionalRiftRoom riftRoom = null;
        RoomType roomType = null;
        String typeName = "";
        boolean isBoss = false;
        List<Point3D> points = new FastList<Point3D>();
        Map<RestartType, Point3D> teleports = new FastMap<RestartType, Point3D>();
        List<L2Spawn> spawns = new FastList<L2Spawn>();

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
                        spawns.add(spawn);
                    } else
                    {
                        _log.error("DimensionalRiftManager: Unknown npc template '" + mobId + "' !");
                    }
                }
            } else if ("teleport".equalsIgnoreCase(n.getNodeName()))
            {
                teleports.put(RestartType.RestartNormal, parsePoint(n));
            } else if ("type".equalsIgnoreCase(n.getNodeName()))
            {
                typeName = n.getTextContent();
            } else if ("boss".equalsIgnoreCase(n.getNodeName()))
            {
                isBoss = n.getTextContent().equals("1");
            } else if ("point".equalsIgnoreCase(n.getNodeName()))
            {
                points.add(parsePoint(n));
            }

        roomType = RoomType.getRoomTypeEnum(typeName);

        if (roomType == null)
        {
            _log.error("DimensionalRiftManager: Unknown room type '" + typeName + "' !");
            return null;
        }

        if (points.size() == 2)
        {
            riftRoom = new DimensionalRiftRoom();
        } else
            return null;

        riftRoom.setId(id);
        riftRoom.setZoneName(name);
        riftRoom.setIsBoss(isBoss);
        riftRoom.setRoomType(roomType);

        for (Point3D point : points)
            riftRoom.addPoint(point);
        for (Map.Entry<RestartType, Point3D> teleport : teleports.entrySet())
            riftRoom.addRestartPoint(teleport.getKey(), teleport.getValue());
        for (L2Spawn spawn : spawns)
        {
            Location loc = riftRoom.getRandomLocation();
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

    public boolean checkIfInRiftZone(int x, int y, int z, boolean ignorePeaceZone)
    {
        if (ignorePeaceZone)
            return getRooms().get(RoomType.DimensionalRift).get((byte)1).checkIfInZone(x, y, z);
        else
            return getRooms().get(RoomType.DimensionalRift).get((byte)1).checkIfInZone(x, y, z)
                    && !getRooms().get(RoomType.Start).get((byte)1).checkIfInZone(x, y, z);
    }

    public boolean checkIfInPeaceZone(int x, int y, int z)
    {
        return getRooms(RoomType.Start).get((byte)1).checkIfInZone(x, y, z);
    }

    public Location getWaitingRoomTeleport()
    {
        return getRooms(RoomType.Start).get((byte)1).getTeleport();
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
            if (!checkIfInPeaceZone(p.getX(), p.getY(), p.getZ()))
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
        Start(0), DimensionalRift(0), Recruit(1), Soldier(2), Officer(3), Captain(4), Commander(5), Hero(6);

        int _id;

        private RoomType(int id)
        {
            _id = id;
        }

        public int getId()
        {
            return _id;
        }

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
                if (rt.getId() == id)
                    return rt;

            return null;
        }
    }

    public class DimensionalRiftRoom extends ZoneRect
    {
        private boolean _isBoss;
        protected RoomType _roomType;
        protected final FastList<L2Spawn> _roomSpawns;

        public DimensionalRiftRoom()
        {
            _roomSpawns = new FastList<L2Spawn>();
        }

        public void setRoomType(RoomType roomType)
        {
            _roomType = roomType;
        }

        public RoomType getRoomType()
        {
            return _roomType;
        }

        public void setIsBoss(boolean isBoss)
        {
            _isBoss = isBoss;
        }

        public boolean isBoss()
        {
            return _isBoss;
        }

        public Location getTeleport()
        {
            return getRestartPoint(RestartType.RestartNormal);
        }

        public FastList<L2Spawn> getSpawns()
        {
            return _roomSpawns;
        }

        public void spawn()
        {
            for (L2Spawn spawn : _roomSpawns)
            {
                spawn.doSpawn();
                spawn.startRespawn();
            }
        }

        public void unspawn()
        {
            for (L2Spawn spawn : _roomSpawns)
            {
                spawn.stopRespawn();
                if (spawn.getLastSpawn() != null)
                    spawn.getLastSpawn().deleteMe();
            }
        }
    }
}
