package com.yujian.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 分页查询的参数
 */
@Data
@ToString
@NoArgsConstructor //无参构造
@AllArgsConstructor //全参构造
public class PageParams {

    //当前页码
    @ApiModelProperty("当前页码")
    private Long pageNo;

    //每页显示记录数
    @ApiModelProperty("每页显示记录数")
    private Long pageSize;
}
