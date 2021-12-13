package org.golde.proxy.test.auto;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class DataPoint {

	private final long elapsedTime;
	private final int timeout;
	private final int threads;
	
}
