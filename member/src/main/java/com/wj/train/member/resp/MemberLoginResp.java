package com.wj.train.member.resp;


import lombok.Data;

/**
 * @author wj
 * @create_time 2023/7/10
 * @description 用户登入返回给前端的类，记得关键信息不能返回给前端
 */

@Data
public class MemberLoginResp {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户手机号
     */
    private String mobile;

}
