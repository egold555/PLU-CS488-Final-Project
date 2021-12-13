package org.golde.proxy;

import lombok.Getter;

@Getter
public class Config {

	private String theme;
	private ScraperConfig scraperConfig;
	private TesterConfig testerConfig;
	
	@Getter
	private static class ScraperConfig {
		private String importLastPath;
		private String exportLastPath;
		private int threads;
		private int timeout;
	}
	
	@Getter
	private static class TesterConfig {
		private String importLastPath;
		private String exportLastPath;
		private int threads;
		private int timeout;
	}
	
}
