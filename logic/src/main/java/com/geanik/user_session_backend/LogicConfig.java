package com.geanik.user_session_backend;

import com.geanik.user_session_backend.dal.Repositories;
import com.geanik.user_session_backend.logic.BusinessLogic;
import com.geanik.user_session_backend.logic.BusinessLogicImpl;
import com.geanik.user_session_backend.logic.util.TokenManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DalConfig.class)
public class LogicConfig {

    @Bean
    BusinessLogic businessLogic(Repositories repositories, TokenManager tokenManager) {
        return new BusinessLogicImpl(repositories, tokenManager);
    }

    @Bean
    TokenManager tokenManager(Repositories repositories) {
        return new TokenManager(repositories, 30);
    }

}
