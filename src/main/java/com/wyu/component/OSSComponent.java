package com.wyu.component;

import com.aliyun.oss.*;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author novo
 * @since 2023-02-27 22:21
 */
@Component
@Slf4j
public class OSSComponent {

    @Autowired
    private OSSProperties ossProperties;

    public String upload(MultipartFile file, String fileName) {
        if (file.isEmpty()) {
            log.info("文件为空");
            return "";
        }
        String endpoint = this.ossProperties.getEndPoint();
        String accessKeyId = this.ossProperties.getAccessKeyId();
        String accessKeySecret = this.ossProperties.getAccessKeySecret();
        String bucketName = this.ossProperties.getBucketName();

        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null : "filename is null";

        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // java8日期
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String folder = formatter.format(now);

        String finalName = "3chuang" + "/" + folder + "/" + fileName;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        String imageUrl = "";
        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, finalName, file.getInputStream());

            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);

            // 设置该属性可以返回response。如果不设置，则返回的response为空。
            putObjectRequest.setProcess("true");
            // 上传字符串。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            // 如果上传成功，则返回200。
            if (result.getResponse().getStatusCode() == HttpStatus.OK.value()) {
                //imageUrl = result.getResponse().getUri();
                imageUrl = String.format("http://%s.%s/%s", bucketName, endpoint, finalName);
                log.info("文件上传成功 url:[{}]", imageUrl);
            } else {
                log.info("文件上传失败:[{}]", result.getResponse());
            }
        } catch (OSSException oe) {
            log.error("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            log.error("Error Message:" + ce.getMessage());
        } catch (IOException e) {
            log.error("Error Message:" + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return imageUrl;
    }


    public void getFileToZip(List<String> fileNames, HttpServletResponse response) {
        String endpoint = this.ossProperties.getEndPoint();
        String accessKeyId = this.ossProperties.getAccessKeyId();
        String accessKeySecret = this.ossProperties.getAccessKeySecret();
        String bucketName = this.ossProperties.getBucketName();
        try {
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            //压缩包名称
            String zipName = "test.zip";
            //创建临时文件
            File zipFile = File.createTempFile("test", ".zip");
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
            log.info("开始压缩");
            /**
             * 作用是为任何outputstream产生校验和
             * 第一个参数是制定产生校验和的输出流，第二个参数是指定checksum类型（Adler32（较快）和CRC32两种）
             */
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new Adler32());
            //用于将数据压缩成zip文件格式
            ZipOutputStream zos = new ZipOutputStream(cos);

            for (String fileName : fileNames) {
                //获取object，返回结果ossObject对象
                OSSObject ossObject = ossClient.getObject(bucketName, fileName);
                //读取object内容，返回
                InputStream inputStream = ossObject.getObjectContent();
                // 对于每一个要被存放到压缩包的文件，都必须调用ZipOutputStream对象的putNextEntry()方法，确保压缩包里面文件不同名
                String fileN = fileName.substring(fileName.lastIndexOf("/") + 1);
                // 直接传fileName 会在压缩包里创建文件夹3chuang/2023/02/28/
                zos.putNextEntry(new ZipEntry(fileN));
                int bytesRead = 0;
                // 向压缩文件中输出数据
                while ((bytesRead = inputStream.read()) != -1) {
                    zos.write(bytesRead);
                }
                inputStream.close();
                zos.closeEntry(); // 当前文件写完，定位为写入下一条项目
            }

            zos.close();
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + zipName);
            FileInputStream fis = new FileInputStream(zipFile);
            BufferedInputStream buff = new BufferedInputStream(fis);
            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            byte[] car = new byte[1024];
            int l = 0;
            while (l < zipFile.length()) {
                int j = buff.read(car, 0, 1024);
                l += j;
                out.write(car, 0, j);
            }
            // 关闭流
            fis.close();
            buff.close();
            out.close();

            ossClient.shutdown();
            // 删除临时文件
            zipFile.delete();
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
