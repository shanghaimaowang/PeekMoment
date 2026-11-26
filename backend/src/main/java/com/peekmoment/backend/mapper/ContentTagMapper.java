package com.peekmoment.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.peekmoment.backend.dto.ContentTagRow;

/**
 * 动态-标签关联表操作，联合主键不走实体。
 */
public interface ContentTagMapper {

    @Insert("INSERT IGNORE INTO content_tag (content_id, tag_id) VALUES (#{contentId}, #{tagId})")
    int insert(@Param("contentId") Long contentId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM content_tag WHERE content_id = #{contentId}")
    int deleteByContentId(@Param("contentId") Long contentId);

    @Select("<script>"
            + "SELECT ct.content_id AS contentId, t.name AS name "
            + "FROM content_tag ct JOIN tag t ON ct.tag_id = t.id "
            + "WHERE ct.content_id IN "
            + "<foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<ContentTagRow> selectTagsByContentIds(@Param("ids") List<Long> ids);

    @Select("SELECT content_id FROM content_tag WHERE tag_id = #{tagId}")
    List<Long> selectByTagId(@Param("tagId") Long tagId);
}
