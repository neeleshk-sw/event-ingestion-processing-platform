package com.platform.intake.config;

import com.platform.common.filter.MdcClientHttpRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    private static final Logger logger = LoggerFactory.getLogger(RestClientConfig.class);

    @Bean
    public RestClient.Builder restClientBuilder(MdcClientHttpRequestInterceptor interceptor) {
        return RestClient.builder()
                .requestInterceptor(interceptor);
    }
}
