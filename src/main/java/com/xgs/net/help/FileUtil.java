package com.xgs.net.help;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang.StringUtils;

import com.jfinal.kit.PropKit;

public class FileUtil {
	
	public final static String FILEDIR = PropKit.use("cnf.txt").get("file.upload.dir");
	public static void sortFiles(File[] files) throws Exception {
		sortFiles(files, 0, false);
	}

	public static void sortFiles(File[] files, int type, boolean desc)
			throws Exception {
		final int fType = type;
		final boolean fDesc = desc;
		Arrays.sort(files, new Comparator<File>() {
			Collator collator = Collator.getInstance();

			public int compare(File f1, File f2) {
				switch (fType) {
				case 1:
					Long l1 = f1.isDirectory() ? -1 : f1.length();
					Long l2 = f2.isDirectory() ? -1 : f2.length();
					if (fDesc)
						return l2.compareTo(l1);
					return l1.compareTo(l2);
				case 2:
					CollationKey t1 = collator
							.getCollationKey(f1.isDirectory() ? "0" : "1"
									+ getFileType(f1).toLowerCase());
					CollationKey t2 = collator
							.getCollationKey(f2.isDirectory() ? "0" : "1"
									+ getFileType(f2).toLowerCase());
					if (fDesc)
						return t2.compareTo(t1);
					return t1.compareTo(t2);
				case 3:
					Long d1 = f1.lastModified();
					Long d2 = f2.lastModified();
					boolean b1 = f1.isDirectory(),
					b2 = f2.isDirectory();

					if (b1 && !b2)
						d1 = Long.MIN_VALUE;
					if (b2 && !b1)
						d2 = Long.MIN_VALUE;
					if (fDesc)
						return d2.compareTo(d1);
					return d1.compareTo(d2);
				default:
					CollationKey k1 = collator.getCollationKey((f1
							.isDirectory() ? 0 : 1)
							+ f1.getName().toLowerCase());
					CollationKey k2 = collator.getCollationKey((f2
							.isDirectory() ? 0 : 1)
							+ f2.getName().toLowerCase());
					if (fDesc)
						return k2.compareTo(k1);
					return k1.compareTo(k2);
				}
			}
		});
	}

	public static boolean deleteFolder(File folder) {
		File files[] = folder.listFiles();
		for (File file : files) {
			if (file.isDirectory())
				deleteFolder(file);
			else if (!file.delete())
				return false;
		}
		if (!folder.delete())
			return false;
		return true;
	}

	public static File getUniqueFile(File file) {
		if (file.exists()) {
			String path, dir, name, ext;
			int i;

			path = file.getAbsolutePath();
			dir = extractDir(path);
			name = extractFilename(path);
			i = name.lastIndexOf('.');
			if (i > -1 && i < name.length() - 1) {
				ext = name.substring(i);
				name = name.substring(0, i);
			} else
				ext = "";
			i = 1;
			do {
				file = new File(dir, name + i + ext);
				i++;
			} while (file.exists());
		}
		return file;
	}
	
	
	public static File[] getFiles(String path){
		
		File f = new File(path);
		if(f != null && f.isDirectory()){
			return f.listFiles();
		}
		
		return null;
	}

	public static void copyFile(File source, File dest, boolean autoRename,
			boolean isCut) throws Exception {
		boolean exists = dest.exists();
		if (autoRename && exists) {
			dest = getUniqueFile(dest);
			exists = false;
		}
		if (!exists)
			dest.createNewFile();
		FileChannel in = null;
		FileChannel out = null;
		try {
			in = new FileInputStream(source).getChannel();
			out = new FileOutputStream(dest).getChannel();
			in.transferTo(0, in.size(), out);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Throwable t) {
				}
			}
			if (out != null)
				out.close();
		}
		dest.setLastModified(source.lastModified());
		if (isCut)
			source.delete();
	}

	public static void copyFolder(File source, File dest, boolean autoRename,
			boolean isCut) throws Exception {
		File files[], destFile, destDir;
		String name;
		boolean exists = dest.exists();

		if (autoRename && exists) {
			dest = getUniqueFile(dest);
			exists = false;
		}
		if (!exists)
			dest.mkdirs();
		destDir = dest;
		files = source.listFiles();
		for (File file : files) {
			name = file.getName();
			destFile = new File(destDir, name);
			if (file.isFile())
				copyFile(file, destFile, autoRename, isCut);
			else
				copyFolder(file, destFile, autoRename, isCut);
		}
		if (isCut)
			source.delete();
	}

	public static String readText(File file, String charset) throws Exception {
		FileInputStream fs = new FileInputStream(file);
		try {
			int size = (int) file.length();
			byte[] bs = new byte[size];
			fs.read(bs);
			if (StringUtils.isEmpty(charset))
				return new String(bs);
			else
				return new String(bs, charset);
		} finally {
			fs.close();
		}
	}

