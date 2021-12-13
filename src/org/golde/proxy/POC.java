package org.golde.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class POC {

	private static final int PING_ERROR_HTTPS = -600;
	private static final int PING_ERROR_OTHER = -601;
	
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	public static void main(String[] args) {
		
	}
	
	IPInfo getBody(Proxy proxy, String userAgent, int timeout) {
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
				
				IPInfo info = GSON.fromJson(new InputStreamReader(conn.getInputStream()), IPInfo.class);
				
				return info;
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

			HttpURLConnection conn = (HttpURLConnection) weburl.openConnection();
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
			e.printStackTrace();
			return PING_ERROR_OTHER;
		}
		
	}

}
