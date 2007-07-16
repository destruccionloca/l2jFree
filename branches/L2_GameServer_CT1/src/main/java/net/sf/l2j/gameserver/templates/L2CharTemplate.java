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
package net.sf.l2j.gameserver.templates;

/**
 * Base template for all type of characters
 * this template has property that will be set by setters.
 * <br/>
 * <br/>
 * <font color="red">
 * <b>Property don't change in the time, this is just a template, not the currents status 
 * of characters !</b>
 * </font>
 */
public class L2CharTemplate
{
	// BaseStats
	private  int baseSTR;
    private  int baseCON;
    private  int baseDEX;
    private  int baseINT;
    private  int baseWIT;
    private  int baseMEN;
    private  float baseHpMax;
    private  float baseCpMax;
    private  float baseMpMax;
	
	/** HP Regen base */
    private  float baseHpReg;
	
	/** MP Regen base */
    private  float baseMpReg;
       
    private  int basePAtk;
    private  int baseMAtk;
    private  int basePDef;
    private  int baseMDef;
	private  int basePAtkSpd;
	private  int baseMAtkSpd;
	private  float baseMReuseRate;
	private  int baseShldDef;
	private  int baseAtkRange;
	private  int baseShldRate;
	private  int baseCritRate;
	private  int baseRunSpd;
	// SpecialStats
	private  int baseBreath;
	private  int baseAggression;
	private  int baseBleed;
	private  int basePoison;
	private  int baseStun;
	private  int baseRoot;
	private  int baseMovement;
	private  int baseConfusion;
	private  int baseSleep;
	private  int baseFire;
	private  int baseWind;
	private  int baseWater;
	private  int baseEarth;
	private  int baseHoly;
	private  int baseDark;	
	private  int baseAggressionRes;
	private  int baseBleedRes;
	private  int basePoisonRes;
	private  int baseStunRes;
	private  int baseRootRes;
	private  int baseMovementRes;
	private  int baseConfusionRes;
	private  int baseSleepRes;
	private  int baseFireRes;
	private  int baseWindRes;
	private  int baseWaterRes;
	private  int baseEarthRes;
	private  int baseHolyRes;
	private  int baseDarkRes;

	private  boolean isUndead;
	
    //C4 Stats
    private  int baseMpConsumeRate;
    private  int baseHpConsumeRate;
	
	private  int collisionRadius;   
	private  int collisionHeight;
	
    /**
     * Empty constructor (we have to use setter to initialize the object).
     * 
     * Be carefull, setter don't do the same verification that instantiation with statset {@link #L2CharTemplate(StatsSet)}
     * Don't use it ! 
     * This constructor is designed for hibernate
     */
    public L2CharTemplate()
    {
    }
    
