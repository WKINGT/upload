package com.xgs.net.app;



import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.impl.Log4jLoggerFactory;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.xgs.net.entity.ChunkFileEntity;
import com.xgs.net.entity.DownFileEntity;
import com.xgs.net.entity.FileMsgEntity;
import com.xgs.net.entity.RespEntity;
import com.xgs.net.entity.WholeFileEntity;
import com.xgs.net.help.FileUtil;
import com.xgs.net.help.GetBigFileMD5;
import com.xgs.net.help.GetName;
import com.xgs.net.help.UUIDHexGenerator;
import com.xgs.net.model.ChunkFile;
import com.xgs.net.model.FileMark;
import com.xgs.net.model.FileUpload;
import com.xgs.net.model.UploadWholeFile;
import com.xgs.net.model.WholeFile;
import com.xgs.net.service.UploadFileService;

public class TestUpload extends Controller{
	
	
	UploadFileService uploadFileService = Enhancer.enhance(UploadFileService.class);
	Logger log = new Log4jLoggerFactory().getLogger(this.getClass().getName());
	
	public void index(){
		String oldStr = this.getPara("WholeFileMsg");
		render("index.html");
//		this.renderFile(file);
	}
	/**
	 * 分片上传文件
	 * @throws Exception
	 */
	public void uploadSliceFile() throws Exception{
		log.debug(getRequest().getRemoteAddr());
		UploadFile ufile = this.getFile();
		
		String WholeFileMsg = this.getPara("WholeFileMsg");
//		String urlStr = URLDecoder.decode(WholeFileMsg, "UTF-8");  
//		oldStr = new String(oldStr.getBytes("gbk"),"utf-8");
		if(WholeFileMsg!=null){// whole file info
//			String WholeFileMsg = new String(oldStr.getBytes(), "UTF-8");
			log.debug(WholeFileMsg);
			WholeFileEntity wholeFile = JSON.parseObject(WholeFileMsg,WholeFileEntity.class);
			String wholeFileMD5 = wholeFile.getWfMD5();
			String fileName = wholeFile.getName();
			String ext = wholeFile.getExt();
			String mimeType = wholeFile.getMimeType();
			int size = (int)wholeFile.getSize();
			Map<String, String> cfMD5s = wholeFile.getCfMD5s();
			int chunkNum = wholeFile.getnSplitter();
			String fileId = null;
			//查询该上传文件的的MD5是否存在，若存在，则秒传，若不存在，则上传所有分块
			if (uploadFileService.findWholeFile(wholeFileMD5) == null){//不存在，上传所有分块
				fileId = uploadFileService.saveWholeFile(wholeFileMD5, size, chunkNum, cfMD5s, fileName, ext, mimeType);
				List<Record> records = uploadFileService.findUnuploadChunk(wholeFileMD5);
				RespEntity entity = new RespEntity();
				entity.setSucc(true);
				entity.setCode(1);
				entity.setFileId(fileId);
				entity.setRecords(records);
				entity.setText("接受文件信息成功，该文件不存在，请分片上传");
				this.renderJson(JsonKit.toJson(entity));
			}else {//文件存在
				FileUpload fu = uploadFileService.findFileUpload(wholeFileMD5);//查询该md5文件的文件信息
				fileId = fu.getId();
				List<Record> records = uploadFileService.findUnuploadChunk(wholeFileMD5);
				if (!fileName.equals(fu.getFileName())){//文件名不一样,写入UploadFile记录
					fileId = uploadFileService.saveUploadFile(wholeFileMD5, fileName, ext, mimeType, size);
				}
				log.debug(String.valueOf(records.size()));
				log.debug(String.valueOf(records!=null));
				if (records!=null && records.size()!= 0){
					RespEntity entity = new RespEntity();
					entity.setSucc(true);
					entity.setCode(1);
					entity.setText("该文件已存在，但还有未上传成功的分块需要上传");
					entity.setRecords(records);
					entity.setFileId(fileId);
					this.renderJson(JsonKit.toJson(entity));
				}else{
					RespEntity entity = new RespEntity();
					entity.setSucc(true);
					entity.setCode(0);
					entity.setText("该文件已存在，不需要上传");
					entity.setFileId(fileId);
					this.renderJson(JsonKit.toJson(entity));
				}
			}
			return;
		}
		/**
		 * 接收分块信息
		 */
		String ChunkMsg = this.getPara("ChunkMsg");
		if(ChunkMsg!=null){
			log.debug(ChunkMsg);
//			log.debug("he is name {},and age is {};", "alice",22);

			ChunkFileEntity chunkFile = JSON.parseObject(ChunkMsg,ChunkFileEntity.class);
			String chunkFileMD5 = chunkFile.getCfMD5();
			String wholeFileMD5 = chunkFile.getWfMD5();
			int serial = chunkFile.getSerial();
			
			// 查询该上传分块的MD5是否存在，若存在，则分块秒传，若不存在，则上传分块
			if (uploadFileService.findChunkFile(chunkFileMD5) != null){//分块存在，秒传分块，客户端不需要再上传改分块
				uploadFileService.changeUpload(chunkFileMD5, wholeFileMD5, serial);//将分块的上传状态改为已上传
				RespEntity entity = new RespEntity();
				entity.setSucc(true);
				entity.setCode(0);
				entity.setText("接受分块信息成功，该分块已存在，不需要上传");
				this.renderJson(JsonKit.toJson(entity));
			}else{//分块不不存在，上传该分块
				RespEntity entity = new RespEntity();
				entity.setSucc(true);
				entity.setCode(1);
				entity.setText("接受分块信息成功，且该分块不存在，请上传该分块！");
				this.renderJson(JsonKit.toJson(entity));
			}
			return;
		}
		
		/**
		 * 接收分块
		 */
		String ChunkFileMsg = this.getPara("ChunkFileMsg");
		if(ChunkFileMsg!=null){//chunk file
			log.debug(ChunkFileMsg);
			
			ChunkFileEntity chunkFile = JSON.parseObject(ChunkFileMsg,ChunkFileEntity.class);
			String chunkFileMD5 = chunkFile.getCfMD5();
			String wholeFileMD5 = chunkFile.getWfMD5();
			int serial = chunkFile.getSerial();
			int size = (int)chunkFile.getSize();
			// 计算上传的分块的md5是否与该分块信息的md5相同
			File file = ufile.getFile();
			String fileMd5 = GetBigFileMD5.getMD5(file);
			log.debug("上传分块的序号为："+ serial + "上传分块信息的Md5值为："+chunkFileMD5);
			log.debug("接收分块文件的Md5值为："+fileMd5);
			log.debug("Md5值是否相等："+ chunkFileMD5.equals(fileMd5));
			if (!chunkFileMD5.equals(fileMd5)){
				//上传文件的MD5值不同，返回上传失败，重新上传块文件
				RespEntity entity = new RespEntity();
				entity.setSucc(false);
				entity.setCode(1);
				entity.setText("分块上传失败，请重新上传该分块！");
				this.renderJson(JsonKit.toJson(entity));
			}else{
				
				String pathInfo = FileUtil.FILEDIR+File.separator+UUIDHexGenerator.getId();
				File dest = new File(pathInfo);
				FileUtil.copyFile(file, dest, false, true);
				
				uploadFileService.saveChunkFile(chunkFileMD5, size, pathInfo, serial, wholeFileMD5);
				RespEntity entity = new RespEntity();
				entity.setSucc(true);
				entity.setCode(0);
				entity.setText("接受分块成功！");
				this.renderJson(JsonKit.toJson(entity));
			}
		}
	}
	/**
	 * 整个文件上传
	 * @throws Exception 
	 */
	public void uploadWholeFile() throws Exception{
		log.debug(getRequest().getRemoteAddr());
		UploadFile ufile = this.getFile();
		
		String fileName = ufile.getFileName();
		String mimeType = ufile.getContentType();
		String ext = GetName.getExtensionName(fileName);
		long size = ufile.getFile().length();

		File file = ufile.getFile();

		// 将接受到的文件改名另存
		String pathInfo = null;
		if (ext != null) {
			pathInfo = FileUtil.FILEDIR + File.separator
					+ UUIDHexGenerator.getId() + "." + ext;
		} else {
			pathInfo = FileUtil.FILEDIR + File.separator
					+ UUIDHexGenerator.getId();
		}
		File dest = new File(pathInfo);
		FileUtil.copyFile(file, dest, false, true);
		String fileId = uploadFileService.saveUploadWholeFile(fileName, ext,
				mimeType, (int) size, pathInfo);
		RespEntity entity = new RespEntity();
		entity.setSucc(true);
		entity.setCode(0);
		entity.setText("接受整个文件成功！");
		entity.setFileId(fileId);
		this.renderJson(JsonKit.toJson(entity));

	}
	
