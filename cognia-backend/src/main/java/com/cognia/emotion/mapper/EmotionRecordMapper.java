package com.cognia.emotion.mapper;

import com.cognia.emotion.entity.EmotionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface EmotionRecordMapper extends BaseMapper<EmotionRecord> {

    @Select("SELECT record_date, emotion_score FROM emotion_record " +
            "WHERE user_id = #{userId} AND record_date >= #{startDate} AND record_date <= #{endDate} " +
            "ORDER BY record_date")
    List<Map<String, Object>> selectEmotionTrend(@Param("userId") Long userId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Select("SELECT emotion_type, COUNT(*) as count FROM emotion_record " +
            "WHERE user_id = #{userId} GROUP BY emotion_type")
    List<Map<String, Object>> selectEmotionDistribution(@Param("userId") Long userId);
}
