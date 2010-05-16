package elayne.model.instance;

import elayne.model.L2Character;

public class L2ClanSkillEntry extends L2Character
{
	private int _classIndex;
	private final L2ClanSkillsGroup _parentSkillsGroup;
	private int _skillId;
	private int _skillLevel;
	private String _skillName;

	public L2ClanSkillEntry(int skillId, int skillLevel, String skillName, L2ClanSkillsGroup parentSkillsGroup)
	{
		_skillId = skillId;
		_skillLevel = skillLevel;
		_skillName = skillName;
		_parentSkillsGroup = parentSkillsGroup;
	}

	public int getClassIndex()
	{
		return _classIndex;
	}

	@Override
	public String getName()
	{
		return _skillName;
	}

	@Override
	public L2ClanSkillsGroup getParent()
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