	/**
	 * 下载文件信息
	 */
	public void downloadFileMsg(){
		log.debug(getRequest().getRemoteAddr());
//		UploadFile ufile = this.getFile();
		
//		String fileId = this.getPara("FileId");
		String fileId = this.getPara();
		
		FileMark fileMark = uploadFileService.findFileMark(fileId);
		if(fileMark==null){
			Map<String, Object> map = new HashMap<>();
			map.put("code", 1);
			map.put("message", "没有这个文件");
			this.renderJson(map);
			return;
		}
		FileMsgEntity fileMsg = new FileMsgEntity();
		if(fileMark.getIsCut()){//文件分片
			FileUpload  fileUpload = uploadFileService.findSliceFileMsg(fileId);
			if (fileUpload==null){
				Map<String, Object> map = new HashMap<>();
				map.put("code", 1);
				map.put("message", "没有这个文件");
				this.renderJson(map);
				return;
			}
			fileMsg.setName(fileUpload.getFileName());
			fileMsg.setSize(fileUpload.getSize());
			fileMsg.setExt(fileUpload.getFileExt());
			fileMsg.setMimeType(fileUpload.getMimeType());
			fileMsg.setCreatTime(fileUpload.getCreateTime());
		}else{//文件未分片
			UploadWholeFile uploadWholeFile = uploadFileService.findWholeUploadFile(fileId);
			if (uploadWholeFile==null){
				Map<String, Object> map = new HashMap<>();
				map.put("code", 1);
				map.put("message", "没有这个文件");
				this.renderJson(map);
				return;
			}
			fileMsg.setName(uploadWholeFile.getFileName());
			fileMsg.setSize(uploadWholeFile.getSize());
			fileMsg.setExt(uploadWholeFile.getFileExt());
			fileMsg.setMimeType(uploadWholeFile.getMimeType());
			fileMsg.setCreatTime(uploadWholeFile.getCreateTime());
		}
		this.renderJson(fileMsg);
	}
	
