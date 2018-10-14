package com.geanik.user_session_backend.graphql;

import com.geanik.user_session_backend.domain.dto.UserDto;
import com.geanik.user_session_backend.logic.BusinessLogic;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;

import java.util.Optional;

public class UserService {

    private final BusinessLogic logic;

    public UserService(BusinessLogic businessLogic) {
        this.logic = businessLogic;
    }

    @GraphQLQuery(name = "authenticateUser")
    public Optional<String> authenticateUser(@GraphQLArgument(name = "email") String email,
                                             @GraphQLArgument(name = "password") String password) {
        return logic.authenticateUser(email, password);
    }

    @GraphQLQuery(name = "registerUser")
    public Optional<String> registerUser(@GraphQLArgument(name = "userDto") UserDto userDto,
                                         @GraphQLArgument(name = "password") String password) {
        return logic.registerUser(userDto, password);
    }

    @GraphQLQuery(name = "updateUser")
    public boolean updateUser(@GraphQLArgument(name = "sessionToken") String sessionToken,
                           @GraphQLArgument(name = "userDto") UserDto userDto,
                           @GraphQLArgument(name = "password") String password) {
        return logic.updateUser(sessionToken, userDto, password);
    }

    @GraphQLQuery(name = "findUserById")
    public Optional<UserDto> findUserById(@GraphQLArgument(name = "userId") Long userId) {
        return logic.findUserById(userId);
    }

}
