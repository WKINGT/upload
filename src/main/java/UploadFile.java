import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.impl.Log4jLoggerFactory;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.JsonKit;
import com.xgs.net.entity.ChunkFileEntity;
import com.xgs.net.entity.RespEntity;
import com.xgs.net.entity.WholeFileEntity;
import com.xgs.net.help.GetBigFileMD5;
import com.xgs.net.help.GetName;

public class UploadFile {
	Logger log = new Log4jLoggerFactory().getLogger(this.getClass().getName());
	
	private final int CHUNKSIZE = 1024 * 1024 * 4;

	private final String URLSTR = "http://127.0.0.1:8080/upload/uploadSliceFile";
    private static String TURL = "http://127.0.0.1:8080/upload/downLoadFormUrl";
	private final String URL_WHOLE_FILE_UPLOAD = "http://127.0.0.1:8080/upload/uploadWholeFile";
	/**
	 * 上传文件信息
	 * @param file
	 */
	public void uploadFileMsg(File file){
		Map<String,String> map = new LinkedHashMap<String,String>();
		map = GetBigFileMD5.getAllMD5(file);
		log.debug(JsonKit.toJson(map));
		long filelength = file.length();
		int chunks = (int) Math.ceil((double)filelength/(double)CHUNKSIZE);
		String wfMD5 = map.get("all");
		String fileName = file.getName();
		WholeFileEntity wholeFile = new WholeFileEntity();
		wholeFile.setWfMD5(wfMD5);
		wholeFile.setnSplitter(chunks);
		wholeFile.setName(fileName);
		wholeFile.setExt(GetName.getExtensionName(fileName));
//		wholeFile.setMimeType(GetName.getMimeType(file));
		wholeFile.setMimeType(null);
		wholeFile.setSize(filelength);
		map.remove("all");
		wholeFile.setCfMD5s(map);
		
		Map<String, String> entityMap = new HashMap<String, String>();
		entityMap.put("WholeFileMsg", JSON.toJSONString(wholeFile));
		String ret = formUpload(URLSTR,entityMap, null);
//		System.out.println(ret); 
		RespEntity resp = JSON.parseObject(ret, RespEntity.class);
		// 如果上传成功，且服务器不存在该文件，则开始分片上传文件,若服务器存在该文件，则直接秒传
		if (resp.isSucc()){
//			System.out.println(resp.getText());
			switch(resp.getCode()){
			case 1:
				System.out.println(resp.getText()); 
				uploadChunk(file, map, wfMD5);
				break;
			case 0:
				System.out.println(resp.getText()); 
				break;
			default:
				uploadChunk(file, map, wfMD5);
			}
		}else{
			uploadFileMsg(file);
		}

    }
    /**
     * 上传分块
     * @param file
     * @param map
     * @param wfMD5
     */
	public void uploadChunk(File file, Map<String,String> map, String wfMD5) {
		
		byte[] buffer = new byte[CHUNKSIZE];
		int chunk = 1;
		int len = -1;
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);

			while ((len = fileInputStream.read(buffer)) != -1) {

				File outChunk = new File(file.getParentFile() + File.separator
						+ file.getName() + "_" + chunk);
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(outChunk);
					fos.write(buffer, 0, len);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (fos != null) {
						fos.close();
					}
				}
				String cfMD5 = map.get(String.valueOf(chunk));
				ChunkFileEntity chunkFile = new ChunkFileEntity();
				chunkFile.setSerial(chunk);
				chunkFile.setSize(len);
				chunkFile.setCfMD5(cfMD5);
				chunkFile.setWfMD5(wfMD5);
//				chunkFile.setSuccUpload(false);
				