	/**
	 * 批量下载文件信息
	 */
	public void downloadFileMsgs(){
		String fileIds = this.getPara("fileIds");
		
		if(fileIds==null){
			Map<String, Object> map = new HashMap<>();
			map.put("code", 1);
			map.put("message", "fileIds为空");
			this.renderJson(map);
			return;
		}
		List<String> ids = JSON.parseArray(fileIds, String.class);
		Map<String,Object> map = new LinkedHashMap<String,Object>();
		for(String id : ids){
			FileMark fileMark = uploadFileService.findFileMark(id);
			if(fileMark==null){
				map.put(id, null);
				continue;
			}
			FileMsgEntity fileMsg = new FileMsgEntity();
			if(fileMark.getIsCut()){//文件分片
				FileUpload  fileUpload = uploadFileService.findSliceFileMsg(id);
				if (fileUpload==null){
					map.put(id, null);
					continue;
				}
				fileMsg.setName(fileUpload.getFileName());
				fileMsg.setSize(fileUpload.getSize());
				fileMsg.setExt(fileUpload.getFileExt());
				fileMsg.setMimeType(fileUpload.getMimeType());
				fileMsg.setCreatTime(fileUpload.getCreateTime());
			}else{//文件未分片
				UploadWholeFile uploadWholeFile = uploadFileService.findWholeUploadFile(id);
				if (uploadWholeFile==null){
					map.put(id, null);
					continue;
				}
				fileMsg.setName(uploadWholeFile.getFileName());
				fileMsg.setSize(uploadWholeFile.getSize());
				fileMsg.setExt(uploadWholeFile.getFileExt());
				fileMsg.setMimeType(uploadWholeFile.getMimeType());
				fileMsg.setCreatTime(uploadWholeFile.getCreateTime());
			}
			map.put(id, fileMsg);
		}
		this.renderJson(map);
	}
	
