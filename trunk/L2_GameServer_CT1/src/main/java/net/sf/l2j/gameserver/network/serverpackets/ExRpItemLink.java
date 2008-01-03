/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;

/**
 *
 * @author  KenM
 */
public final class ExRpItemLink extends L2GameServerPacket
{
    private final static String S_FE_6C_EXPRPITEMLINK = "[S] FE:6C ExRpItemLink";
    private final L2ItemInstance _item;
    
    public ExRpItemLink(L2ItemInstance item)
    {
        _item = item;
    }
    
    /**
     * @see net.sf.l2j.gameserver.serverpackets.L2GameServerPacket#getType()
     */
    @Override
    public String getType()
    {
        return S_FE_6C_EXPRPITEMLINK;
    }

    /**
     * @see net.sf.l2j.gameserver.serverpackets.L2GameServerPacket#writeImpl()
     */
    @Override
    protected void writeImpl()
    {
        writeC(0xfe);
        writeH(0x6c);
        // guessing xD
        writeD(_item.getObjectId());
        writeD(_item.getItemId());
        writeD(_item.getCount());
        writeH(_item.getItem().getType2());
        writeD(_item.getItem().getBodyPart());
        writeH(_item.getEnchantLevel());
        writeH(_item.getCustomType2());  // item type3
        writeD(_item.isAugmented() ? _item.getAugmentation().getAugmentationId() : 0x00);
        writeH(0x00);
        writeD(_item.getMana());
        // T1
        writeD(-2);
        writeD(0x00);
        writeD(0x00);
        writeD(0x00);
        writeD(0x00);
        writeD(0x00);
        writeD(0x00);
        writeD(0x00);
        
    }
    
}
