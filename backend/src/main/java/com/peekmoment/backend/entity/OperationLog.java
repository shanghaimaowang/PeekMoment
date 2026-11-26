package com.peekmoment.backend.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 管理端操作日志。
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long adminId;

    /** 如 disable_user、remove_content */
    private String action;

    /** user / content / comment */
    private String targetType;

    private Long targetId;

    private String detail;

    private LocalDateTime createdAt;
}