	public L2CharTemplate(StatsSet set)
	{
		// Base stats
		baseSTR            = set.getInteger("baseSTR");
		baseCON            = set.getInteger("baseCON");
		baseDEX            = set.getInteger("baseDEX");
		baseINT            = set.getInteger("baseINT");
		baseWIT            = set.getInteger("baseWIT");
		baseMEN            = set.getInteger("baseMEN");
		baseHpMax          = set.getFloat ("baseHpMax");
    	baseCpMax          = set.getFloat("baseCpMax");
		baseMpMax          = set.getFloat ("baseMpMax");
		baseHpReg          = set.getFloat ("baseHpReg");
		baseMpReg          = set.getFloat ("baseMpReg");
		basePAtk           = set.getInteger("basePAtk");
		baseMAtk           = set.getInteger("baseMAtk");
		basePDef           = set.getInteger("basePDef");
		baseMDef           = set.getInteger("baseMDef");
		basePAtkSpd        = set.getInteger("basePAtkSpd");
		baseMAtkSpd        = set.getInteger("baseMAtkSpd");
		baseMReuseRate     = set.getFloat ("baseMReuseDelay", 1.f);
		baseShldDef        = set.getInteger("baseShldDef");
		baseAtkRange       = set.getInteger("baseAtkRange");
		baseShldRate       = set.getInteger("baseShldRate");
		baseCritRate       = set.getInteger("baseCritRate");
		baseRunSpd         = set.getInteger("baseRunSpd");
		// SpecialStats
		baseBreath         = set.getInteger("baseBreath",         100);
		baseAggression     = set.getInteger("baseAggression",     0);
		baseBleed          = set.getInteger("baseBleed",          0);
		basePoison         = set.getInteger("basePoison",         0);
		baseStun           = set.getInteger("baseStun",           0);
		baseRoot           = set.getInteger("baseRoot",           0);
		baseMovement       = set.getInteger("baseMovement",       0);
		baseConfusion      = set.getInteger("baseConfusion",      0);
		baseSleep          = set.getInteger("baseSleep",          0);
		baseFire           = set.getInteger("baseFire",           0);
		baseWind           = set.getInteger("baseWind",           0);
		baseWater          = set.getInteger("baseWater",          0);
		baseEarth          = set.getInteger("baseEarth",          0);
		baseHoly           = set.getInteger("baseHoly",           0);
		baseDark           = set.getInteger("baseDark",           0);
		baseAggressionRes  = set.getInteger("baseAaggressionRes", 1); // setting baseRes stat to 1 for mul type effects in DP
		baseBleedRes       = set.getInteger("baseBleedRes",       1);
		basePoisonRes      = set.getInteger("basePoisonRes",      1);
		baseStunRes        = set.getInteger("baseStunRes",        1);
		baseRootRes        = set.getInteger("baseRootRes",        1);
		baseMovementRes    = set.getInteger("baseMovementRes",    1);
		baseConfusionRes   = set.getInteger("baseConfusionRes",   1);
		baseSleepRes       = set.getInteger("baseSleepRes",       1);
		baseFireRes        = set.getInteger("baseFireRes",        1);
		baseWindRes        = set.getInteger("baseWindRes",        1);
		baseWaterRes       = set.getInteger("baseWaterRes",       1);
		baseEarthRes       = set.getInteger("baseEarthRes",       1);
		baseHolyRes        = set.getInteger("baseHolyRes",        1);
		baseDarkRes        = set.getInteger("baseDarkRes",        1);
		
		isUndead			= (set.getInteger("isUndead", 0) == 1);
        
        //C4 Stats
        baseMpConsumeRate      = set.getInteger("baseMpConsumeRate",        0);
        baseHpConsumeRate      = set.getInteger("baseHpConsumeRate",        0);
		
		// Geometry
		collisionRadius    = set.getInteger("collision_radius");
		collisionHeight    = set.getInteger("collision_height");
	}

    /**
     * @return the baseAggression
     */
    public int getBaseAggression()
    {
        return baseAggression;
    }

    /**
     * @param baseAggression the baseAggression to set
     */
    public void setBaseAggression(int baseAggression)
    {
        this.baseAggression = baseAggression;
    }

    /**
     * @return the baseAggressionRes
     */
    public int getBaseAggressionRes()
    {
        return baseAggressionRes == 0 ? 1 : baseAggressionRes;
    }

    /**
     * @param baseAggressionRes the baseAggressionRes to set
     */
    public void setBaseAggressionRes(int baseAggressionRes)
    {
        this.baseAggressionRes = baseAggressionRes;
    }

    /**
     * @return the baseAtkRange
     */
    public int getBaseAtkRange()
    {
        return baseAtkRange;
    }

    /**
     * @param baseAtkRange the baseAtkRange to set
     */
    public void setBaseAtkRange(int baseAtkRange)
    {
        this.baseAtkRange = baseAtkRange;
    }

    /**
     * @return the baseBleed
     */
    public int getBaseBleed()
    {
        return baseBleed;
    }

    /**
     * @param baseBleed the baseBleed to set
     */
    public void setBaseBleed(int baseBleed)
    {
        this.baseBleed = baseBleed;
    }

    /**
     * @return the baseBleedRes
     */
    public int getBaseBleedRes()
    {
        return baseBleedRes == 0 ? 1 : baseBleedRes;
    }

