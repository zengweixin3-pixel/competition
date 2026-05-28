package com.cognia.mistake.mapper;

import com.cognia.mistake.entity.Mistake;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface MistakeMapper extends BaseMapper<Mistake> {

    @Select("SELECT mistake_type, COUNT(*) as count FROM mistake " +
            "WHERE user_id = #{userId} GROUP BY mistake_type")
    List<Map<String, Object>> selectMistakeTypeDistribution(@Param("userId") Long userId);

    @Select("SELECT subject, COUNT(*) as count FROM mistake " +
            "WHERE user_id = #{userId} GROUP BY subject")
    List<Map<String, Object>> selectSubjectDistribution(@Param("userId") Long userId);
}
