package com.peekmoment.backend.dto;

import lombok.Data;

/**
 * content_tag 关联查询行：动态 id 对应标签名。
 */
@Data
public class ContentTagRow {

    private Long contentId;

    private String name;
}
