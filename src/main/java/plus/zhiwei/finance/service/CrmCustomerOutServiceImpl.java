package plus.zhiwei.finance.service;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.spring.SpringUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import plus.zhiwei.finance.bean.CrmCustomerDO;
import plus.zhiwei.finance.bean.CrmCustomerImportOutReqVO;
import plus.zhiwei.finance.dao.CrmCustomerMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;


/**
 * 客户 Service 实现类
 *
 * @author Wanwan
 */
@Service
@Slf4j
@Validated
public class CrmCustomerOutServiceImpl implements CrmCustomerOutService {

    @Resource
    private CrmCustomerMapper customerMapper;

    private static final String CHARSET_NAME = "UTF-8";
    private static final String CIPHER_NAME = "AES/CBC/PKCS5Padding";
    private static final String SIGN_KEY = "yz00cbe2ae";
    private static final String SOURCE = "hzjf-yrk";
    private static final String key = "vlpmcx5i5omn3nt0";
    private static final String iv = "cih0bpfc5o6lavb3";

    @Override
    public long checkExist(String md5phone) {
        return customerMapper.countByMd5Phone(md5phone);
    }

    @Override
    public boolean importOut(CrmCustomerImportOutReqVO importOutReqVO) {
        String phone = importOutReqVO.getPhone();
        try {
            phone = decrypt(phone, key, iv);
        } catch (Exception e) {
            log.error("导入importOut:{}", e.getMessage(), e);
        }
        // 情况一：判断如果不存在，在进行插入
        CrmCustomerDO existCustomer = customerMapper.selectByCustomerName(phone);
        if (existCustomer == null) {
            // 1.1 插入客户信息
            CrmCustomerDO customer = new CrmCustomerDO();
            customer.setName(importOutReqVO.getName());
            customer.setFollowUpStatus(false);
            String remark = String.format("客户%s，来自%s，性别%s，出生日期%s，信用状况：%s，住房情况：%s，社保情况：%s，车辆情况：%s，职业：%s，芝麻信用分：%s。",
                    importOutReqVO.getName(), importOutReqVO.getCity(), importOutReqVO.getSex(), importOutReqVO.getBirthDate(), importOutReqVO.getOverdue(),
                    importOutReqVO.getHouse(), importOutReqVO.getSocial(), importOutReqVO.getVehicle(), importOutReqVO.getVocation(), importOutReqVO.getSesame());
            customer.setRemark(remark);
            customer.setOwnerUserId(142L);
            customer.setMobile(phone);
            customer.setOwnerTime(LocalDateTime.now());
            customer.setCreator("142");
            customer.setUpdater("142");
            customer.setCreateTime(LocalDateTime.now());
            customer.setUpdateTime(LocalDateTime.now());
            customerMapper.insert(customer);
            return true;
        } else {
            return false;
        }
    }


    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private CrmCustomerOutServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }


    /**
     * 签名
     */
    public String getSign(Map<String, String> paramMap, String signKey) {
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

    /**
     * 加密
     */
    public String encrypt(String content, String key, String iv) throws Exception {
        byte[] raw = key.getBytes(CHARSET_NAME);
        SecretKeySpec secretKey = new SecretKeySpec(raw, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
        Cipher cipher = Cipher.getInstance(CIPHER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(content.getBytes(CHARSET_NAME));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 解密
     */
    public String decrypt(String content, String key, String iv) throws Exception {
        byte[] raw = key.getBytes(CHARSET_NAME);
        SecretKeySpec secretKey = new SecretKeySpec(raw, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
        Cipher cipher = Cipher.getInstance(CIPHER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        return new String(cipher.doFinal(Base64.getDecoder().decode(content)));
    }

}
