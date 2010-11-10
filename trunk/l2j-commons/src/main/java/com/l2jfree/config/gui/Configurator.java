package com.l2jfree.config.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import org.apache.commons.io.IOUtils;

import com.l2jfree.config.model.ConfigClassInfo;
import com.l2jfree.config.model.ConfigFieldInfo;
import com.l2jfree.config.model.ConfigClassInfo.PrintMode;

public final class Configurator extends JFrame
{
	private static final long serialVersionUID = -265717665729033569L;
	
	static
	{
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		}
		catch (Exception e)
		{
			// old JRE
		}
	}
	
	private final ConfigClassInfo _configClassInfo;
	
	public Configurator(ConfigClassInfo configClassInfo)
	{
		_configClassInfo = configClassInfo;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		{
			final JMenuBar jMenuBar = new JMenuBar();
			
			{
				jMenuBar.add(getLoadJButton());
			}
			{
				jMenuBar.add(getRefreshJButton());
			}
			{
				jMenuBar.add(Box.createHorizontalGlue());
			}
			{
				jMenuBar.add(getSaveJButton());
			}
			{
				jMenuBar.add(getStoreJButton());
			}
			
			setJMenuBar(jMenuBar);
		}
		
		{
			final JPanel jPanel = new JPanel();
			
			jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
			
			{
				for (ConfigFieldInfo info : _configClassInfo.getConfigFieldInfos())
				{
					final ConfigFieldInfoView view = new ConfigFieldInfoView(info);
					
					// TODO
					
					jPanel.add(view);
				}
			}
			
			add(new JScrollPane(jPanel));
		}
		
		pack();
		setSize(600, 400);
		setLocationByPlatform(true);
		setVisible(true);
	}
	
	private JButton _loadJButton;
	
	private JButton getLoadJButton()
	{
		if (_loadJButton == null)
		{
			final JButton jButton = new JButton("Load from file");
			
			jButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					// TODO
				}
			});
			
			_loadJButton = jButton;
		}
		
		return _loadJButton;
	}
	
	private JButton _refreshJButton;
	
	private JButton getRefreshJButton()
	{
		if (_refreshJButton == null)
		{
			final JButton jButton = new JButton("Refresh from config class");
			
			jButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					// TODO
				}
			});
			
			_refreshJButton = jButton;
		}
		
		return _refreshJButton;
	}
	
	private JButton _saveJButton;
	
	private JButton getSaveJButton()
	{
		if (_saveJButton == null)
		{
			final JButton jButton = new JButton("Save to config class");
			
			jButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					// TODO
				}
			});
			
			_saveJButton = jButton;
		}
		
		return _saveJButton;
	}
	
	private JButton _storeJButton;
	
	private JButton getStoreJButton()
	{
		if (_storeJButton == null)
		{
			final JButton jButton = new JButton("Store to file");
			
			jButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					// TODO
				}
			});
			
			_storeJButton = jButton;
		}
		
		return _storeJButton;
	}
	
	private static final class ConfigFieldInfoView extends JPanel
	{
		private static final long serialVersionUID = -3349651508350981499L;
		
		private final ConfigFieldInfo _info;
		
		public ConfigFieldInfoView(ConfigFieldInfo info)
		{
			_info = info;
			
			setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			setLayout(new BorderLayout());
			
			// TODO: just placeholder currently
			final JTextPane jTextPane = new JTextPane();
			
			{
				StringWriter sw = null;
				PrintWriter pw = null;
				try
				{
					sw = new StringWriter();
					pw = new PrintWriter(sw);
					
					_info.print(pw, PrintMode.FULL);
					
					jTextPane.setText(sw.getBuffer().toString());
				}
				finally
				{
					IOUtils.closeQuietly(pw);
					IOUtils.closeQuietly(sw);
				}
			}
			
			add(jTextPane, BorderLayout.CENTER);
		}
	}
}
