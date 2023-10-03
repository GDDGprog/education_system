package com.yujian.content.model.paramdto;

import com.yujian.content.model.pojo.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    List<CourseCategoryTreeDto> childrenTreeNodes;
}
