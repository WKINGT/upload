package com.xgs.net.help;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Utils {
	
	public final static String pwdRule(String pwd,String salt){
		String _salt = "#_#$%";
		String rule = pwd+_salt+salt;
		return md5(rule);
	}
	public final static String generateNum(int len){
		String str = "0,1,2,3,4,5,6,7,8,9";  
        String str2[] = str.split(","); 
        Random rand = new Random(); 
        int index = 0;  
        String randStr = "";
        for (int i=0; i<len; ++i)  
        {  
            index = rand.nextInt(str2.length-1);
            randStr += str2[index]; 
        }
        return randStr;
	}
	public final static String generate(int len){
		String str = "0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z";  
        String str2[] = str.split(","); 
        Random rand = new Random(); 
        int index = 0;  
        String randStr = "";
        for (int i=0; i<len; ++i)  
        {  
            index = rand.nextInt(str2.length-1);
            randStr += str2[index]; 
        }
        return randStr;
	}
	
	public final static String salt(){
        return generate(6);
	}
	
	public final static String md5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
	public static final String Arr2Str(String...strs){
		StringBuffer sb = new StringBuffer();
		for(String s : strs){
			sb.append(",").append(s);
		}
		return sb.toString().substring(1);
	}
	
	public static final String List2Str(List<String> list){
		return Arr2Str(list.toArray(new String[]{}));
	}
	
	public static final String Arr2queryStr(String...strs){
		String str = Arr2Str(strs);
		return toQuery(str);
	}
	public static final String List2queryStr(List<String> list){
		String str = Arr2Str(list.toArray(new String[]{}));
		return toQuery(str);
	}
	
	private static final String toQuery(String str){
		return "('"+str.replace(",", "','")+"')";
	}
	public static String listToString(List<String> stringList){
        if (stringList==null) {
            return null;
        }
        StringBuilder result=new StringBuilder();
        boolean flag=false;
        for (String string : stringList) {
            if (flag) {
                result.append(",");
            }else {
                flag=true;
            }
            result.append(string);
        }
        return result.toString();
    }
	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
		String[] str = {"abc","xyz"};
		List<String> list = new ArrayList<String>();
		list.add("abc");
		list.add("def");
		System.out.println(Arr2Str(str));
		System.out.println(List2Str(list));
		//c4ca4238a0b923820dcc509a6f75849b
		//c4ca4238a0b923820dcc509a6f75849b
		//c4ca4238a0b923820dcc509a6f75849b
		System.out.println(System.currentTimeMillis());
	}
}
