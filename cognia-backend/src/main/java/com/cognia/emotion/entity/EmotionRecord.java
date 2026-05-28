package com.cognia.emotion.entity;

import com.cognia.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("emotion_record")
public class EmotionRecord extends BaseEntity {
    private Long userId;
    private String emotionType;
    private String emotionLabel;
    private String content;
    private String relatedActivity;
    private String aiResponse;
    private LocalDate recordDate;
    private Integer emotionScore;
}
