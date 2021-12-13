package org.golde.proxy.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.golde.proxy.IPInfo;
import org.golde.proxy.utils.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class TestHTTPPOC implements Runnable {

	private static final int PING_ERROR_HTTPS = -600;
	private static final int PING_ERROR_OTHER = -601;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	static final int TIMEOUT = 10000;
	static final int THREADS = 500;
	static final List<IPInfo> results = Collections.synchronizedList(new ArrayList<IPInfo>());

	static AtomicInteger counter = new AtomicInteger();
	
	static int total = 0;

	public static void main(String[] args) {


		final String[] agents = Utils.readFile(new File("res/agents.txt"));
		final String[] proxies = Utils.readFile(new File("res/out-1638166774983.txt"));
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREADS);

		total = proxies.length;
		
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
			executor.execute(new TestHTTPPOC(proxyStr, proxy, userAgent));
		}


		executor.shutdown();

		String data = GSON.toJson(results);
		Utils.writeFile("checked-http-" + System.currentTimeMillis() + ".json", data);

	}

	private final Proxy proxy;
	private final String userAgent;
	private final String proxyStringRaw;
	public TestHTTPPOC(String proxyStringRaw, Proxy proxy, String userAgent) {
		this.proxy = proxy;
		this.userAgent = userAgent;
		this.proxyStringRaw = proxyStringRaw;
	}

	@Override
	public void run() {
		IPInfo result = this.doTest(proxyStringRaw, proxy, userAgent, TIMEOUT);
		int i = counter.incrementAndGet();
		System.out.println("Finished: " + i + " / " + total + " - Alive: " + results.size() + " / " + total);
		if(result != null) {
			results.add(result);
			try {
				
				String toString = GSON.toJson(results);
				Utils.writeFile("tmp/bkup-" + System.currentTimeMillis() + ".json", toString);
				
			}
			catch(ConcurrentModificationException e) {
				
			}
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
		System.out.println("Alive: " + info.toString());
		
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
					//System.err.println("GSON Failed: " + proxy.toString() + " UA: " + userAgent + " JSON: " + jsonRaw);
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
			//System.err.println(proxy.toString() + " - Failed to ping google through proxy!");
			return PING_ERROR_OTHER;
		}

	}

	long getPingToExample(Proxy proxy, String userAgent, int timeout, boolean https) {

		final String url = "http" + (https ? "s" : "") + "://example.com/";

		try {
			URL weburl = new URL(url);

			HttpURLConnection conn = (HttpURLConnection) weburl.openConnection(proxy);
			conn.setDefaultUseCaches(false);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", userAgent);
			conn.setRequestProperty("charset", "utf-8");
			conn.setUseCaches(false);
			conn.setConnectTimeout(timeout);
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
			//System.err.println(proxy.toString() + " - Failed to ping example through proxy!");
			return PING_ERROR_OTHER;
		}

	}

}
