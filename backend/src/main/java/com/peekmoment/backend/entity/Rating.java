package com.peekmoment.backend.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 评分，一人一评（唯一键），重复评分即覆盖。
 */
@Data
@TableName("rating")
public class Rating {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contentId;

    private Long userId;

    /** 1-5 */
    private Integer score;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
