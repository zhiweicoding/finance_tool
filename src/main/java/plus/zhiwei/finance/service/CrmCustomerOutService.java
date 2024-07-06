package plus.zhiwei.finance.service;


import plus.zhiwei.finance.bean.CrmCustomerImportOutReqVO;

/**
 * 客户 Service 接口
 *
 * @author Wanwan
 */
public interface CrmCustomerOutService {

    long checkExist(String md5phone);

    boolean importOut(CrmCustomerImportOutReqVO importOutReqVO);

}
