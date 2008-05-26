package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2Clan.SubPledge;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 *
 * sample
 * 0000: 68 
 * b1010000 
 * 48 00 61 00 6d 00 62 00 75 00 72 00 67 00 00 00   H.a.m.b.u.r.g...
 * 43 00 61 00 6c 00 61 00 64 00 6f 00 6e 00 00 00   C.a.l.a.d.o.n...
 * 00000000  crestid | not used (nuocnam)
 * 00000000 00000000 00000000 00000000 
 * 22000000 00000000 00000000 
 * 00000000 ally id
 * 00 00    ally name
 * 00000000 ally crrest id 
 * 
 * 02000000
 *  
 * 6c 00 69 00 74 00 68 00 69 00 75 00 6d 00 31 00 00 00  l.i.t.h.i.u.m...
 * 0d000000     level 
 * 12000000     class id
 * 00000000     
 * 01000000     offline 1=true
 * 00000000
 *  
 * 45 00 6c 00 61 00 6e 00 61 00 00 00   E.l.a.n.a...
 * 08000000 
 * 19000000 
 * 01000000 
 * 01000000 
 * 00000000
 * 
 *  
 * format   dSS dddddddddSd d (Sddddd)
 *          
 * @version $Revision: 1.6.2.2.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgeShowMemberListAll extends ServerBasePacket
{
    private static final String _S__68_PLEDGESHOWMEMBERLISTALL = "[S] 53 PledgeShowMemberListAll";
    private L2Clan _clan;
    private L2PcInstance _activeChar;
    private L2ClanMember[] _members;
    private int _pledgeType;
    
    public PledgeShowMemberListAll(L2Clan clan, L2PcInstance activeChar, int val1, int val2, int val3, int val4, int val5, int val6, int val7)
    {
        _clan = clan;
        _activeChar = activeChar;
        activeChar.sendPacket(new PledgeShowMemberListDeleteAll());
    }

    final void writeImpl()
    {
        
        _pledgeType = 0;
        writePledge(0);
        
        SubPledge[] subPledge = _clan.getAllSubPledges();
        for (int i = 0; i<subPledge.length; i++)
        {
            _activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(subPledge[i]));
        }
        
        for (L2ClanMember m : _members)
        {
            if (m.getPledgeType() == 0) continue;
            _activeChar.sendPacket(new PledgeShowMemberListAdd(m));
        }

        // unless this is sent sometimes, the client doesn't recognise the player as the leader
        _activeChar.sendPacket(new UserInfo(_activeChar));
                
    }
    
    public PledgeShowMemberListAll(L2Clan clan, L2PcInstance activeChar)
    {
        _clan = clan;
        _activeChar = activeChar;
        activeChar.sendPacket(new PledgeShowMemberListDeleteAll());
        SubPledge[] subPledge = _clan.getAllSubPledges();
        //final PledgeReceiveSubPledgeCreated response = new PledgeReceiveSubPledgeCreated(subPledge);
        for (int i = 0; i<subPledge.length; i++)
        {
            activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(subPledge[i]));
        }
        /*
        _subMembers = _clan.getSubMembers();
        for (L2ClanMember m : _subMembers)
        {
            if (m.getName() == activeChar.getName())
                activeChar.sendPacket(new PledgeShowMemberListAdd(m, 1));
            else
                activeChar.sendPacket(new PledgeShowMemberListAdd(m));
        }*/
    }
    
    final void runImpl()
    {
        _members = _clan.getMembers();
    }
    
    void writePledge(int mainOrSubpledge)
    {
        if (getClient().getRevision() >= 690)
        {
            writeC(0x53);
            writeD(mainOrSubpledge); //c5 main clan 0 or any subpledge 1?
            writeD(_clan.getClanId());
            writeD(_pledgeType); //c5 - possibly pledge type?
            writeS(_clan.getName());
            writeS(_clan.getLeaderName());
            writeD(_clan.getCrestId()); // creast id .. is used again
            writeD(_clan.getLevel());
            writeD(_clan.getHasCastle());
            writeD(_clan.getHasHideout());
            writeD(_clan.getRank());
            writeD(_clan.getReputationScore()); 
            writeD(0);
            writeD(0);
            
            writeD(_clan.getAllyId());
            writeS(_clan.getAllyName());
            writeD(_clan.getAllyCrestId());
            writeD(_clan.isAtWar());// new c3
            writeD(_pledgeType == 0 ? _clan.getMembersCount() :_clan.getSubPledgeMembersCount(_pledgeType));
            for (L2ClanMember m : _members)
            {
                writeS(m.getName());
                writeD(m.getLevel());
                writeD(m.getClassId());
                writeD(0); 
                writeD(m.getObjectId());//writeD(1);             
                writeD(m.isOnline() ? 1 : 0);  // 1=online 0=offline
                writeD(0);// does a clan member have a sponsor, 0 - true, 1 - false
            }
        }
        else
        {
            writeC(0x53);
            writeD(_clan.getClanId());
            writeS(_clan.getName());
            writeS(_clan.getLeaderName());
            writeD(_clan.getCrestId()); // creast id .. is used again
            writeD(_clan.getLevel());
            writeD(_clan.getHasCastle());
            writeD(_clan.getHasHideout());
            writeD(0);
            writeD(_activeChar.getLevel()); 
            writeD(0);
            writeD(0);
            
            writeD(_clan.getAllyId());
            writeS(_clan.getAllyName());
            writeD(_clan.getAllyCrestId());
            writeD(_clan.isAtWar());// new c3
            writeD(_members.length);
            for (L2ClanMember m : _members)
            {
                writeS(m.getName());
                writeD(m.getLevel());
                writeD(m.getClassId());
                writeD(0); 
                writeD(1);
                writeD(m.isOnline() ? m.getObjectId() : 0); // 1=online 0=offline
            }
        }       
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__68_PLEDGESHOWMEMBERLISTALL;
    }

}