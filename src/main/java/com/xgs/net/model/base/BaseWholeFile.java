package com.xgs.net.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseWholeFile<M extends BaseWholeFile<M>> extends Model<M> implements IBean {

	public void setWholeMd5(java.lang.String wholeMd5) {
		set("whole_md5", wholeMd5);
	}

	public java.lang.String getWholeMd5() {
		return get("whole_md5");
	}

	public void setSize(java.lang.Long size) {
		set("size", size);
	}

	public java.lang.Long getSize() {
		return get("size");
	}

	public void setChunkNum(java.lang.Integer chunkNum) {
		set("chunk_num", chunkNum);
	}

	public java.lang.Integer getChunkNum() {
		return get("chunk_num");
	}

}
