package elayne.model.instance;

import elayne.model.L2Character;

public class L2SkillEntry extends L2Character
{
	private int class_index;
	private final L2SkillsGroup parentSkillsGroup;
	private int skillId;
	private int skillLevel;
	private String skillName;

	public L2SkillEntry(int skillId, int skillLevel, String skillName, int class_index, L2SkillsGroup parentSkillsGroup)
	{
		this.skillId = skillId;
		this.skillLevel = skillLevel;
		this.skillName = skillName;
		this.class_index = class_index;
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
	public L2SkillsGroup getParent()
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
