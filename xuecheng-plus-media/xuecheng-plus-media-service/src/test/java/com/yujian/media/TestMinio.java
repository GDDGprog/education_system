package com.yujian.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;

public class TestMinio {

    //创建minio客户端
    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.200.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    //上传文件到minio中
    @Test
    void test_upload() throws Exception {
        //上传文件的参数信息

        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".jpg");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; //通用mimeType,字节流

        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }

        UploadObjectArgs agrs = UploadObjectArgs.builder()
                .bucket("testbuckets") //桶名
                .filename("C:\\Desktop\\file\\photo\\yujian.jpg") //文件名
                //.object("yujian.jpg") //对象名 在桶下放置文件
                .object("photo/yujian.jpg") //对象名 放在子目录下
                .contentType(mimeType)
                .build();
        //上传文件
        minioClient.uploadObject(agrs);
    }

    //测试删除minio中的文件
    @Test
    void test_delete() throws Exception{
        // RemoveObjectArgs
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("testbuckets")
                .object("photo/yujian.jpg")
                .build();

        //删除文件
        minioClient.removeObject(removeObjectArgs);
    }

    //从minio中下载文件
    @Test
    void test_getFile() throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbuckets")
                .object("photo/yujian.jpg")
                .build();
        FilterInputStream object = minioClient.getObject(getObjectArgs);
        //指定输出流
        FileOutputStream stream = new FileOutputStream(new File("C:\\Desktop\\file\\photo\\yujian123.jpg"));
        IOUtils.copy(object, stream);

        //校验文件的完整性对文件的内容进行md5处理
        FileInputStream fileInputStream = new FileInputStream(new File("C:\\Desktop\\file\\photo\\yujian.jpg"));
        String source_md5 = DigestUtils.md5DigestAsHex(fileInputStream);
        FileInputStream fileInputStream1 = new FileInputStream(new File("C:\\Desktop\\file\\photo\\yujian123.jpg"));
        String target_md5 = DigestUtils.md5DigestAsHex(fileInputStream1);
        if (source_md5.equals(target_md5)) {
            System.out.println("文件完整");
        }
    }
}