	/**
	 * 下载文件
	 */
	public void downloadFile(){
		log.debug(getRequest().getRemoteAddr());
		String fileId = this.getPara();
		log.debug("文件的id:"+fileId);
		if(fileId==null){
//			this.renderJson("没有这个文件");
			Map<String, Object> map = new HashMap<>();
			map.put("code", 1);
			map.put("message", "没有这个文件");
			this.renderJson(map);
			return;
		}
		FileMark fileMark = uploadFileService.findFileMark(fileId);
		if(fileMark==null){
//			this.renderJson("没有这个文件");
			Map<String, Object> map = new HashMap<>();
			map.put("code", 1);
			map.put("message", "没有这个文件");
			this.renderJson(map);
			return;
		}
		
		if(fileMark.getIsCut()){//文件分片
			WholeFile wholeFile = uploadFileService.findSliceUploadFile(fileId);
			FileUpload fileUpload = uploadFileService.findSliceFileMsg(fileId);
			if (wholeFile==null){
				Map<String, Object> map = new HashMap<>();
				map.put("cut", fileMark.getIsCut());
				map.put("code", 1);
				map.put("message", "没有这个文件");
				this.renderJson(map);
				return;
			}
			
			List<Record> records = uploadFileService.findChunkMsg(wholeFile.getWholeMd5());
			if(wholeFile.getChunkNum()!=records.size()){
				Map<String, Object> map = new HashMap<>();
				map.put("cut", fileMark.getIsCut());
				map.put("code", 1);
				map.put("message", "文件已损坏无法下载");
				this.renderJson(map);
				return;
			}
			Map<String,String> map = new LinkedHashMap<String,String>();
			Iterator<Record> it = records.iterator();
			while(it.hasNext()){
				Record record = it.next();
				String chunkMd5 = record.get("chunk_md5");
				String serial = String.valueOf(record.getInt("serial"));
				map.put(serial, chunkMd5);
			}
			DownFileEntity downFileMsg = new DownFileEntity();
			downFileMsg.setCode(0);
			downFileMsg.setCut(fileMark.getIsCut());
			downFileMsg.setSize(fileUpload.getSize());
			downFileMsg.setWfMD5(wholeFile.getWholeMd5());
			downFileMsg.setCfMD5s(map);
			this.renderJson(downFileMsg);
//			Iterator<Record> it1 = records.iterator();
//			while(it1.hasNext()){
//				Record record = it1.next();
//				String chunkMd5 = record.get("chunk_md5");
//				ChunkFile chunkFile = uploadFileService.findChunk(chunkMd5);
//				File file = new File(chunkFile.getPathInfo());
//				this.renderFile(file);
//			}
		}else{//文件未分片
			Map<String, Object> map = new HashMap<>();
			UploadWholeFile uploadWholeFile = uploadFileService.findWholeUploadFile(fileId);
			map.put("cut", fileMark.getIsCut());
			map.put("code", 0);
			map.put("size", uploadWholeFile.getSize());
			this.renderJson(map);
//			UploadWholeFile uploadWholeFile = uploadFileService.findWholeUploadFile(fileId);
//			File file = new File(uploadWholeFile.getPathInfo());
//			this.renderFile(file);
		}
	}
	/**
	 * 下载分块
	 */
	public void downloadChunkFile(){
		log.debug(getRequest().getRemoteAddr());
		String chunkMd5 = this.getPara();
		if(chunkMd5==null){
			Map<String, Object> map = new HashMap<>();
			map.put("code", 1);
			map.put("message", "没有这个文件");
			this.renderJson(map);
			return;
		}
		ChunkFile chunkFile = uploadFileService.findChunk(chunkMd5);
		if(chunkFile!=null){
			File file = new File(chunkFile.getPathInfo());
			this.renderFile(file);
			return;
		}
		Map<String, Object> map = new HashMap<>();
		map.put("code", 1);
		map.put("message", "没有这个文件");
		this.renderJson(map);
	}
	/**
	 * 下载整块文件
	 */
	public void downloadWholeFile(){
		log.debug(getRequest().getRemoteAddr());
		String fileId = this.getPara();
		if(fileId==null){
			Map<String, Object> map = new HashMap<>();
			map.put("code", 1);
			map.put("message", "没有这个文件");
			this.renderJson(map);
			return;
		}
		UploadWholeFile uploadWholeFile = uploadFileService.findWholeUploadFile(fileId);
		if(uploadWholeFile!=null){
			File file = new File(uploadWholeFile.getPathInfo());
			this.renderFile(file);
			return;
		}
		Map<String, Object> map = new HashMap<>();
		map.put("code", 1);
		map.put("message", "没有这个文件");
		this.renderJson(map);
	}
	
