package com.yujian.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.base.exception.XueChengPlusException;
import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.yujian.content.mapper.*;
import com.yujian.content.model.paramdto.AddCourseDto;
import com.yujian.content.model.paramdto.CourseBaseInfoDto;
import com.yujian.content.model.paramdto.EditCourseDto;
import com.yujian.content.model.paramdto.QueryCourseParamDto;
import com.yujian.content.model.pojo.*;
import com.yujian.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseBaseServiceImpl implements CourseBaseService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public PageResult<CourseBase> list(PageParams params, QueryCourseParamDto queryCourseParamDto) {
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamDto.getAuditStatus());
        //课程名称
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamDto.getCourseName());
        //发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamDto.getPublishStatus()),
                CourseBase::getStatus,
                queryCourseParamDto.getPublishStatus());

        //进行分页操作
        Long page = params.getPageNo();
        Long size = params.getPageSize();
        Page<CourseBase> courseBasePage = new Page<>(page, size);

        //进行分页查询
        Page<CourseBase> selectPage = courseBaseMapper.selectPage(courseBasePage, queryWrapper);

        //进行结果封装
        List<CourseBase> items = selectPage.getRecords();
        //获取数量总数目
        Long counts = courseBasePage.getTotal();
        PageResult<CourseBase> pageResult = new PageResult<CourseBase>(items, counts, page, size);
        return pageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourse(Long companyId, AddCourseDto dto) {

        //合法性校验
        /**
        if (StringUtils.isBlank(dto.getName())) {
            //throw new RuntimeException("课程名称为空");
            XueChengPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            //throw new RuntimeException("课程分类为空");
            XueChengPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            //throw new RuntimeException("课程分类为空");
            XueChengPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            //throw new RuntimeException("课程等级为空");
            XueChengPlusException.cast("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            //throw new RuntimeException("教育模式为空");
            XueChengPlusException.cast("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            //throw new RuntimeException("适应人群为空");
            XueChengPlusException.cast("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            //throw new RuntimeException("收费规则为空");
            XueChengPlusException.cast("收费规则为空");
        }
         */

        //向课程基本信息表course_base写入数据
        CourseBase courseBase = new CourseBase();
        //将传入的页面参数放到courseBase中
        BeanUtils.copyProperties(dto, courseBase); //将dto中的数据拷贝到courseBase中(属性名一样就可以进行拷贝)
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        //发布状态为未发布
        courseBase.setStatus("203001");
        //插入数据库
        int insert = courseBaseMapper.insert(courseBase);
        if (insert != 1) {
            //throw new RuntimeException("课程基本信息表插入失败");
            XueChengPlusException.cast("课程基本信息表插入失败");
        }

        //向课程营销表保存课程营销信息
        //课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        //将传入的页面参数放到courseMarketNew中
        BeanUtils.copyProperties(dto,courseMarketNew);
        //课程的id
        Long courseId = courseBase.getId();
        courseMarketNew.setId(courseId);

        //调用saveCourseMarket方法,保存/修改课程营销信息
        int i = saveCourseMarket(courseMarketNew);
        if(i<=0){
            //throw new RuntimeException("保存课程营销信息失败");
            XueChengPlusException.cast("保存课程营销信息失败");
        }
        //查询课程基本信息及营销信息并返回
        return getCourseBaseById(courseId);
    }

    //根据id查询课程基本信息
    @Override
    public CourseBaseInfoDto getCourseBaseById(Long id) {
        //查询课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if(courseBase == null){
            return null;
        }
        //查询课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        //将课程基本信息和课程营销信息封装到CourseBaseInfoDto
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        //将课程分类放到课程基本信息中
        String oneCategory = courseCategoryMapper.selectCourseOneCategory(courseBase.getMt());
        String twoCategory = courseCategoryMapper.selectCourseTwoCategory(courseBase.getSt());
        courseBaseInfoDto.setMtName(oneCategory);
        courseBaseInfoDto.setStName(twoCategory);
        //返回课程基本信息
        return courseBaseInfoDto;
    }

    //修改课程信息
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {
        //查询课程id
        Long courseId  = dto.getId();
        CourseBaseInfoDto courseBase  = getCourseBaseById(courseId);
        if(courseBase==null){
            XueChengPlusException.cast("课程不存在");
        }
        //校验本机构只能修改本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }
        //封装基本信息的数据
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now()); //设置修改时间

        //更新课程基本信息
        int i = courseBaseMapper.updateById(courseBase);
        if(i!=1){
            XueChengPlusException.cast("课程基本信息更新失败");
        }
        //封装营销信息的数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        saveCourseMarket(courseMarket);
        //查询课程信息
        CourseBaseInfoDto courseBaseInfo = getCourseBaseById(courseId);
        return courseBaseInfo;
    }

    @Override
    public void deleteCourseBase(Long id) {
        //1.根据课程id删除讲师信息
        LambdaQueryWrapper<CourseTeacher> teacherQueryWrapper = new LambdaQueryWrapper<>();
        teacherQueryWrapper.eq(CourseTeacher::getCourseId,id);
        courseTeacherMapper.delete(teacherQueryWrapper);
        //2.根据课程id删除营销信息
        courseMarketMapper.deleteById(id);
        //3.根据课程id删除课程计划信息(大章节和小章节)
        LambdaQueryWrapper<Teachplan> teachPlanqueryWrapper = new LambdaQueryWrapper<>();
        teachPlanqueryWrapper.eq(Teachplan::getCourseId,id);
        List<Teachplan> teachplanList = teachplanMapper.selectList(teachPlanqueryWrapper);
        for (Teachplan teachplan : teachplanList) {
            if (teachplan.getParentid() != 0){
                teachplanMapper.deleteById(teachplan.getParentid());
            }
        }
        teachplanMapper.delete(teachPlanqueryWrapper);
        //4.根据课程id删除课程基本信息
        courseBaseMapper.deleteById(id);
    }

    //保存课程营销信息
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //收费规则
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new RuntimeException("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue()<=0){
                //throw new RuntimeException("课程为收费价格不能为空且必须大于0");
                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");
            }
        }
        //根据id从课程营销表中查询数据
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        //判断课程营销表中是否有数据
        if(courseMarketObj == null){
            //无数据就将数据插入
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            //有数据将数据更新
            BeanUtils.copyProperties(courseMarketNew,courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }
}
