/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.tools.geometry.Point3D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CastleManager
{
	protected static Log _log = LogFactory.getLog(CastleManager.class.getName());

	private static CastleManager _instance;
	private Map<Integer, Castle> _castles;

	public static final CastleManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new CastleManager();
			_instance.load();
		}
		return _instance;
	}

	private CastleManager()
	{}

	public void reload()
	{
		getCastles().clear();
		load();
	}

	private final void load()
	{

		Document doc = null;

		File castlesXml = new File(Config.DATAPACK_ROOT, "data/castles.xml");

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(castlesXml);
		}
		catch (Exception e)
		{
			_log.error("CastleManager: Error loading " + castlesXml.getAbsolutePath() + "! " + e.getMessage(), e);
		}
		try
		{
			parseDocument(doc);
		}
		catch (Exception e)
		{
			_log.error("CastleManager: Error while reading " + castlesXml.getAbsolutePath() + "! " + e.getMessage(), e);
		}

		_log.info("CastleManager: Loaded " + getCastles().size() + " castles.");
	}

	protected void parseDocument(Document doc)
	{
		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if("list".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("item".equalsIgnoreCase(d.getNodeName()))
					{
						Castle castle = parseEntity(d);
						if(castle != null)
							getCastles().put(castle.getCastleId(), castle);
					}
				}
			}
			else if("item".equalsIgnoreCase(n.getNodeName()))
			{
				Castle castle = parseEntity(n);
				if(castle != null)
					getCastles().put(castle.getCastleId(), castle);
			}
		}
	}

	protected Castle parseEntity(Node n)
	{
		int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		String name = n.getAttributes().getNamedItem("name").getNodeValue();

		Castle castle = new Castle(id, name);

		FastList<L2Spawn> controlTowerSpawns = null;
		FastList<L2Spawn> artefactSpawns = null;

		Node first = n.getFirstChild();
		for(n = first; n != null; n = n.getNextSibling())
		{
			if("zone".equalsIgnoreCase(n.getNodeName()))
			{
				IZone zone = ZoneManager.parseZone(n);

				if(zone != null)
				{
					if(zone.getSettings() == null)
						zone.setSettings(new StatsSet());
					zone.getSettings().set("castleId", id);
					ZoneManager.addZone(zone);
				}
			}
			else if("settings".equalsIgnoreCase(n.getNodeName()))
			{
				castle.setSettings(ZoneManager.parseSettings(n));
			}
			else if("restart".equalsIgnoreCase(n.getNodeName()))
			{
				String type = n.getAttributes().getNamedItem("type").getNodeValue();
				RestartType restartType = RestartType.getRestartTypeEnum(type);

				if(restartType == null)
					continue;

				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("point".equalsIgnoreCase(d.getNodeName()))
					{
						Point3D point = ZoneManager.parsePoint(d);
						castle.addRestartPoint(restartType, point);
					}
				}
			}
			else if("control_tower".equalsIgnoreCase(n.getNodeName()))
			{
				int npcId = Integer.parseInt(n.getAttributes().getNamedItem("npcId").getNodeValue());
				int npcHp = Integer.parseInt(n.getAttributes().getNamedItem("hp").getNodeValue());
				Point3D point = null;

				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("point".equalsIgnoreCase(d.getNodeName()))
					{
						point = ZoneManager.parsePoint(d);
					}
				}

				if(point == null)
					continue;

				L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);

				if(template == null)
					continue;

				template.setBaseHpMax(npcHp);

				L2Spawn spawn = new L2Spawn(template);
				spawn.setLocx(point.getX());
				spawn.setLocy(point.getY());
				spawn.setLocz(point.getZ());
				spawn.setHeading(0);

				if(controlTowerSpawns == null)
					controlTowerSpawns = new FastList<L2Spawn>();

				controlTowerSpawns.add(spawn);
			}
			else if("artefact".equalsIgnoreCase(n.getNodeName()))
			{
				int npcId = Integer.parseInt(n.getAttributes().getNamedItem("npcId").getNodeValue());
				int heading = Integer.parseInt(n.getAttributes().getNamedItem("heading").getNodeValue());
				Point3D point = null;

				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("point".equalsIgnoreCase(d.getNodeName()))
					{
						point = ZoneManager.parsePoint(d);
					}
				}

				if(point == null)
					continue;

				L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);

				if(template == null)
					continue;

				L2Spawn spawn = new L2Spawn(template);
				spawn.setLocx(point.getX());
				spawn.setLocy(point.getY());
				spawn.setLocz(point.getZ());
				spawn.setHeading(heading);

				if(artefactSpawns == null)
					artefactSpawns = new FastList<L2Spawn>();

				artefactSpawns.add(spawn);
			}
		}

		if(castle != null && castle.getSiege() != null)
		{
			for(L2Spawn spawn : controlTowerSpawns)
				castle.getSiege().getControlTowerSpawns().add(spawn);

			for(L2Spawn spawn : artefactSpawns)
				castle.getSiege().getArtefactSpawns().add(spawn);

		}

		return castle;
	}

	public final Castle getCastleById(int castleId)
	{
		return getCastles().get(castleId);
	}

	public final Castle getCastleByName(String name)
	{
		for(Castle castle : getCastles().values())
		{
			if(castle != null && castle.getName().equalsIgnoreCase(name.trim()))
				return castle;
		}
		return null;
	}

	public final Castle getCastleByOwner(L2Clan clan)
	{
		if(clan == null)
			return null;

		for(Castle castle : getCastles().values())
		{
			if(castle != null && castle.getOwnerId() == clan.getClanId())
				return castle;
		}
		return null;
	}

	public final Castle getCastleByLoc(int x, int y, int z)
	{
		for(Castle castle : getCastles().values())
		{
			if(castle != null && castle.getZone(ZoneType.CastleArea) != null)
			{
				if(castle.getZone(ZoneType.CastleArea).checkIfInZone(x, y, z))
					return castle;
			}
		}
		return null;
	}

	public final Castle getClosestCastle(L2Object activeObject)
	{
		Castle castle = null;

		if(activeObject instanceof L2Character)
			castle = getCastles().get(((L2Character) activeObject).getInsideCastle());

		if(castle == null)
		{
			double closestDistance = Double.MAX_VALUE;
			double distance;

			for(Castle c : getCastles().values())
			{
				if(castle != null && castle.getZone(ZoneType.CastleArea) != null)
				{
					distance = c.getZone(ZoneType.CastleArea).getZoneDistance(activeObject.getX(), activeObject.getY());
					if(closestDistance > distance)
					{
						closestDistance = distance;
						castle = c;
					}
				}
			}
		}
		return castle;
	}

	public final Map<Integer, Castle> getCastles()
	{
		if(_castles == null)
			_castles = new FastMap<Integer, Castle>();
		return _castles;
	}

	public final void validateTaxes(int sealStrifeOwner)
	{
		int maxTax;
		switch(sealStrifeOwner)
		{
			case SevenSigns.CABAL_DUSK:
				maxTax = 5;
				break;
			case SevenSigns.CABAL_DAWN:
				maxTax = 25;
				break;
			default: // no owner
				maxTax = 15;
				break;
		}

		for(Castle castle : _castles.values())
			if(castle.getTaxPercent() > maxTax)
				castle.setTaxPercent(maxTax);
	}

	public int getCircletByCastleId(int castleId)
	{
		Castle castle = getCastles().get(castleId);

		if(castle != null)
			return castle.getCircletId();

		return 0;
	}
}
