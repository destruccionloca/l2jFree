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
package net.sf.l2j.gameserver.network;

import java.nio.ByteBuffer;
import java.util.concurrent.RejectedExecutionException;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.clientpackets.*;
import net.sf.l2j.tools.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jserver.mmocore.network.IClientFactory;
import com.l2jserver.mmocore.network.IMMOExecutor;
import com.l2jserver.mmocore.network.IPacketHandler;
import com.l2jserver.mmocore.network.MMOConnection;
import com.l2jserver.mmocore.network.ReceivablePacket;

/**
 * Stateful Packet Handler<BR>
 * The Stateful approach prevents the server from handling inconsistent packets, examples:<BR>
 * <li>Clients sends a MoveToLocation packet without having a character attached. (Potential errors handling the packet).</li>
 * <li>Clients sends a RequestAuthLogin being already authed. (Potential exploit).</li>
 * <BR><BR>
 * Note: If for a given exception a packet needs to be handled on more then one state, then it should be added to all these states.
 * @author  KenM
 */
public final class L2GamePacketHandler implements IPacketHandler<L2GameClient>, IClientFactory<L2GameClient>, IMMOExecutor<L2GameClient>
{
	private static final Log _log = LogFactory.getLog(L2GamePacketHandler.class.getName());
	
	public ReceivablePacket<L2GameClient> handlePacket(ByteBuffer buf, L2GameClient client)
	{
		int opcode = buf.get() & 0xFF;
		
		ReceivablePacket<L2GameClient> msg = null;
		GameClientState state = client.getState();
		
		switch (state)
		{
			case CONNECTED:
				switch (opcode)
				{
					case 0:
						msg = new RequestAuthSequence();
						break;
					case 14:
						msg = new ProtocolVersion();
						break;
					case 43:
		                msg = new AuthLogin();
		                break;
					default:
						printDebug(opcode, buf, state, client);
					break;
				}
				break;
			case AUTHED:
				switch (opcode)
				{
					case 0:
						msg = new Logout();
						break;
					case 12:
						msg = new CharacterCreate();	
						break;
					case 13:
						msg = new CharacterDelete();	
						break;
					case 18:
						msg = new CharacterSelected();
						break;
					case 19:
						msg = new NewCharacter();
						break;
					case 123:
						msg = new CharacterRestore();
						break;
		            case 208: 
		                int idx = buf.getShort() & 0xffff;
		                switch(idx)
		                {
		                	case 57:
		                		msg = new CharacterPrevState();
		                		break;
		                }
		                break;
					default:
						printDebug(opcode, buf, state, client);
						break;
				}
				break;
			case IN_GAME:
				switch(opcode)
				{
	            	case 0:
	            		msg = new Logout();
	            		break;
	                case 1:
	                    msg = new AttackRequest();
	                    break;
	                case 3:
	                    msg = new RequestStartPledgeWar();
	                    break;
	                case 4:
	                    msg = new RequestReplyStartPledgeWar();
	                    break;
	                case 5:
	                    msg = new RequestStopPledgeWar();
	                    break;
	                case 6:
	                    msg = new RequestReplyStopPledgeWar();
	                    break;
	                case 7:
	                    msg = new GameGuardReply();
	                    break;
	                case 9:
	                    msg = new RequestSetPledgeCrest();
	                    break;
	                case 11:
	                    msg = new RequestGiveNickName();
	                    break;
	                case 15:
	                    msg = new MoveBackwardToLocation();
	                    break;
	                case 17:
	                    msg = new EnterWorld();
	                    break;
	                case 20:
	                    msg = new RequestItemList();
	                    break;
	                case 22:
	                    msg = new RequestUnEquipItem();
	                    break;
	                case 23:
	                    msg = new RequestDropItem();
	                    break;
	                case 25:
	                    msg = new UseItem();
	                    break;
	                case 26:
	                    msg = new TradeRequest();
	                    break;
	                case 27:
	                    msg = new AddTradeItem();
	                    break;
	                case 28:
	                    msg = new TradeDone();
	                    break;
	                case 31:
	                    msg = new Action();
	                    break;
	                case 34:
	                    msg = new RequestLinkHtml();
	                    break;
	                case 35:
	                    msg = new RequestBypassToServer();
	                    break;
	                case 36:
	                    msg = new RequestBBSwrite();
	                    break;
	                case 38:
	                    msg = new RequestJoinPledge();
	                    break;
	                case 39:
	                    msg = new RequestAnswerJoinPledge();
	                    break;
	                case 40:
	                    msg = new RequestWithdrawalPledge();
	                    break;
	                case 41:
	                    msg = new RequestOustPledgeMember();
	                    break;
	                case 44:
	                    msg = new RequestGetItemFromPet();
	                    break;
	                case 46:
	                    msg = new RequestAllyInfo();
	                    break;
	                case 47:
	                    msg = new RequestCrystallizeItem();
	                    break;
	                case 49:
	                    msg = new SetPrivateStoreListSell();
	                    break;
	                case 52:
	                    msg = new RequestSocialAction();
	                    break;
	                case 53:
	                    msg = new ChangeMoveType2();
	                    break;
	                case 54:
	                    msg = new ChangeWaitType2();
	                    break;
	                case 55:
	                    msg = new RequestSellItem();
	                    break;
	                case 56:
	                    msg = new RequestMagicSkillList();
	                    break;
	                case 57:
	                    msg = new RequestMagicSkillUse();
	                    break;
	                case 58:
	                    msg = new Appearing();
	                    break;
	                case 59:
	                    msg = new SendWareHouseDepositList();
	                    break;
	                case 60:
	                    msg = new SendWareHouseWithDrawList();
	                    break;
	                case 61:
	                    msg = new RequestShortCutReg();
	                    break;
	                case 63:
	                    msg = new RequestShortCutDel();
	                    break;
	                case 64:
	                    msg = new RequestBuyItem();
	                    break;
	                case 66:
	                    msg = new RequestJoinParty();
	                    break;
	                case 67:
	                    msg = new RequestAnswerJoinParty();
	                    break;
	                case 68:
	                    msg = new RequestWithDrawalParty();
	                    break;
	                case 69:
	                    msg = new RequestOustPartyMember();
	                    break;
	                case 71:
	                    msg = new CannotMoveAnymore();
	                    break;
	                case 72:
	                    msg = new RequestTargetCanceld();
	                    break;
	                case 73:
	                    msg = new Say2();
	                    break;
	                case 77:
	                    msg = new RequestPledgeMemberList();
	                    break;
	                case 80:
	                    msg = new RequestMagicSkillList();
	                    break;
	                case 83:
	                    msg = new RequestGetOnVehicle();
	                    break;
	                case 84:
	                    msg = new RequestGetOffVehicle();
	                    break;
	                case 85:
	                    msg = new AnswerTradeRequest();
	                    break;
	                case 86:
	                    msg = new RequestActionUse();
	                    break;
	                case 87:
	                    msg = new RequestRestart();
	                    break;
	                case 89:
	                    msg = new ValidatePosition();
	                    break;
	                case 91:
	                    msg = new StartRotating();
	                    break;
	                case 92:
	                    msg = new FinishRotating();
	                    break;
	                case 94:
	                    msg = new RequestShowBoard();
	                    break;
	                case 95:
	                    msg = new RequestEnchantItem();
	                    break;
	                case 96:
	                    msg = new RequestDestroyItem();
	                    break;
	                case 98:
	                    msg = new RequestQuestList();
	                    break;
	                case 99:
	                    msg = new RequestQuestAbort();
	                    break;
	                case 101:
	                    msg = new RequestPledgeInfo();
	                    break;
	                case 102:
	                    msg = new RequestPledgeInfo();
	                    break;
	                case 103:
	                    msg = new RequestPledgeCrest();
	                    break;
	                case 107:
	                    msg = new RequestSendFriendMsg();
	                    break;
/*
	                case 108:
	                    msg = new RequestOpenMinimap();
	                    break;
*/
	                case 110:
	                    msg = new RequestRecordInfo();
	                    break;
	                case 111:
	                    msg = new RequestHennaEquip();
	                    break;
/*
	                case 112:
	                    msg = new RequestHennaUnequipList();
	                    break;

	                case 113:
	                    msg = new RequestHennaUnequipItemInfo();
	                    break;

	                case 114:
	                    msg = new RequestHennaUnEquip();
	                    break;
*/
	                case 115:
	                    msg = new RequestAquireSkillInfo();
	                    break;
	                case 116:
	                    msg = new SendBypassBuildCmd();
	                    break;
	                case 118:
	                    msg = new CannotMoveAnymore();
	                    break;
	                case 119:
	                    msg = new RequestFriendInvite();
	                    break;
	                case 120:
	                    msg = new RequestAnswerFriendInvite();
	                    break;
	                case 121:
	                    msg = new RequestFriendList();
	                    break;
	                case 122:
	                    msg = new RequestFriendDel();
	                    break;
	                case 124:
	                    msg = new RequestAquireSkill();
	                    break;
	                case 125:
	                    msg = new RequestRestartPoint();
	                    break;
	                case 126:
	                    msg = new RequestGMCommand();
	                    break;
	                case 131: 
	                    msg = new RequestPrivateStoreBuy();
	                    break;
/*
	                case 134: 
	                    msg = new RequestTutorialPassCmdToServer();
	                    break;
*/
	                case 135: 
	                    msg = new RequestTutorialQuestionMark();
	                    break;
	                case 136: 
	                    msg = new RequestTutorialClientEvent();
	                    break;
	                case 137: 
	                    msg = new RequestPetition();
	                    break;
	                case 138: 
	                    msg = new RequestPetitionCancel();
	                    break;
	                case 139: 
	                    msg = new RequestGmList();
	                    break;
	                case 140: 
	                    msg = new RequestJoinAlly();
	                    break;
	                case 141: 
	                    msg = new RequestAnswerJoinAlly();
	                    break;
	                case 144: 
	                    msg = new RequestDismissAlly();
	                    break;
	                case 145: 
	                    msg = new RequestSetAllyCrest();
	                    break;
	                case 146: 
	                    msg = new RequestAllyCrest();
	                    break;
	                case 147: 
	                    msg = new RequestChangePetName();
	                    break;
	                case 148: 
	                    msg = new RequestPetUseItem();
	                    break;
	                case 149: 
	                    msg = new RequestGiveItemToPet();
	                    break;
	                case 150: 
	                    msg = new RequestPrivateStoreQuitSell();
	                    break;
	                case 151: 
	                    msg = new SetPrivateStoreMsgSell();
	                    break;
	                case 152: 
	                    msg = new RequestPetGetItem();
	                    break;
	                case 153: 
	                    msg = new RequestPrivateStoreManageBuy();
	                    break;
	                case 154: 
	                    msg = new SetPrivateStoreListBuy();
	                    break;
	                case 156: 
	                    msg = new RequestPrivateStoreQuitBuy();
	                    break;
	                case 157: 
	                    msg = new SetPrivateStoreMsgBuy();
	                    break;
	                case 159: 
	                    msg = new RequestPrivateStoreSell();
	                    break;
	                case 167: 
	                    msg = new RequestPackageSendableItemList();
	                    break;
	                case 168: 
	                    msg = new RequestPackageSend();
	                    break;
	                case 169: 
	                    msg = new RequestBlock();
	                    break;
	                case 171: 
	                    msg = new RequestSiegeAttackerList();
	                    break;
	                case 172: 
	                    msg = new RequestSiegeDefenderList();
	                    break;
	                case 173: 
	                    msg = new RequestJoinSiege();
	                    break;
	                case 174: 
	                    msg = new RequestConfirmSiegeWaitingList();
	                    break;
	                case 176: 
	                    msg = new MultiSellChoose();
	                    break;
	                case 179: 
	                    msg = new RequestUserCommand();
	                    break;
	                case 180: 
	                    msg = new SnoopQuit();
	                    break;
	                case 181: 
	                    msg = new RequestRecipeBookOpen();
	                    break;
	                case 182: 
	                    msg = new RequestRecipeBookDestroy();
	                    break;
	                case 183: 
	                    msg = new RequestRecipeItemMakeInfo();
	                    break;
	                case 184: 
	                    msg = new RequestRecipeItemMakeSelf();
	                    break;
	                case 186: 
	                    msg = new RequestRecipeShopMessageSet();
	                    break;
	                case 187: 
	                    msg = new RequestRecipeShopListSet();
	                    break;
	                case 188: 
	                    msg = new RequestRecipeShopManageQuit();
	                    break;
	                case 190: 
	                    msg = new RequestRecipeShopMakeInfo();
	                    break;
	                case 191: 
	                    msg = new RequestRecipeShopMakeItem();
	                    break;
	                case 192: 
	                    msg = new RequestRecipeShopManagePrev();
	                    break;
	                case 193: 
	                    msg = new ObserverReturn();
	                    break;
	                case 194: 
	                    msg = new RequestEvaluate();
	                    break;
	                case 195: 
	                    msg = new RequestHennaList();
	                    break;
	                case 196: 
	                    msg = new RequestHennaItemInfo();
	                    break;
	                case 197: 
	                    msg = new RequestBuySeed();
	                    break;
	                case 198: 
	                    msg = new DlgAnswer();
	                    break;
	                case 199: 
	                    msg = new RequestWearItem();
	                    break;
	                case 200: 
	                    msg = new RequestSSQStatus();
	                    break;
	                case 203: 
	                    msg = new GameGuardReply();
	                    break;
	                case 204: 
	                    msg = new RequestPledgePower();
	                    break;
	                case 205: 
	                    msg = new RequestMakeMacro();
	                    break;
	                case 206: 
	                    msg = new RequestDeleteMacro();
	                    break;
	                case 207: 
	                    msg = new RequestBuyProcure();
	                    break;
	                case 208: 
		            	int id2 = -1;
		            	if (buf.remaining() >= 2)
		            	{
		            		id2 = buf.getShort() & 0xffff;
		            	}
		            	else
		            	{
		            		_log.warn("Client: "+client.toString()+" sent a 0xd0 without the second opcode.");
		            		break;
		            	}
		                switch (id2)
		                {
			                case 1:
			                    msg = new RequestManorList();
			                    break;
			                case 2:
			                    msg = new RequestProcureCropList();
			                    break;	
			                case 3:
			                    msg = new RequestSetSeed();
			                    break;	
			                case 4:
			                    msg = new RequestSetCrop();
			                    break;	
			                case 5:
			                    msg = new RequestWriteHeroWords();
			                    break;	
			                case 6:
			                    msg = new RequestExAskJoinMPCC();
			                    break;	
			                case 7:
			                    msg = new RequestExAcceptJoinMPCC();
			                    break;	
			                case 8:
			                    msg = new RequestExOustFromMPCC();
			                    break;	
			                case 9:
			                    msg = new RequestOustFromPartyRoom();
			                    break;	
			                case 10:
			                    msg = new RequestDismissPartyRoom();
			                    break;	
			                case 11:
			                    msg = new RequestWithdrawPartyRoom();
			                    break;	
			                case 12:
			                    msg = new RequestChangePartyLeader();
			                    break;	
			                case 13:
			                    msg = new RequestAutoSoulShot();
			                    break;	
			                case 14:
			                    msg = new RequestExEnchantSkillInfo();
			                    break;	
			                case 15:
			                    msg = new RequestExEnchantSkill();
			                    break;	
			                case 17:
			                    msg = new RequestExPledgeCrestLarge();
			                    break;	
			                case 18:
			                    msg = new RequestPledgeSetAcademyMaster();
			                    break;	
			                case 19:
			                    msg = new RequestPledgePowerGradeList();
			                    break;	
			                case 20:
			                    msg = new RequestPledgeMemberPowerInfo();
			                    break;	
			                case 21:
			                    msg = new RequestPledgeSetMemberPowerGrade();
			                    break;	
			                case 22:
			                    msg = new RequestPledgeMemberInfo();
			                    break;	
			                case 23:
			                    msg = new RequestPledgeWarList();
			                    break;	
			                case 24:
			                    msg = new RequestExFishRanking();
			                    break;	
			                case 27:
			                    msg = new RequestDuelStart();
			                    break;	
			                case 28:
			                    msg = new RequestDuelAnswerStart();
			                    break;
/*	
			                case 30:
			                    msg = new RequestExRqItemLink();
			                    break;	
			                case 33:
			                    msg = new RequestKeyMapping();
			                    break;	
			                case 34:
			                    msg = new RequestSaveKeyMapping();
		                    	break;	
			                case 36:
			                    msg = new RequestSaveInventoryOrder();
			                    break;
*/	
			                case 37:
			                    msg = new RequestExitPartyMatchingWaitingRoom();
			                    break;	
			                case 38:
			                    msg = new RequestConfirmTargetItem();
			                    break;	
			                case 39:
			                    msg = new RequestConfirmRefinerItem();
			                    break;	
			                case 40:
			                    msg = new RequestConfirmGemStone();
			                    break;	
			                case 41:
			                    msg = new RequestOlympiadObserverEnd();
			                    break;	
			                case 42:
			                    msg = new RequestCursedWeaponList();
			                    break;	
			                case 43:
			                    msg = new RequestCursedWeaponLocation();
			                    break;	
			                case 44:
			                    msg = new RequestPledgeReorganizeMember();
			                    break;	
			                case 46:
			                    msg = new RequestExMPCCShowPartyMembersInfo();
			                    break;	
			                case 47:
			                    msg = new RequestOlympiadMatchList();
			                    break;	
			                case 48:
			                    msg = new RequestAskJoinPartyRoom();
			                    break;	
			                case 49:
			                    msg = new AnswerJoinPartyRoom();
			                    break;
	
/*			                case 56:
			                    msg = new RequestEnchantItemAttribute();
			                    break;	
			                case 63:
			                    msg = new RequestAllCastleInfo();
			                    break;	
			                case 64:
			                    msg = new RequestAllFortressInfo();
			                    break;	
			                case 65:
			                    msg = new RequestAllAgitInfo();
			                    break;	
			                case 66:
			                    msg = new RequestOpenMinimap();
			                    break;
*/	
			                case 67:
			                    msg = new RequestGetBossRecord();
			                    break;	
			                case 68:
			                    msg = new RequestRefine();
			                    break;	
			                case 69:
			                    msg = new RequestConfirmCancelItem();
			                    break;
			                case 70:
			                    msg = new RequestRefineCancel();
			                    break;
			                case 71:
			                    msg = new RequestExMagicSkillUseGround();
			                    break;
			                case 72:
			                    msg = new RequestDuelSurrender();
			                    break;
		                    default: 
		                     	printDebugDoubleOpcode(opcode, id2, buf, state, client);
		                    	break;
		                }
						break;
		            /*case 0xee:
						msg = new RequestChangePartyLeader(data, _client);     
						break;*/
					default:
						printDebug(opcode, buf, state, client);
						break;
				}
				break;
		}
		return msg;
	}
	
