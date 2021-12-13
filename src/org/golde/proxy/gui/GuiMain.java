package org.golde.proxy.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.golde.proxy.IPInfo;
import org.golde.proxy.gui.components.ITabBarCallable;
import org.golde.proxy.gui.pane.GuiCheckPane;
import org.golde.proxy.gui.pane.GuiScrapePane;

import com.formdev.flatlaf.IntelliJTheme;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import lombok.Getter;

public class GuiMain extends JFrame {

	private static final long serialVersionUID = -5884958144103422145L;

	private GuiCheckPane checkPane;
	private GuiScrapePane scrapePane;
	
	@Getter private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public GuiMain() {
		super("Eric Golde - CS488 Final Project - Proxy Scraper / Checker");
		getContentPane().setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		checkPane = new GuiCheckPane(this);
		scrapePane = new GuiScrapePane(this);
		
		tabbedPane.add("Checker", checkPane);
		tabbedPane.add("Scraper", scrapePane);
		
		
		JMenuBar menubar = new JMenuBar();
		
		JMenu menuFile = new JMenu("File");
		
		JMenuItem menuFileImport = new JMenuItem("Import Proxies");
		JMenuItem menuFileExport = new JMenuItem("Export Checked Proxies");
		
		menuFileImport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((ITabBarCallable) tabbedPane.getSelectedComponent()).importProxies();
			}
		});
		
		menuFileExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((ITabBarCallable) tabbedPane.getSelectedComponent()).exportProxies();
			}
		});
		
		menuFile.add(menuFileImport);
		menuFile.add(menuFileExport);
		
		menubar.add(menuFile);
		
		setJMenuBar(menubar);
		
		tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println("Tab changed: " + tabbedPane.getSelectedIndex());
				
				if(tabbedPane.getSelectedComponent() instanceof GuiCheckPane) {
					menuFileImport.setText("Import Proxies");
					menuFileExport.setText("Export Checked Proxies");
				}
				else if(tabbedPane.getSelectedComponent() instanceof GuiScrapePane) {
					menuFileImport.setText("Import URLS");
					menuFileExport.setText("Export Scraped Proxies");
				}
			}
		});
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setSize(400, 300);
		setVisible(true);
	}
	

	public void ShowInfoMessage(String message) {
		JOptionPane.showMessageDialog(this,
			    message,
			    "Success",
			    JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void showWarningMessage(String message) {
		JOptionPane.showMessageDialog(this,
			    message,
			    "Warning",
			    JOptionPane.ERROR_MESSAGE);
	}
	
	public void showErrorMessage(String message) {
		JOptionPane.showMessageDialog(this,
			    message,
			    "Oh shit :(",
			    JOptionPane.ERROR_MESSAGE);
	}

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		
		IntelliJTheme.setup(new FileInputStream(new File("res/themes/light.json")));
		
		GuiMain frame = new GuiMain();

		

//		frame.checkPane.getProgressBar().setValue(347);
//
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//
//		IPInfo[] data = gson.fromJson(new FileReader(new File("res/temp-parsed-data.json")), new IPInfo[0].getClass());
//		
//		for(IPInfo info : data) {;
//			frame.checkPane.getTableData().addIPInfo(info);
//		}
//
//
//		frame.checkPane.getTableData().update();

	}
}
