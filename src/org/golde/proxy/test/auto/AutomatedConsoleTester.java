package org.golde.proxy.test.auto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.golde.proxy.utils.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AutomatedConsoleTester {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	public static void main(String[] args) throws InterruptedException {

		if(args.length != 2) {
			System.err.println("Arguments: [Proxies File] [Agents File]");
			return;
		}
		
		final String[] proxies = Utils.readFile(new File("res/" + args[0]));
		final String[] agents = Utils.readFile(new File("res/" + args[1]));
		
		List<DataPoint> dp = new ArrayList<DataPoint>();
		final long sysout = System.currentTimeMillis();
		
		for(int threads = 1024; threads >= 1; threads /= 2) {
			
			long time1000 = new SubTester().startTest(proxies, agents, threads, 1000);
			dp.add(new DataPoint(time1000, 1000, threads));
			
			long time5000 = new SubTester().startTest(proxies, agents, threads, 5000);
			dp.add(new DataPoint(time5000, 5000, threads));
			
			long time10000 = new SubTester().startTest(proxies, agents, threads, 10000);
			dp.add(new DataPoint(time10000, 10000, threads));
			
			Utils.writeFile("dp-" + threads + "-" + sysout + ".json", GSON.toJson(dp));
			
			dp.clear();
			
		}
		
	}
	
}
