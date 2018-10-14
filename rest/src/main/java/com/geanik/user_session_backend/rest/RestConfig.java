package com.geanik.user_session_backend.rest;

import com.geanik.user_session_backend.LogicConfig;
import com.geanik.user_session_backend.logic.BusinessLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(LogicConfig.class)
public class RestConfig {

    @Bean
    public GraphqlController graphQlController(BusinessLogic logic) {
        return new GraphqlController(logic);
    }

}
