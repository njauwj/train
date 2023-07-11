package com.wj.train.common.context;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wj.train.common.entity.MemberInfo;

/**
 * @author wj
 * @create_time 2023/7/11
 * @description 使用本地线程存储用户信息
 */
public class LocalContext {

    private static final ThreadLocal<MemberInfo> threadLocal = new ThreadLocal<>();

    public static void set(JSONObject jsonObject) {
        MemberInfo memberInfo = JSONUtil.toBean(jsonObject, MemberInfo.class);
        threadLocal.set(memberInfo);
    }

    public static MemberInfo get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }

    public static Long getMemberId() {

        return threadLocal.get().getId();

    }
}
