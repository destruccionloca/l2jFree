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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;

/**
 * @author hex1r0
 */
public final class DatabaseBackupManager
{
	private static final Log _log = LogFactory.getLog(DatabaseBackupManager.class);
	
	private static final int BUFFER = 10 * 1024 * 1024;
	
	public static DatabaseBackupManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private DatabaseBackupManager()
	{
		_log.info("DatabaseBackupManager: initialized.");
	}
	
	public void makeBackup()
	{
		try
		{
			new File(Config.DATAPACK_ROOT.getAbsolutePath() + Config.DATABASE_BACKUP_SAVE_PATH).mkdirs();
		}
		catch (SecurityException e)
		{
			_log.warn("DatabaseBackupManager: You don't have privileges to create directory for backups!");
			return;
		}
		
		_log.info("DatabaseBackupManager: dumping `" + Config.DATABASE_BACKUP_DATABASE_NAME + "` ...");
		
		Process run = null;
		try
		{
			run = Runtime.getRuntime().exec(Config.DATABASE_BACKUP_MYSQLDUMP_PATH + "/" +
						"mysqldump" +
						" --user=" + Config.DATABASE_BACKUP_USER + 
						" --password=" + Config.DATABASE_BACKUP_PASSWORD +
						" --compact --complete-insert --extended-insert --skip-comments --skip-triggers " +
						Config.DATABASE_BACKUP_DATABASE_NAME);
		}
		catch (SecurityException e)
		{
			_log.warn("DatabaseBackupManager: You dont have privileges to execute mysqldump!");
			return;
		}
		catch (IOException e)
		{
			_log.warn("DatabaseBackupManager: Can't find mysqldump!");
			return;
		}
		
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(run.getInputStream()));
			StringBuffer buffer = new StringBuffer();

			char[] cbuf = new char[BUFFER];
			int count;
			while ((count = br.read(cbuf, 0, BUFFER)) != -1)
				buffer.append(cbuf, 0, count);

			br.close();
			
			byte[] data = buffer.toString().getBytes();
			
			// Generate backup file name
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
			String fileName = Config.DATAPACK_ROOT.getAbsolutePath() + Config.DATABASE_BACKUP_SAVE_PATH
					+ dateFormat.format(new Date()) + (Config.DATABASE_BACKUP_MAKE_ZIP ? ".zip" : ".sql");
			
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
			
			_log.info("DatabaseBackupManager: saving dump to [ " + fileName + " ] ...");
			
			if (Config.DATABASE_BACKUP_MAKE_ZIP)
			{
				ZipOutputStream zipStream = new ZipOutputStream(bos);
				zipStream.setMethod(ZipOutputStream.DEFLATED);
				zipStream.setLevel(Deflater.BEST_COMPRESSION);
				zipStream.putNextEntry(new ZipEntry(Config.DATABASE_BACKUP_DATABASE_NAME + ".sql"));
				zipStream.write(data);
				zipStream.close();
			}
			else
			{
				bos.write(data);
				bos.close();
			}
			
			_log.info("DatabaseBackupManager: Done.");
			_log.info("DatabaseBackupManager: File size:		" + Double.valueOf((double) data.length / 1024 / 1024) + " 		MB.");
			_log.info("DatabaseBackupManager: Packed size:		" + Double.valueOf((double) (new File(fileName).length()) / 1024 / 1024) + "		MB.");
		}
		catch (Exception e)
		{
			_log.warn("DatabaseBackupManager: Could not make backup: ", e);
		}
	}
	
	private static final class SingletonHolder
	{
		public static final DatabaseBackupManager INSTANCE = new DatabaseBackupManager();
	}
}
