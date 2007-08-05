/**
 * 
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
