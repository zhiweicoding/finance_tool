package plus.zhiwei.finance.bean;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 站内信 DO
 *
 * @author xrcoder
 */
@TableName(value = "system_notify_message", autoResultMap = true)
@KeySequence("system_notify_message_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyMessageDO extends BaseDO {

    @TableId
    private Long id;
    private Long userId;
    private Integer userType;
    private Long templateId;
    private String templateCode;
    private Integer templateType;
    private String templateNickname;
    private String templateContent;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> templateParams;
    private Boolean readStatus;
    private LocalDateTime readTime;
    private Long tenantId;

}