	private void printDebug(int opcode, ByteBuffer buf, GameClientState state, L2GameClient client)
	{
		int size = buf.remaining(); 
     	_log.warn("Unknown Packet: "+Integer.toHexString(opcode)+" on State: "+state.name()+" Client: "+client.toString()); 
     	byte[] array = new byte[size];
     	buf.get(array);
     	_log.warn(Util.printData(array, size));
	}
	
	private void printDebugDoubleOpcode(int opcode, int id2, ByteBuffer buf, GameClientState state, L2GameClient client)
	{
		int size = buf.remaining(); 
     	_log.warn("Unknown Packet: "+Integer.toHexString(opcode)+":" + Integer.toHexString(id2)+" on State: "+state.name()+" Client: "+client.toString()); 
     	byte[] array = new byte[size]; 
     	buf.get(array);
     	_log.warn(Util.printData(array, size));
	}

	// impl
	public L2GameClient create(MMOConnection<L2GameClient> con)
	{
		return new L2GameClient(con);
	}
	
	public void execute(ReceivablePacket<L2GameClient> rp)
	{
		try
		{
			if (rp.getClient().getState() == GameClientState.IN_GAME)
			{
				ThreadPoolManager.getInstance().executePacket(rp);
			}
			else
			{
				ThreadPoolManager.getInstance().executeIOPacket(rp);
			}
		}
		catch (RejectedExecutionException e)
		{
			// if the server is shutdown we ignore
			if (!ThreadPoolManager.getInstance().isShutdown())
			{
				_log.fatal("Failed executing: "+rp.getClass().getSimpleName()+" for Client: "+rp.getClient().toString());
			}
		}
	}
}
