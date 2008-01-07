package net.sf.l2j.gameserver.util;

import java.io.File;
import java.io.FilenameFilter;

class CustomFileNameFilter implements FilenameFilter 
{
	String _ext;
	
	public CustomFileNameFilter(String extention)
	{
		_ext = extention;
	}
    public boolean accept(File dir, String name) 
    {
        return (name.endsWith(_ext));
    }
}
