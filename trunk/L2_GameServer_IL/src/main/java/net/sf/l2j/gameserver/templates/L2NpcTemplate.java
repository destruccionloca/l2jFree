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

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2DropCategory;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2MinionData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.skills.Stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This cl contains all generic data of a L2Spawn object.<BR><BR>
 * 
 * <B><U> Data</U> :</B><BR><BR>
 * <li>npcId, type, name, sex</li>
 * <li>rewardExp, rewardSp</li>
 * <li>aggroRange, factionId, factionRange</li>
 * <li>rhand, lhand, armor</li>
 * <li>isUndead</li>
 * <li>_drops</li>
 * <li>_minions</li>
 * <li>_teachInfo</li>
 * <li>_skills</li>
 * <li>_questsStart</li><BR><BR>
 * 
 * this template has property that will be set by setters.
 * <br/>
 * <br/>
 * <font color="red">
 * <b>Property don't change in the time, this is just a template, not the currents status 
 * of characters !</b>
 * </font> 
 * 
 * @version $Revision: 1.1.2.4 $ $Date: 2005/04/02 15:57:51 $
 */
public final class L2NpcTemplate extends L2CharTemplate
{
    /**
     * Logger
     */
	private final static Log _log = LogFactory.getLog(L2NpcTemplate.class.getName());

    private int     _npcId;
    private int     _idTemplate;
    private String  _type;
    private String  _name;
    private boolean _serverSideName;
    private String  _title;
    private boolean _serverSideTitle;
    private String  _sex;
    private byte    _level;
    private int     _rewardExp;
    private int     _rewardSp;
    private int     _aggroRange;
    private int     _rhand;
    private int     _lhand;
    private int     _armor;
    private String  _factionId;
    private int     _factionRange;
    private int     _absorbLevel;
    private int     _npcFaction;
    private String  _npcFactionName;
    private String  _jClass;
    
    /** fixed skills*/
    private int     _race;
    
    /** The table containing all Item that can be dropped by L2NpcInstance using this L2NpcTemplate*/
    private final List<L2DropCategory> _categories = new FastList<L2DropCategory>();   
    
    /** The table containing all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate*/
    private final List<L2MinionData> _minions     = new FastList<L2MinionData>(0);
    
    /** The list of class that this NpcTemplate can Teach */
    private List<ClassId> _teachInfo;
    
    /** List of skills of this npc */
    private Map<Integer, L2Skill> _skills;
    
    /** List of resist stats for this npc*/
    private Map<Stats, Integer> _resists;
    
    /** contains a list of quests for each event type (questStart, questAttack, questKill, etc)*/
    private Map<Quest.QuestEventType, Quest[]> _questEvents;

    private byte _specialFaction = 0;

    /** Some factions needed for special mob ai */
    public static final byte FACTION_KETRA = 1;
    public static final byte FACTION_VARKA = 2;

    /**
     * Constructor of L2Character.<BR><BR>
     * 
     * @param set The StatsSet object to transfert data to the method
     * 
     */
    public L2NpcTemplate(StatsSet set)
    {
        super(set);
        _npcId     = set.getInteger("npcId");
        _idTemplate = set.getInteger("idTemplate");
        _type      = set.getString("type");
        _name      = set.getString("name");
        _serverSideName = set.getBool("serverSideName");
        _title     = set.getString("title");
        _serverSideTitle = set.getBool("serverSideTitle");
        _sex       = set.getString("sex");
        _level     = set.getByte("level");
        _rewardExp = set.getInteger("rewardExp");
        _rewardSp  = set.getInteger("rewardSp");
        _aggroRange= set.getInteger("aggroRange");
        _rhand     = set.getInteger("rhand");
        _lhand     = set.getInteger("lhand");
        _armor     = set.getInteger("armor");
        setFactionId(set.getString("factionId", null));
        _factionRange  = set.getInteger("factionRange");
        _absorbLevel  = set.getInteger("absorb_level", 0);
        _npcFaction = set.getInteger("NPCFaction", 0);
        _npcFactionName = set.getString("NPCFactionName", "Devine Clan");
        _jClass= set.getString("jClass");
        _race = 0;
        _teachInfo = null;
    }
    
    /**
     * Add the class id this npc can teach
     * @param classId
     */
    public void addTeachInfo(ClassId classId)
    {
        if (_teachInfo == null)
            _teachInfo = new FastList<ClassId>();
        _teachInfo.add(classId);
    }
    
    /**
     * @return the teach infos
     */
    public List<ClassId> getTeachInfo()
    {
        return _teachInfo;
    }
    
