package com.yujian.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.base.model.PageParams;
import com.yujian.base.model.PageResult;
import com.yujian.content.mapper.CourseBaseMapper;
import com.yujian.content.mapper.CourseCategoryMapper;
import com.yujian.content.mapper.CourseMarketMapper;
import com.yujian.content.model.paramdto.AddCourseDto;
import com.yujian.content.model.paramdto.CourseBaseInfoDto;
import com.yujian.content.model.paramdto.QueryCourseParamDto;
import com.yujian.content.model.pojo.CourseBase;
import com.yujian.content.model.pojo.CourseCategory;
import com.yujian.content.model.pojo.CourseMarket;
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
        if (StringUtils.isBlank(dto.getName())) {
            throw new RuntimeException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }

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
            throw new RuntimeException("课程基本信息表插入失败");
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
            throw new RuntimeException("保存课程营销信息失败");
        }
        //查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(courseId);
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
                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
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

    //根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId){
        //查询课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        //查询课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //将课程基本信息和课程营销信息放到courseBaseInfoDto中
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        //将课程基本信息和课程营销信息放到courseBaseInfoDto中
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;
    }
}
