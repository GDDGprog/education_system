package com.yujian.content.service;

import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.yujian.content.model.paramdto.CourseCategoryTreeDto;
import com.yujian.content.model.paramdto.QueryCourseParamDto;
import com.yujian.content.model.pojo.CourseBase;

import java.util.List;

/**
 * 课程分类管理接口
 */
public interface CourseCategoryService {

    /**
     * 查询课程分类
     * @return 课程分类的树图
     */
    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
