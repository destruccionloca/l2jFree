package com.l2jfree.config.model;

import java.util.ArrayList;
import java.util.List;

public final class ConfigGroup
{
	private final List<ConfigFieldInfo> _infos = new ArrayList<ConfigFieldInfo>();
	
	public void add(ConfigFieldInfo info)
	{
		_infos.add(info);
	}
	
	public boolean isModified()
	{
		for (ConfigFieldInfo info : _infos)
			if (info.isModified())
				return true;
		
		return false;
	}
}
