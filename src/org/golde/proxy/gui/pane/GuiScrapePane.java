package org.golde.proxy.gui.pane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.golde.proxy.gui.GuiMain;
import org.golde.proxy.gui.components.ITabBarCallable;
import org.golde.proxy.gui.components.NumberOnlyTextField;
import org.golde.proxy.scrape.ScrapeData;
import org.golde.proxy.scrape.Scraper;
import org.golde.proxy.utils.Timings;
import org.golde.proxy.utils.Utils;

import lombok.Getter;

public class GuiScrapePane extends JPanel implements ITabBarCallable {

	private static final long serialVersionUID = -4320325423727643351L;

	@Getter private JProgressBar progressBar;

	private JTextArea textAreaUrls;

	private Scraper scraper;

	NumberOnlyTextField textField;



	public GuiScrapePane(GuiMain baseGui) {
		setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setMinimumSize(new Dimension(0,0));
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(0.5);
		splitPane.setEnabled(false);

		add(splitPane, BorderLayout.CENTER);

		textAreaUrls = new JTextArea();
		textAreaUrls.setText("# Urls");
		JScrollPane scrollLeft = new JScrollPane (textAreaUrls, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		splitPane.setLeftComponent(scrollLeft);

		JTextArea textAreaProxies = new JTextArea();
		textAreaProxies.setText("# Scraped Proxies");
		textAreaProxies.setEditable(false);
		JScrollPane scrollRight = new JScrollPane (textAreaProxies, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		splitPane.setRightComponent(scrollRight);

		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		progressBar = new JProgressBar();
		progressBar.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = (double)progressBar.getValue() / progressBar.getMaximum();
				value *= 100;
				progressBar.setString(String.format("%d/%d Urls Parsed. (%,.2f%%)", progressBar.getValue(), progressBar.getMaximum(), value));
			}
		});

		JPanel panelUnderProgressBar = new JPanel();
		panel.add(panelUnderProgressBar, BorderLayout.SOUTH);

		textField = new NumberOnlyTextField();
		textField.setColumns(10);
		panelUnderProgressBar.add(textField);

		System.out.println("UI Thread: " + Thread.currentThread().getName() + " - " + Thread.currentThread().getId());

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				ScrapeData data = new ScrapeData();
				String[] urlsToCheck = textAreaUrls.getText().split("\n");
				scraper = new Scraper(urlsToCheck, new String[] {"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0"}, data);

				progressBar.setMaximum(urlsToCheck.length);

				new Thread(scraper).start();

				//javax.swing.Timer


				Timer timer = new Timer(500, null);
				timer.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("Thread: " + Thread.currentThread().getName() + " - " + Thread.currentThread().getId());

						Timings timingsGlobal = new Timings("Run - Global").start();
						Timings timingsDrainTo = new Timings("Run - DrainTo");
						Timings timingsCreateTextObj = new Timings("Run - CreateTextObj");
						Timings timingsCombineStrings = new Timings("Run - CombineStrings");
						Timings timingsSetText = new Timings("Run - SetText");
						Timings timingsSetProgressBar = new Timings("Run - SetProgressBar");

						String textIntern = "";
						if(!data.ips.isEmpty()) {
							List<String> newList = new ArrayList<String>();
							timingsDrainTo.start();
							data.ips.drainTo(newList); //more eff then a for loop
							System.err.println("  - " + timingsDrainTo.stop());

							//System.err.println("Adding " + newList.size() + " IPS to the textview...");
							timingsCreateTextObj.start();
							for(String s : newList) {
								textIntern += "\n" + s;
							}
							System.err.println("  - " + timingsCreateTextObj.stop());

							timingsCombineStrings.start();
							String temp2 = textAreaProxies.getText() + textIntern;
							System.err.println("  - " + timingsCombineStrings.stop());

							timingsSetText.start();
							textAreaProxies.setText(temp2);
							System.err.println("  - " + timingsSetText.stop());
						}

						if(data.areWeFinishedYet.get()) {
							timer.stop();
							btnStart.setEnabled(true);
						}

						timingsSetProgressBar.start();
						int newProgress = data.urlsChecked.get();

						if(newProgress > progressBar.getValue()) {
							progressBar.setValue(newProgress);
							System.err.println("  - " + timingsSetProgressBar.stop());
						}

						System.err.println(timingsGlobal.stop());
						System.out.println();
					}
				});
				timer.setRepeats(true);
				timer.start();

				
				btnStart.setEnabled(false);
				
			}
		});
		panelUnderProgressBar.add(btnStart);

		progressBar.setStringPainted(true);
		progressBar.setMaximum(1);
		panel.add(progressBar);
	}

	@Override
	public void importProxies() {
		System.out.println("Scrape: Import");
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/res"));
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.addChoosableFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "Text file (*.txt)";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
			}
		});

		int returnValue = jfc.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = jfc.getSelectedFile();

			String[] lines = Utils.readFile(selectedFile);
			for(String line : lines) {
				textAreaUrls.setText(textAreaUrls.getText() + "\n" + line);
			}

			progressBar.setMaximum(lines.length);

			System.out.println(selectedFile.getAbsolutePath());
		}
	}
}
