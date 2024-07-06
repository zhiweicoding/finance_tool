package plus.zhiwei.finance.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import plus.zhiwei.finance.bean.CrmCustomerDO;

/**
 * 客户 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerMapper extends MPJBaseMapper<CrmCustomerDO> {

    @Select("SELECT COUNT(t.id) FROM `ruoyi-vue-pro`.crm_customer t WHERE MD5(t.mobile) = #{md5phone}")
    long countByMd5Phone(@Param("md5phone") String md5phone);


    default CrmCustomerDO selectByCustomerName(String name) {
        LambdaQueryWrapper<CrmCustomerDO> wrapper = Wrappers.<CrmCustomerDO>lambdaQuery().eq(CrmCustomerDO::getName, name);
        return selectOne(wrapper);
    }

}
