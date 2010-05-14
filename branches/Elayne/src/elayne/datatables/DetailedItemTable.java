package elayne.datatables;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
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
import elayne.templates.L2PictureLocation;

/**
 * @author polbat02
 */
public class DetailedItemTable
{
	private static DetailedItemTable _instance;

	public static DetailedItemTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new DetailedItemTable();
			return _instance;
		}
		return _instance;
	}

	private FastMap<Integer, L2PictureLocation> items = new FastMap<Integer, L2PictureLocation>();

	public DetailedItemTable()
	{
		items = new FastMap<Integer, L2PictureLocation>();
	}

	public boolean doesItemHavePicture(int itemId)
	{
		return items.containsKey(itemId);
	}

	public L2PictureLocation getItem(int itemId)
	{
		return items.get(itemId);
	}

	public Collection<L2PictureLocation> getItems()
	{
		return items.values();
	}

	public Set<Integer> getItemsIds()
	{
		return items.keySet();
	}

	public void load() throws IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);

		// get the plugin bundle
		Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);

		// create the path relative to the plugin root
		Path path = new Path(IFileReference.ITEMS_FILE);

		// find the file URL for this path
		URL fileURL = FileLocator.find(bundle, path, null);

		// get the absolute path
		String filePath = FileLocator.toFileURL(fileURL).getFile();

		File file = new File(filePath);

		if (!file.exists())
		{
			System.out.println("DetailedItemTable: ATTENTION! THE ITEMS.XML FILE IS MISSING!");
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
							String grade = attrs.getNamedItem("grade").getNodeValue();
							String picloc = attrs.getNamedItem("picloc").getNodeValue();
							items.put(itemId, new L2PictureLocation(itemId, name, grade, picloc));
							loaded++;
						}
					}
				}
			}
			System.out.println("DetailedItemTable: " + loaded + " general Items loaded correctly.");
		}
		catch (SAXException e)
		{
			System.out.println("DetailedItemTable: Error while loading items: " + e.toString());
		}
		catch (IOException e)
		{
			System.out.println("DetailedItemTable: Error while loading items: " + e.toString());
		}
		catch (ParserConfigurationException e)
		{
			System.out.println("DetailedItemTable: Error while loading items: " + e.toString());
		}
	}
}
