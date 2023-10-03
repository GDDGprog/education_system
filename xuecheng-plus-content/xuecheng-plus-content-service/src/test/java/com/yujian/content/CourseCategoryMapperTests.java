package com.yujian.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.yujian.content.mapper.CourseBaseMapper;
import com.yujian.content.mapper.CourseCategoryMapper;
import com.yujian.content.model.paramdto.CourseCategoryTreeDto;
import com.yujian.content.model.paramdto.QueryCourseParamDto;
import com.yujian.content.model.pojo.CourseBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseCategoryMapperTests {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;


    @Test
    public void TestSelectCategoryMapper() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
