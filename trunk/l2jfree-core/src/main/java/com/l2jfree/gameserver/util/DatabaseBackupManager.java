/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;

/**
 * Database backup utility, directly dependent on mysqldump tool.
 * @author hex1r0
 * @author savormix
 */
public final class DatabaseBackupManager
{
	private static final Log _log = LogFactory.getLog(DatabaseBackupManager.class);
	private static final int BUFFER_SIZE = 5 * 1024 * 1024;
	
	private ByteBuffer _buf;
	
	private DatabaseBackupManager()
	{
		_log.info("DatabaseBackupManager: initialized.");
	}
	
	public void makeBackup()
	{
		File f = new File(Config.DATAPACK_ROOT, Config.DATABASE_BACKUP_SAVE_PATH);
		if (!f.mkdirs() && !f.exists())
		{
			_log.warn("Could not create folder " + f.getAbsolutePath());
			return;
		}
		
		_log.info("DatabaseBackupManager: backing up `" + Config.DATABASE_BACKUP_DATABASE_NAME + "`...");
		
		Process run = null;
		try
		{
			run = Runtime.getRuntime().exec("mysqldump" +
						" --user=" + Config.DATABASE_LOGIN +
						" --password=" + Config.DATABASE_PASSWORD +
						" --compact --complete-insert --default-character-set=utf8 --extended-insert --lock-tables --quick --skip-triggers " +
						Config.DATABASE_BACKUP_DATABASE_NAME, null, new File(Config.DATABASE_BACKUP_MYSQLDUMP_PATH));
		}
		catch (Exception e)
		{
		}
		finally
		{
			if (run == null)
			{
				_log.warn("Could not execute mysqldump!");
				return;
			}
		}
		
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			Date time = new Date();
			
			File bf = new File(f, sdf.format(time) + (Config.DATABASE_BACKUP_COMPRESSION ? ".zip" : ".sql"));
			if (!bf.createNewFile())
				throw new IOException("Cannot create backup file: " + bf.getCanonicalPath());
			_buf = ByteBuffer.allocateDirect(BUFFER_SIZE);
			ReadableByteChannel input = Channels.newChannel(new BufferedInputStream(run.getInputStream()));
			FileOutputStream out = new FileOutputStream(bf);
			WritableByteChannel dest;
			if (Config.DATABASE_BACKUP_COMPRESSION)
			{
				ZipOutputStream dflt = new ZipOutputStream(out);
				dflt.setMethod(ZipOutputStream.DEFLATED);
				dflt.setLevel(Deflater.BEST_COMPRESSION);
				dflt.setComment("L2JFree Schema Backup Utility\r\n\r\nBackup date: " +
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS z").format(time));
				dflt.putNextEntry(new ZipEntry(Config.DATABASE_BACKUP_DATABASE_NAME + ".sql"));
				dest = Channels.newChannel(dflt);
			}
			else
				dest = out.getChannel();
			int read, written;
			while ((read = input.read(_buf)) != -1)
			{
				_buf.flip();
				for (written = 0; written < read;)
					written += dest.write(_buf); // avoid empty statement
				_buf.rewind();
			}
			input.close();
			dest.close();
			run.waitFor();
			
			_log.info("DatabaseBackupManager: Schema `" + Config.DATABASE_BACKUP_DATABASE_NAME +
					"` backed up successfully in " + (System.currentTimeMillis() - time.getTime()) / 1000 + " s.");
		}
		catch (Exception e)
		{
			_log.warn("DatabaseBackupManager: Could not make backup: ", e);
		}
		finally
		{
			_buf = null;
		}
	}
	
	public static DatabaseBackupManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static final class SingletonHolder
	{
		public static final DatabaseBackupManager _instance = new DatabaseBackupManager();
	}
}
