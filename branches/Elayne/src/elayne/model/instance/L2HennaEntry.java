package elayne.model.instance;

import elayne.datatables.HennaTable;
import elayne.model.L2Character;

public class L2HennaEntry extends L2Character
{
	private int classIndex;
	private L2HennaGroup parent;
	private int slot;
	private int symbolId;

	public L2HennaEntry(L2HennaGroup parent, int symbolId, int slot, int class_index)
	{
		this.parent = parent;
		this.symbolId = symbolId;
		this.slot = slot;
		this.classIndex = class_index;
	}

	public int getClassIndex()
	{
		return classIndex;
	}

	@Override
	public String getName()
	{
		return HennaTable.getInstance().getHenna(symbolId).getName();
	}

	@Override
	public L2GroupEntry getParent()
	{
		return parent;
	}

	public String getPicLoc()
	{
		return HennaTable.getInstance().getHenna(symbolId).getPicLoc();
	}

	public int getSlot()
	{
		return slot;
	}

	public int getSymbolId()
	{
		return symbolId;
	}
}
