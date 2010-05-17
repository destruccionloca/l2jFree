package elayne.model;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.datatables.SkillsTable;
import elayne.model.instance.L2CharacterBriefEntry;
import elayne.model.instance.L2CharacterEntry;
import elayne.model.instance.L2ClanEntry;
import elayne.model.instance.L2ClanSkillEntry;
import elayne.model.instance.L2GroupEntry;
import elayne.model.instance.L2HennaEntry;
import elayne.model.instance.L2PcInstance;
import elayne.model.instance.L2RegularGroup;
import elayne.model.instance.L2SkillEntry;
import elayne.model.instance.L2SubClass;
import elayne.templates.L2Skill;

/**
 * This class defines how things will be shown in the user Interface depending
 * on the type of class they are. In this class we define the "layout" structure
 * that most of the labels, text, viewers and views we'll be dealing with.<br>
 * When a new class that needs a special layout (or not even special in some
 * cases) is created (for example a new L2PcInstance), this class manages how
 * this will be shown to the users.
 * @author polbat02
 */
public class L2AdapterFactory implements IAdapterFactory
{

	private IWorkbenchAdapter briefAdapter = new IWorkbenchAdapter()
	{
		public Object[] getChildren(Object o)
		{
			return new Object[0];
		}

		public ImageDescriptor getImageDescriptor(Object object)
		{
			L2CharacterBriefEntry entry = ((L2CharacterBriefEntry) object);
			if (entry.getAccessLevel() < 0)
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.BANNED_PLAYER);
			else if (entry.isOnline())
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.ONLINE);
			else
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.OFFLINE);
		}

		public String getLabel(Object o)
		{
			L2CharacterBriefEntry entry = ((L2CharacterBriefEntry) o);
			return entry.getName() + " Level: " + entry.getLevel();
		}

		public Object getParent(Object o)
		{
			return ((L2CharacterBriefEntry) o).getParent();
		}
	};

	/**
	 * A basic {@link IAdapterFactory} for a normal Clan Entry
	 */
	private IWorkbenchAdapter clanEntryAdapter = new IWorkbenchAdapter()
	{
		public Object[] getChildren(Object o)
		{
			return new Object[0];
		}

		public ImageDescriptor getImageDescriptor(Object object)
		{
			return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.GENERAL);
		}

		public String getLabel(Object o)
		{
			L2ClanEntry entry = ((L2ClanEntry) o);
			return entry.getName();
		}

		public Object getParent(Object o)
		{
			return ((L2ClanEntry) o).getParent();
		}
	};

	private IWorkbenchAdapter clanSkillAdapter = new IWorkbenchAdapter()
	{
		public Object[] getChildren(Object o)
		{
			return new Object[0];
		}

		public ImageDescriptor getImageDescriptor(Object object)
		{
			L2ClanSkillEntry entry = ((L2ClanSkillEntry) object);
			int skillId = entry.getSkillId();
			String pic_loc = getSkillPictureLocation(skillId);
			ImageDescriptor image = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/" + pic_loc + ".png");
			if (image != null)
				return image;
			return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PIC_NOT_FOUND);
		}

		public String getLabel(Object o)
		{
			L2ClanSkillEntry entry = ((L2ClanSkillEntry) o);
			return entry.getName() + " level: " + entry.getSkillLevel();
		}

		public Object getParent(Object o)
		{
			return ((L2ClanSkillEntry) o).getParent();
		}
	};

	/**
	 * A basic {@link IWorkbenchAdapter} for a normal Entry
	 */
	private IWorkbenchAdapter entryAdapter = new IWorkbenchAdapter()
	{
		public Object[] getChildren(Object o)
		{
			return new Object[0];
		}

		public ImageDescriptor getImageDescriptor(Object object)
		{
			return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.GENERAL);
		}

		public String getLabel(Object o)
		{
			L2CharacterEntry entry = ((L2CharacterEntry) o);
			return entry.getName();
		}

		public Object getParent(Object o)
		{
			return ((L2CharacterEntry) o).getParent();
		}
	};

	/**
	 * The group adapter is the main adapter for Groups if they don't have
	 * another more concrete layout defined.
	 */
	private IWorkbenchAdapter groupAdapter = new IWorkbenchAdapter()
	{
		public Object[] getChildren(Object o)
		{
			return ((L2GroupEntry) o).getEntries();
		}

		public ImageDescriptor getImageDescriptor(Object object)
		{
			L2GroupEntry group = ((L2GroupEntry) object);

			if (!(group instanceof L2RegularGroup))
				return group.getImageDescriptor();
			else if (group.getName().startsWith("Stats"))
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.STATS_GROUP);
			else if (group.getName().startsWith("Weapons"))
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.WEAPON_GROUP);
			else if (group.getName().startsWith("Armors"))
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.ARMOR_GROUP);
			else if (group.getName().startsWith("Items"))
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.ITEM_GROUP);
			else if (group.getName().equalsIgnoreCase("Clan Members"))
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.CLAN_MEMBERS_GROUP);
			else
				return group.getImageDescriptor();
		}

		public String getLabel(Object o)
		{
			L2GroupEntry group = ((L2GroupEntry) o);
			if (group.getName().startsWith("Weapons") || group.getName().startsWith("Armors") || group.getName().startsWith("Items"))
				return group.getName() + " (" + group.getEntries().length + " items)";
			if (group instanceof L2SubClass)
			{
				int level = ((L2SubClass) group).getLevel();
				return group.getName() + " - Level: " + level;
			}
			else if (group instanceof L2PcInstance)
			{
				if (((L2PcInstance) group).getLevel() > -1)
				{
					return group.getName() + " - Account: " + ((L2PcInstance) group).getAccount() + " - Level: " + ((L2PcInstance) group).getLevel() + " (" + ((L2PcInstance) group).getOnlineStatus() + ")";
				}
			}
			return group.getName();
		}

		public Object getParent(Object o)
		{
			return ((L2GroupEntry) o).getParent();
		}
	};

	private IWorkbenchAdapter hennaAdapter = new IWorkbenchAdapter()
	{
		public Object[] getChildren(Object o)
		{
			return new Object[0];
		}

		public ImageDescriptor getImageDescriptor(Object object)
		{
			L2HennaEntry entry = ((L2HennaEntry) object);

			return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, entry.getPicLoc());
		}

		public String getLabel(Object o)
		{
			L2HennaEntry entry = ((L2HennaEntry) o);
			return entry.getName();
		}

		public Object getParent(Object o)
		{
			return ((L2HennaEntry) o).getParent();
		}
	};

	/**
	 * This adapter defines the way skills should be shown.
	 */
	private IWorkbenchAdapter skillAdapter = new IWorkbenchAdapter()
	{
		public Object[] getChildren(Object o)
		{
			return new Object[0];
		}

		public ImageDescriptor getImageDescriptor(Object object)
		{
			L2SkillEntry entry = ((L2SkillEntry) object);
			int skillId = entry.getSkillId();
			String pic_loc = getSkillPictureLocation(skillId);
			ImageDescriptor image = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/" + pic_loc + ".png");
			if (image == null || image.getImageData() == null)
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PIC_NOT_FOUND);
			return image;
		}

		public String getLabel(Object o)
		{
			L2SkillEntry entry = ((L2SkillEntry) o);

			int skillLevel = entry.getSkillLevel();
			String enchantInfo = "";

			if (skillLevel > 100)
			{
				final int route = skillLevel / 100;
				final int enchantLevel = skillLevel % 100;

				L2Skill skill = SkillsTable.getInstance().getSkill(entry.getSkillId());
				if (skill != null)
					skillLevel = skill.getSkillMaxLevel();
				else
					skillLevel = 0;

				enchantInfo = " - Enchant: " + enchantLevel + " Route: " + route;
			}

			return entry.getName() + " - Level: " + skillLevel + "" + enchantInfo;
		}

		public Object getParent(Object o)
		{
			return ((L2SkillEntry) o).getParent();
		}
	};

	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType)
	{
		if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof L2GroupEntry)
			return groupAdapter;
		else if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof L2CharacterEntry)
			return entryAdapter;
		else if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof L2ClanEntry)
			return clanEntryAdapter;
		else if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof L2SkillEntry)
			return skillAdapter;
		else if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof L2ClanSkillEntry)
			return clanSkillAdapter;
		else if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof L2CharacterBriefEntry)
			return briefAdapter;
		else if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof L2HennaEntry)
			return hennaAdapter;
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class[] getAdapterList()
	{
		return new Class[] { IWorkbenchAdapter.class };
	}

	private String getSkillPictureLocation(int skillId)
	{
		String pic_loc = "";
		if (skillId < 10)
			pic_loc = "skill000" + skillId + "";
		else if (skillId >= 10 && skillId < 100)
			pic_loc = "skill00" + skillId + "";
		else if (skillId >= 100 && skillId < 1000)
			pic_loc = "skill0" + skillId + "";
		else if (skillId >= 1000)
			pic_loc = "skill" + skillId + "";
		return pic_loc;
	}
}