    /**
     * @param baseBleedRes the baseBleedRes to set
     */
    public void setBaseBleedRes(int baseBleedRes)
    {
        this.baseBleedRes = baseBleedRes;
    }

    /**
     * @return the baseBreath
     */
    public int getBaseBreath()
    {
        return baseBreath == 0 ? 100 : baseBreath;
    }

    /**
     * @param baseBreath the baseBreath to set
     */
    public void setBaseBreath(int baseBreath)
    {
        this.baseBreath = baseBreath;
    }

    /**
     * @return the baseCON
     */
    public int getBaseCON()
    {
        return baseCON;
    }

    /**
     * @param baseCON the baseCON to set
     */
    public void setBaseCON(int baseCON)
    {
        this.baseCON = baseCON;
    }

    /**
     * @return the baseConfusion
     */
    public int getBaseConfusion()
    {
        return baseConfusion;
    }

    /**
     * @param baseConfusion the baseConfusion to set
     */
    public void setBaseConfusion(int baseConfusion)
    {
        this.baseConfusion = baseConfusion;
    }

    /**
     * @return the baseConfusionRes
     */
    public int getBaseConfusionRes()
    {
        return baseConfusionRes == 0 ? 1 : baseConfusionRes;
    }

    /**
     * @param baseConfusionRes the baseConfusionRes to set
     */
    public void setBaseConfusionRes(int baseConfusionRes)
    {
        this.baseConfusionRes = baseConfusionRes;
    }

    /**
     * @return the baseCpMax
     */
    public float getBaseCpMax()
    {
        return baseCpMax;
    }

    /**
     * @param baseCpMax the baseCpMax to set
     */
    public void setBaseCpMax(float baseCpMax)
    {
        this.baseCpMax = baseCpMax;
    }

    /**
     * @return the baseCritRate
     */
    public int getBaseCritRate()
    {
        return baseCritRate;
    }

    /**
     * @param baseCritRate the baseCritRate to set
     */
    public void setBaseCritRate(int baseCritRate)
    {
        this.baseCritRate = baseCritRate;
    }

    /**
     * @return the baseDark
     */
    public int getBaseDark()
    {
        return baseDark;
    }

    /**
     * @param baseDark the baseDark to set
     */
    public void setBaseDark(int baseDark)
    {
        this.baseDark = baseDark;
    }

    /**
     * @return the baseDarkRes
     */
    public int getBaseDarkRes()
    {
        return baseDarkRes == 0 ? 1 : baseDarkRes;
    }

    /**
     * @param baseDarkRes the baseDarkRes to set
     */
    public void setBaseDarkRes(int baseDarkRes)
    {
        this.baseDarkRes = baseDarkRes;
    }

    /**
     * @return the baseDEX
     */
    public int getBaseDEX()
    {
        return baseDEX;
    }

    /**
     * @param baseDEX the baseDEX to set
     */
    public void setBaseDEX(int baseDEX)
    {
        this.baseDEX = baseDEX;
    }

    /**
     * @return the baseEarth
     */
    public int getBaseEarth()
    {
        return baseEarth;
    }

    /**
     * @param baseEarth the baseEarth to set
     */
    public void setBaseEarth(int baseEarth)
    {
        this.baseEarth = baseEarth;
    }

    /**
     * @return the baseEarthRes
     */
    public int getBaseEarthRes()
    {
        return baseEarthRes == 0 ? 1 : baseEarthRes;
    }

    /**
     * @param baseEarthRes the baseEarthRes to set
     */
    public void setBaseEarthRes(int baseEarthRes)
    {
        this.baseEarthRes = baseEarthRes;
    }

    /**
     * @return the baseFire
     */
    public int getBaseFire()
    {
        return baseFire;
    }

    /**
     * @param baseFire the baseFire to set
     */
    public void setBaseFire(int baseFire)
    {
        this.baseFire = baseFire;
    }

    /**
     * @return the baseFireRes
     */
    public int getBaseFireRes()
    {
        return baseFireRes == 0 ? 1 : baseFireRes;
    }

    /**
     * @param baseFireRes the baseFireRes to set
     */
    public void setBaseFireRes(int baseFireRes)
    {
        this.baseFireRes = baseFireRes;
    }

