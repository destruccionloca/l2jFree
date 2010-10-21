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
package com.l2jfree.gameserver.network.serverpackets;

import com.l2jfree.gameserver.datatables.CharTemplateTable;
import com.l2jfree.gameserver.model.base.ClassId;
import com.l2jfree.gameserver.templates.chars.L2PcTemplate;

public final class NewCharacterSuccess extends StaticPacket
{
	private static final String _S__NEWCHARACTERSUCCESS = "[S] 0D NewCharacterSuccess c[d->dddddddddddddddddddd<-]";
	
	public static final NewCharacterSuccess PACKET = new NewCharacterSuccess();
	
	private final L2PcTemplate[] _chars = new L2PcTemplate[] {
			CharTemplateTable.getInstance().getTemplate(0),
			CharTemplateTable.getInstance().getTemplate(ClassId.HumanFighter),
			CharTemplateTable.getInstance().getTemplate(ClassId.HumanMystic),
			CharTemplateTable.getInstance().getTemplate(ClassId.ElvenFighter),
			CharTemplateTable.getInstance().getTemplate(ClassId.ElvenMystic),
			CharTemplateTable.getInstance().getTemplate(ClassId.DarkFighter),
			CharTemplateTable.getInstance().getTemplate(ClassId.DarkMystic),
			CharTemplateTable.getInstance().getTemplate(ClassId.OrcFighter),
			CharTemplateTable.getInstance().getTemplate(ClassId.OrcMystic),
			CharTemplateTable.getInstance().getTemplate(ClassId.DwarvenFighter),
			CharTemplateTable.getInstance().getTemplate(ClassId.MaleSoldier),
			CharTemplateTable.getInstance().getTemplate(ClassId.FemaleSoldier) };
	
	private NewCharacterSuccess()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x0D);
		writeD(_chars.length);
		
		for (L2PcTemplate temp : _chars)
		{
			if (temp == null)
				continue;
			
			writeD(temp.getRace().ordinal());
			writeD(temp.getClassId().getId());
			writeD(0x46);
			writeD(temp.getBaseSTR());
			writeD(0x0A);
			writeD(0x46);
			writeD(temp.getBaseDEX());
			writeD(0x0A);
			writeD(0x46);
			writeD(temp.getBaseCON());
			writeD(0x0A);
			writeD(0x46);
			writeD(temp.getBaseINT());
			writeD(0x0A);
			writeD(0x46);
			writeD(temp.getBaseWIT());
			writeD(0x0A);
			writeD(0x46);
			writeD(temp.getBaseMEN());
			writeD(0x0A);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__NEWCHARACTERSUCCESS;
	}
}
