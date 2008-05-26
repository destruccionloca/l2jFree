package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RequestBlock extends ClientBasePacket
{
   private static final String _C__A0_REQUESTBLOCK = "[C] A0 RequestBlock";
   private final static Log _log = LogFactory.getLog(L2PcInstance.class.getName());
   
   private final static int BLOCK = 0;
   private final static int UNBLOCK = 1;
   private final static int BLOCKLIST = 2;
   private final static int ALLBLOCK = 3;
   private final static int ALLUNBLOCK = 4;
   
   private String _name;
   private Integer _type;
   private L2PcInstance _target;
   
   public RequestBlock(ByteBuffer buf, ClientThread client)
   {
       super(buf, client);
       _type = readD(); //0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock

       if( _type == BLOCK || _type == UNBLOCK )
       {
           _name = readS();
           _target = L2World.getInstance().getPlayer(_name);
       }
   }
   
   void runImpl()
   {
       L2PcInstance activeChar = getClient().getActiveChar();
       
       if (activeChar == null)
    	    return;
       
       SystemMessage sm;
       
       switch (_type)
       {
           case BLOCK:
           {
               if (_target == null)
               {
                   // Incorrect player name.
                   sm = new SystemMessage(SystemMessage.FAILED_TO_REGISTER_TO_IGNORE_LIST);
                   activeChar.sendPacket(sm);
                   return;
               }
               
               if (_target.isGM())
               {
                   // Cannot block a GM character.
                   sm = new SystemMessage(SystemMessage.YOU_MAY_NOT_IMPOSE_BLOCK_ON_GM);
                   activeChar.sendPacket(sm);
                   return;
               }
               
               L2BlockList.addToBlockList(activeChar, _target);
               
               sm = new SystemMessage(SystemMessage.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
               sm.addString(_target.getName());
               activeChar.sendPacket(sm);
              
               sm = new SystemMessage(SystemMessage.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
               sm.addString(activeChar.getName());
               _target.sendPacket(sm);
               
               break;
           }
           case UNBLOCK:
           {
        	   if (L2BlockList.isInBlockList(activeChar, _name))
        	   {
        		   if (_target == null)
                   {
                	   L2BlockList.removeFromBlockList(activeChar, _name);
                   } else
                	   L2BlockList.removeFromBlockList(activeChar, _target);
                   
        		   sm = new SystemMessage(SystemMessage.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
                   sm.addString(_name);
                   activeChar.sendPacket(sm);
        	   }
               break;
           }
           case BLOCKLIST:
           {
        	   sm = new SystemMessage(SystemMessage.BLOCK_LIST_HEADER);
               activeChar.sendPacket(sm);
        	   for (String blockname : L2BlockList.getBlockNames(activeChar))
        		   activeChar.sendMessage(blockname);
               break;
           }
           case ALLBLOCK:
           {
    		   sm = new SystemMessage(SystemMessage.MESSAGE_REFUSAL_MODE);
               activeChar.sendPacket(sm);
               L2BlockList.setBlockAll(activeChar, true);
               break;
           }
           case ALLUNBLOCK:
           {
        	   sm = new SystemMessage(SystemMessage.MESSAGE_ACCEPTANCE_MODE);
               activeChar.sendPacket(sm);
               L2BlockList.setBlockAll(activeChar, false);
               break;
           }
           default:
               _log.info("Unknown 0x0a block type: " + _type);
       }
   }
   
   public String getType()
   {
       return _C__A0_REQUESTBLOCK;
   }
}