    /**
     * Check if this npc can teach to this class
     * @param classId
     * @return true if this npc can teach to this class
     */
    public boolean canTeach(ClassId classId)
    {
        if (_teachInfo == null)
            return false;
        
        // If the player is on a third class, fetch the class teacher
        // information for its parent class.
        if (classId.getId() >= 88)
            return _teachInfo.contains(classId.getParent());
        
        return _teachInfo.contains(classId);
    }
    
    
 
    /**
     * add a drop to a given category.  If the category does not exist, create it.
     * @param drop
     * @param categoryType
     */
    public void addDropData(L2DropData drop, int categoryType)
    {
        if (drop.isQuestDrop())
		{
//          if (_questDrops == null)
//              _questDrops = new FastList<L2DropData>(0);
//          _questDrops.add(drop);
        }
		else
		{
            // if the category doesn't already exist, create it first
            synchronized (_categories)
            {
                boolean catExists = false;
                for(L2DropCategory cat:_categories)
                    // if the category exists, add the drop to this category.
                    if (cat.getCategoryType() == categoryType)
                    {
                        cat.addDropData(drop);
                        catExists = true;
                        break;
                    }
                // if the category doesn't exit, create it and add the drop
                if (!catExists)
                {
                    L2DropCategory cat = new L2DropCategory(categoryType);
                    cat.addDropData(drop);
                    _categories.add(cat);
                }
            }
        }
    }
    
    public void addRaidData(L2MinionData minion)
    {
        _minions.add(minion);
    }
    
    public void addSkill(L2Skill skill)
    {
        if (_skills == null)
            _skills = new FastMap<Integer, L2Skill>();
        _skills.put(skill.getId(), skill);
    }
    public void addResist(Stats id, int resist)
    {
        if (_resists == null)
            _resists = new FastMap<Stats, Integer>();
        _resists.put(id, new Integer(resist));
    }
    public int getResist(Stats id)
    {
        if(_resists == null || _resists.get(id) == null)
            return 0;
        return _resists.get(id);
    }
    public int removeResist(Stats id)
    {
        return _resists.remove(id);
    }
    
    /**
     * Return the list of all possible UNCATEGORIZED drops of this L2NpcTemplate.<BR><BR>
     * @return the drop categories
     */
    public List<L2DropCategory> getDropData()
    {
        return _categories;
    }   
    
    /**
     * Return the list of all possible item drops of this L2NpcTemplate.<BR>
     * (ie full drops and part drops, mats, miscellaneous & UNCATEGORIZED)<BR><BR>
     */
    public List<L2DropData> getAllDropData()
    {
        FastList<L2DropData> lst = new FastList<L2DropData>();
        for (L2DropCategory tmp:_categories)
        {
            lst.addAll(tmp.getAllDrops());
        }
        return lst;
    }
    
    /**
     * Empty all possible drops of this L2NpcTemplate.<BR><BR>
     */
    public synchronized void clearAllDropData()
    {
        while (_categories.size() > 0)
        {
            _categories.get(0).clearAllDrops();
            _categories.remove(0);
        }
        _categories.clear();
    }

    /**
     * Return the list of all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate.<BR><BR>
     */
    public List<L2MinionData> getMinionData()
    {
        return _minions;
    }

    public Map<Integer, L2Skill> getSkills()
    {
        return _skills;
    }
        
    public void addQuestEvent(Quest.QuestEventType EventType, Quest q)
    {
    	if (_questEvents == null) 
    		_questEvents = new FastMap<Quest.QuestEventType, Quest[]>();
    		
		if (_questEvents.get(EventType) == null) {
			_questEvents.put(EventType, new Quest[]{q});
		} 
		else 
		{
			Quest[] _quests = _questEvents.get(EventType);
			int len = _quests.length;
			
			// if only one registration per npc is allowed for this event type
			// then only register this NPC if not already registered for the specified event.
			// if a quest allows multiple registrations, then register regardless of count
			if (EventType.isMultipleRegistrationAllowed() || (len < 1))
			{
				Quest[] tmp = new Quest[len+1];
				for (int i=0; i < len; i++) {
					if (_quests[i].getName().equals(q.getName())) {
						_quests[i] = q;
						return;
		            }
					tmp[i] = _quests[i];
		        }
				tmp[len] = q;
				_questEvents.put(EventType, tmp);
			}
			else
			{
				_log.warn("Quest event not allowed in multiple quests.  Skipped addition of Event Type \""+EventType+"\" for NPC \""+this._name +"\" and quest \""+q.getName()+"\".");
			}
		}
    }
    
	public Quest[] getEventQuests(Quest.QuestEventType EventType)
	{
		if (_questEvents == null)
			return null;
		return _questEvents.get(EventType);
    }
    
    public void setRace(int newrace)
    {
        _race = newrace;
    }
    
    public int getNpcFaction()
    {
        return _npcFaction;
    }
    
    public void setNpcFaction(int npcFaction)
    {
        _npcFaction = npcFaction;
    }    
    
