package com.xgs.net.service;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.xgs.net.help.DateStyle;
import com.xgs.net.help.DateUtil;
import com.xgs.net.help.UUIDHexGenerator;
import com.xgs.net.model.ChunkFile;
import com.xgs.net.model.FileMark;
import com.xgs.net.model.FileUpload;
import com.xgs.net.model.UploadWholeFile;
import com.xgs.net.model.WholeChunkMapping;
import com.xgs.net.model.WholeFile;


public class UploadFileService {
	/**
	 * 保存上传文件与分块信息
	 * @param wholeMd5
	 * @param size
	 * @param chunkNum
	 * @param cfMD5s
	 */
	@Before(Tx.class)
	public String  saveWholeFile(String wholeMd5, long size, int chunkNum, Map<String, String> cfMD5s,String name, String ext, String mimeType){
		String fileId = null;
		WholeFile wholefile = new WholeFile();
		wholefile.setWholeMd5(wholeMd5);
		wholefile.setSize(size);
		wholefile.setChunkNum(chunkNum);
		wholefile.save();
		
		for (String c : cfMD5s.keySet()){
			String cfmd5 = cfMD5s.get(c);
			this.saveWholeChunkMapping(cfmd5, wholeMd5, "0", Integer.parseInt(c));
		}
		fileId = this.saveUploadFile(wholeMd5, name, ext, mimeType, size);
		return fileId;
	}
	/**
	 * 保存文件和分块的映射
	 * @param chunkMd5
	 * @param wholeMd5
	 * @param isUpload
	 * @param serial
	 */
	public void saveWholeChunkMapping(String chunkMd5, String wholeMd5, String isUpload, int serial){
		WholeChunkMapping wholechunkmapping = new WholeChunkMapping();
		wholechunkmapping.setChunkMd5(chunkMd5);
		wholechunkmapping.setWholeMd5(wholeMd5);
		wholechunkmapping.setIsUpload(isUpload);
		wholechunkmapping.setSerial(serial);
		wholechunkmapping.save();
	}
	/**
	 * 保存上传文件的相关信息
	 * @param wholeMd5
	 * @param name
	 * @param ext
	 * @param mimeType
	 */
	@Before(Tx.class)
	public String saveUploadFile(String wholeMd5, String name, String ext, String mimeType, long size){
		FileUpload fileUpload = new FileUpload();
		
		String fileId = UUIDHexGenerator.getId();
		
		fileUpload.setId(fileId);
		fileUpload.setWholeMd5(wholeMd5);
		fileUpload.setFileName(name);
		fileUpload.setFileExt(ext);
		fileUpload.setMimeType(mimeType);
		fileUpload.setSize(size);
		fileUpload.setCreateTime(DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS));
		fileUpload.save();
		
		FileMark fileMark = new FileMark();
		fileMark.setFileId(fileId);
		fileMark.setIsCut(true);
		fileMark.save();
		
