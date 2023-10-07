package com.yujian.content.service;

import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.yujian.content.model.paramdto.AddCourseDto;
import com.yujian.content.model.paramdto.CourseBaseInfoDto;
import com.yujian.content.model.paramdto.EditCourseDto;
import com.yujian.content.model.paramdto.QueryCourseParamDto;
import com.yujian.content.model.pojo.CourseBase;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 课程信息管理接口
 */
public interface CourseBaseService {
    /**
     * 分页查询课程信息
     * @param params 分页参数
     * @param queryCourseParamDto 查询条件
     * @return 课程信息
     */
    PageResult<CourseBase> list(PageParams params, QueryCourseParamDto queryCourseParamDto);

    /**
     *添加课程基本信息
     * @param companyId 教学机构id
     * @param addCourseDto 添加的课程信息
     * @return 课程信息
     */
    CourseBaseInfoDto createCourse(Long companyId,AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程基本信息
     * @param id 课程id
     * @return 返回 CourseBaseInfoDto 课程信息
     */
    CourseBaseInfoDto getCourseBaseById(Long id);

    /**
     * 修改课程基本信息
     * @param companyId 教学机构id
     * @param dto 修改的课程信息
     * @return 课程信息
     */
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto);

    /**
     * 根据课程id删除课程信息
     * (包括
     * 基本信息 ,
     * 营销信息 ,
     * 课程计划(章节的删除)
     * 课程教师信息)
     * @param id 课程id
     */
    void deleteCourseBase(Long id);
}
