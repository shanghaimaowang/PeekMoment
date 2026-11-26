package com.peekmoment.backend.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 动态内容。评分/评论数冗余列避免列表页反复聚合。
 */
@Data
@TableName("content")
public class Content {

    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_PENDING = 2;
    public static final int STATUS_REMOVED = 3;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 标题选填，最多 40 字 */
    private String title;

    /** 正文，最多 1000 字 */
    private String body;

    /** 1 发布 2 待审核 3 下架 */
    private Integer status;

    private Integer ratingCount;

    private Double ratingAvg;

    private Integer commentCount;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