		return fileId;
	}
	/**
	 * 保存上传的分块
	 * @param chunkMd5
	 * @param size
	 * @param pathInfo
	 * @param serial
	 * @param wholeMd5
	 */
	@Before(Tx.class)
	public void saveChunkFile(String chunkMd5, long size, String pathInfo, int serial, String wholeMd5){
		ChunkFile chunkFile = new ChunkFile();
		chunkFile.setChunkMd5(chunkMd5);
		chunkFile.setSize(size);
		chunkFile.setPathInfo(pathInfo);
		chunkFile.save();
		
		this.changeUpload(chunkMd5, wholeMd5, serial);
		
	}
	/**
	 * 查询分片上传的文件的信息
	 * @param wholeMd5
	 * @return
	 */
	public WholeFile findWholeFile(String wholeMd5){
		
		WholeFile wholeFile = WholeFile.dao.findById(wholeMd5);
		if (wholeFile != null){
			return wholeFile;
		}
		return null;
	}
	/**
	 * 查询文件的信息
	 * @param wholeMd5
	 * @return
	 */
	public FileUpload findFileUpload(String wholeMd5){
		String sql = " select * from file_upload where whole_md5 = ? ";
		FileUpload fileUpload = FileUpload.dao.findFirst(sql,wholeMd5);
		if (fileUpload != null){
			return fileUpload;
		}
		return null;
	}
	/**
	 * 查询分块是否存在
	 * @param chunkMd5
	 * @return
	 */
	public ChunkFile findChunkFile(String chunkMd5){
		ChunkFile chunkFile = ChunkFile.dao.findById(chunkMd5);
		if (chunkFile != null){
			return chunkFile;
		}
		return null;
	}
	/**
	 * 将分块的上传状态改为已上传
	 * @param chunkMd5
	 * @param wholeMd5
	 */
	public void changeUpload(String chunkMd5, String wholeMd5 ,int serial){
		String sql = " update whole_chunk_mapping set is_upload = '1' where serial = ? and whole_md5 = ? and chunk_md5 = ?";
		Db.update(sql, serial, wholeMd5, chunkMd5);
	}
	/**
	 * 查询文件未上传成功的分块的序列号和md5
	 * @param wholeMd5
	 * @return List<Record> Record:chunkMd5, serial
	 */
	public List<Record> findUnuploadChunk(String wholeMd5){
		String sql = " select chunk_md5 chunkMd5, serial from whole_chunk_mapping where whole_md5 = ? and is_upload = '0' order by serial asc ";
		return Db.find(sql, wholeMd5);
	}
	/**
	 * 保存整个文件上传的文件信息
	 * @param wholeMd5
	 * @param fileName
	 * @param fileExt
	 * @param mimeType
	 * @param size
	 * @param pathInfo
	 */
	@Before(Tx.class)
	public String saveUploadWholeFile(String fileName, String fileExt, String mimeType, long size, String pathInfo){
		UploadWholeFile uploadWholeFile = new UploadWholeFile();
		String fileId = UUIDHexGenerator.getId();
		uploadWholeFile.setId(fileId);
		uploadWholeFile.setFileName(fileName);
		uploadWholeFile.setFileExt(fileExt);
		uploadWholeFile.setMimeType(mimeType);
		uploadWholeFile.setSize(size);
		uploadWholeFile.setPathInfo(pathInfo);
		uploadWholeFile.setCreateTime(DateUtil.DateToString(new Date(), DateStyle.YYYY_MM_DD_HH_MM_SS));
		uploadWholeFile.save();
		
		FileMark fileMark = new FileMark();
		fileMark.setFileId(fileId);
		fileMark.setIsCut(false);
		fileMark.save();
		
		return fileId;
	}
	/**
	 * 查询文件是分片上传还是整片上传
	 * @param fileId
	 * @return
	 */
	public FileMark findFileMark(String fileId){
		FileMark fileMark = FileMark.dao.findById(fileId);
		return fileMark;
	}
	/**
	 * 查询整片上传的文件信息
	 * @param fileId
	 * @return path_info
	 */
	public UploadWholeFile findWholeUploadFile(String fileId){
//		String sql = " select * from upload_whole_file where id = ? ";
		return UploadWholeFile.dao.findById(fileId);
	}
	/**
	 * 查询文件分片上传的分片信息
	 * @param fileId
	 * @return
	 */
	public WholeFile findSliceUploadFile(String fileId){
		String sql = " select wf.* from file_upload fu inner join whole_file wf on fu.whole_md5 = wf.whole_md5 where fu.id = ? ";
		WholeFile wholeFile =  WholeFile.dao.findFirst(sql, fileId);
		if (wholeFile!=null){
			return wholeFile;
		}
		return null;
	}
	/**
	 * 查询分片上传文件相关信息
	 * @param fileId
	 * @return
	 */
	public FileUpload findSliceFileMsg(String fileId){
		String sql = " select * from file_upload where id = ? ";
		FileUpload fileMsg =  FileUpload.dao.findFirst(sql, fileId);
		if (fileMsg!=null){
			return fileMsg;
		}
		return null;
	}
	/**
	 * 查询分块信息
	 * @param wholeMd5
	 * @return
	 */
	public List<Record> findChunkMsg(String wholeMd5){
		String sql = " select chunk_md5,serial from whole_chunk_mapping where whole_md5 = ? order by serial asc ";
		return Db.find(sql, wholeMd5);
	}
	public ChunkFile findChunk(String wholeMd5){
		String sql = " select * from chunk_file where chunk_md5 = ? ";
		return ChunkFile.dao.findFirst(sql, wholeMd5);
	}
	/**
	 * 删除整片上传文件
	 * @param fileId
	 */
	public void deleteWholeUploadFile(String fileId,String filePath){
		String sql = " delete from upload_whole_file where id = ? ";
		File file = new File(filePath);
		file.delete();
		Db.update(sql, fileId);
	}
	/**
	 * 删除分片上传文件
	 * @param fileId
	 */
	public void deleteSliceUploadFile(String fileId) {
		String sql = " delete from file_upload where id = ? ";
		Db.update(sql, fileId);
	}
}
