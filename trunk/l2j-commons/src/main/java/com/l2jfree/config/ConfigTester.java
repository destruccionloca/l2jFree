package com.l2jfree.config;

import com.l2jfree.config.annotation.ConfigClass;
import com.l2jfree.config.annotation.ConfigField;
import com.l2jfree.config.annotation.ConfigGroupBeginning;
import com.l2jfree.config.annotation.ConfigGroupEnding;
import com.l2jfree.config.model.ConfigClassInfo;

@ConfigClass(fileName = "config/test.properties")
public class ConfigTester
{
	@ConfigGroupBeginning(name = "OUTER GROUP", comment = "group start comment")
	@ConfigField(name = "configValue1", value = "default1")
	public static String TEST1;
	
	@ConfigGroupBeginning(name = "MIXED GROUP", comment = { "line1", "line2", "line3" })
	@ConfigField(name = "configValue2", value = "default2")
	public static String TEST2;
	
	@ConfigGroupBeginning(name = "INNER GROUP", comment = "group start comment")
	@ConfigField(name = "configValue3", value = "default3")
	@ConfigGroupEnding(name = "INNER GROUP", comment = "group end comment")
	public static String TEST3;
	
	@ConfigField(name = "configValue4", value = "default4")
	@ConfigGroupEnding(name = "OUTER GROUP", comment = "group end comment")
	public static String TEST4;
	
	@ConfigField(name = "configValue5", value = "default5")
	public static String TEST5;
	
	@ConfigField(name = "configValue6", value = "default6")
	public static String TEST6;
	
	@ConfigField(name = "configValue7", value = "default7")
	public static String TEST7;
	
	@ConfigField(name = "configValue8", value = "default8")
	@ConfigGroupEnding(name = "MIXED GROUP")
	public static String TEST8;
	
	@ConfigField(name = "configValue9", value = "default9", comment = { "This", "is", "a", "multi-line", "comment!" })
	public static String TEST9;
	
	@ConfigField(name = "doubleTest", value = "0.12,1.34")
	public static double[] DOUBLE_ARRAY;
	
	public static void main(String[] args) throws Exception
	{
		ConfigTester.TEST1 = "default11";
		ConfigTester.TEST2 = "default21";
		ConfigTester.TEST3 = "default31";
		ConfigTester.TEST4 = "default41";
		ConfigTester.TEST5 = "default51";
		ConfigTester.TEST6 = "default61";
		ConfigTester.TEST7 = "default71";
		ConfigTester.TEST8 = "default81";
		ConfigTester.TEST9 = "default91";
		
		ConfigClassInfo ci = new ConfigClassInfo(ConfigTester.class);
		
		ci.load();
		ci.print(System.out);
		ci.store();
	}
}
