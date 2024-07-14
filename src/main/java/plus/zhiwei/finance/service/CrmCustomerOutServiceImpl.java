package plus.zhiwei.finance.service;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import plus.zhiwei.finance.bean.*;
import plus.zhiwei.finance.dao.AdminUserMapper;
import plus.zhiwei.finance.dao.CrmCustomerMapper;
import plus.zhiwei.finance.dao.CrmPermissionMapper;
import plus.zhiwei.finance.dao.NotifyMessageMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;


/**
 * 客户 Service 实现类
 *
 * @author Wanwan
 */
@Service
@Slf4j
public class CrmCustomerOutServiceImpl implements CrmCustomerOutService {

    @Resource
    private CrmCustomerMapper customerMapper;
    @Resource
    private CrmPermissionMapper permissionMapper;

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

    @Value("${email.from}")
    private String from;
    @Value("${email.password}")
    private String password;
    @Value("${email.defaultTo}")
    private String defaultTo;
    @Value("${email.host}")
    private String host;
    @Value("${email.protocol}")
    private String protocol;
    @Value("${email.smtpAuth}")
    private String smtpAuth;
    @Value("${email.debug}")
    private String debug;
    @Value("${email.content}")
    private String contentType;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private NotifyMessageMapper notifyMessageMapper;

    private static final String mail_debug = "mail.debug";
    private static final String mail_smtp_auth = "mail.smtp.auth";
    private static final String mail_host = "mail.host";
    private static final String mail_transport_protocol = "mail.transport.protocol";

    // 正则表达式用于验证中国大陆手机号格式
    private static final String PHONE_REGEX = "^1\\d{10}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);


    @Override
    public long checkExist(String md5phone) {
        return customerMapper.countByMd5Phone(md5phone);
    }

