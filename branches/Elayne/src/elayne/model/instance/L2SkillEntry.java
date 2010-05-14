package elayne.model.instance;

import elayne.model.L2Character;

public class L2SkillEntry extends L2Character
{
	private int _class_index;
	private final L2SkillsGroup _parentSkillsGroup;
	private int _skillId;
	private int _skillLevel;
	private String _skillName;

	public L2SkillEntry(int skillId, int skillLevel, String skillName, int class_index, L2SkillsGroup parentSkillsGroup)
	{
		_skillId = skillId;
		_skillLevel = skillLevel;
		_skillName = skillName;
		_class_index = class_index;
		_parentSkillsGroup = parentSkillsGroup;
	}

	public int getClassIndex()
	{
		return _class_index;
	}

	@Override
	public String getName()
	{
		return _skillName;
	}

	@Override
	public L2SkillsGroup getParent()
	{
		return _parentSkillsGroup;
	}

	public int getSkillId()
	{
		return _skillId;
	}

	public int getSkillLevel()
	{
		return _skillLevel;
	}

	public String getSkillName()
	{
		return _skillName;
	}
}
