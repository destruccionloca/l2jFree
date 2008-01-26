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
package net.sf.l2j.tools.random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.SecureRandom;

public class RandomIntGenerator
{
    private static final Log _log = LogFactory.getLog(RandomIntGenerator.class); 

    private SecureRandom _random = new SecureRandom();
    
    private static RandomIntGenerator _instance;
    
    public SecureRandom getSecureRandom()
    {
    	return _random;
    }
    
    public static final RandomIntGenerator getInstance()
    {
        if (_instance == null)
            _instance = new RandomIntGenerator();
        return _instance;
    }
    
    private RandomIntGenerator()
    {
        _log.info("RandomIntGenerator: initialized");
    }
}