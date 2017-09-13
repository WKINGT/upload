package com.xgs.net.entity;

import java.util.HashMap;
import java.util.Map;

public class DownFileEntity {
	private int code;
	private long size;
	private boolean cut;
	private String wfMD5;
	private Map<String, String> cfMD5s = new HashMap<>();
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getWfMD5() {
		return wfMD5;
	}
	public void setWfMD5(String wfMD5) {
		this.wfMD5 = wfMD5;
	}
	public Map<String, String> getCfMD5s() {
		return cfMD5s;
	}
	public void setCfMD5s(Map<String, String> cfMD5s) {
		this.cfMD5s = cfMD5s;
	}
	public boolean isCut() {
		return cut;
	}
	public void setCut(boolean cut) {
		this.cut = cut;
	}
	
}
