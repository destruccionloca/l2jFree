package net.sf.l2j.gameserver.model;

import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.l2j.tools.random.Rnd;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.Util;
/**
 * This class implements methods to get more secure process for client-server HTML commands bypass
 * 
 * @author Balancer
 *
 */
public class BypassEngine{

    private final static Log                                      _log             = LogFactory.getLog(BypassEngine.class.getName());

    //private static final String                                 DIGEST_NAME      = "SHA";
    //private static final int                                    DIGEST_LENGTH    = 20;
    private static final String                                   DIGEST_NAME      = "MD5";
    private static final int                                      DIGEST_LENGTH    = 16;

    private static final long                                     MAX_STORAGE_TIME = 20 * 60 * 1000; // 20 minutes

    private static final ConcurrentHashMap<String, BypassStorage> bypassMap        = new ConcurrentHashMap<String, BypassStorage>();
    private static final Pattern                                  p                = Pattern.compile("\"(bypass +-h +)(.+?)\"");

    private static class BypassStorage{

        public final String  _bypass;
        public final long    _time;
        public final int     _charObjId;
        public final boolean _useParams;

        public BypassStorage(L2PcInstance player, String bypass, boolean useParams)
        {
            _bypass = bypass;
            _charObjId = player.getObjectId();
            _useParams = useParams;
            _time = System.currentTimeMillis() + MAX_STORAGE_TIME;
        }

        public String getBypass()
        {
            return _bypass;
        }

        public boolean isObsolete(long currentTime)
        {
            return _time < currentTime;
        }

        public boolean isOwner(L2PcInstance player)
        {
            return player.getObjectId() == _charObjId;
        }

        public boolean isUseParams()
        {
            return _useParams;
        }
    }

    /**
     * Process HTML data before sending to client, store original bypass and replace with hash
     * @param player
     * @param html
     * @return
     */
    public static final String encode(L2PcInstance player, String html)
    {
        Matcher m = p.matcher(html);
        StringBuffer sb = new StringBuffer();
        
        while(m.find())
        {
            String bypass = m.group(2);

            try
            {
                String code = bypass;
                String params = "";
                boolean useParams = false;

                int pos = bypass.indexOf(" $");
                if(pos >= 0)
                {
                    code = bypass.substring(0, pos);
                    params = bypass.substring(pos).replace("$", "\\$");
                    useParams = true;
                }

                MessageDigest md = MessageDigest.getInstance(DIGEST_NAME);
                String hash = Util.bytesToString(md.digest(("" + Rnd.get()).getBytes("UTF-8"))).substring(0, DIGEST_LENGTH);

                bypassMap.put(hash, new BypassStorage(player, code, useParams));
                m.appendReplacement(sb, "\"bypass -h " + hash + params + "\"");

            }
            catch (Exception e)
            {
                _log.warn("BypassEngine: Encoding exception!", e);
            }
        }
        
        m.appendTail(sb);

        return sb.toString();
    }

    /**
     * Check received bypass hash from client and returns original bypass command
     * @param player
     * @param bypass - hashed bypass
     * @return
     */
    public static final String decode(L2PcInstance player, String bypass)
    {
        // initial check
        if(player == null || bypass == "" || bypass.length() == 0)
            return "";

        // exclude client-side only bypasses
        if(Util.matches(bypass, "^(bbs_|_mrsl|_clbbs|_friend|_mm|_mail|_bbs|_diary|friendlist|friendmail|manor_menu_select|menu_select).*", Pattern.DOTALL))
            return bypass;

        if(bypass.length() < DIGEST_LENGTH)
        {
            _log.warn("BypassEngine [player " + player.getName() + "]: Too short bypass '" + bypass + "'.");
            return "";
        }

        String hash = bypass.substring(0, DIGEST_LENGTH);

        if(!hash.matches("[0-9a-f]{" + DIGEST_LENGTH + "}"))
        {
            _log.warn("BypassEngine [player " + player.getName() + "]: Wrong hash '" + hash + "'.");
            return "";
        }

        BypassStorage bs = bypassMap.get(hash);

        if(bs == null)
        {
            _log.warn("BypassEngine [player " + player.getName() + "]: Unregistered hash '" + hash + "' in bypass '" + bypass + "'.");
            return "";
        }
        if(!bs.isUseParams() && !hash.equals(bypass))
        {
            _log.warn("BypassEngine [player " + player.getName() + "]: Bypass with wrong parameters '" + bypass.substring(DIGEST_LENGTH) + "' with command '" + bs.getBypass() + "'.");
            return "";
        }
        if(!bs.isOwner(player))
        {
            _log.warn("BypassEngine [player " + player.getName() + "]: Try to execute '" + bs.getBypass() + "' over stored bypass '" + bypass + "'.");
            return "";
        }
        
        String restoreBypass = bs.getBypass();

        if(bs.isUseParams())
            restoreBypass += bypass.substring(DIGEST_LENGTH);

        bypassMap.remove(hash);

        clean(player);

        return restoreBypass;
    }

    /**
     * Delete all obsolete or proceed stored bypasses
     * @param player
     */
    private static final void clean(L2PcInstance player)
    {
        long currentTime = System.currentTimeMillis();
        for(Object element : bypassMap.keySet())
        {
            String hash = (String) element;
            BypassStorage bs = bypassMap.get(hash);
            if(bs.isObsolete(currentTime) || bs.isOwner(player))
                bypassMap.remove(hash);
        }
    }
}
