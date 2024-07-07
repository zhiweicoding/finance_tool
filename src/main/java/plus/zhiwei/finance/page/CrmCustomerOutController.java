package plus.zhiwei.finance.page;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.zhiwei.finance.bean.CommonResult;
import plus.zhiwei.finance.bean.CrmCustomerCheckOutReqVO;
import plus.zhiwei.finance.bean.CrmCustomerImportOutReqVO;
import plus.zhiwei.finance.service.CrmCustomerOutService;

import java.util.Map;

@RestController
@RequestMapping("/crm/customer/out/")
@Slf4j
public class CrmCustomerOutController {

    @Autowired
    private CrmCustomerOutService crmCustomerOutService;

    private static final String CHARSET_NAME = "UTF-8";
    private static final String CIPHER_NAME = "AES/CBC/PKCS5Padding";

    @Value("${crm.sign:yz00cbe2ae}")
    private String signKey;
    @Value("${crm.source:hzjf-yrk}")
    private String source;
    @Value("${crm.key:vlpmcx5i5omn3nt0}")
    private String key;
    @Value("${crm.iv:cih0bpfc5o6lavb3}")
    private String iv;

    @PostMapping("/check")
    public CommonResult<Object> check(@RequestBody CrmCustomerCheckOutReqVO checkOutReqVO) {
        try {
            String jsonString = JSON.toJSONString(checkOutReqVO);
            Map<String, String> dict = JSON.parseObject(jsonString, new TypeReference<>() {
            });
            String sign = dict.remove("sign");
            String signGenerate = getSign(dict, signKey);
            if (signGenerate.equals(sign)) {
                log.info("检测手机号是否存在 sign success");
            }
        } catch (Exception e) {
            log.error("检测手机号是否存在 sign fail:{}", e.getMessage(), e);
        }
        long l = crmCustomerOutService.checkExist(checkOutReqVO.getMd5Phone());
        if (l == 0) {
            CommonResult<Object> success = CommonResult.success(null);
            success.setMsg("撞库通过");
            return success;
        } else {
            CommonResult<Object> error = CommonResult.error(1001, "已存在");
            error.setData(null);
            return error;
        }

    }

    @PostMapping("/importOut")
    public CommonResult<Object> importOut(@RequestBody CrmCustomerImportOutReqVO importReqVO) {
        try {
            String jsonString = JSON.toJSONString(importReqVO);
            Map<String, String> dict = JSON.parseObject(jsonString, new TypeReference<Map<String, String>>() {
            });
            String sign = dict.remove("sign");
            String signGenerate = getSign(dict, signKey);
            if (signGenerate != null && signGenerate.equals(sign)) {
                log.info("导入外部用户信息 sign success");
            }
        } catch (Exception e) {
            log.error("导入外部用户信息 sign fail:{}", e.getMessage(), e);
        }
        int result = crmCustomerOutService.importOut(importReqVO);
        if (result == 0) {
            CommonResult<Object> success = CommonResult.success(null);
            success.setMsg("入库成功");
            return success;
        } else if (result == -1) {
            CommonResult<Object> error = CommonResult.error(1001, "手机号转换异常");
            error.setData(null);
            return error;
        } else {
            CommonResult<Object> error = CommonResult.error(1001, "手机号已存在");
            error.setData(null);
            return error;
        }
    }

    /**
     * 签名
     */
    public static String getSign(Map<String, String> paramMap, String signKey) {
        try {
            StringBuilder signBuilder = new StringBuilder();
            paramMap.forEach((k, v) -> {
                if (!k.equals("sign")) {
                    signBuilder.append(v);
                }
            });
            //将传入的所有参数加秘钥后一起 MD5 计算的结果即为验证值
            return DigestUtil.md5Hex(signBuilder + signKey);
        } catch (Exception e) {
            return null;
        }
    }
}
