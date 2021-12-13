package org.golde.proxy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.golde.proxy.utils.Utils;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class Gah {

	public static void main(String[] args) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
//		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
//
//
//		IPInfo[] data = gson.fromJson(new FileReader(new File("res/temp-parsed-data.json")), new IPInfo[0].getClass());
//		List<IPInfo> newData = new ArrayList<IPInfo>();
//		
//		
//		for(IPInfo info : data) {
//			info.setProxyType(Proxy.Type.HTTP.name());
//			newData.add(info);
//		}
//		
//		Utils.writeFile("temp-parsed-data-2.json", gson.toJson(newData));
		
		
		String[] proxies = Utils.readFile(new File("res/2point4m.txt"));
		
		List<String> proxiesList = Arrays.asList(proxies);
		
		Collections.shuffle(proxiesList);
		
		Utils.writeFile("scrambed.txt", proxiesList.toArray(new String[0]));
		
	}

}
