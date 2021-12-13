package org.golde.proxy.test.auto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.golde.proxy.IPInfo;
import org.golde.proxy.utils.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class SubTester {

	private static final int PING_ERROR_HTTPS = -600;
	private static final int PING_ERROR_OTHER = -601;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	int TIMEOUT;
	int THREADS;
	final List<IPInfo> results = Collections.synchronizedList(new ArrayList<IPInfo>());


	AtomicInteger checkedCountTotal = new AtomicInteger();

	public long startTest(final String[] proxies, final String[] agents, final int threads, final int timeout) throws InterruptedException {

		THREADS = threads;
		TIMEOUT = timeout;

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREADS);

		System.out.println("Starting check with " + proxies.length + " proxies, " + agents.length + " agents, " + THREADS + " threads, " + TIMEOUT + " http timeout.");

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
			executor.execute(new SubTesterInner(proxyStr, proxy, userAgent));
		}

		long start = System.currentTimeMillis();

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				System.out.println("Checked " + checkedCountTotal.get() + "/" + proxies.length + ". Alive: " + results.size() + ". ET: " + (System.currentTimeMillis() - start) + " AT: " + executor.getActiveCount());
			}
		}, 0, 1000);

		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

		timer.cancel();
		System.out.println("Checked " + checkedCountTotal.get() + "/" + proxies.length + ". Alive: " + results.size() + ". ET: " + (System.currentTimeMillis() - start) + " AT: " + executor.getActiveCount());

		long stop = System.currentTimeMillis();

		String outputFile = "checked-http-" + threads + "-" + timeout + "-" + System.currentTimeMillis() + ".json";

		System.out.println("Finished! It took: " + (stop - start) + "ms.");
		System.out.println("Saved to output file: " + outputFile);

		String data = GSON.toJson(results);
		Utils.writeFile(outputFile, data);

		return (stop - start);
	}

	class SubTesterInner implements Runnable {

		private final Proxy proxy;
		private final String userAgent;
		private final String proxyStringRaw;
		private SubTesterInner(String proxyStringRaw, Proxy proxy, String userAgent) {
			this.proxy = proxy;
			this.userAgent = userAgent;
			this.proxyStringRaw = proxyStringRaw;
		}

		@Override
		public void run() {
			IPInfo result = this.doTest(proxyStringRaw, proxy, userAgent, TIMEOUT);
			checkedCountTotal.incrementAndGet();
			if(result != null) {
				results.add(result);
			}
		}

		IPInfo doTest(String proxyStringRaw, Proxy proxy, String userAgent, int timeout) {
			Boolean https = true;
			long ping = getPingToGoogle(proxy, userAgent, timeout, true);
			if(ping == PING_ERROR_HTTPS) {
				https = false;
				ping = getPingToGoogle(proxy, userAgent, timeout, false);
			}
			else if(ping == PING_ERROR_OTHER) {
				ping = -1;
				https = null;
			}

			IPInfo info = getIPInfo(proxy, userAgent, timeout);
			if(info == null) {
				return null;
			}
			info.setPing(ping);
			info.setHttps(https);
			info.setProxy(proxyStringRaw);

			return info;
		}

		IPInfo getIPInfo(Proxy proxy, String userAgent, int timeout) {
			try {
				URL weburl = new URL("http://ipinfo.io/json");

				HttpURLConnection conn = (HttpURLConnection) weburl.openConnection(proxy);
				conn.setDefaultUseCaches(false);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("User-Agent", userAgent);
				conn.setRequestProperty("charset", "utf-8");
				conn.setUseCaches(false);
				conn.setConnectTimeout(timeout);
				conn.setReadTimeout(timeout);
				conn.connect();
				int status = conn.getResponseCode();

				if (100 <= status && status <= 399) {

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024];
					int length = 0;
					while ((length = conn.getInputStream().read(buffer)) != -1) {
						baos.write(buffer, 0, length);
					}

					String jsonRaw = baos.toString("utf-8");

					try {
						return GSON.fromJson(jsonRaw, IPInfo.class);

					}
					catch(JsonSyntaxException e) {
						return null;
					}


				} 
				else {
					return null;
				}
			}
			catch(IOException e) {
				return null;
			}

		}

		long getPingToGoogle(Proxy proxy, String userAgent, int timeout, boolean https) {

			final String url = "http" + (https ? "s" : "") + "://google.com/404";

			try {
				URL weburl = new URL(url);

				HttpURLConnection conn = (HttpURLConnection) weburl.openConnection(proxy);
				conn.setDefaultUseCaches(false);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("User-Agent", userAgent);
				conn.setRequestProperty("charset", "utf-8");
				conn.setUseCaches(false);
				conn.setConnectTimeout(timeout);
				conn.setReadTimeout(timeout);
				long start = System.currentTimeMillis();
				conn.connect();
				long end = System.currentTimeMillis();
				long ms = end - start;

				return ms;

			}
			catch(IOException e) {
				if(e.getMessage().startsWith("Unable to tunnel through proxy.")) {
					return PING_ERROR_HTTPS;
				}
				return PING_ERROR_OTHER;
			}
		}

	}

}
