package com.xgs.net.entity;

import java.util.HashMap;
import java.util.Map;

public class WholeFileEntity {
	private String name;//文件名
	private String ext;//文件后缀
	private String mimeType;//媒体类型
	private long size;
	private String wfMD5;//文件的MD5值
	private int nSplitter;//文件分块的个数
	private Map<String, String> cfMD5s = new HashMap<>();//分块文件的序号和MD5值
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getWfMD5() {
		return wfMD5;
	}
	public void setWfMD5(String wfMD5) {
		this.wfMD5 = wfMD5;
	}
	public int getnSplitter() {
		return nSplitter;
	}
	public void setnSplitter(int nSplitter) {
		this.nSplitter = nSplitter;
	}
	public Map<String, String> getCfMD5s() {
		return cfMD5s;
	}
	public void setCfMD5s(Map<String, String> cfMD5s) {
		this.cfMD5s = cfMD5s;
	}

	
}
