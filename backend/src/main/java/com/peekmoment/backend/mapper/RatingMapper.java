package com.peekmoment.backend.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.peekmoment.backend.entity.Rating;

public interface RatingMapper extends BaseMapper<Rating> {

    /** 重算动态的评分冗余列 */
    @Update("UPDATE content SET rating_count = "
            + "(SELECT COUNT(*) FROM rating WHERE content_id = #{contentId}), "
            + "rating_avg = "
            + "(SELECT IFNULL(AVG(score), 0) FROM rating WHERE content_id = #{contentId}) "
            + "WHERE id = #{contentId}")
    int recalcRating(@Param("contentId") Long contentId);
}
