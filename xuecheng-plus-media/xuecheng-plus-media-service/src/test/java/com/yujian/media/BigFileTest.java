package com.yujian.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 测试大文件上传
 */
public class BigFileTest {

    //分块测试
    @Test
    public void testChunk() throws IOException {

        //源文件
        File sourceFile = new File("D:\\斗鱼视频\\2-详解Class加载过程.mp4");
        //分块文件存储路径
        String chunkFilePath = "C:\\Desktop\\media\\chunk\\";
        //分块文件大小
        int chunkSize = 1024 * 1024 * 5;
        //分块文件个数
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 /chunkSize);
        //使用流从源文件读取数据,向分块文件中写数据
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        //缓冲区
        byte[] buffer = new byte[chunkSize];
        for (int i = 0; i < chunkNum; i++) {
            //分块文件
            File chunkFile = new File(chunkFilePath+i);
            //向分块文件中写数据
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len=raf_r.read(buffer))!=-1){
                raf_rw.write(buffer, 0, len);
                if (chunkFile.length() >= chunkSize){
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();
    }

    //测试文件合并方法
    @Test
    public void testMerge() throws IOException{
        //块文件目录
        File chunkFolder = new File("C:\\Desktop\\media\\chunk\\");
        //原始文件
        File originalFile = new File("D:\\斗鱼视频\\2-详解Class加载过程.mp4");
        //合并文件
        File mergeFile = new File("D:\\斗鱼视频\\2-详解Class加载过程_1.mp4");
        if (mergeFile.exists()) {
            mergeFile.delete();
        }

        //取出所有分块文件
        File[] files = chunkFolder.listFiles();
        //将数组转为list
        List<File> fileList = Arrays.asList(files);

        //对分块文件进行排序
        Collections.sort(fileList, (o1, o2) -> {
            return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
        });

        //向合并文件写入流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        //缓冲区
        byte[] buffer = new byte[1024];
        //遍历分块文件,向合并文件写入数据
        for (File file : fileList) {
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len=raf_r.read(buffer))!=-1){
                raf_rw.write(buffer, 0, len);
            }
            raf_r.close();
        }
        raf_rw.close();


        //合并文件完成后对合并的文件进行校验
        FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);
        FileInputStream fileInputStream_original = new FileInputStream(originalFile);

        String md5_merge = DigestUtils.md5Hex(fileInputStream_merge);
        String md5_original = DigestUtils.md5Hex(fileInputStream_original);
        if (!md5_merge.equals(md5_original)) {
            throw new RuntimeException("文件校验失败");
        }else{
            System.out.println("文件校验成功");
        }
    }
}
