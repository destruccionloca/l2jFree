/*
 * $Header: PlayerClass.java, 24/11/2005 12:56:01 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 24/11/2005 12:56:01 $
 * $Revision: 1 $
 * $Log: PlayerClass.java,v $
 * Revision 1  24/11/2005 12:56:01  luisantonioa
 * Added copyright notice
 *
 * 
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
package net.sf.l2j.gameserver.model.base;

import static net.sf.l2j.gameserver.model.base.ClassLevel.Fifth;
import static net.sf.l2j.gameserver.model.base.ClassLevel.First;
import static net.sf.l2j.gameserver.model.base.ClassLevel.Fourth;
import static net.sf.l2j.gameserver.model.base.ClassLevel.Second;
import static net.sf.l2j.gameserver.model.base.ClassLevel.Sixth;
import static net.sf.l2j.gameserver.model.base.ClassLevel.Third;
import static net.sf.l2j.gameserver.model.base.ClassType.Fighter;
import static net.sf.l2j.gameserver.model.base.ClassType.Mystic;
import static net.sf.l2j.gameserver.model.base.ClassType.Priest;
import static net.sf.l2j.gameserver.model.base.Race.human;
import static net.sf.l2j.gameserver.model.base.Race.darkelf;
import static net.sf.l2j.gameserver.model.base.Race.dwarf;
import static net.sf.l2j.gameserver.model.base.Race.elf;
import static net.sf.l2j.gameserver.model.base.Race.kamael;
import static net.sf.l2j.gameserver.model.base.Race.orc;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public enum PlayerClass {
    humanFighter(human, Fighter, First), Warrior(human, Fighter, Second), Gladiator(human, Fighter,
            Third), Warlord(human, Fighter, Third), humanKnight(human, Fighter, Second), Paladin(human,
            Fighter, Third), DarkAvenger(human, Fighter, Third), Rogue(human, Fighter, Second), TreasureHunter(
            human, Fighter, Third), Hawkeye(human, Fighter, Third), humanMystic(human, Mystic, First), humanWizard(
            human, Mystic, Second), Sorceror(human, Mystic, Third), Necromancer(human, Mystic, Third), Warlock(
            human, Mystic, Third), Cleric(human, Priest, Second), Bishop(human, Priest, Third), Prophet(
            human, Priest, Third),

    ElvenFighter(elf, Fighter, First), ElvenKnight(elf, Fighter, Second), TempleKnight(
            elf, Fighter, Third), Swordsinger(elf, Fighter, Third), ElvenScout(elf,
            Fighter, Second), Plainswalker(elf, Fighter, Third), SilverRanger(elf, Fighter,
            Third), ElvenMystic(elf, Mystic, First), ElvenWizard(elf, Mystic, Second), Spellsinger(
            elf, Mystic, Third), ElementalSummoner(elf, Mystic, Third), ElvenOracle(elf,
            Priest, Second), ElvenElder(elf, Priest, Third),

    DarkElvenFighter(darkelf, Fighter, First), PalusKnight(darkelf, Fighter, Second), ShillienKnight(
            darkelf, Fighter, Third), Bladedancer(darkelf, Fighter, Third), Assassin(darkelf, Fighter,
            Second), AbyssWalker(darkelf, Fighter, Third), PhantomRanger(darkelf, Fighter, Third), DarkElvenMystic(
            darkelf, Mystic, First), DarkElvenWizard(darkelf, Mystic, Second), Spellhowler(darkelf,
            Mystic, Third), PhantomSummoner(darkelf, Mystic, Third), ShillienOracle(darkelf, Priest,
            Second), ShillienElder(darkelf, Priest, Third),

    orcFighter(orc, Fighter, First), orcRaider(orc, Fighter, Second), Destroyer(orc, Fighter, Third), orcMonk(
            orc, Fighter, Second), Tyrant(orc, Fighter, Third), orcMystic(orc, Mystic, First), orcShaman(
            orc, Mystic, Second), Overlord(orc, Mystic, Third), Warcryer(orc, Mystic, Third),

    DwarvenFighter(dwarf, Fighter, First), DwarvenScavenger(dwarf, Fighter, Second), BountyHunter(dwarf,
            Fighter, Third), DwarvenArtisan(dwarf, Fighter, Second), Warsmith(dwarf, Fighter, Third),
            
    dummyEntry1(null, null, null), dummyEntry2(null, null, null), dummyEntry3(null, null, null), dummyEntry4(
            null, null, null), dummyEntry5(null, null, null), dummyEntry6(null, null, null), dummyEntry7(
            null, null, null), dummyEntry8(null, null, null), dummyEntry9(null, null, null), dummyEntry10(
            null, null, null), dummyEntry11(null, null, null), dummyEntry12(null, null, null), dummyEntry13(
            null, null, null), dummyEntry14(null, null, null), dummyEntry15(null, null, null), dummyEntry16(
            null, null, null), dummyEntry17(null, null, null), dummyEntry18(null, null, null), dummyEntry19(
            null, null, null), dummyEntry20(null, null, null), dummyEntry21(null, null, null), dummyEntry22(
            null, null, null), dummyEntry23(null, null, null), dummyEntry24(null, null, null), dummyEntry25(
            null, null, null), dummyEntry26(null, null, null), dummyEntry27(null, null, null), dummyEntry28(
            null, null, null), dummyEntry29(null, null, null), dummyEntry30(null, null, null), dummyEntry31(
            null, null, null), dummyEntry32(null, null, null), dummyEntry33(null, null, null), dummyEntry34(
            null, null, null),

    /*
     * (3rd classes)
     */
    duelist(human, Fighter, Fourth), dreadnought(human, Fighter, Fourth), phoenixKnight(human, Fighter,
            Fourth), hellKnight(human, Fighter, Fourth), sagittarius(human, Fighter, Fourth), adventurer(
            human, Fighter, Fourth), archmage(human, Mystic, Fourth), soultaker(human, Mystic, Fourth), arcanaLord(
            human, Mystic, Fourth), cardinal(human, Mystic, Fourth), hierophant(human, Mystic, Fourth),

    evaTemplar(elf, Fighter, Fourth), swordMuse(elf, Fighter, Fourth), windRider(elf,
            Fighter, Fourth), moonlightSentinel(elf, Fighter, Fourth), mysticMuse(elf, Mystic,
            Fourth), elementalMaster(elf, Mystic, Fourth), evaSaint(elf, Mystic, Fourth),

    shillienTemplar(darkelf, Fighter, Fourth), spectralDancer(darkelf, Fighter, Fourth), ghostHunter(
            darkelf, Fighter, Fourth), ghostSentinel(darkelf, Fighter, Fourth), stormScreamer(darkelf,
            Mystic, Fourth), spectralMaster(darkelf, Mystic, Fourth), shillienSaint(darkelf, Mystic,
            Fourth),

    titan(orc, Fighter, Fourth), grandKhauatari(orc, Fighter, Fourth), dominator(orc, Mystic, Fourth), doomcryer(
            orc, Mystic, Fourth),

    fortuneSeeker(dwarf, Fighter, Fourth), maestro(dwarf, Fighter, Fourth),

    maleSoldier(kamael, Fighter, First), trooper(kamael, Fighter, Second), berserker(kamael, Fighter, Third),
    		maleSoulbreaker(kamael, Fighter, Third), doombringer(kamael, Fighter, Fourth), maleSoulhound(kamael,
    		Fighter, Fourth),
    
    femaleSoldier(kamael, Fighter, First), warder(kamael, Fighter, Second), arbalester(kamael, Fighter, Third),
    		femaleSoulbreaker(kamael, Fighter, Third), trickster(kamael, Fighter, Fourth), femaleSoulhound(kamael,
    		Fighter, Fourth),

    inspector(kamael, Fighter, Fifth), judicator(kamael, Fighter, Sixth);
    		
    private Race _race;
    private ClassLevel _level;
    private ClassType _type;

    private static final Set<PlayerClass> mainSubclassSet;
    private static final Set<PlayerClass> neverSubclassed = EnumSet.of(Overlord, Warsmith);

    private static final Set<PlayerClass> subclasseSet1 = EnumSet.of(DarkAvenger, Paladin, TempleKnight,
                                                                     ShillienKnight);
    private static final Set<PlayerClass> subclasseSet2 = EnumSet.of(TreasureHunter, AbyssWalker,
                                                                     Plainswalker);
    private static final Set<PlayerClass> subclasseSet3 = EnumSet.of(Hawkeye, SilverRanger,
                                                                     PhantomRanger);
    private static final Set<PlayerClass> subclasseSet4 = EnumSet.of(Warlock, ElementalSummoner,
                                                                     PhantomSummoner);
    private static final Set<PlayerClass> subclasseSet5 = EnumSet.of(Sorceror, Spellsinger, Spellhowler);

    private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap = new EnumMap<PlayerClass, Set<PlayerClass>>(
                                                                                                                            PlayerClass.class);

    static
    {
        Set<PlayerClass> subclasses = getSet(null, Third);
        subclasses.removeAll(neverSubclassed);

        mainSubclassSet = subclasses;

        subclassSetMap.put(DarkAvenger, subclasseSet1);
        subclassSetMap.put(Paladin, subclasseSet1);
        subclassSetMap.put(TempleKnight, subclasseSet1);
        subclassSetMap.put(ShillienKnight, subclasseSet1);

        subclassSetMap.put(TreasureHunter, subclasseSet2);
        subclassSetMap.put(AbyssWalker, subclasseSet2);
        subclassSetMap.put(Plainswalker, subclasseSet2);

        subclassSetMap.put(Hawkeye, subclasseSet3);
        subclassSetMap.put(SilverRanger, subclasseSet3);
        subclassSetMap.put(PhantomRanger, subclasseSet3);

        subclassSetMap.put(Warlock, subclasseSet4);
        subclassSetMap.put(ElementalSummoner, subclasseSet4);
        subclassSetMap.put(PhantomSummoner, subclasseSet4);

        subclassSetMap.put(Sorceror, subclasseSet5);
        subclassSetMap.put(Spellsinger, subclasseSet5);
        subclassSetMap.put(Spellhowler, subclasseSet5);
    }

    PlayerClass(Race pRace, ClassType pType, ClassLevel pLevel)
    {
        _race = pRace;
        _level = pLevel;
        _type = pType;
    }

    public final Set<PlayerClass> getAvaliableSubclasses(L2PcInstance player)
    {
        Set<PlayerClass> subclasses = null;

        if (_level == Third)
        {
        	if (player.getRace() != kamael)
        	{
		        subclasses = EnumSet.copyOf(mainSubclassSet);
		
		        subclasses.removeAll(neverSubclassed);
		        subclasses.remove(this);
		
		        switch (_race)
		        {
		            case elf:
		                subclasses.removeAll(getSet(darkelf, Third));
		                break;
		            case darkelf:
		                subclasses.removeAll(getSet(elf, Third));
		                break;
		        }

	            Set<PlayerClass> unavaliableClasses = subclassSetMap.get(this);

	            if (unavaliableClasses != null)
	            {
	                subclasses.removeAll(unavaliableClasses);
	            }
        	}
        	else
        	{
        		subclasses = new HashSet<PlayerClass>();
        		subclasses.add(inspector);
        	}
        }
        else if (_level == Fifth)
        {
    		subclasses = new HashSet<PlayerClass>();
        	subclasses.add(judicator);
        }

        return subclasses;
    }

    public static final EnumSet<PlayerClass> getSet(Race race, ClassLevel level)
    {
        EnumSet<PlayerClass> allOf = EnumSet.noneOf(PlayerClass.class);

        for (PlayerClass playerClass : EnumSet.allOf(PlayerClass.class))
        {
            if (race == null || playerClass.isOfRace(race))
            {
                if (level == null || playerClass.isOfLevel(level))
                {
                    allOf.add(playerClass);
                }
            }
        }

        return allOf;
    }

    public final boolean isOfRace(Race pRace)
    {
        return _race == pRace;
    }

    public final boolean isOfType(ClassType pType)
    {
        return _type == pType;
    }

    public final boolean isOfLevel(ClassLevel pLevel)
    {
        return _level == pLevel;
    }
    public final ClassLevel getLevel()
    {
        return _level;
    }
}