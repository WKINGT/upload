package com.xgs.net.help;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * 计算大文件MD5 David 2012-10-12
 */
public class GetBigFileMD5 {



	/**
	 * 对一个文件获取md5值
	 * 
	 * @return md5串
	 */
	public static String getMD5(File file) {
		
		FileInputStream fileInputStream = null;
		try {
			MessageDigest MD5 = MessageDigest.getInstance("MD5");
			fileInputStream = new FileInputStream(file);
			byte[] buffer = new byte[1024*1024*4];
			int length;
			while ((length = fileInputStream.read(buffer)) != -1) {
				MD5.update(buffer, 0, length);
			}
			return new String(Hex.encodeHex(MD5.digest()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (fileInputStream != null)
					fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
public static Map<String,String> getAllMD5(File file) {
		Map<String,String> map = new LinkedHashMap<String,String>();
		FileInputStream fileInputStream = null;
		try {
			MessageDigest MD5 = MessageDigest.getInstance("MD5");
			fileInputStream = new FileInputStream(file);
			byte[] buffer = new byte[1024*1024*4];
			int length;
			int count = 1;
			while ((length = fileInputStream.read(buffer)) != -1) {
					MessageDigest cMD5 = MessageDigest.getInstance("MD5");
					cMD5.update(buffer, 0, length);
					MD5.update(buffer, 0, length);
					map.put(String.valueOf(count),new String(Hex.encodeHex(cMD5.digest())));
					count++;
			}
			map.put("all", new String(Hex.encodeHex(MD5.digest())));
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		finally {
			try {
				if (fileInputStream != null)
					fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 求一个字符串的md5值
	 * 
	 * @param target
	 *            字符串
	 * @return md5 value
	 */
	public static String MD5(String target) {
		return DigestUtils.md5Hex(target);
	}

	public static void main(String[] args) {
		Map<String,String> map = new LinkedHashMap<String,String>();
		long beginTime = System.currentTimeMillis();
		File fileZIP = new File("F:/123.txt");
		String md5 = getMD5(fileZIP);
		map = getAllMD5(fileZIP);
		long endTime = System.currentTimeMillis();
		System.out.println("MD5:" + md5 + "\n time:"
				+ ((endTime - beginTime) / 1000) + "s");
		System.out.println("测试方法getAllMD5()");
		for (String s : map.keySet()){
			System.out.println(s+"MD5:" + map.get(s));
		}
	}
}