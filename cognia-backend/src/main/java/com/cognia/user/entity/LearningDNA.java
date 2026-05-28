package com.cognia.user.entity;

import com.cognia.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("learning_dna")
public class LearningDNA extends BaseEntity {
    private Long userId;
    private String dnaType;
    private String subType;
    private String tags;
    private String strengths;
    private String weaknesses;
    private String suggestions;
    private Integer understanding;
    private Integer memory;
    private Integer focus;
    private Integer execution;
    private Integer emotion;
    private Integer logic;
}
