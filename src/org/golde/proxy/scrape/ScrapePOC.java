package org.golde.proxy.scrape;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.golde.proxy.utils.Utils;

public class ScrapePOC implements Runnable {

	Pattern pattern = Pattern.compile("\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})?");
	
	Pattern specialURLEntry = Pattern.compile("\\{(.*?)\\}");
	
	public static void main(String[] args) {
		new ScrapePOC().run();
	}

	@Override
	public void run() {
		System.out.println("start");
		
		final String[] agents = Utils.readFile(new File("res/agents.txt"));
		String[] httpUrlsRegex = Utils.readFile(new File("res/http-urls-regex.txt"));
		httpUrlsRegex = parseUrls(httpUrlsRegex);
		
		for(String s : httpUrlsRegex) {
			System.out.println(s);
		}
		
		List<String> ipsGathered = new ArrayList<String>();
		
		for(String url : httpUrlsRegex) {
			System.out.println("Pinging: " + url);
			String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0";
			String body = getHTML(url, userAgent);
			if(body != null) {
				String[] ips = getProxies(body);
				if(ips.length == 0) {
					System.err.println("  - Gathered " + ips.length + " proxies");
				}
				else {
					System.out.println("  - Gathered " + ips.length + " proxies");
				}
				
				for(String ip : ips) {
					ipsGathered.add(ip);
				}
			}
			else {
				System.err.println(url + " was null with agent: " + userAgent);
			}
			
		}
		System.out.println("Done! Writing " + ipsGathered.size() + " to file...");
		Utils.writeFile("res/out-" + System.currentTimeMillis() + ".txt", ipsGathered.toArray(new String[0]));
	}
	
	
	
	String[] parseUrls(String[] urls) {
		
		List<String> newUrls = new ArrayList<String>();
		
		for(String url : urls) {
			Matcher mat = specialURLEntry.matcher(url);
			if(mat.find()) {
				String findOrig = mat.group();
				String find = findOrig.substring(1, findOrig.length() - 1); //remove {}
				String[] split = find.split("-");
				int start = Integer.parseInt(split[0]);
				int last = Integer.parseInt(split[1]);
				
				for(int i = start; i <= last; i++) {
					String newUrl = url;
					newUrl = newUrl.replace(findOrig, "" + i);
					newUrls.add(newUrl);
				}
			}
			else {
				newUrls.add(url);
			}
		}
		
		return newUrls.toArray(new String[0]);
		
	}

	String getHTML(String url, String userAgent) {
		try {
			
			URL weburl = new URL(url);

			HttpURLConnection conn = (HttpURLConnection) weburl.openConnection();
			conn.setDefaultUseCaches(false);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", userAgent);
			conn.setRequestProperty("charset", "utf-8");
			conn.setUseCaches(false);
			conn.setConnectTimeout(5000);
			conn.connect();
			int status = conn.getResponseCode();

			InputStream is = null;
			if (100 <= status && status <= 399) {
				is = conn.getInputStream();
			} 
			else {
				is = conn.getErrorStream();
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = is.read(buffer)) != -1) {
				baos.write(buffer, 0, length);
			}

			return baos.toString("utf-8");
		}
		catch(IOException e) {
			return null;
		}
	}



	String[] getProxies(String html) {
		List<String> list = new ArrayList<>();
		Matcher mat = pattern.matcher(html);
		while(mat.find()) {
			list.add(mat.group());
		}
		return list.toArray(new String[0]);
	}



}
