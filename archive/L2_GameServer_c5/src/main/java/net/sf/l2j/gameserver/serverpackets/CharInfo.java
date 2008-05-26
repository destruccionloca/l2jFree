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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * 0000: 03 32 15 00 00 44 fe 00 00 80 f1 ff ff 00 00 00    .2...D..........<p>
 * 0010: 00 6b b4 c0 4a 45 00 6c 00 6c 00 61 00 6d 00 69    .k..JE.l.l.a.m.i<p>
 * 0020: 00 00 00 01 00 00 00 01 00 00 00 12 00 00 00 00    ................<p>
 * 0030: 00 00 00 2a 00 00 00 42 00 00 00 71 02 00 00 31    ...*...B...q...1<p>
 * 0040: 00 00 00 18 00 00 00 1f 00 00 00 25 00 00 00 00    ...........%....<p>
 * 0050: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 f9    ................<p>
 * 0060: 00 00 00 b3 01 00 00 00 00 00 00 00 00 00 00 7d    ...............}<p>
 * 0070: 00 00 00 5a 00 00 00 32 00 00 00 32 00 00 00 00    ...Z...2...2....<p>
 * 0080: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 67    ...............g<p>
 * 0090: 66 66 66 66 66 f2 3f 5f 63 97 a8 de 1a f9 3f 00    fffff.?_c.....?.<p>
 * 00a0: 00 00 00 00 00 1e 40 00 00 00 00 00 00 37 40 01    .............7..<p>
 * 00b0: 00 00 00 01 00 00 00 01 00 00 00 00 00 c1 0c 00    ................<p>
 * 00c0: 00 00 00 00 00 00 00 00 00 01 01 00 00 00 00 00    ................<p>
 * 00d0: 00 00<p>
 * <p>
 *  dddddSdddddddddddddddddddddddddddffffdddSdddccccccc (h)<p>
 *  dddddSdddddddddddddddddddddddddddffffdddSdddddccccccch
 *  dddddSddddddddddddddddddddddddddddffffdddSdddddccccccch (h) c (dchd) ddc dcc c cddd d
 *  dddddSdddddddddddddddhhhhhhhhhhhhhhhhhhhhhhhhddddddddddddddffffdddSdddddccccccch chaotic throne


 * @version $Revision: 1.7.2.6.2.11 $ $Date: 2005/04/11 10:05:54 $
 */
public class CharInfo extends ServerBasePacket
{

    private static final String _S__03_CHARINFO = "[S] 03 CharInfo";
    private L2PcInstance _cha;
    private Inventory _inv;
    private int _rhand, _lhand;
    private int _x, _y, _z, _heading;
    private int _mAtkSpd, _pAtkSpd;
    private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
    private float moveMultiplier;
    private String _name = "";
    private String _title = "";
    private boolean _isAttackable;    
    
    /**
     * @param _characters
     */
    public CharInfo(L2PcInstance cha)
    {
        _cha = cha;
        _inv = cha.getInventory();
        _x = _cha.getX();
        _y = _cha.getY();
        _z = _cha.getZ();
        _heading = _cha.getHeading();
        _mAtkSpd = _cha.getMAtkSpd();
        _pAtkSpd = _cha.getPAtkSpd();
        if (_cha.getPoly().isMorphed() && Config.ALT_POLYMORPH)
        {
           _rhand = NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).rhand;
           _lhand = NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).lhand;
           