    public String getNpcFactionName()
    {
        return _npcFactionName;
    }

    /**
     * @return the absorb_level
     */
    public int getAbsorbLevel()
    {
        return _absorbLevel;
    }

    /**
     * @param absorb_level the absorb_level to set
     */
    public void setAbsorbLevel(int absorb_level)
    {
        _absorbLevel = absorb_level;
    }

    /**
     * @return the aggroRange
     */
    public int getAggroRange()
    {
        return _aggroRange;
    }

    /**
     * @param aggroRange the aggroRange to set
     */
    public void setAggroRange(int aggroRange)
    {
        _aggroRange = aggroRange;
    }

    /**
     * @return the armor
     */
    public int getArmor()
    {
        return _armor;
    }

    /**
     * @param armor the armor to set
     */
    public void setArmor(int armor)
    {
        _armor = armor;
    }

    /**
     * @return the factionId
     */
    public String getFactionId()
    {
        return _factionId;
    }

    /**
     * @param factionId the factionId to set
     */
    public void setFactionId(String factionId)
    {
        if( factionId == null)
        {
            _factionId = null;
            return;
        }
        if(factionId.equals("ketra"))
            _specialFaction = FACTION_KETRA;
        else if(factionId.equals("varka"))
            _specialFaction = FACTION_VARKA;
        _factionId = factionId.intern();
    }

    /**
     * @return the factionRange
     */
    public int getFactionRange()
    {
        return _factionRange;
    }

    /**
     * @param factionRange the factionRange to set
     */
    public void setFactionRange(int factionRange)
    {
        _factionRange = factionRange;
    }

    /**
     * @return the idTemplate
     */
    public int getIdTemplate()
    {
        return _idTemplate;
    }

    /**
     * @param idTemplate the idTemplate to set
     */
    public void setIdTemplate(int idTemplate)
    {
        _idTemplate = idTemplate;
    }

    /**
     * @return the level
     */
    public byte getLevel()
    {
        return _level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(byte level)
    {
        _level = level;
    }

    /**
     * @return the lhand
     */
    public int getLhand()
    {
        return _lhand;
    }

    /**
     * @param lhand the lhand to set
     */
    public void setLhand(int lhand)
    {
        _lhand = lhand;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return _name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        _name = name;
    }

    /**
     * @return the npcId
     */
    public int getNpcId()
    {
        return _npcId;
    }

    /**
     * @param npcId the npcId to set
     */
    public void setNpcId(int npcId)
    {
        _npcId = npcId;
    }

    /**
     * @return the rewardExp
     */
    public int getRewardExp()
    {
        return _rewardExp;
    }

    /**
     * @param rewardExp the rewardExp to set
     */
    public void setRewardExp(int rewardExp)
    {
        _rewardExp = rewardExp;
    }

    /**
     * @return the rewardSp
     */
    public int getRewardSp()
    {
        return _rewardSp;
    }

    /**
     * @param rewardSp the rewardSp to set
     */
    public void setRewardSp(int rewardSp)
    {
        _rewardSp = rewardSp;
    }

    /**
     * @return the rhand
     */
    public int getRhand()
    {
        return _rhand;
    }

    /**
     * @param rhand the rhand to set
     */
    public void setRhand(int rhand)
    {
        _rhand = rhand;
    }

    /**
     * @return the serverSideName
     */
    public boolean isServerSideName()
    {
        return _serverSideName;
    }

    /**
     * @param serverSideName the serverSideName to set
     */
    public void setServerSideName(boolean serverSideName)
    {
        _serverSideName = serverSideName;
    }

    /**
     * @return the serverSideTitle
     */
    public boolean isServerSideTitle()
    {
        return _serverSideTitle;
    }

    /**
     * @param serverSideTitle the serverSideTitle to set
     */
    public void setServerSideTitle(boolean serverSideTitle)
    {
        _serverSideTitle = serverSideTitle;
    }

    /**
     * @return the sex
     */
    public String getSex()
    {
        return _sex;
    }

    /**
     * @param sex the sex to set
     */
    public void setSex(String sex)
    {
        _sex = sex;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return _title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        _title = title;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return _type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        _type = type;
    }

    /**
     * @return the race
     */
    public int getRace()
    {
        return _race;
    }

    /**
     * @param factionName the nPCFactionName to set
     */
    public void setNPCFactionName(String factionName)
    {
        _npcFactionName = ( factionName == null ? "Devine Clan" : factionName);
    }

    /**
     * @return the jClass
     */
    public String getJClass()
    {
        return _jClass;
    }

    /**
     * @param class1 the jClass to set
     */
    public void setJClass(String class1)
    {
        _jClass = class1;
    }

    public byte getSpecialFaction()
    {
        return _specialFaction;
    }
}
