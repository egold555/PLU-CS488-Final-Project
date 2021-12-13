package org.golde.proxy.test.using;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.golde.proxy.IPInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SubTesterInner implements Runnable {

	private static final int PING_ERROR_HTTPS = -600;
	private static final int PING_ERROR_OTHER = -601;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	private final Proxy proxy;
	private final String userAgent;
	private final String proxyStringRaw;
	private final int timeout;
	private final SubTesterData data;

	@Override
	public void run() {
		IPInfo result = this.doTest(proxyStringRaw, proxy, userAgent, timeout);
		if(result != null) {
			data.aliveProxiesCount.incrementAndGet();
			try {
				data.proxies.offer(result, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			} 
			catch (InterruptedException e) {
				System.err.println("Failed to offer proxy to data.proxies");
				e.printStackTrace();
			}
		} 
		else {
			data.deadProxiesCount.incrementAndGet();
		}
	}

	IPInfo doTest(String proxyStringRaw, Proxy proxy, String userAgent, int timeout) {
		Boolean https = true;
		long ping = getPingToGoogle(proxy, userAgent, true);
		if(ping == PING_ERROR_HTTPS) {
			https = false;
			ping = getPingToGoogle(proxy, userAgent, false);
		}
		else if(ping == PING_ERROR_OTHER) {
			ping = -1;
			https = null;
		}

		IPInfo info = getIPInfo(proxy, userAgent);
		if(info == null) {
			return null;
		}
		info.setPing(ping);
		info.setHttps(https);
		info.setProxy(proxyStringRaw);

		return info;
	}

	IPInfo getIPInfo(Proxy proxy, String userAgent) {
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

	long getPingToGoogle(Proxy proxy, String userAgent, boolean https) {

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
		catch(Exception e) {
			return PING_ERROR_OTHER;
		}
	}

}