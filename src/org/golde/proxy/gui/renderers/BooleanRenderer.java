package org.golde.proxy.gui.renderers;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class BooleanRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 7883969232115499404L;

	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
        
        Boolean boolVal = null;
        if(value == null) {
        	boolVal = null;
        }
        else if(value instanceof String) {
        	boolVal = Boolean.parseBoolean((String)value);
        }
        else if(value instanceof Boolean) {
        	boolVal = (Boolean)value;
        }
        
        
        if(boolVal == null) {
        	c.setForeground(Color.BLUE);
        	setText("N/A");
        	return c;
        }

        c.setForeground(boolVal ? new Color(0, 128, 0) : Color.RED);
        return c;
    }
	
}
