package org.golde.proxy.utils;

import lombok.Getter;

public class Timings {

	private long start, stop;

	@Getter private long elapsed;
	@Getter private final String name;

	public Timings(String name) {
		this.name = name;
	}

	public Timings start() {
		this.start = System.currentTimeMillis();
		return this;
	}

	public Timings stop() {
		this.stop = System.currentTimeMillis();
		this.elapsed = (this.stop - this.start);
		return this;
	}

	@Override
	public String toString() {
		return "Timer '" + this.name + "' took " + elapsed + "ms";
	}

}
