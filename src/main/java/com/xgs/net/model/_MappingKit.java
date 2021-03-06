package com.xgs.net.model;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * Generated by JFinal, do not modify this file.
 * <pre>
 * Example:
 * public void configPlugin(Plugins me) {
 *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);
 *     _MappingKit.mapping(arp);
 *     me.add(arp);
 * }
 * </pre>
 */
public class _MappingKit {

	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("chunk_file", "chunk_md5", ChunkFile.class);
		arp.addMapping("file_info", "id", FileInfo.class);
		arp.addMapping("file_mark", "file_id", FileMark.class);
		arp.addMapping("file_upload", "id", FileUpload.class);
		arp.addMapping("upload_whole_file", "id", UploadWholeFile.class);
		// Composite Primary Key order: chunk_md5,whole_md5
		arp.addMapping("whole_chunk_mapping", "chunk_md5,whole_md5", WholeChunkMapping.class);
		arp.addMapping("whole_file", "whole_md5", WholeFile.class);
	}
}

