package elayne.datatables;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

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
import elayne.templates.L2Armor;

/**
 * This class is responsible for loading and holding information about Lineage2
 * armors. Read into the class for more information.
 * @author polbat02
 */
public class ArmorTable
{
	private static ArmorTable _instance;

	/** Returns the only instance of this class. */
	public static ArmorTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ArmorTable();
			return _instance;
		}
		return _instance;
	}

	/**
	 * Map containing all the information of the loaded armors.
	 */
	private FastMap<Integer, L2Armor> armors = new FastMap<Integer, L2Armor>();

	/** Constructor. */
	private ArmorTable()
	{
		armors = new FastMap<Integer, L2Armor>();
	}

	public Collection<L2Armor> getAllArmors()
	{
		return armors.values();
	}

	public L2Armor getArmor(int id)
	{
		return armors.get(id);
	}

	/**
	 * Checks if a given name corresponds to a armor Id, and returns the id of
	 * the found armor (if any).
	 * @param name The name to look for.
	 * @return The Id of the found armor if any, 0 else.
	 */
	public int getArmorId(String name)
	{
		int id = 0;
		// Check if this is an armor.
		for (L2Armor armor : armors.values())
		{
			if (armor.getName().toLowerCase().equals(name.toLowerCase()))
			{
				id = armor.getId();
				break;
			}
		}
		return id;
	}

	public boolean isArmor(int id)
	{
		return armors.containsKey(id);
	}

	/**
	 * Loads all the armors defined in the Armors File (see
	 * {@link IFileReference}), and stores them in a FastMap.
	 * @throws IOException
	 */
	public void load() throws IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);

		// get the plugin bundle
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		// create the path relative to the plugin root
		Path path = new Path(IFileReference.ARMORS_FILE);

		// find the file URL for this path
		URL fileURL = FileLocator.find(bundle, path, null);

		// get the absolute path
		String filePath = FileLocator.toFileURL(fileURL).getFile();

		File file = new File(filePath);

		if (!file.exists())
		{
			System.out.println("ArmorTable: ATTENTION! THE ARMORS.XML FILE IS MISSING!");
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
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int itemId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							String name = attrs.getNamedItem("name").getNodeValue();
							int weight = Integer.parseInt(attrs.getNamedItem("weight").getNodeValue());
							int price = Integer.parseInt(attrs.getNamedItem("price").getNodeValue());
							String sellable = attrs.getNamedItem("sellable").getNodeValue();
							String armor_type = attrs.getNamedItem("armor_type").getNodeValue();
							armors.put(itemId, new L2Armor(itemId, name, weight, price, sellable, armor_type));
							charged++;
						}
					}
				}
			}
			System.out.println("ArmorTable: " + charged + " armors charged correctly.");
		}
		catch (SAXException e)
		{
			System.out.println("ArmorTable: Error while loading items: " + e.toString());
		}
		catch (IOException e)
		{
			System.out.println("ArmorTable: Error while loading items: " + e.toString());
		}
		catch (ParserConfigurationException e)
		{
			System.out.println("ArmorTable: Error while loading items: " + e.toString());
		}
	}
}
