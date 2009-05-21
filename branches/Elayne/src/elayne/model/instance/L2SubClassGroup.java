package elayne.model.instance;

import javolution.util.FastList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.model.L2Character;

public class L2SubClassGroup extends L2GroupEntry
{
	public L2SubClassGroup(L2PcInstance parent, String name)
	{
		super(parent, name);
	}

	/**
	 * This Void adds the skills of the given sub to a given playerGroup entry.
	 * @param subId --> The sub to get the skills from.
	 * @param entry --> The entry onto which we have to add the skills.
	 */
	private boolean addSkills(final int subId, final L2SkillsGroup entry)
	{
		try
		{
			entry.getParent().setPlayerSkills(subId, entry);
			if (entry.getParent().getPlayerSkillsByClass(subId).size() > 0)
			{
				for (L2SkillEntry skill : entry.getParent().getPlayerSkillsByClass(subId))
					entry.addEntry(skill);
			}
			else
				return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.SUBCLASS_GROUP);
	}

	@Override
	public L2PcInstance getParent()
	{
		return (L2PcInstance) parent;
	}

	public void restore()
	{
		if (getParent().getSubs().size() > 0)
		{
			int subNum = 0;
			FastList<Integer> classIds = new FastList<Integer>();
			for (L2SubClass sub : getParent().getSubs())
			{
				subNum++;
				if (classIds.contains(sub.getClassIndex()))
					continue;
				classIds.add(sub.getClassIndex());
				for (L2Character entry : sub.getEntries())
				{
					sub.removeEntry(entry);
				}
				sub.setParent(this);
				addEntry(sub);

				// ADD SKILLS
				L2SkillsGroup skills = new L2SkillsGroup(getParent(), "Skills", sub);
				sub.addEntry(skills);
				if (!addSkills(sub.getClassIndex(), skills))
					sub.removeEntry(skills);

				// ADD HENNA GROUP
				if (getParent().getHennaGroup(sub.getClassIndex()).getEntries().length > 0)
					sub.addEntry(getParent().getHennaGroup(sub.getClassIndex()));
			}
			System.out.println("L2SubClassGroup: Restored " + subNum + " sub classes.");
		}
		if (getEntries().length != 0)
			getParent().addEntry(this);
	}
}
