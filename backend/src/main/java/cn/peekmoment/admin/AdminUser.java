package cn.peekmoment.admin;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("users")
public class AdminUser {
    @TableId
    private String id;
    private String nickname;
    @TableField("created_at")
    private long createdAt;

    public String getId() { return id; }
    public String getNickname() { return nickname; }
    public long getCreatedAt() { return createdAt; }
}
