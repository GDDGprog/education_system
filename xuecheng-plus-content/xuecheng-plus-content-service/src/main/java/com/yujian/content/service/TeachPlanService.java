package com.yujian.content.service;

import com.yujian.content.model.paramdto.SaveTeachplanDto;
import com.yujian.content.model.paramdto.TeachplanDto;

import java.util.List;

/**
 * 课程计划管理相关接口
 */
public interface TeachPlanService {
    /**
     *根据课程id查询课程计划
     * @param courseId 课程id
     * @return 课程计划
     */
    List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 新增/修改/保存 课程计划
     * @param teachplan 课程计划信息
     */
    void saveTeachplan(SaveTeachplanDto teachplan);

    /**
     * 根据课程计划对应的id删除对应的章节
     * @param id 课程计划id
     */
    void deleteTeachplan(Long id);

    /**
     * 向上/向下移动小节
     * @param move_type 移动类型 :  movedown和moveup
     * @param id : 课程计划id
     */
    void move(String move_type, Long id);
}
