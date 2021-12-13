package org.golde.proxy.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.golde.proxy.IPInfo;

public class CustomTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -7699899500414897041L;
	
	private List<IPInfo> ipAddresses = new ArrayList<IPInfo>();

	@Override
	public int getRowCount() {
		return ipAddresses.size();
	}

	@Override
	public int getColumnCount() {
		return 8;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
		case 1: return Long.class;
		default: return String.class;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object value = "??";
		IPInfo ip = ipAddresses.get(rowIndex);
		switch (columnIndex) {
		case 0: return ip.getProxy();
		case 1: return ip.getPing();
		case 2: return ip.getProxyType();
		case 3: return ip.getHttps();
		case 4: return ip.getCountry();
		case 5: return ip.getRegion();
		case 6: return ip.getCity();
		case 7: return ip.getTimezone();
		}

		return value;
	}

	@Override
	public String getColumnName(int column) {
		switch(column) {
		case 0: return "IP Address";
		case 1: return "Ping";
		case 2: return "Type";
		case 3: return "HTTPS";
		case 4: return "Country";
		case 5: return "Region";
		case 6: return "City";
		case 7: return "Timezone";

		default: return super.getColumnName(column);
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public IPInfo getIPInfoAtRow(int row) {
		return ipAddresses.get(row);
	}

	public void addIPInfo(IPInfo mod) {
		this.ipAddresses.add(mod);
	}

	public void addIPInfo(Collection<IPInfo> mods) {
		this.ipAddresses.addAll(mods);
	}

	public IPInfo removeIPInfo(int index) {
		IPInfo removed = this.ipAddresses.remove(index);
		return removed;
	}

	public void update() {
		this.fireTableDataChanged();
	}

}
