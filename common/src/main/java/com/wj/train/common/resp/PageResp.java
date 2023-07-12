package com.wj.train.common.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wj
 * @create_time 2023/7/11
 * @description 分页查询通用返回类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResp<T> {


    /**
     * 查询总条数
     */
    private Long total;
    /**
     * 数据集合
     */
    private List<T> list;
}
