package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.recipes.model.L2Recipe;
import net.sf.l2j.gameserver.recipes.service.L2RecipeService;
import net.sf.l2j.gameserver.registry.IServiceRegistry;
import net.sf.l2j.gameserver.serverpackets.RecipeBookItemList;
import net.sf.l2j.tools.L2Registry;

public class RequestRecipeBookDestroy extends L2GameClientPacket 
{
    private static final String _C__AC_REQUESTRECIPEBOOKDESTROY = "[C] AD RequestRecipeBookDestroy";
    //private final static Log _log = LogFactory.getLog(RequestSellItem.class.getName());

    private int _RecipeID;

    /**
    * Unknown Packet:ad
    * 0000: ad 02 00 00 00
    */
    protected void readImpl()
    {
        _RecipeID = readD();
    }
            
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar != null)
        {
            L2RecipeService l2RecipeService = (L2RecipeService) L2Registry.getBean(IServiceRegistry.RECIPE);
        	L2Recipe rp =l2RecipeService.getRecipeList(_RecipeID-1) ;
         	if (rp == null) 
         		return;
            activeChar.unregisterRecipeList(_RecipeID);
            
            RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(),activeChar.getStat().getMaxMp()); 
         	if (rp.isDwarvenRecipe()) 
         		response.addRecipes(activeChar.getDwarvenRecipeBook()); 
         	else 
         		response.addRecipes(activeChar.getCommonRecipeBook()); 
            
            activeChar.sendPacket(response);
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType() 
    {
        return _C__AC_REQUESTRECIPEBOOKDESTROY;
    }
}