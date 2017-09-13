package com.xgs.net.entity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.JsonKit;

public class TestWholeFileEntity {
Logger logger = LoggerFactory.getLogger("com.xgs.net.test");
	
	@Test
	public void testToString(){

		WholeFileEntity wholeFile = new WholeFileEntity();
		Map<String,String> map = new LinkedHashMap<String,String>();
		map.put("1", "第一个chunkFileMD5值");
		map.put("2", "第二个chunkFileMD5值");
		map.put("3", "第三个chunkFileMD5值");
		wholeFile.setWfMD5("wholeFileMD5值");
		wholeFile.setnSplitter(3);
		wholeFile.setName(null);
		wholeFile.setExt(null);
		wholeFile.setMimeType(null);
		wholeFile.setSize(11111);
		wholeFile.setCfMD5s(map);
		Map<String, String> entityMap = new HashMap<String, String>();
		entityMap.put("WholeFileMsg", JSON.toJSONString(wholeFile));
		entityMap.put("WholeFileMsg", JsonKit.toJson(wholeFile));
		logger.debug(JsonKit.toJson(wholeFile));
	}
}
