package com.yujian.content.api;

import com.yujian.content.model.pojo.CourseTeacher;
import com.yujian.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "师资信息管理接口",tags = "师资信息管理接口")
@RestController
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("讲师信息查询")
    @GetMapping("/courseTeacher/list/{course_id}")
    public List<CourseTeacher> list(@PathVariable Long course_id) {
        return courseTeacherService.list(course_id);
    }

    @ApiOperation("讲师信息保存")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveAandUpdateTeaher(@RequestBody @Validated CourseTeacher courseTeacher) {
        return courseTeacherService.saveAandUpdateTeaher(courseTeacher);
    }

    @ApiOperation("讲师信息删除")
    @DeleteMapping("/courseTeacher/course/{course_id}/{id}")
    public void delete(@PathVariable Long course_id,@PathVariable Long id) {
        courseTeacherService.deleteTeacherInfo(course_id,id);
    }
}
