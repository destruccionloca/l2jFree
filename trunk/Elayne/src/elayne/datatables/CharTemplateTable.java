package elayne.datatables;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javolution.util.FastMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import elayne.IFileReference;
import elayne.application.Activator;
import elayne.templates.L2CharacterTemplate;

public class CharTemplateTable
{
	private static CharTemplateTable _instance;

	private static final String[] CHAR_CLASSES = { "Human Fighter", "Warrior", "Gladiator", "Warlord", "Human Knight", "Paladin", "Dark Avenger", "Rogue", "Treasure Hunter", "Hawkeye",
							"Human Mystic", "Human Wizard", "Sorceror", "Necromancer", "Warlock", "Cleric", "Bishop", "Prophet", "Elven Fighter", "Elven Knight", "Temple Knight", "Swordsinger",
							"Elven Scout", "Plainswalker", "Silver Ranger", "Elven Mystic", "Elven Wizard", "Spellsinger", "Elemental Summoner", "Elven Oracle", "Elven Elder", "Dark Fighter",
							"Palus Knight", "Shillien Knight", "Bladedancer", "Assassin", "Abyss Walker", "Phantom Ranger", "Dark Elven Mystic", "Dark Elven Wizard", "Spellhowler",
							"Phantom Summoner", "Shillien Oracle", "Shillien Elder", "Orc Fighter", "Orc Raider", "Destroyer", "Orc Monk", "Tyrant", "Orc Mystic", "Orc Shaman", "Overlord",
							"Warcryer", "Dwarven Fighter", "Dwarven Scavenger", "Bounty Hunter", "Dwarven Artisan", "Warsmith", "dummyEntry1", "dummyEntry2", "dummyEntry3", "dummyEntry4",
							"dummyEntry5", "dummyEntry6", "dummyEntry7", "dummyEntry8", "dummyEntry9", "dummyEntry10", "dummyEntry11", "dummyEntry12", "dummyEntry13", "dummyEntry14", "dummyEntry15",
							"dummyEntry16", "dummyEntry17", "dummyEntry18", "dummyEntry19", "dummyEntry20", "dummyEntry21", "dummyEntry22", "dummyEntry23", "dummyEntry24", "dummyEntry25",
							"dummyEntry26", "dummyEntry27", "dummyEntry28", "dummyEntry29", "dummyEntry30", "Duelist", "DreadNought", "Phoenix Knight", "Hell Knight", "Sagittarius", "Adventurer",
							"Archmage", "Soultaker", "Arcana Lord", "Cardinal", "Hierophant", "Eva Templar", "Sword Muse", "Wind Rider", "Moonlight Sentinel", "Mystic Muse", "Elemental Master",
							"Eva's Saint", "Shillien Templar", "Spectral Dancer", "Ghost Hunter", "Ghost Sentinel", "Storm Screamer", "Spectral Master", "Shillien Saint", "Titan", "Grand Khauatari",
							"Dominator", "Doomcryer", "Fortune Seeker", "Maestro", "dummyEntry31", "dummyEntry32", "dummyEntry33", "dummyEntry34", "Male Soldier", "Female Soldier", "Trooper",
							"Warder", "Berserker", "Male Soulbreaker", "Female Souldbreaker", "Arbalester", "Doombringer", "Male Soulhound", "Female Soulhound", "Trickster", "Inspector", "Judicator" };

	public static CharTemplateTable getInstance()
	{
		if (_instance == null)
			_instance = new CharTemplateTable();
		return _instance;
	}

	private FastMap<String, L2CharacterTemplate> templates = new FastMap<String, L2CharacterTemplate>();

	public final String getClassNameById(int classId)
	{
		return CHAR_CLASSES[classId];
	}

	public L2CharacterTemplate getTemplate(int classId)
	{
		return templates.get(classId);
	}

	public Set<String> getTemplateNames()
	{
		return templates.keySet();
	}

	public FastMap<String, L2CharacterTemplate> getTemplates()
	{
		return templates;
	}

	public void load() throws IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);

		// get the plugin bundle
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		// create the path relative to the plugin root
		Path path = new Path(IFileReference.CHARACTER_TEMPLATES_FILE);

		// find the file URL for this path
		URL fileURL = FileLocator.find(bundle, path, null);

		// get the absolute path
		String filePath = FileLocator.toFileURL(fileURL).getFile();

		File file = new File(filePath);

		if (!file.exists())
		{
			System.out.println("CharTemplateTable: ATTENTION! THE CHAR_TEMPLATES.XML FILE IS MISSING!");
			return;
		}

		int charged = 0;
		Document doc;
		try
		{
			doc = factory.newDocumentBuilder().parse(file);
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("template".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int classId = Integer.parseInt(attrs.getNamedItem("ClassId").getNodeValue());
							String className = attrs.getNamedItem("ClassName").getNodeValue();

							L2CharacterTemplate template = new L2CharacterTemplate(classId, className);

							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("stat".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									String name = attrs.getNamedItem("name").getNodeValue();
									String val = attrs.getNamedItem("val").getNodeValue();
									template.setParameter(name, val);
								}
							}
							templates.put(className, template);
							charged++;
						}
					}
				}
			}
			System.out.println("CharTemplateTable: " + charged + " templates charged correctly.");
		}
		catch (SAXException e)
		{
			System.out.println("CharTemplateTable: : Error while loading templates: " + e.toString());
		}
		catch (IOException e)
		{
			System.out.println("CharTemplateTable: : Error while loading templates: " + e.toString());
		}
		catch (ParserConfigurationException e)
		{
			System.out.println("CharTemplateTable: : Error while loading templates: " + e.toString());
		}
	}
}
