package com.geanik.user_session_backend.logic;

import com.geanik.user_session_backend.dal.Repositories;
import com.geanik.user_session_backend.domain.User;
import com.geanik.user_session_backend.domain.SessionToken;
import com.geanik.user_session_backend.domain.dto.UserDto;
import com.geanik.user_session_backend.logic.util.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

//@Transactional(propagation = Propagation.REQUIRED)
public class BusinessLogicImpl implements BusinessLogic {

    private static Logger log = LoggerFactory.getLogger(BusinessLogicImpl.class);

    private final Repositories repositories;
    private final TokenManager tokenManager;

    public BusinessLogicImpl(Repositories repositories, TokenManager tokenManager) {
        this.repositories = repositories;
        this.tokenManager = tokenManager;
    }

    @Override
    public Optional<String> authenticateUser(String email, String password) {

        if (email != null && password != null) {
            Optional<User> userOptional = repositories.users().findByEmail(email);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // if password is correct -> generate new SessionToken
                if (user.getPassword().equals(password)) {
                    SessionToken sessionToken = tokenManager.generateToken(user.getId());
                    sessionToken = repositories.sessionTokens().save(sessionToken);

                    log.info("Authenticated user (email: '{}')", user.getEmail());
                    return Optional.of(sessionToken.getToken());
                }
            } else
                log.debug("Couldn't find User (email: '{}')", email);
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> registerUser(UserDto userDto, String password) {

        if (userDto != null &&
                userDto.getUsername() != null &&
                userDto.getFirstName() != null &&
                userDto.getLastName() != null &&
                userDto.getEmail() != null) {

            if (!repositories.users().findByEmail(userDto.getEmail()).isPresent()) {
                User newUser = new User(userDto, password);

                newUser = repositories.users().save(newUser);
                log.info("Registered new User (email: '{}')", newUser.getEmail());

                return authenticateUser(newUser.getEmail(), newUser.getPassword());
            } else
                log.debug("Email already associated to existing User (email: '{}')", userDto.getEmail());
        }

        return Optional.empty();
    }

    @Override
    public Optional<UserDto> findUserById(Long userId) {

        if (userId != null) {
            Optional<User> userOptional = repositories.users().findById(userId);

            return userOptional.map(UserDto::new);
        } else
            return Optional.empty();
    }

    @Override
    public boolean updateUser(String sessionTokenString, UserDto userDto, String password) {

        if (sessionTokenString != null && userDto != null && password != null) {
            Optional<SessionToken> sessionTokenOptional = repositories.sessionTokens().findById(sessionTokenString);

            if (sessionTokenOptional.isPresent()) {
                SessionToken sessionToken = sessionTokenOptional.get();

                if (tokenManager.validateAndRenewToken(sessionToken)) {
                    Optional<User> userToUpdateOptional = repositories.users().findById(sessionToken.getUserId());

                    if (userToUpdateOptional.isPresent()) {
                        User userToUpdate = userToUpdateOptional.get();

                        userToUpdate.update(userDto);

                        // save updated user and renew session token time
                        repositories.users().save(userToUpdate);
                        log.info("Updated user info (email: '{}')", userToUpdate.getEmail());

                        return true;
                    } else
                        log.debug("Couldn't find User (email: '{}')", userDto.getEmail());
                }
            }
        }

        return false;
    }

}
