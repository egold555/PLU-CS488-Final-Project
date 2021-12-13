package org.golde.proxy.gui.pane;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.golde.proxy.IPInfo;
import org.golde.proxy.gui.CustomTableModel;
import org.golde.proxy.gui.GuiMain;
import org.golde.proxy.gui.compare.BooleanCompare;
import org.golde.proxy.gui.compare.PingCompare;
import org.golde.proxy.gui.components.ITabBarCallable;
import org.golde.proxy.gui.components.NumberOnlyTextField;
import org.golde.proxy.gui.renderers.BooleanRenderer;
import org.golde.proxy.gui.renderers.CountryRenderer;
import org.golde.proxy.gui.renderers.PingRenderer;
import org.golde.proxy.test.using.SubTesterAsync;
import org.golde.proxy.test.using.SubTesterData;
import org.golde.proxy.utils.Utils;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import javax.swing.SwingConstants;

public class GuiCheckPane extends JPanel implements ITabBarCallable {

	private static final long serialVersionUID = 7533666442561210340L;

	private JProgressBar progressBar;
	private CustomTableModel tableData;

	private final GuiMain baseGui;
	private JButton btnStart;

	private String[] proxiesToCheck;
	
	private NumberOnlyTextField notfThreads;
	private NumberOnlyTextField notfTimeout;

	public GuiCheckPane(GuiMain baseGui) {
		this.baseGui = baseGui;
		setLayout(new BorderLayout(0, 0));

		tableData = new CustomTableModel();

		final JTable table = new JTable(tableData)
		{

			private static final long serialVersionUID = 6677120485681773425L;

			public boolean getScrollableTracksViewportWidth()
			{
				return getPreferredSize().width < getParent().getWidth();
			}

			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				switch(column) {
				case 1: return new PingRenderer(0, 5000);
				case 3: return new BooleanRenderer();
				case 4: return new CountryRenderer();
				default: return super.getCellRenderer(row, column);
				}
			}

		};
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

		TableRowSorter<CustomTableModel> rowSorter = new TableRowSorter<CustomTableModel>(tableData);
		table.setRowSorter(rowSorter);

		rowSorter.setComparator(1, new PingCompare());
		rowSorter.setComparator(3, new BooleanCompare());

		table.getTableHeader().setResizingAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);

		table.getRowSorter().toggleSortOrder(1);

		final JScrollPane scrollPane = new JScrollPane( table );
		add( scrollPane );

		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		progressBar = new JProgressBar();
