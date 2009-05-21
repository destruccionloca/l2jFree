/*
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
package net.sf.l2j.gameserver.script;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;



/**
 * @author Luis Arias
 */
public class ScriptEngine
{
    private static Map<String, ParserFactory> parserFactories = new HashMap<String, ParserFactory>();

    protected static Parser createParser(String name)
        throws ParserNotCreatedException
    {
        ParserFactory s = parserFactories.get(name);
        if(s == null) // shape not found
        {
            try
            {
                Class.forName("net.sf.l2j.gameserver.script."+name);
                // By now the static block with no function would
                // have been executed if the shape was found.
                // the shape is expected to have put its factory
                // in the hashtable.

                s = parserFactories.get(name);
                if(s == null) // if the shape factory is not there even now
                {
                    throw (new ParserNotCreatedException());
                }
            }
            catch(ClassNotFoundException e)
            {
                // We'll throw an exception to indicate that
                // the shape could not be created
                throw(new ParserNotCreatedException());
            }
        }
        return(s.create());
    }

    public static Map<String, ParserFactory> getParserFactories()
    {
        return parserFactories;
    }

    public static void setParserFactories(Hashtable<String, ParserFactory> parserFactories)
    {
        ScriptEngine.parserFactories = parserFactories;
    }
}
