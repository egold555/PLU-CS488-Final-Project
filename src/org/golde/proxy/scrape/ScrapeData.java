package org.golde.proxy.scrape;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Holdes data for the current multi threaded scraping process
 */
public class ScrapeData {

	public AtomicInteger urlsChecked = new AtomicInteger(0);
	public BlockingQueue<String> ips = new ArrayBlockingQueue<String>(1000);
	public AtomicBoolean areWeFinishedYet = new AtomicBoolean(false);
}
