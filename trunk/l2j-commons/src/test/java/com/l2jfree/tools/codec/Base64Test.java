/**
 * Added copyright notice
 *
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfree.tools.codec;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

/**
 * Class for Base64 testing
 * 
 */
public class Base64Test extends TestCase {

	/**
	 * Test that decode an encoded string give the string in entry
	 */
	public final void testDecodeString() {
		String entrance = "This Server is running L2JFree";

		String entranceEncoded = Base64.encodeBytes(entrance.getBytes());

		String result = null;

		try {
			result = new String(Base64.decode(entranceEncoded), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			fail(e.getMessage());
		}

		assertEquals(entrance, result);
	}

	/**
	 * Test that decode an encoded object give the same object
	 */
	public final void testDecodeObject() {
		String entrance = "This Server is running L2JFree";

		String entranceEncoded = Base64.encodeObject(entrance);

		String result = null;

		result = (String) Base64.decodeToObject(entranceEncoded);

		assertEquals(entrance, result);
	}

}
