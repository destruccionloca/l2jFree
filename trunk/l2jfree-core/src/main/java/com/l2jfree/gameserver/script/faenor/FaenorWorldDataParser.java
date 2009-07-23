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
package com.l2jfree.gameserver.script.faenor;

import javolution.util.FastMap;

import org.w3c.dom.Node;

import com.l2jfree.gameserver.script.IntList;
import com.l2jfree.gameserver.script.Parser;
import com.l2jfree.gameserver.script.ParserFactory;
import com.l2jfree.gameserver.script.ScriptEngine;

/**
 * @author Luis Arias
 *
 */
public class FaenorWorldDataParser extends FaenorParser
{
	//Script Types
	private final static String	PET_DATA	= "PetData";

	@Override
	public void parseScript(Node eventNode)
	{
		if (_log.isDebugEnabled())
			_log.debug("Parsing WorldData");

		for (Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling())
		{

			if (isNodeName(node, PET_DATA))
			{
				parsePetData(node);
			}
		}
	}

	public class PetData
	{
		public int				petId;
		public int				levelStart;
		public int				levelEnd;
		FastMap<String, String>	statValues;

		public PetData()
		{
			statValues = new FastMap<String, String>();
		}
	}

	private void parsePetData(Node petNode)
	{
		PetData petData = new PetData();

		try
		{
			petData.petId = getInt(attribute(petNode, "ID"));
			int[] levelRange = IntList.parse(attribute(petNode, "Levels"));
			petData.levelStart = levelRange[0];
			petData.levelEnd = levelRange[1];

			for (Node node = petNode.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if (isNodeName(node, "Stat"))
				{
					parseStat(node, petData);
				}
			}
			_bridge.addPetData(petData.petId, petData.levelStart, petData.levelEnd, petData.statValues);
		}
		catch (Exception e)
		{
			petData.petId = -1;
			_log.warn("Error in pet Data parser.", e);
		}
	}

	private void parseStat(Node stat, PetData petData)
	{
		try
		{
			String statName = attribute(stat, "Name");

			for (Node node = stat.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if (isNodeName(node, "Formula"))
				{
					String formula = parseForumla(node);
					petData.statValues.put(statName, formula);
				}
			}
		}
		catch (Exception e)
		{
			petData.petId = -1;
			System.err.println("ERROR(parseStat):" + e.getMessage());
		}
	}

	private String parseForumla(Node formulaNode)
	{
		return formulaNode.getTextContent().trim();
	}

	static class FaenorWorldDataParserFactory extends ParserFactory
	{
		@Override
		public Parser create()
		{
			return (new FaenorWorldDataParser());
		}
	}

	static
	{
		ScriptEngine.getParserFactories().put(getParserName("WorldData"), new FaenorWorldDataParserFactory());
	}
}