    /**
     * @return the baseHoly
     */
    public int getBaseHoly()
    {
        return baseHoly;
    }

    /**
     * @param baseHoly the baseHoly to set
     */
    public void setBaseHoly(int baseHoly)
    {
        this.baseHoly = baseHoly;
    }

    /**
     * @return the baseHolyRes
     */
    public int getBaseHolyRes()
    {
        return baseHolyRes == 0 ? 1 : baseHolyRes;
    }

    /**
     * @param baseHolyRes the baseHolyRes to set
     */
    public void setBaseHolyRes(int baseHolyRes)
    {
        this.baseHolyRes = baseHolyRes;
    }

    /**
     * @return the baseHpConsumeRate
     */
    public int getBaseHpConsumeRate()
    {
        return baseHpConsumeRate;
    }

    /**
     * @param baseHpConsumeRate the baseHpConsumeRate to set
     */
    public void setBaseHpConsumeRate(int baseHpConsumeRate)
    {
        this.baseHpConsumeRate = baseHpConsumeRate;
    }

    /**
     * @return the baseHpMax
     */
    public float getBaseHpMax()
    {
        return baseHpMax;
    }

    /**
     * @param baseHpMax the baseHpMax to set
     */
    public void setBaseHpMax(float baseHpMax)
    {
        this.baseHpMax = baseHpMax;
    }

    /**
     * @return the baseHpReg
     */
    public float getBaseHpReg()
    {
        return baseHpReg;
    }

    /**
     * @param baseHpReg the baseHpReg to set
     */
    public void setBaseHpReg(float baseHpReg)
    {
        this.baseHpReg = baseHpReg;
    }

    /**
     * @return the baseINT
     */
    public int getBaseINT()
    {
        return baseINT;
    }

    /**
     * @param baseINT the baseINT to set
     */
    public void setBaseINT(int baseINT)
    {
        this.baseINT = baseINT;
    }

    /**
     * @return the baseMAtk
     */
    public int getBaseMAtk()
    {
        return baseMAtk;
    }

    /**
     * @param baseMAtk the baseMAtk to set
     */
    public void setBaseMAtk(int baseMAtk)
    {
        this.baseMAtk = baseMAtk;
    }

    /**
     * @return the baseMAtkSpd
     */
    public int getBaseMAtkSpd()
    {
        return baseMAtkSpd;
    }

    /**
     * @param baseMAtkSpd the baseMAtkSpd to set
     */
    public void setBaseMAtkSpd(int baseMAtkSpd)
    {
        this.baseMAtkSpd = baseMAtkSpd;
    }

    /**
     * @return the baseMDef
     */
    public int getBaseMDef()
    {
        return baseMDef;
    }

    /**
     * @param baseMDef the baseMDef to set
     */
    public void setBaseMDef(int baseMDef)
    {
        this.baseMDef = baseMDef;
    }

    /**
     * @return the baseMEN
     */
    public int getBaseMEN()
    {
        return baseMEN;
    }

    /**
     * @param baseMEN the baseMEN to set
     */
    public void setBaseMEN(int baseMEN)
    {
        this.baseMEN = baseMEN;
    }

    /**
     * @return the baseMovement
     */
    public int getBaseMovement()
    {
        return baseMovement;
    }

    /**
     * @param baseMovement the baseMovement to set
     */
    public void setBaseMovement(int baseMovement)
    {
        this.baseMovement = baseMovement;
    }

    /**
     * @return the baseMovementRes
     */
    public int getBaseMovementRes()
    {
        return baseMovementRes == 0 ? 1 : baseMovementRes;
    }

    /**
     * @param baseMovementRes the baseMovementRes to set
     */
    public void setBaseMovementRes(int baseMovementRes)
    {
        this.baseMovementRes = baseMovementRes;
    }

    /**
     * @return the baseMpConsumeRate
     */
    public int getBaseMpConsumeRate()
    {
        return baseMpConsumeRate;
    }

