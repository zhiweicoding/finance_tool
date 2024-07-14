package plus.zhiwei.finance.bean;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import plus.zhiwei.finance.bean.type.JsonLongSetTypeHandler;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 管理后台的用户 DO
 *
 * @author 芋道源码
 */
@TableName(value = "system_users", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@KeySequence("system_users_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDO extends BaseDO {
    @TableId
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String remark;
    private Long deptId;
    @TableField(typeHandler = JsonLongSetTypeHandler.class)
    private Set<Long> postIds;
    private String email;
    private String mobile;
    private Integer sex;
    private String avatar;
    private Integer status;
    private String loginIp;
    private LocalDateTime loginDate;
    private int tenantId = 164;

}
