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
package net.sf.l2j.gameserver.model;

/**
 * @author zabbix
 *
 */
public class SeedProduction
{
    private int _seedId;
    private int _canProduce;
    private int _price;
    
    public SeedProduction(int id,int amount,int pri)
    {
        _seedId = id;
        _canProduce = amount;
        _price = pri;
    }
    
    public int getSeedId(){return _seedId;}
    public int getCanProduce(){return _canProduce;}
    //For edit the seed data by l2Emu team 23 - 29
    public void setCanProduce(int amount){ _canProduce = amount;}
    public void setSeedId(int id){ _seedId = id;}
    public void setPrice(int pri){ _price = pri;}
    public int getPrice(){return _price;}   
}