    /**
     * @param baseMpConsumeRate the baseMpConsumeRate to set
     */
    public void setBaseMpConsumeRate(int baseMpConsumeRate)
    {
        this.baseMpConsumeRate = baseMpConsumeRate;
    }

    /**
     * @return the baseMpMax
     */
    public float getBaseMpMax()
    {
        return baseMpMax;
    }

    /**
     * @param baseMpMax the baseMpMax to set
     */
    public void setBaseMpMax(float baseMpMax)
    {
        this.baseMpMax = baseMpMax;
    }

    /**
     * @return the baseMpReg
     */
    public float getBaseMpReg()
    {
        return baseMpReg;
    }

    /**
     * @param baseMpReg the baseMpReg to set
     */
    public void setBaseMpReg(float baseMpReg)
    {
        this.baseMpReg = baseMpReg;
    }

    /**
     * @return the baseMReuseRate
     */
    public float getBaseMReuseRate()
    {
        return baseMReuseRate == 0.f ? 1.f : baseMReuseRate;
    }

    /**
     * @param baseMReuseRate the baseMReuseRate to set
     */
    public void setBaseMReuseRate(float baseMReuseRate)
    {
        this.baseMReuseRate = baseMReuseRate;
    }

    /**
     * @return the basePAtk
     */
    public int getBasePAtk()
    {
        return basePAtk;
    }

    /**
     * @param basePAtk the basePAtk to set
     */
    public void setBasePAtk(int basePAtk)
    {
        this.basePAtk = basePAtk;
    }

    /**
     * @return the basePAtkSpd
     */
    public int getBasePAtkSpd()
    {
        return basePAtkSpd;
    }

    /**
     * @param basePAtkSpd the basePAtkSpd to set
     */
    public void setBasePAtkSpd(int basePAtkSpd)
    {
        this.basePAtkSpd = basePAtkSpd;
    }

    /**
     * @return the basePDef
     */
    public int getBasePDef()
    {
        return basePDef;
    }

    /**
     * @param basePDef the basePDef to set
     */
    public void setBasePDef(int basePDef)
    {
        this.basePDef = basePDef;
    }

    /**
     * @return the basePoison
     */
    public int getBasePoison()
    {
        return basePoison;
    }

    /**
     * @param basePoison the basePoison to set
     */
    public void setBasePoison(int basePoison)
    {
        this.basePoison = basePoison;
    }

    /**
     * @return the basePoisonRes
     */
    public int getBasePoisonRes()
    {
        return basePoisonRes == 0 ? 1 : basePoisonRes;
    }

    /**
     * @param basePoisonRes the basePoisonRes to set
     */
    public void setBasePoisonRes(int basePoisonRes)
    {
        this.basePoisonRes = basePoisonRes;
    }

    /**
     * @return the baseRoot
     */
    public int getBaseRoot()
    {
        return baseRoot;
    }

    /**
     * @param baseRoot the baseRoot to set
     */
    public void setBaseRoot(int baseRoot)
    {
        this.baseRoot = baseRoot;
    }

    /**
     * @return the baseRootRes
     */
    public int getBaseRootRes()
    {
        return baseRootRes == 0 ? 1 : baseRootRes;
    }

    /**
     * @param baseRootRes the baseRootRes to set
     */
    public void setBaseRootRes(int baseRootRes)
    {
        this.baseRootRes = baseRootRes;
    }

    /**
     * @return the baseRunSpd
     */
    public int getBaseRunSpd()
    {
        return baseRunSpd;
    }

    /**
     * @param baseRunSpd the baseRunSpd to set
     */
    public void setBaseRunSpd(int baseRunSpd)
    {
        this.baseRunSpd = baseRunSpd;
    }

    /**
     * @return the baseShldDef
     */
    public int getBaseShldDef()
    {
        return baseShldDef;
    }

    /**
     * @param baseShldDef the baseShldDef to set
     */
    public void setBaseShldDef(int baseShldDef)
    {
        this.baseShldDef = baseShldDef;
    }

    /**
     * @return the baseShldRate
     */
    public int getBaseShldRate()
    {
        return baseShldRate;
    }

