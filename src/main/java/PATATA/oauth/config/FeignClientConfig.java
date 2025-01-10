package PATATA.oauth.config;

import PATATA.PatataApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackageClasses = PatataApplication.class)
public class FeignClientConfig {

}
