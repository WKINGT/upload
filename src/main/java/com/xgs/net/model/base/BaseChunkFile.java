package com.xgs.net.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseChunkFile<M extends BaseChunkFile<M>> extends Model<M> implements IBean {

	public void setChunkMd5(java.lang.String chunkMd5) {
		set("chunk_md5", chunkMd5);
	}

	public java.lang.String getChunkMd5() {
		return get("chunk_md5");
	}

	public void setSize(java.lang.Long size) {
		set("size", size);
	}

	public java.lang.Long getSize() {
		return get("size");
	}

	public void setPathInfo(java.lang.String pathInfo) {
		set("path_info", pathInfo);
	}

	public java.lang.String getPathInfo() {
		return get("path_info");
	}

}
