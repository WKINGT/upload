package com.xgs.net.help;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpHelp {
	public static String buildRequestPara(Map<String, Object> params) throws UnsupportedEncodingException {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		String prestr = "";
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = (String) params.get(key);
			value = URLEncoder.encode(value, "UTF-8");
			if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}
		return prestr;
	}

	public static String query(Map<String, Object> params,
			String notifyUrl) {
		String inputLine = "false";
		StringBuilder b = new StringBuilder();
		try {
			String p = buildRequestPara(params);
			if(!p.equals("")){
				notifyUrl += "?" + p;
			}
			URL url = new URL(notifyUrl);
			HttpURLConnection urlConnection;
			if (notifyUrl.startsWith("https://")) {
				urlConnection = (HttpsURLConnection) url.openConnection();
			} else {
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream(), "UTF-8"));
			while ((inputLine = in.readLine()) != null) {
				b.append(inputLine);
			}
			// String utf8 = new String(gbk.getBytes("gbk"), "utf-8");
			inputLine = b.toString();
			// inputLine = new String(inputLine.getBytes("gbk"), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inputLine;
	}
	
	public static String query(String url) {
		Map<String,Object> map = new HashMap<String,Object>();
		return query(map, url);
	}
}
