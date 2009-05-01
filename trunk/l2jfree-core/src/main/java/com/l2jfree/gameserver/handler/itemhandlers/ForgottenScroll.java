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
package com.l2jfree.gameserver.handler.itemhandlers;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 *
 * @author Cromir, Kreastr
 * 
 */
public class ForgottenScroll implements IItemHandler
{
	private static final String						SCROLLS_FILE	= "forgottenscrolls.xml";

	private FastMap<Integer, ForgottenScrollData>	_scrolls		= new FastMap<Integer, ForgottenScrollData>();

	class ForgottenScrollData
	{
		private int		_ItemID;
		private int		_SkillID;
		private String	_ClassRequire;

		public ForgottenScrollData(int ItemID, int SkillID, String ClassRequire)
		{
			_ItemID = ItemID;
			_SkillID = SkillID;
			_ClassRequire = ClassRequire;
		}

		public int getItemID()
		{
			return _ItemID;
		}

		public int getSkillID()
		{
			return _SkillID;
		}

		public String getClassRequire()
		{
			return _ClassRequire;
		}

	}

	public ForgottenScroll()
	{

		_scrolls.clear();
		try
		{
			this.loadFromXML();
			_log.info("ForgottenScrollsManager: Loaded " + _scrolls.size() + " forgotten scrolls.");
		}
		catch (Exception e)
		{
			_log.fatal("Failed loading forgotten scrolls", e);
		}

	}

	private void loadFromXML() throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "data/" + SCROLLS_FILE);
		if (file.exists())
		{
			Document doc = factory.newDocumentBuilder().parse(file);

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("scroll".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att;
							att = attrs.getNamedItem("itemid");
							if (att == null)
							{
								_log.fatal("Missing itemid for scroll item, skipping");
								continue;
							}
							int itemid = Integer.parseInt(att.getNodeValue());

							att = attrs.getNamedItem("skillid");
							if (att == null)
							{
								_log.fatal("Missing skillid for scroll item, skipping");
								continue;
							}
							int skillid = Integer.parseInt(att.getNodeValue());

							att = attrs.getNamedItem("class");
							if (att == null)
							{
								_log.fatal("Missing class requirements for scroll item, skipping");
								continue;
							}
							String classreq = att.getNodeValue();

							ForgottenScrollData recipeList = new ForgottenScrollData(itemid, skillid, classreq);
							_scrolls.put(_scrolls.size(), recipeList);
						}
					}
				}
			}
		}
		else
		{
			_log.fatal("Forgotten Scrolls data file (" + file.getAbsolutePath() + ") doesnt exists.");
		}
	}

	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance) playable;
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance) playable).getOwner();
		else
			return;

		if (activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isSubClassActive())
		{
			activeChar.sendPacket(SystemMessageId.SKILL_NOT_FOR_SUBCLASS);
			return;
		}

		if (activeChar.getLevel() < 81)
		{
			activeChar.sendPacket(SystemMessageId.YOU_DONT_MEET_SKILL_LEVEL_REQUIREMENTS);
			return;
		}

		int itemId = item.getItemId();

		for (ForgottenScrollData sd : _scrolls.values())
		{
			if (itemId == sd.getItemID())
			{
				L2Skill sk = activeChar.getKnownSkill(sd.getSkillID());
				if (sk == null)
				{
					String[] classes = sd.getClassRequire().split(";");
					for (String cl : classes)
					{
						if (activeChar.getActiveClass() == Integer.parseInt(cl))
						{
							if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
								return;

							SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
							sm.addItemName(item);
							activeChar.sendPacket(sm);

							L2Skill skill = SkillTable.getInstance().getInfo(sd.getSkillID(), 1);
							activeChar.addSkill(skill, true);
							activeChar.sendSkillList();
							activeChar.sendMessage("You learned the skill "+skill.getName()); // Retail MSG?
							return;
						}
					}
				}
				else
				{
					activeChar.sendMessage("That skill is already learned."); // Retail MSG?
					return;
				}
			}
		}
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
		sm.addItemName(item);
		activeChar.sendPacket(sm);
	}

	public int[] getItemIds()
	{
		int[] ITEM_IDS = new int[_scrolls.size()];
		int i = 0;

		for (ForgottenScrollData sd : _scrolls.values())
			ITEM_IDS[i++] = sd.getItemID();

		return ITEM_IDS;
	}
}