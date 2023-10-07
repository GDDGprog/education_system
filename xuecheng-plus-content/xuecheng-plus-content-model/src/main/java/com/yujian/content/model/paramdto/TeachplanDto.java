package com.yujian.content.model.paramdto;

import com.yujian.content.model.pojo.Teachplan;
import com.yujian.content.model.pojo.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TeachplanDto extends Teachplan {

    TeachplanMedia teachplanMedia;

    List<TeachplanDto> teachPlanTreeNodes;
}
