package com.yujian.content.api;

import com.yujian.base.exception.ValidationGroups;
import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.yujian.content.model.paramdto.AddCourseDto;
import com.yujian.content.model.paramdto.CourseBaseInfoDto;
import com.yujian.content.model.paramdto.EditCourseDto;
import com.yujian.content.model.paramdto.QueryCourseParamDto;
import com.yujian.content.model.pojo.CourseBase;
import com.yujian.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 课程信息编辑接口
 */
// 相当于Controller + ResponseBody (用来响应json数据)
@RestController
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
public class CourseInfoController {

    @Autowired
    private CourseBaseService courseBaseService;

    @ApiOperation(value = "课程查询接口")
    @PostMapping("course/list")
    public PageResult<CourseBase> list(@Param("params") PageParams params,
                                       @RequestBody(required = false) @Param("queryCourseParamDto") QueryCourseParamDto queryCourseParamDto) {
        return courseBaseService.list(params,queryCourseParamDto);
    }


    @ApiOperation(value = "新增课程接口")
    @PostMapping("/course")
    //@Validated : 进行 参数校验
    public CourseBaseInfoDto createCourse(@RequestBody @Validated(ValidationGroups.Inster.class) AddCourseDto addCourseDto) {
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseService.createCourse(companyId,addCourseDto);
    }

    @ApiOperation(value="根据课程id查询课程基本信息")
    @GetMapping("/course/{id}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long id) {
        //进行id的校验
        return courseBaseService.getCourseBaseById(id);
    }

    @ApiOperation(value="修改课程接口")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto dto) {
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseService.updateCourseBase(companyId,dto);
    }

    @ApiOperation(value="删除课程接口")
    @DeleteMapping("/course/{id}")
    public void deleteCourseBase(@PathVariable Long id) {
        courseBaseService.deleteCourseBase(id);
    }

}
