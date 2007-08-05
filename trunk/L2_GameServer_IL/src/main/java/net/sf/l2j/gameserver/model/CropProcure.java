/**
 * 
 */
package net.sf.l2j.gameserver.model;

/**
 * @author zabbix
 *
 */
public class CropProcure
{
    private int _cropId;
    private int _canBuy;
    private int _rewardType;
    
    public CropProcure(int id, int amount, int type)
    {
        _cropId = id;
        _canBuy = amount;
        _rewardType = type;
    }
    
    public int getReward(){return _rewardType;}
    public int getId(){return _cropId;}
    public int getAmount(){return _canBuy;}
    // For edit the crops data by L2Emu team 26 - 19
    public void setReward(int rewardtype){ _rewardType = rewardtype;}
    public void setId(int id){ _cropId = id; }
    public void setAmount(int Amount){ _canBuy = Amount;}
}
