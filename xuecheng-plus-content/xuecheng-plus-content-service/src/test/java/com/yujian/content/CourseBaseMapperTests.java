package com.yujian.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.yujian.content.mapper.CourseBaseMapper;
import com.yujian.content.model.paramdto.QueryCourseParamDto;
import com.yujian.content.model.pojo.CourseBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseBaseMapperTests {

    @Autowired
    private CourseBaseMapper courseBaseMapper;


    @Test
    public void TestSelectInfoMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(18);
        Assertions.assertNotNull(courseBase);

        //详细进行分页查询的单元测试
        //查询条件
        QueryCourseParamDto queryCourseParamDto = new QueryCourseParamDto();
        queryCourseParamDto.setCourseName("java"); //课程名称查询条件
        queryCourseParamDto.setAuditStatus("202003");
        //拼接查询结果
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //根据名称模糊查询,在sql中拼接 course_base.name like '%名称%'
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamDto.getCourseName()), CourseBase::getName,
                queryCourseParamDto.getCourseName());
        //根据课程审核状态,在sql中拼接 course_base.audit_status like '%名称%'
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamDto.getAuditStatus()), CourseBase::getAuditStatus,
                queryCourseParamDto.getAuditStatus());

        //创建page分页参数对象
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(3L);
        /**
         * 参数说明: 当前页,每页显示条数
         */
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //开始分页查询
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, queryWrapper);

        PageResult<CourseBase> courseBasePageResult = new PageResult<>(courseBasePage.getRecords(),
                                                                        page.getTotal(),
                                                                        page.getCurrent(),
                                                                        page.getSize());
        System.out.println(courseBasePageResult);

    }
}
