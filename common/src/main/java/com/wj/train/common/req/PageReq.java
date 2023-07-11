package com.wj.train.common.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author wj
 * @create_time 2023/7/11
 * @description 分页查询参数
 */
@Data
public class PageReq {

    @NotNull(message = "页码不能为空")
    private Integer page;

    @NotNull(message = "页数不能为空")
    @Max(value = 20L, message = "最多一次查询20条数据")
    private Integer size;

}
