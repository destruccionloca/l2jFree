package com.l2jfree.gameserver.skills.l2skills;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.templates.StatsSet;

/**
 * @author NB4L1
 */
public final class L2SkillFusion extends L2Skill
{
	private final int _fusionTriggeredId;
	private final int _fusionTriggeredLvl;
	
	public L2SkillFusion(StatsSet set)
	{
		super(set);
		
		_fusionTriggeredId = set.getInteger("fusionTriggeredId");
		_fusionTriggeredLvl = set.getInteger("fusionTriggeredLevel");
	}
	
	@Override
	public void validate() throws Exception
	{
		super.validate();
		
		// must have fusion triggered skill
		if (getFusionTriggeredSkill() == null)
			throw new IllegalStateException(toString());
		
		// can't have triggered skill
		if (getTriggeredSkill() != null)
			throw new IllegalStateException(toString());
		
		// can't have effects
		if (getEffectTemplates() != null)
			throw new IllegalStateException(toString());
	}
	
	public L2Skill getFusionTriggeredSkill()
	{
		return SkillTable.getInstance().getInfo(_fusionTriggeredId, _fusionTriggeredLvl);
	}
}
