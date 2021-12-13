package org.golde.proxy.scrape;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.golde.proxy.utils.Utils;

import lombok.Setter;

public class Scraper implements Runnable {

	private static final Pattern pattern = Pattern.compile("\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})?");
	private static final Pattern specialURLEntry = Pattern.compile("\\{(.*?)\\}");

	//"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0"
	private final String[] urls;
	private final String[] agents;
	
	private final ScrapeData data;
	
	public Scraper(String[] urls, String[] agents, ScrapeData data) {
		this.urls = parseUrls(urls);
		this.agents = agents;
		//this.callback = callback;
		this.data = data;
	}
	
	@Override
	public void run() {
		
		for(String url : urls) {
			//System.out.println("Parsing: " + url);
			String userAgent = Utils.randomString(agents);
			String body = getHTML(url, userAgent);
			if(body != null) {
				String[] ips = getProxies(body);
				
				for(String ip : ips) {
					try {
						data.ips.offer(ip, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//data.ips.add(ip);
					//ipsGathered.add(ip);
				}
				//callback.addProxy(ips);
				
			}
			else {
				System.err.println(url + " was null with agent: " + userAgent);
			}
			//int tmp = checked.incrementAndGet();
			this.data.urlsChecked.incrementAndGet();
			//callback.onUrlChecked(tmp, urls.length);
		}
		
		//callback.onFinished();
		this.data.areWeFinishedYet.getAndSet(true);
	}
	
	
	private static String[] parseUrls(String[] urls) {
		
		List<String> newUrls = new ArrayList<String>();
		
		for(String url : urls) {
			
			//Ignore any lines that start with #
			if(url.charAt(0) == '#') {
				continue;
			}
			
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

	private static String getHTML(String url, String userAgent) {
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



	private static String[] getProxies(String html) {
		List<String> list = new ArrayList<>();
		Matcher mat = pattern.matcher(html);
		while(mat.find()) {
			list.add(mat.group());
		}
		return list.toArray(new String[0]);
	}



}
