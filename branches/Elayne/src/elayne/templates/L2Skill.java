package elayne.templates;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class L2Skill
{
	private final int _skillId;
	private final String _name;
	private final int _skillLevels;
	private final int _skillEnch1;
	private final int _skillEnch2;
	private final int _skillEnch3;

	public L2Skill(int skillId, String name, int skillLevels, int skillEnch1, int skillEnch2, int skillEnch3)
	{
		_skillId = skillId;
		_name = name;
		_skillLevels = skillLevels;
		_skillEnch1 = skillEnch1;
		_skillEnch2 = skillEnch2;
		_skillEnch3 = skillEnch3;
	}

	public int getSkillId()
	{
		return _skillId;
	}

	public String getName()
	{
		return _name;
	}

	public int getSkillMaxLevel()
	{
		return _skillLevels;
	}

	public int getSkillEnch1()
	{
		return _skillEnch1;
	}

	public int getSkillEnch2()
	{
		return _skillEnch2;
	}

	public int getSkillEnch3()
	{
		return _skillEnch3;
	}
}
