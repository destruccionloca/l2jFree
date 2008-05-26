/**
 * 
 */
package net.sf.l2j.gameserver.util;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.l2j.Config;

/**
 * @author Noctarius
 *
 */
public final class PathCreator
{
    private static final Log _log = LogFactory.getLog(PathCreator.class); 

    public PathCreator()
	{
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		new File(Config.DATAPACK_ROOT, "data/pathnode").mkdirs();
		_log.info("Preparations: done");
	}
}
