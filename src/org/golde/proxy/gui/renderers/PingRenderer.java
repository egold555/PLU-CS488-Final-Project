package org.golde.proxy.gui.renderers;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PingRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -1986534481754797113L;
	
	private final int MIN, MAX;
	public PingRenderer(int min, int max) {
		this.MIN = min;
		this.MAX = max;
	}

	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
        
        if(!(value instanceof Long)) {
        	return c;
        }
        
        long valueLong = (Long)value;
        
        if(valueLong < 0) {
        	c.setForeground(Color.BLUE);
        	setText("Failed to ping Google");
        	return c;
        }
        
        if(valueLong > MAX) {
        	valueLong = MAX;
        }

        float h = (float) map(valueLong, MIN, MAX, 0.3, 0);
        
        c.setForeground(Color.getHSBColor(h, 1, 0.8f));
        return c;
    }
	
	static double map(double x, double in_min, double in_max, double out_min, double out_max) {
		  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
		}
	
}
