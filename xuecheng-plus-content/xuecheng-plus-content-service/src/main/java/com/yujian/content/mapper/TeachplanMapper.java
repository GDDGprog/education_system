package com.yujian.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yujian.content.model.paramdto.TeachplanDto;
import com.yujian.content.model.pojo.Teachplan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author yujian
 */
@Mapper
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    List<TeachplanDto> selectTreeNodes(Long courseId);
}
