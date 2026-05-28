package com.cognia.learning.entity;

import com.cognia.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("study_record")
public class StudyRecord extends BaseEntity {
    private Long userId;
    private String subject;
    private Integer duration;
    private Integer score;
    private Integer focusLevel;
    private String content;
    private LocalDate studyDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
