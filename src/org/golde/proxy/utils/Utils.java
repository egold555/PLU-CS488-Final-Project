package org.golde.proxy.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

	private Utils() {}

	public static final String[] readFile(File file) {

		List<String> list = new ArrayList<String>();

		try {
			Scanner scan = new Scanner(file);
			while(scan.hasNextLine()) {
				list.add(scan.nextLine());
			}
			scan.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

		return list.toArray(new String[0]);

	}

	public static final void writeFile(String loc, String[] data) {
		try {
			PrintWriter pw = new PrintWriter(loc);
			for(String d : data) {
				pw.println(d);
			}
			pw.flush();
			pw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static final void writeFile(String loc, String data) {
		try {
			PrintWriter pw = new PrintWriter("res/" + loc);
			pw.print(data);
			pw.flush();
			pw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static final String randomString(String[] strings) {
		return strings[ThreadLocalRandom.current().nextInt(strings.length)];
	}

}
