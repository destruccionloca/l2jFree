package com.l2jfree.gameserver.model.actor.view;


/**
 * @author NB4L1
 */
public interface CharLikeView
{
	public void refresh();
	
	public int getObjectId();
	
	/*public String getName();
	
	public String getTitle();*/

	public int getX();
	
	public int getY();
	
	public int getZ();
	
	public int getHeading();
	
	/*public int getMAtkSpd();
	
	public int getPAtkSpd();*/
	
	public int getRunSpd();
	
	public int getWalkSpd();
	
	public int getSwimRunSpd();
	
	public int getSwimWalkSpd();
	
	public int getFlRunSpd();
	
	public int getFlWalkSpd();
	
	public int getFlyRunSpd();
	
	public int getFlyWalkSpd();
	
	/*public int getCollisionHeight();
	
	public int getCollisionRadius();*/
}
