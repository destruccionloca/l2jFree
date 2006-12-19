/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
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
package net.sf.l2j.util;

import java.math.BigInteger;
import java.util.Random;

/**
 * This class provide tools functions for hexa manipulation
 * 
 */
public class HexUtil
{

    public static byte[] generateHex(int size)
    {
        byte [] array = new byte[size]; 
        Random rnd = new Random();
        for(int i = 0; i < size; i++)
        {
            array[i] =  (byte)rnd.nextInt(256);                                                                                 
        }
        return array;
    }    
    
    /**
     * @param string
     * @return
     */
    public static byte[] stringToHex(String string)
    {
        return new BigInteger(string, 16).toByteArray();
    }
    
    
    public static String hexToString(byte[] hex)
    {
        if(hex == null)
            return "null";
        return new BigInteger(hex).toString(16);
    }    
    
}
