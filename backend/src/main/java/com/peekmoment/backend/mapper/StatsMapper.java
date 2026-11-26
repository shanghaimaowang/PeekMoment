package com.peekmoment.backend.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.peekmoment.backend.dto.NameCountRow;
import com.peekmoment.backend.dto.ScoreRow;
import com.peekmoment.backend.dto.TrendRow;

/**
 * 统计查询。
 */
public interface StatsMapper {

    @Select("SELECT DATE(created_at) AS day, COUNT(*) AS cnt FROM content "
            + "WHERE deleted = 0 AND created_at >= #{since} "
            + "GROUP BY DATE(created_at) ORDER BY day")
    List<TrendRow> trendByDay(@Param("since") LocalDateTime since);

    @Select("SELECT t.name AS name, COUNT(*) AS cnt FROM content_tag ct "
            + "JOIN tag t ON ct.tag_id = t.id "
            + "JOIN content c ON ct.content_id = c.id AND c.deleted = 0 "
            + "GROUP BY t.name ORDER BY cnt DESC LIMIT 20")
    List<NameCountRow> tagDistribution();

    @Select("SELECT score, COUNT(*) AS cnt FROM rating GROUP BY score ORDER BY score")
    List<ScoreRow> scoreDistribution();
}
