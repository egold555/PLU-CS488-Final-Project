package org.golde.proxy.test.using;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.golde.proxy.utils.Utils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SubTesterAsync implements Runnable {

	final String[] proxies;
	final String[] agents;
	final int threads;
	final int timeout;
	final SubTesterData data;
	
	@Override
	public void run() {

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);

		System.out.println("Starting check with " + proxies.length + " proxies, " + agents.length + " agents, " + threads + " threads, " + timeout + " http timeout.");

		for(String proxyStr : proxies) {

			String[] split = proxyStr.split(":");
			String ip = split[0];
			int port = 80;
			if(split.length == 2) {
				try {
					port = Integer.parseInt(split[1]);
				}
				catch(NumberFormatException unused) {

				}
			}

			//Weirdly servers are running on invalid ports? What do we do?
			if(port > 65536) {
				//System.err.println("Invalid IP: " + ip + ":" + port);
				continue;
			}
			Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(ip, port));
			String userAgent = Utils.randomString(agents);
			executor.execute(new SubTesterInner(proxy, userAgent, proxyStr, timeout, data));
		}

		long start = System.currentTimeMillis();

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				printDebug(start);
			}
		}, 0, 1000);

		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.err.println("Failed to awaitTermination in SubTesterAsync");
			e.printStackTrace();
		}

		timer.cancel();
		
		printDebug(start);
		
		
		long stop = System.currentTimeMillis();

		System.out.println("Finished! It took: " + (stop - start) + "ms.");
		data.areWeFinishedYet.getAndSet(true);
	}
	
	private void printDebug(long start) {
		final int alive = data.aliveProxiesCount.get();
		final int dead = data.deadProxiesCount.get();
		final int total = alive + dead;
		
		System.out.println("Checked " + total + "/" + proxies.length + ". Alive: " + alive + ". Dead: " + dead + ". Time: " + (System.currentTimeMillis() - start));	
	}

}