    /**
     * @param baseShldRate the baseShldRate to set
     */
    public void setBaseShldRate(int baseShldRate)
    {
        this.baseShldRate = baseShldRate;
    }

    /**
     * @return the baseSleep
     */
    public int getBaseSleep()
    {
        return baseSleep;
    }

    /**
     * @param baseSleep the baseSleep to set
     */
    public void setBaseSleep(int baseSleep)
    {
        this.baseSleep = baseSleep;
    }

    /**
     * @return the baseSleepRes
     */
    public int getBaseSleepRes()
    {
        return baseSleepRes == 0 ? 1 : baseSleepRes;
    }

    /**
     * @param baseSleepRes the baseSleepRes to set
     */
    public void setBaseSleepRes(int baseSleepRes)
    {
        this.baseSleepRes = baseSleepRes;
    }

    /**
     * @return the baseSTR
     */
    public int getBaseSTR()
    {
        return baseSTR;
    }

    /**
     * @param baseSTR the baseSTR to set
     */
    public void setBaseSTR(int baseSTR)
    {
        this.baseSTR = baseSTR;
    }

    /**
     * @return the baseStun
     */
    public int getBaseStun()
    {
        return baseStun;
    }

    /**
     * @param baseStun the baseStun to set
     */
    public void setBaseStun(int baseStun)
    {
        this.baseStun = baseStun;
    }

    /**
     * @return the baseStunRes
     */
    public int getBaseStunRes()
    {
        return baseStunRes == 0 ? 1 : baseStunRes;
    }

    /**
     * @param baseStunRes the baseStunRes to set
     */
    public void setBaseStunRes(int baseStunRes)
    {
        this.baseStunRes = baseStunRes;
    }

    /**
     * @return the baseWater
     */
    public int getBaseWater()
    {
        return baseWater;
    }

    /**
     * @param baseWater the baseWater to set
     */
    public void setBaseWater(int baseWater)
    {
        this.baseWater = baseWater;
    }

    /**
     * @return the baseWaterRes
     */
    public int getBaseWaterRes()
    {
        return baseWaterRes == 0 ? 1 : baseWaterRes;
    }

    /**
     * @param baseWaterRes the baseWaterRes to set
     */
    public void setBaseWaterRes(int baseWaterRes)
    {
        this.baseWaterRes = baseWaterRes;
    }

    /**
     * @return the baseWind
     */
    public int getBaseWind()
    {
        return baseWind;
    }

    /**
     * @param baseWind the baseWind to set
     */
    public void setBaseWind(int baseWind)
    {
        this.baseWind = baseWind;
    }

    /**
     * @return the baseWindRes
     */
    public int getBaseWindRes()
    {
        return baseWindRes == 0 ? 1 : baseWindRes;
    }

    /**
     * @param baseWindRes the baseWindRes to set
     */
    public void setBaseWindRes(int baseWindRes)
    {
        this.baseWindRes = baseWindRes;
    }

    /**
     * @return the baseWIT
     */
    public int getBaseWIT()
    {
        return baseWIT;
    }

    /**
     * @param baseWIT the baseWIT to set
     */
    public void setBaseWIT(int baseWIT)
    {
        this.baseWIT = baseWIT;
    }

    /**
     * @return the collisionHeight
     */
    public int getCollisionHeight()
    {
        return collisionHeight;
    }

    /**
     * @param collisionHeight the collisionHeight to set
     */
    public void setCollisionHeight(int collisionHeight)
    {
        this.collisionHeight = collisionHeight;
    }

    /**
     * @return the collisionRadius
     */
    public int getCollisionRadius()
    {
        return collisionRadius;
    }

    /**
     * @param collisionRadius the collisionRadius to set
     */
    public void setCollisionRadius(int collisionRadius)
    {
        this.collisionRadius = collisionRadius;
    }

    /**
     * @return the isUndead
     */
    public boolean isUndead()
    {
        return isUndead;
    }

    /**
     * @param isUndead the isUndead to set
     */
    public void setUndead(boolean isUndead)
    {
        this.isUndead = isUndead;
    }
}
