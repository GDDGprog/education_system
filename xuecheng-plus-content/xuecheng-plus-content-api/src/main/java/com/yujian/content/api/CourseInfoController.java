package com.yujian.content.api;

import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.yujian.content.model.paramdto.QueryCourseParamDto;
import com.yujian.content.model.pojo.CourseBase;
import com.yujian.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
