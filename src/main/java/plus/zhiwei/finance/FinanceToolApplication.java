package plus.zhiwei.finance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"plus.zhiwei.finance"})
@EnableTransactionManagement
@MapperScan("plus.zhiwei.finance.dao")
@EnableConfigurationProperties
public class FinanceToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceToolApplication.class, args);
    }

}