           if (NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).serverSideName)
               _name = NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).name;
   
           if (NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).serverSideTitle)
               _title = NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).title;
           else
               _title = null;
           
           String type = NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).type;
           
           if (Config.SHOW_NPC_LVL && "L2Monster".equals(type))
           {
               String t = "Lv " + NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).level + (NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).aggroRange > 0 ? "*" : "");
               if (_title != null)
                   t += " " + _title;
               _title = t;
           }
           
           if ("L2Monster".equals(type) || "L2Minion".equals(type) || "L2Chest".equals(type) || "L2RaidBoss".equals(type) ||
                   "L2Boss".equals(type) || "L2FestivalMonster".equals(type) ||    "L2PenaltyMonster".equals(type))
               _isAttackable = true;
           else
               _isAttackable = false;
        }
        else
        {
           _rhand = _inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND);
           _lhand = _inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND);
           _name = _cha.getName();
           _title = _cha.getTitle();
        }
        //moveMultiplier  = _cha.getMovementSpeedMultiplier();

        //_runSpd         = (int)(_cha.getRunSpeed()/moveMultiplier);
        //_walkSpd        = (int)(_cha.getWalkSpeed()/moveMultiplier);
        //_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
        //_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
    }


    final void runImpl()
    {
        // no long-running tasks
        moveMultiplier  = _cha.getMovementSpeedMultiplier();
        _runSpd         = (int)(_cha.getRunSpeed()/moveMultiplier);
        _walkSpd        = (int)(_cha.getWalkSpeed()/moveMultiplier);
        _swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
        _swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
    }
    
    final void writeImpl()
    {
        if (_cha.getAppearance().getInvisible())
            return;
        if (_cha.getPoly().isMorphed())
        {
            writeC(0x16);
            writeD(_cha.getObjectId());
            writeD(_cha.getPoly().getPolyId()+1000000);  // npctype id
            if(Config.ALT_POLYMORPH)
                writeD(_isAttackable ? 1 : 0);
            else
                writeD(_cha.getKarma() > 0 ? 1 : 0); 
            writeD(_x);
            writeD(_y);
            writeD(_z);
            writeD(_heading);
            writeD(0x11);
            writeD(_mAtkSpd);
            writeD(_pAtkSpd);
            writeD(_runSpd);
            writeD(_walkSpd);
            writeD(_swimRunSpd/*0x32*/);  // swimspeed
            writeD(_swimWalkSpd/*0x32*/);  // swimspeed
            writeD(_flRunSpd);
            writeD(_flWalkSpd);
            writeD(_flyRunSpd);
            writeD(_flyWalkSpd);
            writeF(_cha.getMovementSpeedMultiplier());
            writeF(_cha.getAttackSpeedMultiplier());
            writeF(NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).collisionRadius);
            writeF(NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).collisionHeight);
            if(Config.ALT_POLYMORPH)
                writeD(_rhand); // right hand weapon                
            else
                writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND)); // right hand weapon
            writeD(0x00);
            if(Config.ALT_POLYMORPH)
                writeD(_lhand); // left hand weapon
            else
                writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND)); // left hand weapon
            writeC(1);  // name above char 1=true ... ??
            writeC(_cha.isRunning() ? 1 : 0);
            writeC(_cha.isInCombat() ? 1 : 0);
            writeC(_cha.isAlikeDead() ? 1 : 0);
            writeC(_cha.getAppearance().getInvisible()? 1 : 0); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
            if(Config.ALT_POLYMORPH)
            {
                writeS(_name);//_cha.getName());
                writeS(_title);
            }
            else
            {
                writeS(_cha.getName());
                writeS(_cha.getTitle());
            }
            writeD(0);
            writeD(0);
            writeD(0000);  // hmm karma ??

            writeD(_cha.getAbnormalEffect());  // C2
            writeD(0000);  // C2
            writeD(0000);  // C2
            writeD(0000);  // C2
            writeD(0000);  // C2
            writeC(0000);  // C2
           
            if(_cha.getTeam()==1)
                writeC(0x01); //team circle around feet 1= Blue, 2 = red
            else if(_cha.getTeam()==2)
                writeC(0x02); //team circle around feet 1= Blue, 2 = red
            else
                writeC(0x00); //team circle around feet 1= Blue, 2 = red
            writeF(0x00);  // C4 i think it is collisionRadius a second time
            writeF(0x00);  // C4      "        collisionHeight     "
            writeD(0x00);  // C4 
        }
        else
        {
        writeC(0x03);       
        writeD(_x);
        writeD(_y);
        writeD(_z);
        writeD(_heading);
        writeD(_cha.getObjectId());
        L2PcInstance _cha2 = getClient().getActiveChar();
        if (_cha2 != null && _cha.getClan() != null && _cha2.getClan() != null && _cha.getClan().isAtWarWith(_cha2.getClanId()))
            writeS("*"+_cha.getName());
        else
            writeS(_cha.getName());
        writeD(_cha.getRace().ordinal());
        writeD(_cha.getAppearance().getSex()? 1 : 0);
                
        if (_cha.getClassIndex() == 0)
            writeD(_cha.getClassId().getId());
        else 
            writeD(_cha.getBaseClass());
        
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DHAIR));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
        if (getClient().getRevision() >= 729)
        {
            writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CLOAK));            
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
            writeH(0);
        }
        
        writeD(_cha.getPvpFlag());
        writeD(_cha.getKarma());

        writeD(_mAtkSpd);
        writeD(_pAtkSpd);
        
        writeD(_cha.getPvpFlag());
        writeD(_cha.getKarma());

        writeD(_runSpd);
        writeD(_walkSpd);
        writeD(_swimRunSpd/*0x32*/);  // swimspeed
        writeD(_swimWalkSpd/*0x32*/);  // swimspeed
        writeD(_flRunSpd);
        writeD(_flWalkSpd);
        writeD(_flyRunSpd);
        writeD(_flyWalkSpd);
        writeF(_cha.getMovementSpeedMultiplier()); // _cha.getProperMultiplier()
        writeF(_cha.getAttackSpeedMultiplier()); // _cha.getAttackSpeedMultiplier()
        writeF(_cha.getBaseTemplate().collisionRadius);
        writeF(_cha.getBaseTemplate().collisionHeight);

        writeD(_cha.getAppearance().getHairStyle());
        writeD(_cha.getAppearance().getHairColor());
        writeD(_cha.getAppearance().getFace());
        
        writeS(_cha.getTitle());
        writeD(_cha.getClanId());
        writeD(_cha.getClanCrestId());
        writeD(_cha.getAllyId());
        writeD(_cha.getAllyCrestId());
        writeD(0);  // new in rev 417   siege-flags
        
        writeC(_cha.isSitting() ? 0 : 1);   // standing = 1  sitting = 0
        writeC(_cha.isRunning() ? 1 : 0);   // running = 1   walking = 0
        writeC(_cha.isInCombat() ? 1 : 0);
        writeC(_cha.isAlikeDead() ? 1 : 0);
        
        writeC(_cha.getAppearance().getInvisible()? 1 : 0); // invisible = 1  visible =0
        writeC(_cha.getMountType());    // 1 on strider   2 on wyvern   0 no mount
        writeC(_cha.getPrivateStoreType());   //  1 - sellshop
        
        writeH(_cha.getCubics().size());
        for (int id : _cha.getCubics().keySet())
            writeH(id);
        
        writeC(0x00);   // find party members
        
        writeD(_cha.getAbnormalEffect());

