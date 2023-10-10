package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaBigFileService;
import com.yujian.base.model.RestResponse;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MediaBigFileServiceImpl implements MediaBigFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    //存储视频文件
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Autowired
    private MediaFileServiceImpl mediaFileServiceImpl;

    //检查文件是否存在
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //先查询数据库时候存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null){
            //如果数据库中文件存在,则查询minio
            //获取桶
            String bucket = mediaFiles.getBucket();
            //获取object
            String filePath = mediaFiles.getFilePath();
            GetObjectArgs getObjectArgs = GetObjectArgs
                    .builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            //查询远程服务获取到一个流对象
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null){
                    //文件获取成功
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //文件未获取成功
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //根据 md5 得到分块文件所在目录的路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        //查询minio查找出该数据
        GetObjectArgs getObjectArgs = GetObjectArgs
                .builder()

                .bucket(bucket_video)
                .object(chunkFileFolderPath+chunkIndex)
                .build();

        //查询远程服务获取到一个流对象
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream != null){
                //文件获取成功
                return RestResponse.success(true);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        //文件未获取成功
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        //分块文件的路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        //获取mimeType
        String mimeType = mediaFileServiceImpl.getMimeType(null);
        boolean b = mediaFileServiceImpl.addMediaFilesToMinIo(bucket_video,localChunkFilePath,chunkFilePath,mimeType);
        if (!b){
            return RestResponse.validfail("分块文件上传失败");
        }
        return RestResponse.success(true);
    }

    //合并分块文件
    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //获取分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //获取源文件名称
        String fileName = uploadFileParamsDto.getFilename();
        //获取文件扩展名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        //获得合并后的objectname
        String objectName = getFilePathByMd5(fileMd5, extension);
        //找到所有的分块文件
        List<ComposeSource> sources = new ArrayList<>();

        for (int i = 0; i < chunkTotal; i++) {
            ComposeSource source = ComposeSource
                    .builder()
                    .bucket(bucket_video)
                    .object(chunkFileFolderPath+i)
                    .build();
            sources.add(source);
        }

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .sources(sources)
                .bucket(bucket_video)
                .object(objectName)
                .build();

        try {
            ObjectWriteResponse mergeFilePath = minioClient.composeObject(composeObjectArgs);
            log.debug("合并文件成功:{}",mergeFilePath);
        } catch (Exception e) {
            log.debug("合并文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "合并文件失败。");
        }

        //校验合并后的文件和源文件是否一致,一致才表示上传成功
        File file = downloadFileFromMinIO(bucket_video, objectName);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            //计算合并后的md5
            String md5 = DigestUtils.md5Hex(fileInputStream);
            //与原始文件的md5进行比较
            if (!md5.equals(fileMd5)){
                //表示上传失败
                return RestResponse.validfail(false, "合并文件失败。");
            }
            //文件下载成功,设置文件的大小
            uploadFileParamsDto.setFileSize(file.length());
        }catch(Exception e){
            log.debug("校验文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        }
        //此时说明文件上传成功,将文件入库
        MediaFiles mediaFiles = mediaFileServiceImpl.addMediaFilesToDB(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if (mediaFiles == null){
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        }
        //=====清除分块文件=====
        clearChunkFiles(chunkFileFolderPath,chunkTotal);
        return RestResponse.success(true);
    }

    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        Iterable<DeleteObject> objects = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                .collect(Collectors.toList());
        final RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(objects).build();
        final Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(r->{
            DeleteError deleteError = null;
            try {
                deleteError = r.get();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
            }
        });
    }

    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile= File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }
}
