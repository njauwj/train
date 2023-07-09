package com.wj.train.common.resp;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CommonResp<T> {

    /**
     * 业务上的成功或失败
     */
    private boolean success = true;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 返回泛型数据，自定义类型
     */
    private T content;

    public CommonResp() {
    }

    public CommonResp(T content) {
        this.content = content;
    }

    public CommonResp(T content, String message) {
        this.content = content;
        this.message = message;
    }

    public CommonResp(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

}
