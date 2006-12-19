package net.sf.l2j.log4jextension;

import java.util.List;

import javolution.lang.TextBuilder;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Overload of PatternLayout class to handle throwable
 */
public class ExtendedPatternLayout extends PatternLayout
{
    /**
     * Overload format to handle List use.
     * When a list is given to format, we print each member of the list and
     * replace the list by the constructed string.
     * 
     * @see org.apache.log4j.PatternLayout#format(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    public String format(LoggingEvent arg0)
    {
        Object msg  = arg0.getMessage();
        
        if ( msg instanceof List)
        {
            TextBuilder output = new TextBuilder();
            
            List params = (List)  msg;
            
            for (Object p : params)
            {
                if (p == null) continue;
                output.append(',');
                output.append(' ');
                output.append(p.toString());
            }
            LoggingEvent evt = new LoggingEvent(arg0.fqnOfCategoryClass,
                                                Logger.getLogger(arg0.getLoggerName()),
                                                arg0.getLevel(),
                                                output.toString(),
                                                (arg0.getThrowableInformation() !=null ? arg0.getThrowableInformation().getThrowable() : null));
            return super.format(evt);
        }        
        return super.format(arg0);        
    }
    
    /**
     * Default Constructor
     */
    public ExtendedPatternLayout()
    {
        this(DEFAULT_CONVERSION_PATTERN);
    }

    /**
     * Conctructor with specific pattern
     * @param pattern the pattern
     */
    public ExtendedPatternLayout(String pattern)
    {
        super(pattern);
    }

    /**
     * @see org.apache.log4j.PatternLayout#createPatternParser(java.lang.String)
     */
    public PatternParser createPatternParser(String pattern)
    {
        PatternParser result;
        if (pattern == null)
        {
            result = new ExtendedPatternParser(DEFAULT_CONVERSION_PATTERN);
        }
        else
        {
            result = new ExtendedPatternParser(pattern);
        }

        return result;
    }
    
    
    /** (non-Javadoc)
     * @see org.apache.log4j.PatternLayout#ignoresThrowable()
     * Return false, l'ExtendedPattern utilise les Throwables !
     */
    public boolean ignoresThrowable()
    {
        return false;
    }
}