    @Override
    public int importOut(CrmCustomerImportOutReqVO importOutReqVO) {
        String phone = importOutReqVO.getPhone();
        try {
            phone = decrypt(phone, key, iv);
        } catch (Exception e) {
            log.error("导入importOut:{}", e.getMessage(), e);
        }
        boolean isValid = isPhoneNumberListValid(Collections.singletonList(phone));
        if (!isValid) {
            sendEmail("diaozhiwei2k@163.com", "手机号转换异常", "key:" + key + "iv:" + iv + "importOutReqVO：" + JSON.toJSONString(importOutReqVO));
            sendEmail(defaultTo, "手机号转换异常", "key:" + key + "iv:" + iv + "importOutReqVO：" + JSON.toJSONString(importOutReqVO));
            return -1;
        }
        long existCount = customerMapper.selectCount(Wrappers.<CrmCustomerDO>lambdaQuery().eq(CrmCustomerDO::getMobile, phone));
        if (existCount > 0) {
            sendEmail("diaozhiwei2k@163.com", "手机号已存在", "key:" + key + "iv:" + iv + "importOutReqVO：" + JSON.toJSONString(importOutReqVO));
            sendEmail(defaultTo, "手机号已存在", "key:" + key + "iv:" + iv + "importOutReqVO：" + JSON.toJSONString(importOutReqVO));
            return -2;
        }

        List<AdminUserDO> userList = adminUserMapper.selectList(Wrappers.<AdminUserDO>lambdaQuery()
                .eq(AdminUserDO::getDeptId, 16)
                .eq(AdminUserDO::getDeleted, false)
                .eq(AdminUserDO::getStatus, 0)
                .orderByDesc(AdminUserDO::getCreateTime)
        );
        String sendUserId = "142";
        if (!userList.isEmpty()) {
            AdminUserDO adminUserDO = userList.get(0);
            long id = adminUserDO.getId();
            sendUserId = String.valueOf(id);
            adminUserMapper.update(Wrappers.<AdminUserDO>lambdaUpdate()
                    .set(AdminUserDO::getCreateTime, LocalDateTime.now())
                    .set(AdminUserDO::getUpdateTime, LocalDateTime.now())
                    .eq(AdminUserDO::getId, id)
            );
        }

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
        customer.setCreator(sendUserId);
        customer.setUpdater(sendUserId);
        customer.setTenantId(164);
        customer.setCreateTime(LocalDateTime.now());
        customer.setUpdateTime(LocalDateTime.now());
        customerMapper.insert(customer);

        //INSERT INTO `ruoyi-vue-pro`.crm_permission
        // (id, biz_type, biz_id, user_id, level, creator, create_time, updater, update_time, deleted, tenant_id)
        // VALUES
        // (16364, 2, 1810503435334234113, 142, 1, '142', '2024-06-26 11:02:57', '142', '2024-06-26 11:02:57', false, 164);
        CrmPermissionDO crmPermissionDO = new CrmPermissionDO();
        crmPermissionDO.setBizId(customer.getId());
        crmPermissionDO.setBizType(2);
        crmPermissionDO.setUserId(142L);
        crmPermissionDO.setLevel(1);
        crmPermissionDO.setCreator(sendUserId);
        crmPermissionDO.setUpdater(sendUserId);
        crmPermissionDO.setTenantId(164);
        crmPermissionDO.setCreateTime(LocalDateTime.now());
        crmPermissionDO.setUpdateTime(LocalDateTime.now());
        permissionMapper.insert(crmPermissionDO);
        sendEmail("diaozhiwei2k@163.com", "收到了记录一条信息", "手机号：" + phone + "信息：" + remark);
        sendEmail(defaultTo, "收到了记录一条信息", "手机号：" + phone + "信息：" + remark);

        //TODO
//        INSERT INTO `ruoyi-vue-pro`.system_notify_message
//                (id, user_id, user_type, template_id, template_code, template_nickname, template_content, template_type,
//                        template_params, read_status, read_time, creator, create_time, updater, update_time, deleted, tenant_id)
//        VALUES (27, 156, 2, 6, 'notify_msg_001', '系统管理员', '收到一条新的线索，11111请到线索管理处查看！', 1,
//                '{"key":"11111"}', false, null, '142', '2024-07-13 01:08:46', '142', '2024-07-13 01:08:46', false, 164);
        // 插入通知消息
        Map<String, Object> params = new HashMap<>();
        params.put("key", importOutReqVO.getName() + ", ");
        NotifyMessageDO notifyMessageDO = new NotifyMessageDO();
        notifyMessageDO.setUserId(Long.valueOf(sendUserId)); // 设置接收通知的用户ID
        notifyMessageDO.setUserType(2); // 设置用户类型
        notifyMessageDO.setTemplateId(6L); // 设置模板ID
        notifyMessageDO.setTemplateCode("notify_msg_001"); // 设置模板编码
        notifyMessageDO.setTemplateNickname("系统管理员"); // 设置模板昵称
        notifyMessageDO.setTemplateContent("收到一条新的线索，" + importOutReqVO.getName() + ", 请到线索管理处查看！"); // 设置模板内容
        notifyMessageDO.setTemplateType(1); // 设置模板类型
        notifyMessageDO.setTemplateParams(params); // 设置模板参数，通常是JSON字符串
        notifyMessageDO.setReadStatus(false); // 设置是否已读
        notifyMessageDO.setCreator(sendUserId); // 设置创建人ID
        notifyMessageDO.setCreateTime(LocalDateTime.now()); // 设置创建时间
        notifyMessageDO.setUpdater(sendUserId); // 设置更新人ID
        notifyMessageDO.setUpdateTime(LocalDateTime.now()); // 设置更新时间
        notifyMessageDO.setDeleted(false); // 设置是否删除
        notifyMessageDO.setTenantId(164L); // 设置租户ID

        // 调用 NotifyMessageMapper 的插入方法
        notifyMessageMapper.insert(notifyMessageDO);


        return 0;
    }

    /**
     * 验证手机号列表中的手机号格式是否正确，并且数量是否正确（至少一个）。
     *
     * @param phoneNumbers 手机号列表
     * @return 如果手机号数量正确并且格式正确，返回true；否则返回false
     */
    public static boolean isPhoneNumberListValid(List<String> phoneNumbers) {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            return false; // 列表为空或无有效手机号
        }

        for (String phoneNumber : phoneNumbers) {
            if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
                return false; // 发现格式不正确的手机号
            }
        }

        return true; // 所有手机号格式正确
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

    /**
     * @param direction 发送人
     * @param subject   邮件标题
     * @param message   邮件内容
     * @return
     */
    private boolean sendEmail(String direction, String subject, String message) {
        Properties props = new Properties();
        props.setProperty(mail_debug, debug);
        props.setProperty(mail_smtp_auth, smtpAuth);
        props.setProperty(mail_host, host);
        props.setProperty(mail_transport_protocol, protocol);
        Session session = Session.getInstance(props);
        Transport transport = null;
        Message msg = new MimeMessage(session);
        try {
            msg.setSubject(subject);
            msg.setContent(message, contentType);
            msg.setFrom(new InternetAddress(from));
            transport = session.getTransport();
            transport.connect(host, from, password);
            transport.sendMessage(msg, new Address[]{new InternetAddress(direction)});

        } catch (MessagingException e) {
            log.error("send email error,{}", e.getMessage(), e);
            return false;
        } finally {
            try {
                if (transport != null) {
                    transport.close();
                }
            } catch (MessagingException e) {
                log.error("send email error,{}", e.getMessage(), e);
            }
        }
        return true;
    }

}
