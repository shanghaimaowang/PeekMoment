package com.peekmoment.backend.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 媒体附件。
 */
@Data
@TableName("media")
public class Media {

    public static final int KIND_IMAGE = 1;
    public static final int KIND_VIDEO = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contentId;

    /** 1 图片 2 视频 */
    private Integer kind;

    private String url;

    /** 九宫格展示顺序 */
    private Integer sortOrder;

    private LocalDateTime createdAt;
}
