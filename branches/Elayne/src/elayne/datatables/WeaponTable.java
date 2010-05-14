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
import elayne.templates.L2Weapon;

/**
 * @author polbat02
 */
public class WeaponTable
{
	private static WeaponTable _instance;

	public static WeaponTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new WeaponTable();
			return _instance;
		}
		return _instance;
	}

	private FastMap<Integer, L2Weapon> weapons = new FastMap<Integer, L2Weapon>();

	public Collection<L2Weapon> getAllWeapons()
	{
		return weapons.values();
	}

	public L2Weapon getWeapon(int id)
	{
		return weapons.get(id);

	}

	/**
	 * Checks if a given name corresponds to a weapon Id, and returns the id of
	 * the found weapon (if any).
	 * @param name The name to look for.
	 * @return The Id of the found weapon if any, 0 else.
	 */
	public int getWeaponId(String name)
	{
		int id = 0;
		// Check if this is a weapon.
		for (L2Weapon weapon : weapons.values())
		{
			if (weapon.getName().toLowerCase().equals(name.toLowerCase()))
			{
				id = weapon.getId();
				break;
			}
		}
		return id;
	}

	public boolean isWeapon(int id)
	{
		return weapons.containsKey(id);
	}

	public void load() throws IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);

		// get the plugin bundle
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		// create the path relative to the plugin root
		Path path = new Path(IFileReference.WEAPONS_FILE);

		// find the file URL for this path
		URL fileURL = FileLocator.find(bundle, path, null);

		// get the absolute path
		String filePath = FileLocator.toFileURL(fileURL).getFile();

		File file = new File(filePath);

		if (!file.exists())
		{
			System.out.println("WeaponTable: ATTENTION! THE WEAPONS.XML FILE IS MISSING!");
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
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int itemId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							String name = attrs.getNamedItem("name").getNodeValue();
							int weight = Integer.parseInt(attrs.getNamedItem("weight").getNodeValue());
							int price = Integer.parseInt(attrs.getNamedItem("price").getNodeValue());
							String sellable = attrs.getNamedItem("sellable").getNodeValue();
							String weapon_type = attrs.getNamedItem("weapon_type").getNodeValue();
							weapons.put(itemId, new L2Weapon(itemId, name, weight, price, sellable, weapon_type));
							loaded++;
						}
					}
				}
			}
			System.out.println("WeaponTable: " + loaded + " weapons loaded correctly.");
		}
		catch (SAXException e)
		{
			System.out.println("WeaponTable: Error while loading items: " + e.toString());
		}
		catch (IOException e)
		{
			System.out.println("WeaponTable: Error while loading items: " + e.toString());
		}
		catch (ParserConfigurationException e)
		{
			System.out.println("WeaponTable: Error while loading items: " + e.toString());
		}
	}
}