//		progressBar.addChangeListener(new ChangeListener() {
//			@Override
//			public void stateChanged(ChangeEvent e) {
//				double value = (double)progressBar.getValue() / progressBar.getMaximum();
//				value *= 100;
//				progressBar.setString(String.format("%d/%d Scanned. (%,.2f%%)", progressBar.getValue(), progressBar.getMaximum(), value));
//			}
//		});

		JPanel panelUnderProgressBar = new JPanel();
		panelUnderProgressBar.setLayout(new BorderLayout(0, 0));
		panel.add(panelUnderProgressBar, BorderLayout.SOUTH);
		
		
		
		JPanel panelUnderProgressBar2 = new JPanel();
		panelUnderProgressBar2.setLayout(new BorderLayout(0, 0));
		panelUnderProgressBar.add(panelUnderProgressBar2, BorderLayout.NORTH);
		
		notfThreads = new NumberOnlyTextField();
		notfThreads.setMaxLength(5);
		notfThreads.setValue(512);
		panelUnderProgressBar2.add(notfThreads, BorderLayout.CENTER);
		
		JLabel lblNumberOfThreads = new JLabel("Number of Threads: ");
		panelUnderProgressBar2.add(lblNumberOfThreads, BorderLayout.WEST);
		
		JPanel panelUnderProgressBar3 = new JPanel();
		panelUnderProgressBar3.setLayout(new BorderLayout(0, 0));
		panelUnderProgressBar2.add(panelUnderProgressBar3, BorderLayout.NORTH);
		
		notfTimeout = new NumberOnlyTextField();
		notfTimeout.setMaxLength(5);
		notfTimeout.setValue(10000);
		panelUnderProgressBar3.add(notfTimeout, BorderLayout.CENTER);
		
		JLabel lblThreadTimeout = new JLabel("Timeout: ");
		panelUnderProgressBar3.add(lblThreadTimeout, BorderLayout.WEST);
		
		

		

		btnStart = new JButton("Start");
		btnStart.setEnabled(false);
		btnStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				final String[] agents = new String[] {"Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-TW; rv:1.9.2.4) Gecko/20100611 Firefox/3.6.4 ( .NET CLR 3.5.30729)"};
				final int threads = notfThreads.getValue();
				final int timeout = notfTimeout.getValue();

				SubTesterData data = new SubTesterData();
				SubTesterAsync subTester = new SubTesterAsync(proxiesToCheck, agents, threads, timeout, data);

				progressBar.setMaximum(proxiesToCheck.length);
				
				new Thread(subTester, "SubTester - UI").start();

				Timer timer = new Timer(500, null);
				timer.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						
						if(!data.proxies.isEmpty()) {
							List<IPInfo> newList = new ArrayList<IPInfo>();
							data.proxies.drainTo(newList); //more eff then a for loop
							
							tableData.addIPInfo(newList);
							tableData.update();
							final int alive = data.aliveProxiesCount.get();
							final int dead = data.deadProxiesCount.get();
							final int total = alive + dead;
							
							progressBar.setValue(total);
							
							double value = (double)progressBar.getValue() / progressBar.getMaximum();
							value *= 100;
							progressBar.setString(String.format("%d/%d Scanned. | Alive: %d | Dead: %d | (%,.2f%%)", progressBar.getValue(), progressBar.getMaximum(), alive, dead, value));
							
						}

						if(data.areWeFinishedYet.get()) {
							timer.stop();
							btnStart.setEnabled(true);
						}

					}
				});
				timer.setRepeats(true);
				timer.start();
				btnStart.setEnabled(false);
				
				
			}
		});
		panelUnderProgressBar.add(btnStart, BorderLayout.CENTER);

		progressBar.setStringPainted(true);
		progressBar.setMaximum(1);
		panel.add(progressBar);

	}

	@Override
	public void importProxies() {
		System.out.println("Check: Import");

		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/res"));
		jfc.setAcceptAllFileFilterUsed(false);
		jfc.addChoosableFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "Scraped Proxies (*.txt)";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
			}
		});

		jfc.addChoosableFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "Processed Proxies (*.pproxies)";
			}

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".pproxies");
			}
		});

		int returnValue = jfc.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = jfc.getSelectedFile();

			//Already Processed Proxies
			if(selectedFile.getName().toLowerCase().endsWith(".pproxies")) {

				try {
					IPInfo[] data = baseGui.getGson().fromJson(new FileReader(selectedFile), new IPInfo[0].getClass());
					if(data == null) {
						baseGui.showErrorMessage("Failed to read file: " + selectedFile.getAbsolutePath() + " as a .pproxies file.");
						return;
					}

					for(IPInfo info : data) {;
					this.tableData.addIPInfo(info);
					}
					this.tableData.update();
					this.btnStart.setEnabled(false);

				} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
					baseGui.showErrorMessage("Failed to open file: " + selectedFile.getAbsolutePath());
					e.printStackTrace();
				}

			}
			else if(selectedFile.getName().toLowerCase().endsWith(".txt")) {

				proxiesToCheck = Utils.readFile(selectedFile);
				this.progressBar.setMaximum(proxiesToCheck.length);
				this.baseGui.ShowInfoMessage("Successfully imported " + proxiesToCheck.length + " proxies.");
				this.btnStart.setEnabled(true);

			}
			else {
				baseGui.showErrorMessage("Failed to import proxies. Invalid file type.");
			}

			System.out.println(selectedFile.getAbsolutePath());
		}
	}

}
