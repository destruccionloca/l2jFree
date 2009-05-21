package test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

import elayne.application.Activator;

public class MementoHandler
{

	private static final String ROOT_ID = "ElayneData";

	public static void saveState(String id, String[] values)
	{
		try
		{
			XMLMemento memento = XMLMemento.createWriteRoot(ROOT_ID);
			for (String item : values)
			{
				IMemento child = memento.createChild(id);
				child.putString("Path", item);
			}
			Writer writer = new FileWriter(MementoHandler.getMementoFile());
			memento.save(writer);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String[] loadState(String id)
	{
		String[] values = null;
		try
		{
			FileReader reader = new FileReader(getMementoFile());
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			IMemento[] children = memento.getChildren(ROOT_ID);
			values = new String[children.length];
			for (int i = 0; i < children.length; i++)
			{
				String value = children[i].getString(id);
				values[i] = value;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return values;
	}

	private static File getMementoFile() throws IOException
	{
		File mementofile = Activator.getDefault().getStateLocation().append("data.xml").toFile();
		if (!mementofile.exists())
		{
			mementofile.createNewFile();
			// initial write a root to prevent errors while reading
			XMLMemento memento = XMLMemento.createWriteRoot(ROOT_ID);
			Writer writer = new FileWriter(getMementoFile());
			memento.save(writer);
		}
		return mementofile;
	}
}
