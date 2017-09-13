package com.xgs.net.help;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class FtpUtil
{
    private static Logger log = Logger.getLogger( FtpUtil.class );

    /**
     * 获取FTP连接.
     * 
     * @param host
     * @param port
     * @param userName
     * @param password
     * @return
     * @throws Exception
     */
    public static FTPClient getFtpConnection( String host, int port,
        String userName, String password, int timeout )
    {
        log.info( "host:" + host + ", port:" + port );
        int reply;
        FTPClient ftpClient = new FTPClient();

        try
        {
            ftpClient.connect( host, port );

            reply = ftpClient.getReplyCode();

            if( !FTPReply.isPositiveCompletion( reply ) )
            {
                ftpClient.disconnect();
                log.info( "连接指定FTP服务器" + "host:" + host + ", port:" + port
                    + "失败" );
                return null;
            }

            boolean loginSuccess = ftpClient.login( userName, password );

            if( !loginSuccess )
            {
                ftpClient.disconnect();
                log.info( "登陆指定FTP服务器" + "host:" + host + ", port:" + port
                    + "失败, userName:" + userName );
                return null;
            }

            ftpClient.setDataTimeout( timeout ); // 设置传输超时时间为60秒
            ftpClient.setDefaultTimeout( timeout );
            // 告诉ftp server开通一个端口来传输数据
            ftpClient.enterLocalPassiveMode();
            // 设定二进制方式传输
            ftpClient.setFileType( FTPClient.BINARY_FILE_TYPE );
            ftpClient.setFileTransferMode( FTPClient.BINARY_FILE_TYPE );
        }
        catch ( Exception e )
        {
            // log.error( e );
            ftpClient = null;
        }

        return ftpClient;
    }

    /**
     * 关闭FTP连接
     * 
     * @param ftpClient
     */
    public static void closeFtpConnection( FTPClient ftpClient )
    {
        if( ftpClient != null )
        {

            try
            {
                boolean isLogOut = ftpClient.logout();
                if( isLogOut )
                {
                    log.info( "成功关闭ftp连接" );
                }
            }
            catch ( Exception e )
            {
                log.error( "关闭FTP服务器连接异常" );
            }
            finally
            {
                if( ftpClient.isConnected() )
                {
                    try
                    {
                        ftpClient.disconnect();
                        log.info( "成功关闭ftp连接" );
                    }
                    catch ( IOException e )
                    {
                        log.error( e );
                    }
                }
            }
        }
    }

    /**
     * FTP方式创建目录
     * 
     * @param ftp
     * @param path
     * @param newFileName
     * @param targetFile
     */
    public static void createDirFTP( FTPClient ftp, String path )
    {
        uploadFileFTP( ftp, path, null, null );
    }

    /**
     * FTP方式进入指定目录
     * 
     * @param ftp
     * @param path
     * @return
     */
    public static boolean gotoDirFTP( FTPClient ftp, String path )
    {
        boolean success = false;
        try
        {
            success = ftp.changeWorkingDirectory( path );
        }
        catch ( IOException e )
        {
            success = false;
            e.printStackTrace();
        }

        return success;
    }

    /**
     * 删除指定的文件
     * 
     * @param ftp
     * @param path
     * @param fileName
     * @return
     */
    public static boolean deleteFileFTP( FTPClient ftp, String ftpPath )
    {

        boolean success = false;

        if( ftp == null )
        {
            return success;
        }

        ftp.setControlEncoding( "GBK" );

        try
        {
            success = ftp.deleteFile( new String( ftpPath.getBytes( "GBK" ),
                "iso-8859-1" ) );

            if( !success )
            {
                String dir = new String( ftpPath.getBytes( "GBK" ),
                    "iso-8859-1" );
                // 删除文件夹
                success = ftp.removeDirectory( dir );

            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        log.info( "[FtpUtil] deleteFileFTP pathName:" + ftpPath + ", success:"
            + success );

        return success;
    }

    /**
     * FTP方式上传文件
     * 
     * @param ftp ftp连接
     * @param path 以“/”分隔的路径，不分操作系统，例如 ftp1/ftp2/ftp3,若不存在，将生成三个目录。
     * @param newFileName 新的命名，若为空，则取原始文件名
     * @param targetFile 需要上传的FTP文件
     * @return
     */
    public static boolean uploadFileFTP( FTPClient ftp, String path,
        String newFileName, File targetFile )
    {
        boolean exeFlag = false;

        if( ftp == null )
        {
            return exeFlag;
        }

        ftp.setControlEncoding( "GBK" );

        FileInputStream fileInput = null;
        BufferedInputStream bufInput = null;
        try
        {
            if( targetFile != null )
            {
                fileInput = new FileInputStream( targetFile );
                bufInput = new BufferedInputStream( fileInput );
            }

            // System.out.println( "path: " + paths.length );
            if( !ftp.changeWorkingDirectory( path ) )
            {
                if( path == null )
                {
                    path = "";
                }

                String splitStr = File.separator;
                
                if("\\".equals( splitStr ))
                {
                    splitStr = "\\\\";
                }
                
                String[] paths = split( path,  splitStr );
                String currentPath = null;

                // 检查所有路径信息，若不存在则生成对应路径
                FTPFile[] allFtpFile = ftp.listFiles();
                StringBuffer pathBuf = new StringBuffer();
                boolean showMode = true;
                boolean createMode = false;

                for ( int i = 0; i < paths.length; i++ )
                {
                    currentPath = paths[i];

                    // System.out.println( "dirs: " + allFtpFile.length );
                    FTPFile ftpFile = null;
                    if( showMode )
                    {
                        boolean dirExist = false;
                        for ( int j = 0; j < allFtpFile.length; j++ )
                        {
                            // System.out.println( "目录 : " +
                            // allFtpFile[j].getName()
                            // );
                            ftpFile = allFtpFile[j];

                            // System.out.println( ftpFile.getName() + "
                            // <<<<>>>"
                            // + currentPath );
                            if( ftpFile.getName().equals( currentPath ) )
                            {
                                // 已经存在路径，不生成文件夹，只转到下一步操作
                                // System.out.println( "!!!!!!!!!!!!!!" );
                                allFtpFile = ftp.listFiles( pathBuf.append(
                                    "/" + currentPath ).toString() );
                                ftp.changeWorkingDirectory( currentPath );
                                dirExist = true;
                                break;
                            }
                        }

                        // 不存在给定的目录,进入创建模式
                        if( !dirExist )
                        {
                            createMode = true;
                            showMode = false;
                        }

                    }

                    if( createMode )
                    {
                        // 生成对应文件夹
                        ftp.makeDirectory( new String( currentPath
                            .getBytes( "GBK" ), "iso-8859-1" ) );

                        // 转换到这个文件夹进行下一步操作
                        ftp.changeWorkingDirectory( currentPath );
                    }
                }
            }

            if( targetFile != null )
            {
                if( isStringNull( newFileName ) )
                {
                    newFileName = new String( ( targetFile.getName() )
                        .getBytes( "GBK" ), "iso-8859-1" );
                }

                ftp.storeFile( newFileName, bufInput );

                exeFlag = true;
            }

            ftp.changeWorkingDirectory( "/" );
        }
        catch ( Exception e )
        {
            log.error( e );

        }
        finally
        {
            try
            {
                if( bufInput != null )
                {
                    bufInput.close();
                }

                if( fileInput != null )
                {
                    fileInput.close();
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }

        }
        return exeFlag;
    }

    /**
     * 此方法防止JDK(1.4,1.5,1.6)同名方法内存泄露
     * 
     * @param target
     * @param beginIndex
     * @param endIndex
     * @return
     */
    public static String[] split( String target, String splitChar )
    {
        String[] tmp = target.split( splitChar );
        String[] reArray = new String[tmp.length];

        for ( int i = 0; i < tmp.length; i++ )
        {
            reArray[i] = new String( tmp[i] );
        }

        return reArray;
    }
    /**
     * String是否为空
     * 
     * @param input
     * @return
     */
    public static boolean isStringNull( String input )
    {
        if( null == input || input.trim().equals( "" ) )
        {
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
    	FTPClient client = FtpUtil.getFtpConnection("192.168.10.57", 21, "iuser", "iuser1234", 3000);
    	File file = new File("C:/Users/hasee/Desktop/FS14Demo/libwinpthread-1.dll");
    	
    	String _path = "xyz"+File.separator+"abc";
    	FtpUtil.uploadFileFTP(client,_path, file.getName(), file);
    	FTPFile[] files = client.listFiles();
    	for (FTPFile ftpFile : files) {
			System.out.println(ftpFile.getName()+":"+ftpFile.isDirectory());
		}
    	//client.listFiles();
	}
}
