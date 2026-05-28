package com.cognia.user.entity;

import com.cognia.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String avatar;
    private String email;
    private String phone;
    private String learningType;
    private String emotionState;
    private Integer studyLevel;
    private Integer continuousDays;
    private Double todayFocusTime;
    private Integer status;
}
