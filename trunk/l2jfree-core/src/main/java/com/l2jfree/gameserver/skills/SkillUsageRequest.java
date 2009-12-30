package com.l2jfree.gameserver.skills;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.tools.geometry.Point3D;

/**
 * Skill casting information (used to queue skill cast reuqests)
 * 
 * @author NB4L1
 */
public final class SkillUsageRequest
{
	private final L2Skill _skill;
	private final boolean _ctrlPressed;
	private final boolean _shiftPressed;
	private final Point3D _skillWorldPosition;
	
	public SkillUsageRequest(L2Skill skill, boolean ctrlPressed, boolean shiftPressed, Point3D skillWorldPosition)
	{
		_skill = skill;
		_ctrlPressed = ctrlPressed;
		_shiftPressed = shiftPressed;
		_skillWorldPosition = skillWorldPosition;
	}
	
	public SkillUsageRequest(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
	{
		this(skill, ctrlPressed, shiftPressed, null);
	}
	
	public SkillUsageRequest(L2Skill skill)
	{
		this(skill, false, false, null);
	}
	
	public L2Skill getSkill()
	{
		return _skill;
	}
	
	public int getSkillId()
	{
		return _skill != null ? _skill.getId() : -1;
	}
	
	public boolean isCtrlPressed()
	{
		return _ctrlPressed;
	}
	
	public boolean isShiftPressed()
	{
		return _shiftPressed;
	}
	
	public Point3D getSkillWorldPosition()
	{
		return _skillWorldPosition;
	}
}
