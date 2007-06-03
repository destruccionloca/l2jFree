package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.datatables.PetDataTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;

public class PetStat extends SummonStat
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public PetStat(L2PetInstance activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    public boolean addExp(long value)
    {
        if (!super.addExp(value)) return false;

		/* Micht : Use of PetInfo for C5
        StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
        su.addAttribute(StatusUpdate.EXP, getExp());
        getActiveChar().broadcastPacket(su);
        */
        getActiveChar().broadcastPacket(new PetInfo(getActiveChar()));

        return true;
    }

    public boolean addExpAndSp(long addToExp, int addToSp)
    {
        if (!super.addExpAndSp(addToExp, addToSp)) return false;

        SystemMessage sm = new SystemMessage(SystemMessage.PET_EARNED_S1_EXP);
        sm.addNumber((int)addToExp);
                
        getActiveChar().getOwner().sendPacket(sm);

        return true;
    }

    public final boolean addLevel(byte value)
    {
        if (getLevel() + value > (Experience.MAX_LEVEL - 1)) return false;

        boolean levelIncreased = super.addLevel(value);

        // Sync up exp with current level
        if (getExp() > getExpForLevel(getLevel() + 1) || getExp() < getExpForLevel(getLevel())) setExp(Experience.LEVEL[getLevel()]);

        if (levelIncreased)
        {
            getActiveChar().getOwner().sendMessage("Your pet has increased it's level.");
            getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), 15));
        }

        StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
        su.addAttribute(StatusUpdate.LEVEL, getLevel());
        su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
        su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
        getActiveChar().broadcastPacket(su);

        // Send a Server->Client packet PetInfo to the L2PcInstance
        getActiveChar().getOwner().sendPacket(new PetInfo(getActiveChar()));
        
        if (getActiveChar().getControlItem() != null)
            getActiveChar().getControlItem().setEnchantLevel(getLevel());

        return levelIncreased;
    }

    public final long getExpForLevel(int level) { return PetDataTable.getInstance().getPetData(getActiveChar().getNpcId(), level).getPetMaxExp(); }
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public L2PetInstance getActiveChar() { return (L2PetInstance)super.getActiveChar(); }

    public final int getFeedBattle() { return getActiveChar().getPetData().getPetFeedBattle(); }

    public final int getFeedNormal() { return getActiveChar().getPetData().getPetFeedNormal(); }

    public void setLevel(byte value)
    {
        getActiveChar().stopFeed();
        super.setLevel(value);

        getActiveChar().setPetData(PetDataTable.getInstance().getPetData(getActiveChar().getTemplate().getNpcId(), getLevel()));
        getActiveChar().startFeed( false );

        if (getActiveChar().getControlItem() != null)
            getActiveChar().getControlItem().setEnchantLevel(getLevel());
    }

    public final int getMaxFeed() { return getActiveChar().getPetData().getPetMaxFeed(); }

    public int getMaxHp() { return (int)calcStat(Stats.MAX_HP, getActiveChar().getPetData().getPetMaxHP(), null, null); }
    
    public int getMaxMp() { return (int)calcStat(Stats.MAX_MP, getActiveChar().getPetData().getPetMaxMP(), null, null); }
    
    public int getMAtk(L2Character target, L2Skill skill)
    {
        double attack = getActiveChar().getPetData().getPetMAtk();
        Stats stat = skill == null? null : skill.getStat();
        if (stat != null)
        {
            switch (stat)
            {
            case AGGRESSION: attack += getActiveChar().getTemplate().getBaseAggression(); break;
            case BLEED:      attack += getActiveChar().getTemplate().getBaseBleed();      break;
            case POISON:     attack += getActiveChar().getTemplate().getBasePoison();     break;
            case STUN:       attack += getActiveChar().getTemplate().getBaseStun();       break;
            case ROOT:       attack += getActiveChar().getTemplate().getBaseRoot();       break;
            case MOVEMENT:   attack += getActiveChar().getTemplate().getBaseMovement();   break;
            case CONFUSION:  attack += getActiveChar().getTemplate().getBaseConfusion();  break;
            case SLEEP:      attack += getActiveChar().getTemplate().getBaseSleep();      break;
            case FIRE:       attack += getActiveChar().getTemplate().getBaseFire();       break;
            case WIND:       attack += getActiveChar().getTemplate().getBaseWind();       break;
            case WATER:      attack += getActiveChar().getTemplate().getBaseWater();      break;
            case EARTH:      attack += getActiveChar().getTemplate().getBaseEarth();      break;
            case HOLY:       attack += getActiveChar().getTemplate().getBaseHoly();       break;
            case DARK:       attack += getActiveChar().getTemplate().getBaseDark();       break;
            }
        }
        if (skill != null) attack += skill.getPower();
        return (int)calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
    }
    
    public int getMDef(L2Character target, L2Skill skill)
    {
        double defence = getActiveChar().getPetData().getPetMDef();
        Stats stat = skill == null? null : skill.getStat();
        if (stat != null)
        {
            switch (stat)
            {
            case AGGRESSION: defence += getActiveChar().getTemplate().getBaseAggressionRes(); break;
            case BLEED:      defence += getActiveChar().getTemplate().getBaseBleedRes();      break;
            case POISON:     defence += getActiveChar().getTemplate().getBasePoisonRes();     break;
            case STUN:       defence += getActiveChar().getTemplate().getBaseStunRes();       break;
            case ROOT:       defence += getActiveChar().getTemplate().getBaseRootRes();       break;
            case MOVEMENT:   defence += getActiveChar().getTemplate().getBaseMovementRes();   break;
            case CONFUSION:  defence += getActiveChar().getTemplate().getBaseConfusionRes();  break;
            case SLEEP:      defence += getActiveChar().getTemplate().getBaseSleepRes();      break;
            case FIRE:       defence += getActiveChar().getTemplate().getBaseFireRes();       break;
            case WIND:       defence += getActiveChar().getTemplate().getBaseWindRes();       break;
            case WATER:      defence += getActiveChar().getTemplate().getBaseWaterRes();      break;
            case EARTH:      defence += getActiveChar().getTemplate().getBaseEarthRes();      break;
            case HOLY:       defence += getActiveChar().getTemplate().getBaseHolyRes();       break;
            case DARK:       defence += getActiveChar().getTemplate().getBaseDarkRes();       break;
            }
        }
        return (int)calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
    }
    
    public int getPAtk(L2Character target) { return (int)calcStat(Stats.POWER_ATTACK, getActiveChar().getPetData().getPetPAtk(), target, null); }
    public int getPDef(L2Character target) { return (int)calcStat(Stats.POWER_DEFENCE, getActiveChar().getPetData().getPetPDef(), target, null); }
    public int getAccuracy() { return (int)calcStat(Stats.ACCURACY_COMBAT, getActiveChar().getPetData().getPetAccuracy(), null, null); }
    public int getCriticalHit(L2Character target, L2Skill skill) { return (int)calcStat(Stats.CRITICAL_RATE, getActiveChar().getPetData().getPetCritical(), target, null); }
    public int getEvasionRate(L2Character target) { return (int)calcStat(Stats.EVASION_RATE, getActiveChar().getPetData().getPetEvasion(), target, null); }
    public int getRunSpeed() { return (int)calcStat(Stats.RUN_SPEED, getActiveChar().getPetData().getPetSpeed(), null, null); }
    public int getRegenHp() { return (int)calcStat(Stats.REGENERATE_HP_RATE, getActiveChar().getPetData().getPetRegenHP(), null, null); }
    public int getRegenMp() { return (int)calcStat(Stats.REGENERATE_MP_RATE, getActiveChar().getPetData().getPetRegenMP(), null, null); }
    public int getPAtkSpd() { return (int)calcStat(Stats.POWER_ATTACK_SPEED, getActiveChar().getPetData().getPetAtkSpeed(), null, null); }
    public int getMAtkSpd() { return  (int)calcStat(Stats.MAGIC_ATTACK_SPEED, getActiveChar().getPetData().getPetCastSpeed(), null, null); }
}
