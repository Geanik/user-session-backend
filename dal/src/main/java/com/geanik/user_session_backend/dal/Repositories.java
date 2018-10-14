package com.geanik.user_session_backend.dal;

public interface Repositories {

    UserRepository users();
    SessionTokenRepository sessionTokens();

}