				//上传分块信息
				Map<String, String> chunkMap = new HashMap<String, String>();
				chunkMap.put("ChunkMsg", JSON.toJSONString(chunkFile));
				String ret = formUpload(URLSTR, chunkMap, null);
//				System.out.println(ret);
				RespEntity resp = JSON.parseObject(ret, RespEntity.class);
				if (resp.isSucc()){
//					System.out.println(resp.getText());
					switch(resp.getCode()){
					case 1:
						// 上传分块
						System.out.println(resp.getText());
						Map<String, String> entityMap = new HashMap<String, String>();
						entityMap.put("ChunkFileMsg", JSON.toJSONString(chunkFile));
						Map<String, String> fileMap = new HashMap<String, String>();
						fileMap.put("userfile", outChunk.getPath());
						
						String retc = formUpload(URLSTR, entityMap, fileMap);
						RespEntity respc = JSON.parseObject(retc, RespEntity.class);
						if (respc.isSucc()){
							switch(respc.getCode()){
							case 1:
								System.out.println(respc.getText());
								reUpload(URLSTR, entityMap, fileMap);
								break;
							case 0:
								System.out.println(respc.getText());
								break;
							default:
								reUpload(URLSTR, entityMap, fileMap);
							}
						}else{
							System.out.println(respc.getText());
							reUpload(URLSTR, entityMap, fileMap);
						}
						chunk++;
						break;
					case 0://分块已存在
						System.out.println(resp.getText());
						chunk++;
						continue;
					default:
						formUpload(URLSTR, chunkMap, null);
					}
				}else{
					formUpload(URLSTR, chunkMap, null);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
    
    /** 
     * 上传
     * @param urlStr 
     * @param textMap 
     * @param fileMap 
     * @return 
     */  
    public static String formUpload(String urlStr, Map<String, String> textMap, Map<String, String> fileMap) {  
        String res = "";  
        HttpURLConnection conn = null;  
        String BOUNDARY = "---------------------------123821742118716"; //boundary就是request头和上传文件内容的分隔符    
        try {  
            URL url = new URL(urlStr);  
            conn = (HttpURLConnection) url.openConnection();  
            conn.setConnectTimeout(5000);  
            conn.setReadTimeout(30000);  
            conn.setDoOutput(true);  
            conn.setDoInput(true);  
            conn.setUseCaches(false);  
            conn.setRequestMethod("POST");  
            conn.setRequestProperty("Connection", "Keep-Alive");  
            //enctype="multipart/form-data"
            conn.setRequestProperty("enctype", "multipart/form-data");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");  
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);  
  
            OutputStream out = new DataOutputStream(conn.getOutputStream());  
            // text    
            if (textMap != null) {  
                StringBuffer strBuf = new StringBuffer();  
                Iterator<Map.Entry<String, String>> iter = textMap.entrySet().iterator();  
                while (iter.hasNext()) {  
                    Map.Entry<String, String> entry = iter.next();  
                    String inputName = (String) entry.getKey();  
                    String inputValue = (String) entry.getValue();  
                    if (inputValue == null) {  
                        continue;  
                    }  
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");  
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");  
                    strBuf.append(inputValue);  
                }  
                out.write(strBuf.toString().getBytes());  
            }  
  
            // file    
            if (fileMap != null) {  
                Iterator<Map.Entry<String, String>> iter = fileMap.entrySet().iterator();  
                while (iter.hasNext()) {  
                    Map.Entry<String, String> entry = iter.next();  
                    String inputName = (String) entry.getKey();  
                    String inputValue = (String) entry.getValue();  
                    if (inputValue == null) {  
                        continue;  
                    }  
                    File file = new File(inputValue);  
                    String filename = file.getName();  
//                    MagicMatch match = Magic.getMagicMatch(file, false, true);  
//                    String contentType = match.getMimeType();  
  
                    StringBuffer strBuf = new StringBuffer();  
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");  
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"\r\n");  
                    strBuf.append("Content-Type:image/png;\r\n\r\n");  
  
                    out.write(strBuf.toString().getBytes());  
  
                    DataInputStream in = new DataInputStream(new FileInputStream(file));  
                    int bytes = 0;  
                    byte[] bufferOut = new byte[1024];  
                    while ((bytes = in.read(bufferOut)) != -1) {  
                        out.write(bufferOut, 0, bytes);  
                    }  
                    in.close();  
                }  
            }  
  
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();  
            out.write(endData);  
            out.flush();  
            out.close();  
  
            // 读取返回数据    
            StringBuffer strBuf = new StringBuffer();  
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));  
            String line = null;  
            while ((line = reader.readLine()) != null) {  
                strBuf.append(line).append("\n");  
            }  
            res = strBuf.toString();  
            reader.close();  
            reader = null;  
        } catch (Exception e) {  
            System.out.println("发送POST请求出错。" + urlStr);  
            e.printStackTrace();  
        } finally {  
            if (conn != null) {  
                conn.disconnect();  
                conn = null;  
            }  
        }  
        return res;  
    }
    /**
     * 反复上传分块，直到上传成功为止
     * @param urlStr
     * @param textMap
     * @param fileMap
     */
    public static void reUpload(String urlStr, Map<String, String> textMap, Map<String, String> fileMap) {  
        
    	boolean isReUpload = true;
    	while(isReUpload){
    		String res = "";  
            HttpURLConnection conn = null;  
            String BOUNDARY = "---------------------------123821742118716"; //boundary就是request头和上传文件内容的分隔符    
            try {  
                URL url = new URL(urlStr);  
                conn = (HttpURLConnection) url.openConnection();  
                conn.setConnectTimeout(5000);  
                conn.setReadTimeout(30000);  
                conn.setDoOutput(true);  
                conn.setDoInput(true);  
                conn.setUseCaches(false);  
                conn.setRequestMethod("POST");  
                conn.setRequestProperty("Connection", "Keep-Alive");  
                conn.setRequestProperty("enctype", "multipart/form-data");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");  
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);  
                OutputStream out = new DataOutputStream(conn.getOutputStream());  
                // text    
                if (textMap != null) {  
                    StringBuffer strBuf = new StringBuffer();  
                    Iterator<Map.Entry<String, String>> iter = textMap.entrySet().iterator();  
                    while (iter.hasNext()) {  
                        Map.Entry<String, String> entry = iter.next();  
                        String inputName = (String) entry.getKey();  
                        String inputValue = (String) entry.getValue();  
                        if (inputValue == null) {  
                            continue;  
                        }  
                        strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");  
                        strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");  
                        strBuf.append(inputValue);  
                    }  
                    out.write(strBuf.toString().getBytes());  
                }      
                if (fileMap != null) {  
                    Iterator<Map.Entry<String, String>> iter = fileMap.entrySet().iterator();  
                    while (iter.hasNext()) {  
                        Map.Entry<String, String> entry = iter.next();  
                        String inputName = (String) entry.getKey();  
                        String inputValue = (String) entry.getValue();  
                        if (inputValue == null) {  
                            continue;  
                        }  
                        File file = new File(inputValue);  
                        String filename = file.getName();  
                        StringBuffer strBuf = new StringBuffer();  
                        strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");  
                        strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"; filename=\"" + filename + "\"\r\n");  
                        strBuf.append("Content-Type:image/png;\r\n\r\n");  
                        out.write(strBuf.toString().getBytes());  
                        DataInputStream in = new DataInputStream(new FileInputStream(file));  
                        int bytes = 0;  
                        byte[] bufferOut = new byte[1024];  
                        while ((bytes = in.read(bufferOut)) != -1) {  
                            out.write(bufferOut, 0, bytes);  
                        }  
                        in.close();  
                    }  
                }  
                byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();  
                out.write(endData);  
                out.flush();  
                out.close();  
                // 读取返回数据    
                StringBuffer strBuf = new StringBuffer();  
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));  
                String line = null;  
                while ((line = reader.readLine()) != null) {  
                    strBuf.append(line).append("\n");  
                }  
                res = strBuf.toString();  
                RespEntity resp = JSON.parseObject(res, RespEntity.class);
                if (resp.getCode()== 1){
                	isReUpload = false;
                }
                reader.close();  
                reader = null;  
            } catch (Exception e) {  
                System.out.println("发送POST请求出错。" + urlStr);  
                e.printStackTrace();  
            } finally {  
                if (conn != null) {  
                    conn.disconnect();  
                    conn = null;  
                }  
            } 
    	}
    	
    	  
    }
	

    public static void main(String[] args) {  
//      String filepath = "D:\\Sc.eps";  
//    	String filepath = "F:\\AN University City Scenes&Detail FP.psd";
    	//测试整个文件上传
//    	File file = new File(filepath);
//    	System.out.println(GetBigFileMD5.getMD5(file));
//    	System.out.println("测试整个文件上传");
//        new UploadFile().uploadWholeFile(new File(filepath));
//        //测试文件分片上传
//        System.out.println("测试文件分片上传");
//        new UploadFile().uploadFileMsg(new File(filepath));
        System.out.println(sendGet(TURL,"url=//5b0988e595225.cdn.sohucs.com/images/20180103/015c15f147434b43b019b26cc9ea4ab0.jpeg"));

    }
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "--->" + map.get(key));
//            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
    
    public void uploadWholeFile(File file){
		
//		String md5 = GetBigFileMD5.getMD5(file);
		long filelength = file.length();
		String fileName = file.getName();
		WholeFileEntity wholeFile = new WholeFileEntity();
//		wholeFile.setWfMD5(md5);
		wholeFile.setName(fileName);
		
		wholeFile.setExt(GetName.getExtensionName(fileName));
		
//		wholeFile.setMimeType(GetName.getMimeType(file));
		wholeFile.setMimeType(null);
		wholeFile.setSize(filelength);
		Map<String, String> entityMap = new HashMap<String, String>();
		entityMap.put("UploadWholeFileMsg", JSON.toJSONString(wholeFile));
		
		Map<String, String> fileMap = new HashMap<String, String>();
		fileMap.put("userfile", file.getPath());
		
		
		String ret = formUpload(URL_WHOLE_FILE_UPLOAD, entityMap, fileMap);
		
//		System.out.println(ret); 
		RespEntity resp = JSON.parseObject(ret, RespEntity.class);
		// 如果上传成功，且服务器不存在该文件，则开始分片上传文件,若服务器存在该文件，则直接秒传
		if (resp.isSucc()){
//			System.out.println(resp.getText());
			switch(resp.getCode()){
			case 1:
				System.out.println(resp.getText()); 
				formUpload(URL_WHOLE_FILE_UPLOAD, entityMap, fileMap);
				break;
			case 0:
				System.out.println(resp.getText()); 
				break;
			default:
				formUpload(URL_WHOLE_FILE_UPLOAD, entityMap, fileMap);
			}
		}else{
			formUpload(URL_WHOLE_FILE_UPLOAD, entityMap, fileMap);
		}
	}

}
