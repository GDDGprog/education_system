package com.yujian.content.api;

import com.yujian.content.model.paramdto.CourseCategoryTreeDto;
import com.yujian.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程分类相关接口
 */
@RestController
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        return courseCategoryService.queryTreeNodes("1");
    }
}
