package com.yujian.content.service;

import com.yujian.content.model.pojo.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {
    /**
     * 查询课程对应的讲师信息
     * @param course_id 课程id
     * @return 对应讲师集合
     */
    List<CourseTeacher> list(Long course_id);

    /**
     * 创建/修改 课程教师信息
     * @param courseTeacher 课程教师信息
     * @return 课程教师信息
     */
    CourseTeacher saveAandUpdateTeaher(CourseTeacher courseTeacher);

    /**
     * 删除讲师信息
     * @param course_id 课程id
     * @param id 讲师id
     */
    void deleteTeacherInfo(Long course_id, Long id);
}
