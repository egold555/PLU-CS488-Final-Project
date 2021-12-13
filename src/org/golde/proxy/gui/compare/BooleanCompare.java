package org.golde.proxy.gui.compare;

import java.util.Comparator;

public class BooleanCompare implements Comparator<Boolean>{

	@Override
	public int compare(Boolean o1, Boolean o2) {
		return Boolean.compare(o1, o2);
	}

}