//	public static String readText(File file) throws Exception {
//		return readText(file, Var.get("server.charset"));
//	}

	public static String readUtfText(File file) throws Exception {
		return readText(file, "utf-8");
	}

	public static void writeText(File file, String content, String charset)
			throws Exception {
		if (!file.exists())
			file.createNewFile();
		FileOutputStream os = new FileOutputStream(file);

		try {
			if (StringUtils.isEmpty(charset))
				os.write(content.getBytes());
			else
				os.write(content.getBytes(charset));
		} finally {
			os.close();
		}
	}


	public static void writeUtfText(File file, String content) throws Exception {
		writeText(file, content, "utf-8");
	}

//	public static void saveStream(InputStream is, File file) throws Exception {
//		FileOutputStream os = new FileOutputStream(file);
//		try {
//			SysUtil.isToOs(is, os);
//		} finally {
//			os.close();
//		}
//	}

	public static String extractFileExt(String fileName) {
		if (!StringUtils.isEmpty(fileName)) {
			int i = fileName.lastIndexOf('.');
			if (i != -1)
				return fileName.substring(i + 1);
		}
		return "";
	}

	public static String getFileType(File file) {
		String type;

		try {
			type = FileSystemView.getFileSystemView().getSystemTypeDescription(
					file);
		} catch (Throwable e) {
			type = null;
		}
		if (StringUtils.isEmpty(type))
			return FileUtil.extractFileExt(file.getName());
		else
			return type;
	}

	public static String extractFilename(String fileName) {
		if (StringUtils.isEmpty(fileName))
			return "";
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (p == -1)
			return fileName;
		else
			return fileName.substring(p + 1);
	}

	public static String extractFilenameNoExt(String fileName) {
		String s = extractFilename(fileName);
		int i = s.lastIndexOf('.');
		if (i != -1)
			return s.substring(0, i);
		else
			return s;
	}

	public static String getPath(String path) {
		return StringUtils.replace(path, "\\", "/");
	}

	public static String getPath(File file) {
		return getPath(file.getAbsolutePath());
	}

	public static String extractDir(String fileName) {
		if (StringUtils.isEmpty(fileName))
			return "";
		int pos;

		fileName = getPath(fileName);
		pos = fileName.lastIndexOf('/');
		if (pos == -1)
			return fileName;
		else
			return fileName.substring(0, pos);
	}

	public static boolean isAncestor(File parent, File child) {
		String p = getPath(parent) + "/", c = getPath(child) + "/";

		return c.length() > p.length()
				&& c.substring(0, p.length()).equalsIgnoreCase(p);
	}

	public static boolean hasSubFile(File file, boolean isDir) {
		File[] files = file.listFiles();

		if (files == null)
			return false;
		for (File f : files)
			if (!isDir || f.isDirectory())
				return true;
		return false;
	}
	
	/**
	 * 写入数据
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void write(InputStream in, OutputStream out)
			throws IOException {
		try {
			byte[] buffer = new byte[1024 * 1000];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			out.flush();
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
			}
			try {
				out.close();
			} catch (IOException ex) {
			}
		}
	}
	
	public static void download(String downloadfFileName,
			ServletOutputStream out) {
		try {
			FileInputStream in = new FileInputStream(new File(FILEDIR + "/"
					+ downloadfFileName));
			write(in, out);
		} catch (FileNotFoundException e) {
			try {
				FileInputStream in = new FileInputStream(new File(FILEDIR
						+ "/"
						+ new String(downloadfFileName.getBytes("iso-8859-1"),
								"utf-8")));
				write(in, out);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String initFilePath(String name) {
		String dir = getFileDir(name) + "";
		File file = new File(FILEDIR  + dir);
		if (!file.exists()) {
			file.mkdir();
		}
		Long num = new Date().getTime();
		Double d = Math.random() * num;
		return (file.getPath() + "/" + num + d.longValue() + "_" + name)
				.replaceAll(" ", "-");
	}
	
	private static int getFileDir(String name) {
		return name.hashCode() & 0xf;
	}
	
	public static String getFilesInfo(File[] files, boolean check) {
		StringBuilder buf = new StringBuilder();
		boolean isFirst = true, isDir;
		String name, dir;

		buf.append('[');
		for (File file : files) {
			isDir = file.isDirectory();
			name = file.getName();
			dir = FileUtil.getPath(file);
			if (StringUtils.isEmpty(name))
				name = FileUtil.extractDir(dir);
			if (isFirst)
				isFirst = false;
			else
				buf.append(',');
			name = file.getName();
			buf.append("{text:\"");
			buf.append(name);
			buf.append("\",dir:\"");
			buf.append(dir);
			buf.append("\"");
			buf.append(",isDir:");
			buf.append(isDir);
			if (!FileUtil.hasSubFile(file, false)) {
				if (isDir)
					buf.append(",children:[]");
				else {
					if (check)
						buf.append(",checked:false");
					buf.append(",leaf:true,iconCls:\"default_icon\"");
				}
			}
			buf.append('}');
		}
		buf.append(']');
		return buf.toString();
	}
	
	public static final String getExtension(String fileName){
		String[] names = fileName.split("\\.");
		if(names.length ==1) return "";
		return names[names.length-1];
	}
}