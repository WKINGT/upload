package com.xgs.net.help;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TimeHelper {
	/**
	 * 得到当前的年份
	 * 返回格式:yyyy
	 * @return String
	 */
	public static String getCurrentYear() {
		java.util.Date NowDate = new java.util.Date();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
		return formatter.format(NowDate);
	}
	
	/**
	 * 得到当前的月份
	 * 返回格式:MM
	 * @return String
	 */
	public static String getCurrentMonth() {
		java.util.Date NowDate = new java.util.Date();

		SimpleDateFormat formatter = new SimpleDateFormat("MM");
		return formatter.format(NowDate);
	}
	/**
	 * 得到当前的日期
	 * 返回格式:dd
	 * @return String
	 */
	public static String getCurrentDay() {
		java.util.Date NowDate = new java.util.Date();

		SimpleDateFormat formatter = new SimpleDateFormat("dd");
		return formatter.format(NowDate);
	}
	/**
	 * 得到当前的时间，精确到毫秒,共19位
	 * 返回格式:yyyy-MM-dd HH:mm:ss
	 * @return String
	 */
	public static String getCurrentTime() {
		Date NowDate = new Date();
		SimpleDateFormat formatter =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String CurrentTime = formatter.format(NowDate);
		return CurrentTime;
	}
	/**
	 * 得到当前的时间，精确到毫秒,共6位
	 * 返回格式:HHmmss
	 * @return String
	 */
	public static String getCurrentTime6() {
		Date NowDate = new Date();
		SimpleDateFormat formatter =new SimpleDateFormat("HHmmss");
		String CurrentTime = formatter.format(NowDate);
		return CurrentTime;
	}
	/**
	 * 根据传入的日期进行格式化
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}
	
	/**
	 * 得到当前的日期,共10位
	 * 返回格式：yyyy-MM-dd
	 * @return String
	 */
	public static String getCurrentDate() {
		Date NowDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String CurrentDate = formatter.format(NowDate);
		return CurrentDate;
	}
	
	/**
	 * 得到当前的日期,共8位
	 * 返回格式：yyyyMMdd
	 * @return String
	 */
	public static String getCurrentDate8() {
		Date NowDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String CurrentDate = formatter.format(NowDate);
		return CurrentDate;
	}
	
	/**
	 * 得到当前日期加上某一个整数的日期，整数代表天数
	 * 输入参数：currentdate : String 格式 yyyy-MM-dd
	 * 			add_day		:  int
	 * 返回格式：yyyy-MM-dd
	 */
	public static String addDay(String currentdate,int add_day){
		GregorianCalendar gc=null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		int year,month,day;
		
		try {
			year=Integer.parseInt(currentdate.substring(0,4));
			month=Integer.parseInt(currentdate.substring(5,7))-1;
			day=Integer.parseInt(currentdate.substring(8,10));
			
			gc=new GregorianCalendar(year,month,day);
			gc.add(GregorianCalendar.DATE,add_day);
		
			return formatter.format(gc.getTime());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean validByMin(String date,int min){
		Date d = new Date();
		Date cd = addDateMinut(date,min);
		return d.before(cd);
	}
	
	public static String addMonth(String currentdate,int add_month){
		GregorianCalendar gc=null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		int year,month,day;
		
		try {
			year=Integer.parseInt(currentdate.substring(0,4));
			month=Integer.parseInt(currentdate.substring(5,7))-1;
			day=Integer.parseInt(currentdate.substring(8,10));
			
			gc=new GregorianCalendar(year,month,day);
			gc.add(GregorianCalendar.MONTH, add_month);
			
			return formatter.format(gc.getTime());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Date toDate(String day,String format){
		if(format == null){
			format = "yyyy-MM-dd HH:mm:ss";
		}
        SimpleDateFormat sdf = new SimpleDateFormat(format);// 24小时制  
		Date date = null;   
        try {   
            date = sdf.parse(day);   
        } catch (Exception ex) {   
            ex.printStackTrace();   
        }  
        return date;
	}
	
	/**
	 * 加几分钟（小时或者天）后得到新的日期
	 * @param 输入参数：day : String 格式 yyyy-MM-dd HH:mm:ss
	 * 				x		:  int (分钟)
	 * @return String 格式 yyyy-MM-dd HH:mm:ss
	 */
	public static Date addDateMinut(String day, int x)//返回的是字符串型的时间，输入的
	//是String day, int x
	 {   
	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 24小时制  
	//引号里面个格式也可以是 HH:mm:ss或者HH:mm等等，很随意的，不过在主函数调用时，要和输入的变
	//量day格式一致
	        Date date = null;   
	        try {   
	            date = format.parse(day);   
	        } catch (Exception ex) {   
	            ex.printStackTrace();   
	        }   
	        if (date == null)   
	            return null;   
	        System.out.println("front:" + format.format(date)); //显示输入的日期  
	        Calendar cal = Calendar.getInstance();   
	        cal.setTime(date);   
	        cal.add(Calendar.MINUTE, x);// 24小时制   
	        date = cal.getTime();   
	        System.out.println("after:" + format.format(date));  //显示更新后的日期 
	        cal = null;   
	        return date;   
	  
	    }  
	
	/**
	 * 加几分钟（小时或者天）后得到新的日期
	 * @param 输入参数：day : String 格式 yyyy-MM-dd HH:mm:ss
	 * 				x		:  int (分钟)
	 * @return String 格式 yyyy-MM-dd HH:mm:ss
	 */
	public static Date addDateMinut(Date date, int x)//返回的是字符串型的时间，输入的
	//是String day, int x
	 {   
	        if (date == null)   
	            return null;   
	        Calendar cal = Calendar.getInstance();   
	        cal.setTime(date);   
	        cal.add(Calendar.MINUTE, x);// 24小时制   
	        date = cal.getTime();   
	        cal = null;   
	        return date;   
	  
	    }  
	
	
	public static Calendar getStringToCal(String date) {
        final String year = date.substring(0, 4);
        final String month = date.substring(5, 7);
        final String day = date.substring(8, 10);
        final String hour = date.substring(11, 13);
        final String minute = date.substring(14, 16);
        final String second = date.substring(17, 19);
        final int millisecond = Integer.valueOf(date.substring(20, 23));
        Calendar result =
            new GregorianCalendar(Integer.valueOf(year),
                Integer.valueOf(month) - 1, Integer.valueOf(day),
                Integer.valueOf(hour), Integer.valueOf(minute),
                Integer.valueOf(second));
        result.set(Calendar.MILLISECOND, millisecond);
        result.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return result;
    }
	public static void main(String[] args) {
		String time = "2016-02-26T16:00:00.000Z";
		Calendar cal=getStringToCal(time);
        Date date = new Date(cal.getTimeInMillis());
        System.out.println(formatDate(date, "yyyy-MM-dd HH:mm:ss"));
	}
}
