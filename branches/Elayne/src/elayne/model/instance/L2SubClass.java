package elayne.model.instance;

import org.eclipse.jface.resource.ImageDescriptor;

import elayne.datatables.CharTemplateTable;

public class L2SubClass extends L2GroupEntry
{
	private final int _classId;
	private final int _level;
	private final int _classIndex;

	public L2SubClass(int classId, int level, int classIndex)
	{
		super(null, CharTemplateTable.getInstance().getClassNameById(classId));
		_classId = classId;
		_level = level;
		_classIndex = classIndex;
	}

	public int getClassId()
	{
		return _classId;
	}

	public int getClassIndex()
	{
		return _classIndex;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return getParent().getParent().getImageDescriptor();
	}

	public int getLevel()
	{
		return _level;
	}

	@Override
	public L2SubClassGroup getParent()
	{
		return (L2SubClassGroup) _parent;
	}

	public void setParent(L2SubClassGroup parent)
	{
		_parent = parent;
	}
}
