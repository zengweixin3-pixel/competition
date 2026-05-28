package com.cognia.mistake.entity;

import com.cognia.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mistake")
public class Mistake extends BaseEntity {
    private Long userId;
    private String title;
    private String content;
    private String subject;
    private String mistakeType;
    private Integer errorCount;
    private String difficulty;
    private String aiAnalysis;
    private String status;
    private String imageUrl;
}
