package elayne.datatables;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import elayne.templates.L2Skill;

/**
 * @author Psycho(killer1888) / L2jFree
 */

public class SkillsTable
{
	private static SkillsTable _instance;

	/** Returns the only instance of this class. */
	public static SkillsTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new SkillsTable();
			return _instance;
		}
		return _instance;
	}

	/**
	 * Map containing all the information of the loaded armors.
	 */
	private FastMap<Integer, L2Skill> skills = new FastMap<Integer, L2Skill>();

	/** Constructor. */
	private SkillsTable()
	{
		skills = new FastMap<Integer, L2Skill>();
	}

	public L2Skill getSkill(int id)
	{
		return skills.get(id);
	}

	public String getSkillName(int id)
	{
		String name = new String();

		for (L2Skill skill : skills.values())
		{
			if (skill.getSkillId() == id)
				name = skill.getName();
		}
		if (name.isEmpty())
			return "Unknown";
		return name;
	}

	public void load() throws IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);

		// get the plugin bundle
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		// create the path relative to the plugin root
		Path path = new Path(IFileReference.SKILLS_FILE);

		// find the file URL for this path
		URL fileURL = FileLocator.find(bundle, path, null);

		// get the absolute path
		String filePath = FileLocator.toFileURL(fileURL).getFile();

		File file = new File(filePath);

		if (!file.exists())
		{
			System.out.println("ArmorTable: ATTENTION! THE SKILLS.XML FILE IS MISSING!");
			return;
		}

		int loaded = 0;
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
						if ("skill".equalsIgnoreCase(d.getNodeName()))
						{
							int skillEnch1 = 0;
							int skillEnch2 = 0;
							int skillEnch3 = 0;
							int skillEnch4 = 0;
							int skillEnch5 = 0;
							int skillEnch6 = 0;
							int skillEnch7 = 0;
							int skillEnch8 = 0;

							NamedNodeMap attrs = d.getAttributes();
							int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							int skillLevels = Integer.parseInt(attrs.getNamedItem("levels").getNodeValue());
							String name = attrs.getNamedItem("name").getNodeValue();

							if (attrs.getNamedItem("enchantLevels1") != null)
								skillEnch1 = Integer.parseInt(attrs.getNamedItem("enchantLevels1").getNodeValue());
							if (attrs.getNamedItem("enchantLevels2") != null)
								skillEnch2 = Integer.parseInt(attrs.getNamedItem("enchantLevels2").getNodeValue());
							if (attrs.getNamedItem("enchantLevels3") != null)
								skillEnch3 = Integer.parseInt(attrs.getNamedItem("enchantLevels3").getNodeValue());
							if (attrs.getNamedItem("enchantLevels4") != null)
								skillEnch4 = Integer.parseInt(attrs.getNamedItem("enchantLevels4").getNodeValue());
							if (attrs.getNamedItem("enchantLevels5") != null)
								skillEnch5 = Integer.parseInt(attrs.getNamedItem("enchantLevels5").getNodeValue());
							if (attrs.getNamedItem("enchantLevels6") != null)
								skillEnch6 = Integer.parseInt(attrs.getNamedItem("enchantLevels6").getNodeValue());
							if (attrs.getNamedItem("enchantLevels7") != null)
								skillEnch7 = Integer.parseInt(attrs.getNamedItem("enchantLevels7").getNodeValue());
							if (attrs.getNamedItem("enchantLevels8") != null)
								skillEnch8 = Integer.parseInt(attrs.getNamedItem("enchantLevels8").getNodeValue());
							
							skills.put(skillId, new L2Skill(skillId, name, skillLevels, skillEnch1, skillEnch2, skillEnch3, skillEnch4, skillEnch5, skillEnch6, skillEnch7, skillEnch8));
							loaded++;
						}
					}
				}
			}
			System.out.println("SkillsTable: " + loaded + " skills loaded correctly.");
		}
		catch (SAXException e)
		{
			System.out.println("SkillsTable: Error while loading skill: " + e.toString());
		}
		catch (IOException e)
		{
			System.out.println("SkillsTable: Error while loading skill: " + e.toString());
		}
		catch (ParserConfigurationException e)
		{
			System.out.println("SkillsTable: Error while loading skill: " + e.toString());
		}
	}
}
