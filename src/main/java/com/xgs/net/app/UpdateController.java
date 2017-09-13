package com.xgs.net.app;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.jfinal.core.Controller;
import com.jfinal.kit.Prop;

public class UpdateController extends Controller {

	public void index() {
		renderNull();
	}
	
	public void getJson() {
		
		Prop prop = new Prop("update.txt");
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("version", prop.get("version"));
		
		map.put("url", prop.get("url"));
		
		map.put("description", JSON.parseArray(prop.get("description"), String.class));
		
		map.put("md5", prop.get("md5"));
		
		String json = JSON.toJSONString(map);
		
		renderJson(json);
	}
}
