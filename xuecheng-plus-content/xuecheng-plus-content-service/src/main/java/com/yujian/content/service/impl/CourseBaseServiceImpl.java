package com.yujian.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.yujian.content.mapper.CourseBaseMapper;
import com.yujian.content.model.paramdto.QueryCourseParamDto;
import com.yujian.content.model.pojo.CourseBase;
import com.yujian.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CourseBaseServiceImpl implements CourseBaseService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Override
    public PageResult<CourseBase> list(PageParams params, QueryCourseParamDto queryCourseParamDto) {
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamDto.getAuditStatus());
        //课程名称
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamDto.getCourseName());
        //发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamDto.getPublishStatus()),
                CourseBase::getStatus,
                queryCourseParamDto.getPublishStatus());

        //进行分页操作
        Long page = params.getPageNo();
        Long size = params.getPageSize();
        Page<CourseBase> courseBasePage = new Page<>(page, size);

        //进行分页查询
        Page<CourseBase> selectPage = courseBaseMapper.selectPage(courseBasePage, queryWrapper);

        //进行结果封装
        List<CourseBase> items = selectPage.getRecords();
        //获取数量总数目
        Long counts = courseBasePage.getTotal();
        PageResult<CourseBase> pageResult = new PageResult<CourseBase>(items, counts, page, size);
        return pageResult;
    }
}
