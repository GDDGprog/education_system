package com.yujian.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.yujian.content.mapper.CourseBaseMapper;
import com.yujian.content.model.paramdto.QueryCourseParamDto;
import com.yujian.content.model.pojo.CourseBase;
import com.yujian.content.service.CourseBaseService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseBaseServiceTests {

    @Autowired
    private CourseBaseService courseBaseService;

    @Test
    public void TestSelectInfoService() {
        PageParams pageParams = new PageParams(2L, 3L);
        QueryCourseParamDto queryCourseParamDto = new QueryCourseParamDto();
        queryCourseParamDto.setCourseName("java"); //课程名称查询条件
        //queryCourseParamDto.setAuditStatus("202004"); //课程审核状态(202004表示审核通过)
        //queryCourseParamDto.setPublishStatus("202003"); //课程发布状态
        PageResult<CourseBase> list = courseBaseService.list(pageParams, queryCourseParamDto);
        System.out.println(list);
    }
}
