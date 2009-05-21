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
import elayne.templates.L2Item;

/**
 * @author polbat02
 */
public class ItemTable
{
	private static ItemTable _instance;

	public static ItemTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ItemTable();
			return _instance;
		}
		return _instance;
	}

	private FastMap<Integer, L2Item> items = new FastMap<Integer, L2Item>();

	public Collection<L2Item> getAllItems()
	{
		return items.values();
	}

	public L2Item getItem(int id)
	{
		return items.get(id);
	}

	/**
	 * Checks if a given name corresponds to an item Id, and returns the id of
	 * the found item (if any).
	 * @param name The name to look for.
	 * @return The Id of the found item if any, 0 else.
	 */
	public int getItemId(String name)
	{
		int id = 0;
		// Check if this is a weapon.
		for (L2Item item : items.values())
		{
			if (item.getName().toLowerCase().equals(name.toLowerCase()))
			{
				id = item.getId();
				break;
			}
		}
		return id;
	}

	public boolean isItem(int id)
	{
		return items.containsKey(id);
	}

	public void load() throws IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);

		// get the plugin bundle
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		// create the path relative to the plugin root
		Path path = new Path(IFileReference.ETC_ITEMS_FILE);

		// find the file URL for this path
		URL fileURL = FileLocator.find(bundle, path, null);

		// get the absolute path
		String filePath = FileLocator.toFileURL(fileURL).getFile();

		File file = new File(filePath);

		if (!file.exists())
		{
			System.out.println("ItemTable: ATTENTION! THE ETCITEM.XML FILE IS MISSING!");
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
							String item_type = attrs.getNamedItem("item_type").getNodeValue();
							items.put(itemId, new L2Item(itemId, name, weight, price, sellable, item_type));
							charged++;
						}
					}
				}
			}
			System.out.println("ItemTable: " + charged + " items charged correctly.");
		}
		catch (SAXException e)
		{
			System.out.println("ItemTable: Error while loading items: " + e.toString());
		}
		catch (IOException e)
		{
			System.out.println("ItemTable: Error while loading items: " + e.toString());
		}
		catch (ParserConfigurationException e)
		{
			System.out.println("ItemTable: Error while loading items: " + e.toString());
		}
	}
}
