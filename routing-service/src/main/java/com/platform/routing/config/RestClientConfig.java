package com.platform.routing.config;

import com.platform.common.filter.MdcClientHttpRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder(MdcClientHttpRequestInterceptor interceptor) {
        return RestClient.builder()
                .requestInterceptor(interceptor);
    }
}
