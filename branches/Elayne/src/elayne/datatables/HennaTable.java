package elayne.datatables;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import elayne.IFileReference;
import elayne.application.Activator;
import elayne.templates.L2Henna;

public class HennaTable
{
	private static HennaTable _instance;
	public final static int TYPE_HENNA_CON = 4;
	public final static int TYPE_HENNA_DEX = 5;
	public final static int TYPE_HENNA_INT = 6;
	public final static int TYPE_HENNA_MEN = 1;
	public final static int TYPE_HENNA_STR = 2;
	public final static int TYPE_HENNA_WIT = 3;

	public static HennaTable getInstance()
	{
		if (_instance == null)
			_instance = new HennaTable();
		return _instance;
	}

	private FastMap<Integer, L2Henna> hennaMap = new FastMap<Integer, L2Henna>();

	public L2Henna getHenna(int symbolId)
	{
		return hennaMap.get(symbolId);
	}

	public boolean isKnownHenna(int symbolId)
	{
		return hennaMap.containsKey(symbolId);
	}

	public void restore()
	{
		LineNumberReader lnr = null;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			Path path = new Path(IFileReference.HENNA_FILE);
			URL fileURL = FileLocator.find(bundle, path, null);
			String filePath = FileLocator.toFileURL(fileURL).getFile();
			File file = new File(filePath);

			if (!file.exists())
			{
				System.out.println("HennaTable: ATTENTION! THE HENNA.TXT FILE IS MISSING!");
				return;
			}

			int i = 0;
			String line = null;
			lnr = new LineNumberReader(new FileReader(file));
			while ((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (st.hasMoreTokens())
				{
					String info = st.nextToken();
					StringTokenizer st1 = new StringTokenizer(info, " ");

					int symbolId = Integer.parseInt(st1.nextToken());
					String symbolName = st1.nextToken();
					int dyeId = Integer.parseInt(st1.nextToken());
					int dyeAmount = Integer.parseInt(st1.nextToken());
					int price = Integer.parseInt(st1.nextToken());
					int statINT = Integer.parseInt(st1.nextToken());
					int statSTR = Integer.parseInt(st1.nextToken());
					int statCON = Integer.parseInt(st1.nextToken());
					int statMEM = Integer.parseInt(st1.nextToken());
					int statDEX = Integer.parseInt(st1.nextToken());
					int statWIT = Integer.parseInt(st1.nextToken());
					int typeplus = 0;
					int valueplus = 0;
					int typesub = 0;
					int valuesub = 0;

					// + VALUES
					if (statINT > 0)
					{
						typeplus = TYPE_HENNA_INT;
						valueplus = statINT;
					}
					else if (statSTR > 0)
					{
						typeplus = TYPE_HENNA_STR;
						valueplus = statSTR;
					}

					else if (statCON > 0)
					{
						typeplus = TYPE_HENNA_CON;
						valueplus = statCON;
					}

					else if (statMEM > 0)
					{
						typeplus = TYPE_HENNA_MEN;
						valueplus = statMEM;
					}

					else if (statDEX > 0)
					{
						typeplus = TYPE_HENNA_DEX;
						valueplus = statDEX;
					}

					else if (statWIT > 0)
					{
						typeplus = TYPE_HENNA_WIT;
						valueplus = statWIT;
					}

					// - VALUES
					if (statINT < 0)
					{
						typesub = TYPE_HENNA_INT;
						valuesub = statINT;
					}
					else if (statSTR < 0)
					{
						typesub = TYPE_HENNA_STR;
						valuesub = statSTR;
					}

					else if (statCON < 0)
					{
						typesub = TYPE_HENNA_CON;
						valuesub = statCON;
					}

					else if (statMEM < 0)
					{
						typesub = TYPE_HENNA_MEN;
						valuesub = statMEM;
					}

					else if (statDEX < 0)
					{
						typesub = TYPE_HENNA_DEX;
						valuesub = statDEX;
					}

					else if (statWIT < 0)
					{
						typesub = TYPE_HENNA_WIT;
						valuesub = statWIT;
					}
					hennaMap.put(symbolId, new L2Henna(symbolId, symbolName, dyeId, dyeAmount, price, statINT, statSTR, statCON, statMEM, statDEX, statWIT, typeplus, valueplus, typesub, valuesub));
					i++;
				}
			}
			System.out.println("HennaTable: A total of " + i + " Hennas have been loaded.");
		}
		catch (IOException e1)
		{
			System.out.println("HennaTable: Problems ocurred while restoring the Hennas: " + e1.getMessage());
			e1.printStackTrace();
		}
		finally
		{
			try
			{
				if (lnr != null)
					lnr.close();
			}
			catch (Exception e2)
			{
				// nothing
			}
		}
	}
}
