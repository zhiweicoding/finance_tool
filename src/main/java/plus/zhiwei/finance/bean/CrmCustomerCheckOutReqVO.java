package plus.zhiwei.finance.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrmCustomerCheckOutReqVO {
    /**
     * md5Phone : 71cad276b6c128bc2c8bad24e7b54d7a
     * sign : 4f312a3223883ed610a03ed6a430f020
     * source : hmc
     * timestamp : 1678241481
     */
    private String md5Phone;
    private String sign;
    private String source;
    private String timestamp;

}
