package elayne.model.instance;

import org.eclipse.jface.resource.ImageDescriptor;

import elayne.datatables.CharTemplateTable;

public class L2SubClass extends L2GroupEntry
{
	public int classId, level, classIndex;

	public L2SubClass(int classId, int level, int classIndex)
	{
		super(null, CharTemplateTable.getInstance().getClassNameById(classId));
		this.classId = classId;
		this.level = level;
		this.classIndex = classIndex;
	}

	public int getClassId()
	{
		return classId;
	}

	public int getClassIndex()
	{
		return classIndex;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return getParent().getParent().getImageDescriptor();
	}

	public int getLevel()
	{
		return level;
	}

	@Override
	public L2SubClassGroup getParent()
	{
		return (L2SubClassGroup) parent;
	}

	public void setParent(L2SubClassGroup parent)
	{
		this.parent = parent;
	}
}
