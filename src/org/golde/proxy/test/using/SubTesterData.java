package org.golde.proxy.test.using;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.golde.proxy.IPInfo;

public class SubTesterData {
	
	public AtomicInteger aliveProxiesCount = new AtomicInteger(0);
	public AtomicInteger deadProxiesCount = new AtomicInteger(0);
	public BlockingQueue<IPInfo> proxies = new ArrayBlockingQueue<IPInfo>(1000);
	public AtomicBoolean areWeFinishedYet = new AtomicBoolean(false);

}
