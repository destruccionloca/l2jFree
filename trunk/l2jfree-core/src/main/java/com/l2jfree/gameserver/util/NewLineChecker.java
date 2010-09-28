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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class NewLineChecker
{
	public static void main(String[] args) throws IOException
	{
		parse(new File("../"));
		
		System.out.println("R: " + R);
		System.out.println("N: " + N);
		System.out.println("RN: " + RN);
		System.out.println("MIXED: " + MIXED);
	}
	
	private static final FileFilter FILTER = new FileFilter() {
		@Override
		public boolean accept(File f)
		{
			// to skip svn files
			if (f.isHidden())
				return false;
			
			return f.isDirectory() || !f.getName().endsWith(".zip") && !f.getName().endsWith(".class");
		}
	};
	
	private static void parse(File f) throws IOException
	{
		if (f.isDirectory())
		{
			for (File f2 : f.listFiles(FILTER))
				parse(f2);
			return;
		}
		
		final String input = FileUtils.readFileToString(f);
		
		final int r = StringUtils.countMatches(input, "\r");
		final int n = StringUtils.countMatches(input, "\n");
		final int rn = StringUtils.countMatches(input, "\r\n");
		
		// fully "\r\n"
		if (r == n && r == rn && n == rn)
		{
			RN++;
			return;
		}
		
		// fully "\n"
		if (r == 0 && rn == 0)
		{
			N++;
			return;
		}
		
		System.out.println(f.getCanonicalPath());
		System.out.println("r: " + r);
		System.out.println("n: " + n);
		System.out.println("rn: " + rn);
		
		final String input2 = input.replace("\r\n", "\n").replace("\r", "\n").replace("\n",
				f.getName().endsWith(".sh") ? "\n" : "\r\n");
		
		FileUtils.writeStringToFile(f, input2);
		
		// fully "\r"
		if (n == 0 && rn == 0)
		{
			R++;
			return;
		}
		
		// mixed
		MIXED++;
	}
	
	private static int RN;
	private static int R;
	private static int N;
	private static int MIXED;
}
