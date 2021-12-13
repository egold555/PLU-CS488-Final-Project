package org.golde.proxy.gui.compare;

import java.util.Comparator;

public class PingCompare implements Comparator<Long>{

	@Override
	public int compare(Long o1, Long o2) {
		if(o1 == -1) {
			o1 = Long.MAX_VALUE;
		}
		
		if(o2 == -1) {
			o2 = Long.MAX_VALUE;
		}
		
		return Long.compare(o1, o2);
	}

}
