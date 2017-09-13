package com.xgs.net.entity;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Record;

public class TestRespEntity {
	
	Logger logger = LoggerFactory.getLogger("com.xgs.net.test");
	
	@Test
	public void testToString(){
		
		RespEntity resp = new RespEntity();
		Record record1 = new Record();
		record1.set("chunk_md5", null);
		record1.set("serial", null);
		Record record2 = new Record();
		record2.set("chunk_md5", null);
		record2.set("serial", null);
		List<Record> list = new ArrayList<Record>();
		list.add(record1);
		list.add(record2);
		resp.setSucc(true);
		resp.setCode(0);
		resp.setFileId(null);
		resp.setRecords(list);
		resp.setText(null);

		logger.debug(JsonKit.toJson(resp));
	}
}
