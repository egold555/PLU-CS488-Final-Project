package org.golde.proxy;

import java.net.Proxy;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class IPInfo {

	@SerializedName("ip")
	@Expose
	private String ip;
	
	@SerializedName("city")
	@Expose
	private String city;
	
	@SerializedName("region")
	@Expose
	private String region;
	
	@SerializedName("country")
	@Expose
	private String country;
	
	@SerializedName("loc")
	@Expose
	private String loc;
	
	@SerializedName("org")
	@Expose
	private String org;
	
	@SerializedName("postal")
	@Expose
	private String postal;
	
	@SerializedName("timezone")
	@Expose
	private String timezone;
	
	@Setter private long ping;
	@Setter private Boolean https;
	@Setter private String proxy;
	
	@Setter private String proxyType;
}
