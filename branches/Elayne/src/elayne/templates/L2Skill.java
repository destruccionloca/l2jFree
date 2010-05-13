package elayne.templates;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class L2Skill
{
	private final int _skillId;
	private final String _name;

	public L2Skill(int skillId, String name)
	{
		_skillId = skillId;
		_name = name;
	}

	public int getSkillId()
	{
		return _skillId;
	}

	public String getName()
	{
		return _name;
	}
}
