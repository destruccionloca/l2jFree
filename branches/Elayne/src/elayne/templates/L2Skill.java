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
	private final int _skillEnch4;
	private final int _skillEnch5;
	private final int _skillEnch6;
	private final int _skillEnch7;
	private final int _skillEnch8;

	public L2Skill(int skillId, String name, int skillLevels, int skillEnch1, int skillEnch2, int skillEnch3, int skillEnch4, int skillEnch5, int skillEnch6, int skillEnch7, int skillEnch8)
	{
		_skillId = skillId;
		_name = name;
		_skillLevels = skillLevels;
		_skillEnch1 = skillEnch1;
		_skillEnch2 = skillEnch2;
		_skillEnch3 = skillEnch3;
		_skillEnch4 = skillEnch4;
		_skillEnch5 = skillEnch5;
		_skillEnch6 = skillEnch6;
		_skillEnch7 = skillEnch7;
		_skillEnch8 = skillEnch8;
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

	public int getSkillEnch4()
	{
		return _skillEnch4;
	}

	public int getSkillEnch5()
	{
		return _skillEnch5;
	}

	public int getSkillEnch6()
	{
		return _skillEnch6;
	}

	public int getSkillEnch7()
	{
		return _skillEnch7;
	}

	public int getSkillEnch8()
	{
		return _skillEnch8;
	}
}
