package com.da.usercenter.model.enums;

/**
 * 队伍状态枚举
 */
public enum TeamStatesEnum {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private int value;
    private String text;

    TeamStatesEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public static TeamStatesEnum getEnumByValue(Integer value){
        if (value ==null){
            return null;
        }
        TeamStatesEnum[] values = TeamStatesEnum.values();
        for (TeamStatesEnum teamStatesEnum : values) {
            if(teamStatesEnum.getValue() == value){
                return teamStatesEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
