package org.golde.proxy.scrape;

public interface ScraperCallback {
	void onUrlChecked(int checked, int max);
	void addProxy(String[] ips);
	void onFinished();
}
