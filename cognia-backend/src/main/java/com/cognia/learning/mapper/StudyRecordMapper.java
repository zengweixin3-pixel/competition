package com.cognia.learning.mapper;

import com.cognia.learning.entity.StudyRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface StudyRecordMapper extends BaseMapper<StudyRecord> {

    @Select("SELECT DATE(study_date) as date, SUM(duration) as duration FROM study_record " +
            "WHERE user_id = #{userId} AND study_date >= #{startDate} AND study_date <= #{endDate} " +
            "AND end_time IS NOT NULL " +
            "GROUP BY DATE(study_date) ORDER BY date")
    List<Map<String, Object>> selectStudyTrend(@Param("userId") Long userId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Select("SELECT subject, SUM(duration) as totalDuration FROM study_record " +
            "WHERE user_id = #{userId} AND study_date >= #{startDate} AND study_date <= #{endDate} " +
            "AND end_time IS NOT NULL " +
            "GROUP BY subject")
    List<Map<String, Object>> selectSubjectDistribution(@Param("userId") Long userId, @Param("startDate") String startDate, @Param("endDate") String endDate);
}
