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
package com.l2jfree.gameserver.datatables;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.l2jfree.Config;
import com.l2jfree.gameserver.util.Util;

/**
 * @author NB4L1
 */
public final class ForgottenScrollTable
{
	private static final Log _log = LogFactory.getLog(ForgottenScrollTable.class);
	
	public static ForgottenScrollTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public static final class ForgottenScrollData
	{
		private final int _itemId;
		private final int _skillId;
		private final int _minLevel;
		private final int _classId;
		
		public ForgottenScrollData(int itemId, int skillId, int minLevel, int classId)
		{
			_itemId = itemId;
			_skillId = skillId;
			_minLevel = minLevel;
			_classId = classId;
		}
		
		public int getItemId()
		{
			return _itemId;
		}
		
		public int getSkillId()
		{
			return _skillId;
		}
		
		public int getMinLevel()
		{
			return _minLevel;
		}
		
		public int getClassId()
		{
			return _classId;
		}
	}
	
	private final Map<Integer, Map<Integer, ForgottenScrollData>> _scrolls = new HashMap<Integer, Map<Integer, ForgottenScrollData>>();
	private final Map<Integer, Set<Integer>> _allowedSkills = new HashMap<Integer, Set<Integer>>();
	
	private ForgottenScrollTable()
	{
		try
		{
			loadFromXML();
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
		Document doc = factory.newDocumentBuilder().parse(new File(Config.DATAPACK_ROOT, "data/forgottenscrolls.xml"));
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("scroll".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						
						int itemid = Integer.parseInt(attrs.getNamedItem("itemid").getNodeValue());
						int skillid = Integer.parseInt(attrs.getNamedItem("skillid").getNodeValue());
						int[] classIds = Util.toIntArray(attrs.getNamedItem("class").getNodeValue(), ";");
						
						Node att = attrs.getNamedItem("minlevel");
						int minLevel = (att == null) ? 81 : Integer.parseInt(att.getNodeValue());
						
						for (int classId : classIds)
						{
							ForgottenScrollData sd = new ForgottenScrollData(itemid, skillid, minLevel, classId);
							
							Map<Integer, ForgottenScrollData> map = _scrolls.get(itemid);
							
							if (map == null)
								_scrolls.put(itemid, map = new HashMap<Integer, ForgottenScrollData>());
							
							map.put(classId, sd);
							
							getAllowedSkillIds(classId).add(skillid);
						}
					}
				}
			}
		}
		
		_log.info("ForgottenScrollsManager: Loaded " + _scrolls.size() + " forgotten scrolls.");
	}
	
	public ForgottenScrollData getForgottenScroll(int itemId, int classId)
	{
		return _scrolls.get(itemId).get(classId);
	}
	
	public int[] getItemIds()
	{
		return ArrayUtils.toPrimitive(_scrolls.keySet().toArray(new Integer[_scrolls.size()]));
	}
	
	public Set<Integer> getAllowedSkillIds(int classId)
	{
		Set<Integer> set = _allowedSkills.get(classId);
		
		if (set == null)
			_allowedSkills.put(classId, set = new HashSet<Integer>());
		
		return set;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ForgottenScrollTable _instance = new ForgottenScrollTable();
	}
}
