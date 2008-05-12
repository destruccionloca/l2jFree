package elayne.model.instance;

import elayne.model.L2Character;

public class L2ClanSkillEntry extends L2Character
{
	private int class_index;
	private final L2ClanSkillsGroup parentSkillsGroup;
	private int skillId;
	private int skillLevel;
	private String skillName;

	public L2ClanSkillEntry(int skillId, int skillLevel, String skillName, L2ClanSkillsGroup parentSkillsGroup)
	{
		this.skillId = skillId;
		this.skillLevel = skillLevel;
		this.skillName = skillName;
		this.parentSkillsGroup = parentSkillsGroup;
	}

	public int getClassIndex()
	{
		return class_index;
	}

	@Override
	public String getName()
	{
		return skillName;
	}

	@Override
	public L2ClanSkillsGroup getParent()
	{
		return parentSkillsGroup;
	}

	public int getSkillId()
	{
		return skillId;
	}

	public int getSkillLevel()
	{
		return skillLevel;
	}

	public String getSkillName()
	{
		return skillName;
	}
}
