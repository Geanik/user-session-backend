package com.geanik.user_session_backend.dal;

public class RepositoriesImpl implements Repositories {

    private UserRepository userRepository;
    private SessionTokenRepository sessionTokenRepository;

    public RepositoriesImpl(UserRepository userRepository, SessionTokenRepository sessionTokenRepository) {
        this.userRepository = userRepository;
        this.sessionTokenRepository = sessionTokenRepository;
    }

    @Override
    public UserRepository users() {
        return userRepository;
    }

    @Override
    public SessionTokenRepository sessionTokens() {
        return sessionTokenRepository;
    }

}
