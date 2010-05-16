package elayne.model.instance;

import elayne.datatables.HennaTable;
import elayne.model.L2Character;

public class L2HennaEntry extends L2Character
{
	private int _classIndex;
	private L2HennaGroup _parent;
	private int _slot;
	private int _symbolId;

	public L2HennaEntry(L2HennaGroup parent, int symbolId, int slot, int class_index)
	{
		_parent = parent;
		_symbolId = symbolId;
		_slot = slot;
		_classIndex = class_index;
	}

	public int getClassIndex()
	{
		return _classIndex;
	}

	@Override
	public String getName()
	{
		return HennaTable.getInstance().getHenna(_symbolId).getName();
	}

	@Override
	public L2GroupEntry getParent()
	{
		return _parent;
	}

	public String getPicLoc()
	{
		return HennaTable.getInstance().getHenna(_symbolId).getPicLoc();
	}

	public int getSlot()
	{
		return _slot;
	}

	public int getSymbolId()
	{
		return _symbolId;
	}
}
