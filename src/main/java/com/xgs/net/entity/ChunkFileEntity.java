package com.xgs.net.entity;

public class ChunkFileEntity {
	private int serial;//分块文件的序列号
	private long size;//分块文件的大小
	private String wfMD5;//整个文件的MD5值
	private String cfMD5;//分块文件的MD5值
	public int getSerial() {
		return serial;
	}
	public void setSerial(int serial) {
		this.serial = serial;
	}
	public String getWfMD5() {
		return wfMD5;
	}
	public void setWfMD5(String wfMD5) {
		this.wfMD5 = wfMD5;
	}
	public String getCfMD5() {
		return cfMD5;
	}
	public void setCfMD5(String cfMD5) {
		this.cfMD5 = cfMD5;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	
}