//      Code that works for getEnchantEffect()

        writeC(_cha.getRecomLeft());                       //Changed by Thorgrim
        writeH(_cha.getRecomHave()); //Blue value for name (0 = white, 255 = pure blue)
        writeD(_cha.getClassId().getId());
        
        writeD(_cha.getMaxCp());
        writeD((int) _cha.getCurrentCp());
        writeC(_cha.isMounted() ? 0 : _cha.getEnchantEffect());
        
        if(_cha.getTeam()==1)
           writeC(0x01); //team circle around feet 1= Blue, 2 = red
        else if(_cha.getTeam()==2)
           writeC(0x02); //team circle around feet 1= Blue, 2 = red
        else
           writeC(0x00); //team circle around feet 1= Blue, 2 = red
        
        writeD(_cha.getClanCrestLargeId()); 
        writeC(_cha.isNoble() ? 1 : 0); // Symbol on char menu ctrl+I
        writeC((_cha.isHero() || (_cha.isGM() && Config.GM_HERO_AURA)) ? 1 : 0); // Hero Aura 
        
        writeC(_cha.isFishing() ? 1 : 0); //0x01: Fishing Mode (Cant be undone by setting back to 0)
        writeD(_cha.GetFishx());  
        writeD(_cha.GetFishy());
        writeD(_cha.GetFishz());
        writeD(_cha.getAppearance().getNameColor());

        //          writeC(_cha.isRunning() ? 0x01 : 0x00); //changes the Speed display on Status Window 
        writeD(0x00); // ??
        
        writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FACE));
        
        writeD(_cha.getPledgeClass()); 
        writeD(0x00); // ??
        
        writeD(_cha.getAppearance().getTitleColor());
        
        if (_cha.isCursedWeaponEquiped())
            writeD(CursedWeaponsManager.getInstance().getLevel(_cha.getCursedWeaponEquipedId()));
        else
            writeD(0x00);
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__03_CHARINFO;
    }
    
}