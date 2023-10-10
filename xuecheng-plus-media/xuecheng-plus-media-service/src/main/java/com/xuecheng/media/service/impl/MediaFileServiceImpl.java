package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.yujian.base.exception.XueChengPlusException;
import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient; // minio客户端

    //存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    //根据扩展名获取mimeType
    public String getMimeType(String extension) {
        if (extension == null){
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; //通用mimeType,字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 将文件写入minIO
     * @param bucket_name 桶名
     * @param localFilePath 本地文件路径
     * @param objectName 对象名
     * @param mimeType mimeType
     * @return 是否写入成功
     */
    public boolean addMediaFilesToMinIo(String bucket_name,String localFilePath,String objectName,String mimeType){
        try {
            UploadObjectArgs agrs = UploadObjectArgs.builder()
                    .bucket(bucket_name) //桶名
                    .filename(localFilePath) //文件名
                    //.object("yujian.jpg") //对象名 在桶下放置文件
                    .object(objectName) //对象名 放在子目录下
                    .contentType(mimeType)
                    .build();
            //上传文件
            minioClient.uploadObject(agrs);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket_name,objectName);
            System.out.println("上传成功");
            return true;
        } catch (Exception e) {
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket_name,objectName,e.getMessage(),e);
            XueChengPlusException.cast("上传文件到文件系统失败");
        }
        return false;
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/")+"/";
        return folder;
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //上传文件信息
    //@Transactional //代码中有网络请求的方法,不要使用该注解
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {

        //获取扩展名
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));

        //根据扩展名取出mimeType
        String mimeType = getMimeType(extension);

        //获取桶名
        String bucket_name = bucket_mediafiles;

        //获取文件的md5
        String fileMd5 = getFileMd5(new File(localFilePath));

        //获取对象名(根据时间目录来进行文件的存储)
        String defaultFolderPath = getDefaultFolderPath();
        String objectName = defaultFolderPath+fileMd5+extension;

        //将文件写入minIO
        boolean result = addMediaFilesToMinIo(bucket_name, localFilePath, objectName, mimeType);
        if (!result){
            XueChengPlusException.cast("上传文件到文件系统失败");
        }
        MediaFiles mediaFiles = addMediaFilesToDB(companyId, fileMd5, uploadFileParamsDto, bucket_name, objectName);

        //将文件信息进行返回
        if (mediaFiles == null){
            XueChengPlusException.cast("上传文件失败");
        }
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
        return uploadFileResultDto;
    }

    @Transactional
    public MediaFiles addMediaFilesToDB(Long compareId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName) {
        //从数据库中查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null){
            mediaFiles = new MediaFiles();
            //先将uploadFileParamsDto中的信息拷贝到mediaFiles中
            BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
            //文件id
            mediaFiles.setId(fileMd5);
            //机构id
            mediaFiles.setCompanyId(compareId);
            //桶
            mediaFiles.setBucket(bucket);
            //file_path (存储的就是objectName)
            mediaFiles.setFilePath(objectName);
            //file_id (文件的md5)
            mediaFiles.setFileId(fileMd5);
            //url
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            //上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            //审核状态
            mediaFiles.setAuditStatus("002003");
            //状态
            mediaFiles.setStatus("1");
        }
        //将数据插入数据库
        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert <= 0) {
            log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
            XueChengPlusException.cast("保存文件信息失败");
        }
        log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());
        return mediaFiles;
    }
}
