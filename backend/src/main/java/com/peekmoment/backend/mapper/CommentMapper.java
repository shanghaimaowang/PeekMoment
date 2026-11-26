package com.peekmoment.backend.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.peekmoment.backend.entity.Comment;

public interface CommentMapper extends BaseMapper<Comment> {

    @Update("UPDATE content SET comment_count = comment_count + 1 WHERE id = #{contentId}")
    int incrCommentCount(@Param("contentId") Long contentId);
}
