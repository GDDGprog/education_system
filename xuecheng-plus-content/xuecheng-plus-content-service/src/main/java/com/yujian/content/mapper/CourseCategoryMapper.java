package com.yujian.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yujian.content.model.paramdto.CourseCategoryTreeDto;
import com.yujian.content.model.pojo.CourseCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author yujian
 */
@Mapper
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    //使用递归查询分类
    public List<CourseCategoryTreeDto> selectTreeNodes(String id);

    //查询每一个课程对应的一级分类
    public String selectCourseOneCategory(String id);

    //查询每一个课程对应的二级分类
    public String selectCourseTwoCategory(String id);

}