	/**
	 * 删除文件
	 */
	public void deleteFile(){
		log.debug(getRequest().getRemoteAddr());
		
		String fileId = this.getPara();
		if(fileId==null || fileId.equals("")){
			Map<String, Object> map = new HashMap<>();
			map.put("code", 1);
			map.put("message", "删除失败，fileId为空");
			this.renderJson(map);
			return;
		}
		FileMark fileMark = uploadFileService.findFileMark(fileId);
		if(fileMark==null){
			Map<String, Object> map = new HashMap<>();
			map.put("code", 0);
			map.put("message", "删除文件成功");
			this.renderJson(map);
			return;
		}
		if(fileMark.getIsCut()){//文件分片
			FileUpload  fileUpload = uploadFileService.findSliceFileMsg(fileId);
			if (fileUpload!=null){
				uploadFileService.deleteSliceUploadFile(fileId);
			}
			Map<String, Object> map = new HashMap<>();
			map.put("code", 0);
			map.put("message", "删除文件成功");
			this.renderJson(map);
			return;
			
		}else{//文件未分片
			UploadWholeFile uploadWholeFile = uploadFileService.findWholeUploadFile(fileId);
			if (uploadWholeFile!=null){
				uploadFileService.deleteWholeUploadFile(fileId,uploadWholeFile.getPathInfo());
				
			}
			Map<String, Object> map = new HashMap<>();
			map.put("code", 0);
			map.put("message", "删除文件成功");
			this.renderJson(map);
			return;
		}
	}
	
	/**
	 * 图片渲染
	 */
	public void image() {
		try {
			String id = getPara();
			UploadWholeFile uploadWholeFile = uploadFileService.findWholeUploadFile(id);
			
			if (uploadWholeFile != null && !StringUtils.isEmpty(uploadWholeFile.getPathInfo())) {
				getResponse().setHeader("Content-Type", uploadWholeFile.getMimeType());
				String filePath = uploadWholeFile.getPathInfo();
				
				FileInputStream hFile = new FileInputStream(filePath);
				// 得到文件大小
				int i = hFile.available();
				byte data[] = new byte[i];
				// 读数据
				hFile.read(data);
				// 得到向客户端输出二进制数据的对象
				ServletOutputStream sos = getResponse().getOutputStream();
				// 输出数据
				sos.write(data);
				sos.flush();
				sos.close();
				hFile.close();
				this.renderNull();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		RespEntity msg = new RespEntity();
		msg.setCode(1);
		this.renderJson(msg);
	}
}
