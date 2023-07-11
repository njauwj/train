package com.wj.train.common.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author wj
 * @create_time 2023/7/11
 * @description 分页查询通用返回类
 */
@Data
@AllArgsConstructor
public class PageResp<T> {


    /**
     * 查询总条数
     */
    private Integer total;
    /**
     * 数据集合
     */
    private List<T> list;
}
