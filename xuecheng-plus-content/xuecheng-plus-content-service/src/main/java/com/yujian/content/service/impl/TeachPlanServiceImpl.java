package com.yujian.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yujian.base.exception.XueChengPlusException;
import com.yujian.content.mapper.TeachplanMapper;
import com.yujian.content.model.paramdto.SaveTeachplanDto;
import com.yujian.content.model.paramdto.TeachplanDto;
import com.yujian.content.model.pojo.Teachplan;
import com.yujian.content.service.TeachPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    private TeachplanMapper teachplanMapper;
    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //通过课程计划id判断是新增和修改
        Long teachplanId = saveTeachplanDto.getId();
        if (teachplanId != null) {
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            //将参数复制到teachplan对象中
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        } else {
            //添加
            //取出同父同级别的课程计划数量
            int count = getTeachplanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            Teachplan teachplan = new Teachplan();
            //设置排序号
            teachplan.setOrderby(count+1);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.insert(teachplan);
        }
    }

    //根据课程计划id删除课程计划信息
    @Override
    public void deleteTeachplan(Long id) {
        //根据id获取课程计划信息
        Teachplan teachplan = teachplanMapper.selectById(id);
        //判断是大章节还是小章节
        Long parentid = teachplan.getParentid();
        if (parentid == 0) {
            //删除大章节
            //判断大章节下是否存在小章节
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,id);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if (count > 0) {
                //存在小章节
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作删除","120409");
            }else{
                //删除大章节
                teachplanMapper.deleteById(id);
            }
        }else{
            //删除小章节
            teachplanMapper.deleteById(id);
        }
    }

    //向上/向下移动小节
    @Override
    public void move(String move_type, Long id) {
        //判断是大章节还是小章节
        Long parentid = teachplanMapper.selectById(id).getParentid();
        if (parentid == 0) { //大章节
            //根据id获取课程计划信息
            Teachplan teachplan = teachplanMapper.selectById(id);
            // 获取该课程的课程id
            Long courseId = teachplan.getCourseId(); //courseid = 1
            //获取该课程计划的排序编号
            Integer orderby = teachplan.getOrderby(); //orderby = 2
            //判断是上移还是下移
            if (move_type.equals("moveup")) { //上移
                //判断是否是顶层
                if (orderby == 1) {
                    XueChengPlusException.cast("已经是顶层，无法向上移动","120410");
                }
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                //查询条件 parentid = 0 and course_id = 1 and orderby-1 = 1
                queryWrapper.eq(Teachplan::getParentid, teachplan.getParentid())
                        .eq(Teachplan::getCourseId, courseId)
                        .eq(Teachplan::getOrderby, orderby-1);
                //查询替换位置的课程计划的信息
                Teachplan move_teachPlan = teachplanMapper.selectOne(queryWrapper);
                //将替换位置的课程计划的排序编号+1
                move_teachPlan.setOrderby(move_teachPlan.getOrderby() + 1);
                //将替换位置的课程计划的信息更新
                teachplanMapper.updateById(move_teachPlan);
                //将课程计划的orderby-1
                teachplan.setOrderby(orderby-1);
                //将课程计划的信息更新
                teachplanMapper.updateById(teachplan);
            }else if (move_type.equals("movedown")){ //下移
                LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
                //查询条件 parentid = 0 and course_id = 1 and orderby+1 = 2
                queryWrapper.eq(Teachplan::getParentid, teachplan.getParentid())
                        .eq(Teachplan::getCourseId, courseId);

                Integer count = teachplanMapper.selectCount(queryWrapper);
                if (orderby.equals(count)){
                    XueChengPlusException.cast("已经是底层，无法向下移动","120411");
                }
                queryWrapper.eq(Teachplan::getOrderby, orderby+1);
                //查询替换位置的课程计划的信息
                Teachplan move_teachPlan = teachplanMapper.selectOne(queryWrapper);
                //将替换位置的课程计划的排序编号+1
                move_teachPlan.setOrderby(move_teachPlan.getOrderby() - 1);
                //将替换位置的课程计划的信息更新
                teachplanMapper.updateById(move_teachPlan);
                //将课程计划的orderby-1
                teachplan.setOrderby(orderby+1);
                //将课程计划的信息更新
                teachplanMapper.updateById(teachplan);
            }
        }else{ //小章节
            //根据id获取课程计划信息
            //根据课程计划id获取课程计划信息
            Teachplan teachplan = teachplanMapper.selectById(id);
            //获取课程计划的orerby
            Integer orderby = teachplan.getOrderby();
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            if (move_type.equals("movedown")){ //向下移动
                //判断是否是底层
                queryWrapper = queryWrapper.eq(Teachplan::getParentid, teachplan.getParentid());
                Integer count = teachplanMapper.selectCount(queryWrapper);
                if (orderby.equals(count)){
                    XueChengPlusException.cast("已经是底层，无法向下移动","120411");
                }
                //被替换的小节需要向上进行移动
                LambdaQueryWrapper<Teachplan> orderbyWrapper = queryWrapper.eq(Teachplan::getOrderby, orderby + 1);
                Teachplan replaceTeachplan = teachplanMapper.selectOne(orderbyWrapper);
                //替换小节
                replaceTeachplan.setOrderby(orderby);
                teachplanMapper.updateById(replaceTeachplan);
                //向下移动
                teachplan.setOrderby(orderby + 1);
                teachplanMapper.updateById(teachplan);
            }else if (move_type.equals("moveup")){ //向上移动
                QueryWrapper<Teachplan> moveup_wrapper = new QueryWrapper<>();
                moveup_wrapper.eq("parentid", teachplan.getParentid());
                //判断是否是顶层
                if (orderby == 1) {
                    XueChengPlusException.cast("已经是顶层，无法向上移动","120410");
                }
                //被替换的小节需要向下进行移动
                moveup_wrapper.eq("orderby", orderby - 1);
                Teachplan replaceTeachplan = teachplanMapper.selectOne(moveup_wrapper);

                //替换小节
                replaceTeachplan.setOrderby(orderby);
                teachplanMapper.updateById(replaceTeachplan);
                //向上移动
                System.out.println(123);
                teachplan.setOrderby(orderby - 1);
                teachplanMapper.updateById(teachplan);
            }
        }

    }

    /**
     * @description 获取最新的排序号
     * @param courseId  课程id
     * @param parentId  父课程计划id
     * @return int 最新排序号
     */
    private int getTeachplanCount(long courseId,long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }


}
