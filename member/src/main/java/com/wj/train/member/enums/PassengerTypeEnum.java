package com.wj.train.member.enums;

/**
 * @author wj
 * @create_time 2023/7/11
 * @description 乘客类型枚举类
 */
public enum PassengerTypeEnum {
    ADULT("1", "成年人"),
    CHILD("2", "儿童"),
    STUDENT("3", "学生");

    private final String code;

    private final String desc;

    PassengerTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
