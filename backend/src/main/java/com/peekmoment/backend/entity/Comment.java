package com.peekmoment.backend.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 评论。parentId 为空是一级评论，非空是二级回复。
 */
@Data
@TableName("comment")
public class Comment {

    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_PENDING = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contentId;

    private Long userId;

    private Long parentId;

    private String body;

    /** 1 正常 2 待审核（敏感词） */
    private Integer status;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;
}
