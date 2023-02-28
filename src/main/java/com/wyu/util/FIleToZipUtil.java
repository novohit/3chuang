package com.wyu.util;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author novo
 * @date 2021/11/9-23:58
 */
public class FIleToZipUtil {
    /**
     * @param files    List<File> 作为参数传进来，就是把多个文件的路径放到一个list里面
     * @param response
     * @return
     * @throws Exception
     */
    public static HttpServletResponse downLoadFiles(List<File> files, HttpServletResponse response) throws Exception {

        try {
            // 临时文件夹 最好是放在服务器上，方法最后有删除临时文件的步骤
            String zipFilename = "/root/wyu_collect/file/temp/all.zip";
            File file = new File(zipFilename);
            file.createNewFile();
            if (!file.exists()) {
                file.createNewFile();
            }
            //response.reset();
            // response.getWriter()
            // 创建文件输出流
            FileOutputStream fous = new FileOutputStream(file);
            ZipOutputStream zipOut = new ZipOutputStream(fous);
            zipFile(files, zipOut);
            zipOut.close();
            fous.close();
            return downloadZip(file, response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        //return response;
    }

    /**
     * 把接受的全部文件打成压缩包
     *
     * @param files
     * @param
     */
    public static void zipFile(List files, ZipOutputStream outputStream) {
        int size = files.size();
        for (int i = 0; i < size; i++) {
            File file = (File) files.get(i);
            zipFile(file, outputStream);
        }
    }

    /**
     * 根据输入的文件与输出流对文件进行打包
     *
     * @param
     * @param
     */
    public static void zipFile(File inputFile, ZipOutputStream ouputStream) {
        try {
            if (inputFile.exists()) {
                if (inputFile.isFile()) {
                    FileInputStream IN = new FileInputStream(inputFile);
                    BufferedInputStream bins = new BufferedInputStream(IN, 512);
                    ZipEntry entry = new ZipEntry(inputFile.getName());
                    ouputStream.putNextEntry(entry);
                    // 向压缩文件中输出数据
                    int nNumber;
                    byte[] buffer = new byte[512];
                    while ((nNumber = bins.read(buffer)) != -1) {
                        ouputStream.write(buffer, 0, nNumber);
                    }
                    // 关闭创建的流对象
                    bins.close();
                    IN.close();
                } else {
                    try {
                        File[] files = inputFile.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            zipFile(files[i], ouputStream);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HttpServletResponse downloadZip(File file, HttpServletResponse response) {
        System.out.println(file);
        if (file.exists() == false) {
            System.out.println("待压缩的文件目录：" + file + "不存在.");
            return null;
        } else {
            try {
                // 以流的形式下载文件。
                InputStream fis = new BufferedInputStream(new FileInputStream(file.getPath()));
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                //!!! 坑 需要注释的否则会把允许跨域的response清空
                //response.reset();

                OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
                response.setContentType("application/octet-stream");

                // 如果输出的是中文名的文件，在此处就要用URLEncoder.encode方法进行处理
                response.setHeader("Content-Disposition",
                        "attachment;filename=" + new String(file.getName().getBytes("utf-8"), "ISO8859-1"));
                toClient.write(buffer);
                toClient.flush();
                toClient.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                   /* File f = new File(file.getPath());
                    f.delete();*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return response;
    }
}
