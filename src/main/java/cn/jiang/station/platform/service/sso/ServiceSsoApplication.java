package cn.jiang.station.platform.service.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(scanBasePackages = "cn.jiang.station.platform")
@EnableEurekaClient
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan(value ="cn.jiang.station.platform.service.sso.mapper")
public class ServiceSsoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceSsoApplication.class, args);
    }
}
