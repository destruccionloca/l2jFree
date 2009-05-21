package net.sf.l2j.gameserver.serverpackets;
/**
 * This clas is only for test I try to use the format but dont works
 * Done By L2Emuproject Team
 * User: Scar69
 * Date: Jan 12, 2006
 * Time: 22:13 PM GMT+1
 */

public class ExShowSeedInfo extends ServerBasePacket{
    private static final String _S__FE_1C_EXSHOWSEEDINFO = "[S] FE:1C ExShowSeedInfo";
     @Override
        void runImpl()
        {
            // no long running
        }
     @Override
        void writeImpl()
        {
            writeC(0xFE);
            writeH(0x1C); 
            writeD(5016);
            writeD(5016);  
            writeD(5016);   
            writeD(5016);
            writeD(5016);
            writeC(3);
        }
     @Override
        public String getType()
        {
         return _S__FE_1C_EXSHOWSEEDINFO;
        }
    

}
