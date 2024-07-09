package plus.zhiwei.finance.bean;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * CRM 数据权限 DO
 *
 * @author HUIHUI
 */
@TableName("crm_permission")
@KeySequence("crm_permission_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmPermissionDO extends BaseDO {

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;
    private Integer bizType;
    private Long bizId;
    private Long userId;
    private Integer level;
    private int tenantId = 164;
}
