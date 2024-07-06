package plus.zhiwei.finance.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CrmCustomerImportOutReqVO {

    /**
     * accumulation : 缴纳 6 个月以上
     * amount : 200000
     * birthDate : 1990-01-01
     * business : 有
     * city : 北京市
     * customerType : 企业
     * house : 全款商品房
     * insurance : 缴纳 1 年以上
     * name : 测试张三
     * overdue : 信用良好，无逾期
     * phone : 4CCCmTz/q/sz39gYxM6jmw==
     * productName : 测试金服
     * remark :
     * sesame : 700 分以上
     * sex : 男
     * sign : 4dd87096040db2a173b87a0c38d4f38d
     * social : 缴纳 6 个月以上
     * source : hmc
     * timestamp : 1678327869
     * vehicle : 全款汽车
     * vocation : 企业主
     */

    private String accumulation;
    private int amount;
    private String birthDate;
    private String business;
    private String city;
    private String customerType;
    private String house;
    private String insurance;
    private String name;
    private String overdue;
    private String phone;
    private String productName;
    private String remark;
    private String sesame;
    private String sex;
    private String sign;
    private String social;
    private String source;
    private String timestamp;
    private String vehicle;
    private String vocation;

}
