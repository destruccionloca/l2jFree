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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.cache.HtmCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TutorialShowHtml extends ServerBasePacket
{
    private final static Log _log = LogFactory.getLog(TutorialShowHtml.class.getName());
	private static final String _S__A0_TUTORIALSHOWHTML = "[S] a0 TutorialShowHtml";
	private String _html;
	
    public TutorialShowHtml()
    {
    }
    
	public TutorialShowHtml(String html)
	{
        _html = html;
	}
	
    public void setFile(String path)
    {
        _html = HtmCache.getInstance().getHtmForce(path);
    }
    
    public void replace(String pattern, String value)
    {
        _html = _html.replaceAll(pattern, value);
    }
    
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{
        // html code must not exceed 8192 bytes 
        if(_html.length() > 8192)
        {
            _log.warn("Html is too long! this will crash the client!");
            _html = "<html><body>Html was too long</body></html>";
            return;
        }
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	void writeImpl()
	{
		writeC(0xa0);
		writeS(_html);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__A0_TUTORIALSHOWHTML;
	}
	
}