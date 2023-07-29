package com.wj.train.common.exception;

public enum BusinessExceptionEnum {

    MEMBER_MOBILE_EXIST("手机号已注册"),
    MEMBER_MOBILE_NOT_EXIST("请先获取短信验证码"),
    MEMBER_MOBILE_CODE_ERROR("短信验证码错误"),


    BUSINESS_STATION_NAME_UNIQUE_ERROR("车站已存在"),
    BUSINESS_TRAIN_CODE_UNIQUE_ERROR("车次编号已存在"),
    BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR("同车次站序已存在"),
    BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR("同车次站名已存在"),
    BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR("同车次厢号已存在"),

    BUSINESS_DAILY_TRAIN_TICKET_LACK_ERROR("余票不足"),

    BUSINESS_CONFIRM_ORDER_BUSY("系统繁忙，请稍后重试"),

    BUSINESS_TOO_MANY_PEOPLE("当前车次购票人数太多，请稍后重试"),

    BUSINESS_SK_TOKEN_INIT_ERROR("令牌数量未初始化"),

    BUSINESS_IMAGE_CODE_EXPIRED("图形验证码过期"),
    BUSINESS_IMAGE_CODE_ERROR("图形验证码错误"),

    BUSINESS_CONFIRM_ORDER_INIT_ERROR("车票初始化失败"),

    BUSINESS_PARAMS_ILLEGAL("参数非法");


    private String desc;

    BusinessExceptionEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "BusinessExceptionEnum{" +
                "desc='" + desc + '\'' +
                "} " + super.toString();
    }
}
