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
import java.io.InputStream;
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
	
	private int BUFFER = 10485760;
	
	public static DatabaseBackupManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public DatabaseBackupManager()
	{
		_log.info("DatabaseBackupManager: initialization...");
	}
	
	public void makeBackup()
	{
		new File(Config.DATAPACK_ROOT.getAbsolutePath() + Config.DATABASE_BACKUP_SAVE_PATH).mkdirs();
		try
		{
			_log.info("DatabaseBackupManager: dumping `" + Config.DATABASE_BACKUP_DATABASE_NAME + "` ...");

			Process run = Runtime.getRuntime().exec(
							"mysqldump" +
							" --user=" + Config.DATABASE_BACKUP_USER + 
							" --password=" + Config.DATABASE_BACKUP_PASSWORD +
							" --compact --complete-insert --extended-insert --skip-comments --skip-triggers " +
							Config.DATABASE_BACKUP_DATABASE_NAME);
			InputStream in = run.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(run.getInputStream()));
			
			// Read all data
			StringBuffer temp = new StringBuffer();
			int count;
			char[] cbuf = new char[BUFFER];
			while ((count = reader.read(cbuf, 0, BUFFER)) != -1)
				temp.append(cbuf, 0, count);
			
			reader.close();
			in.close();
			
			byte[] data = temp.toString().getBytes();
			
			// Generate backup file name
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
			String fileName = Config.DATAPACK_ROOT.getAbsolutePath() + Config.DATABASE_BACKUP_SAVE_PATH
					+ dateFormat.format(new Date(System.currentTimeMillis())) + (Config.DATABASE_BACKUP_MAKE_ZIP ? ".zip" : ".sql");
			
			File destinationFile = new File(fileName);
			FileOutputStream destinationFileStream = new FileOutputStream(destinationFile);
			ZipOutputStream zipStream = null;
			
			if (Config.DATABASE_BACKUP_MAKE_ZIP)
			{
				zipStream = new ZipOutputStream(new BufferedOutputStream(destinationFileStream));
				zipStream.setMethod(ZipOutputStream.DEFLATED);
				zipStream.setLevel(Deflater.BEST_COMPRESSION);
			}
			
			_log.info("DatabaseBackupManager: saving dump to [ " + fileName + " ] ...");
			
			if (Config.DATABASE_BACKUP_MAKE_ZIP)
			{
				zipStream.putNextEntry(new ZipEntry(Config.DATABASE_BACKUP_DATABASE_NAME + ".sql"));
				zipStream.write(data);
				zipStream.close();
			}
			else
			{
				destinationFileStream.write(data);
				destinationFileStream.close();
			}
			
			_log.info("DatabaseBackupManager: Done.");
			_log.info("DatabaseBackupManager: File size:		" + Float.valueOf((float) data.length / 1024 / 1024) + " 		MB.");
			_log.info("DatabaseBackupManager: Packed size:		" + Float.valueOf((float) (new File(fileName).length()) / 1024 / 1024) + "		MB.");
		}
		catch (Exception e)
		{
			_log.error("DatabaseBackupManager: Could not make backup: ", e);
		}
	}
	
	private static final class SingletonHolder
	{
		public static final DatabaseBackupManager INSTANCE = new DatabaseBackupManager();
	}
}
