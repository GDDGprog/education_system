package com.yujian.content;

import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.yujian.content.model.paramdto.CourseCategoryTreeDto;
import com.yujian.content.model.paramdto.QueryCourseParamDto;
import com.yujian.content.model.pojo.CourseBase;
import com.yujian.content.service.CourseBaseService;
import com.yujian.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseCategoryServiceTests {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    public void TestSelectInfoService() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
