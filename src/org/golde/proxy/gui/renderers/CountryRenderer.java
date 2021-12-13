package org.golde.proxy.gui.renderers;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.golde.proxy.gui.StretchIcon;

public class CountryRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -2030560105126824209L;
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

		if(!(value instanceof String)) {
			return c;
		}

		String valueStr = (String) value;
		
		setIcon(new StretchIcon("res/icons/flags/" + valueStr.toLowerCase() + ".png"));
		
		
		setHorizontalTextPosition(SwingConstants.RIGHT);
        setVerticalTextPosition(SwingConstants.CENTER);
        setHorizontalAlignment(SwingConstants.LEFT);
        setVerticalAlignment(SwingConstants.CENTER);
		
		return c;
	}

	static double map(double x, double in_min, double in_max, double out_min, double out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}

}
