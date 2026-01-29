package com.design.artwork.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
public class OssUtil {

    // 从 application.properties 读取配置
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    /**
     * 上传文件到 OSS
     * @param file 前端传来的文件
     * @return 文件的完整访问 URL
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // 1. 生成唯一文件名 (防止覆盖)
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + suffix;

        // 2. 创建 OSS 客户端
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            // 3. 上传到阿里云
            // 第一个参数是 bucketName，第二个是文件名，第三个是文件流
            ossClient.putObject(bucketName, newFileName, inputStream);

            // 4. 拼接返回的 URL
            // 格式通常是: https://BucketName.Endpoint/FileName
            String url = "https://" + bucketName + "." + endpoint + "/" + newFileName;
            return url;

        } finally {
            // 5. 关闭客户端 (必须关闭，否则会内存泄漏)
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 从 OSS 删除文件
     * @param fileUrl 文件的完整 URL
     */
    public void deleteFile(String fileUrl) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 从 URL 中解析出文件名 (Object Name)
            // URL 格式: https://bucket.endpoint/objectName
            // 我们只需要最后那一段 objectName
            String objectName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            ossClient.deleteObject(bucketName, objectName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}