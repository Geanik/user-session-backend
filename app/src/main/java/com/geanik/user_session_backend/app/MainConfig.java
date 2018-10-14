package com.geanik.user_session_backend.app;

import com.geanik.user_session_backend.rest.RestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RestConfig.class)
public class MainConfig {

}
