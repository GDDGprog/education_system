package com.yujian.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yujian.base.exception.XueChengPlusException;
import com.yujian.content.mapper.CourseTeacherMapper;
import com.yujian.content.model.pojo.CourseTeacher;
import com.yujian.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    //查询教师信息
    @Override
    public List<CourseTeacher> list(Long course_id) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, course_id);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    //创建课程教师信息
    @Override
    public CourseTeacher saveAandUpdateTeaher(CourseTeacher courseTeacher) {
        //创建课程
        if (courseTeacher.getId() == null){
            CourseTeacher saveCourseTeacher = new CourseTeacher();
            BeanUtils.copyProperties(courseTeacher,saveCourseTeacher);
            saveCourseTeacher.setCreateDate(LocalDateTime.now());
            saveCourseTeacher.setCourseId(courseTeacher.getCourseId());
            int insert = courseTeacherMapper.insert(saveCourseTeacher);
            if (insert == 0){
                XueChengPlusException.cast("创建课程教师信息失败");
            }
            return courseTeacher;
        }else{ //修改
            int update = courseTeacherMapper.updateById(courseTeacher);
            if (update == 0) {
                XueChengPlusException.cast("修改教师信息失败");
            }
            return courseTeacher;
        }
    }

    //删除课程讲师信息
    @Override
    public void deleteTeacherInfo(Long course_id, Long id) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, course_id)
                .eq(CourseTeacher::getId, id);
        courseTeacherMapper.delete(queryWrapper);
    }
}
