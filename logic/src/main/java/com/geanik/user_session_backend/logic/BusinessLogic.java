package com.geanik.user_session_backend.logic;

import com.geanik.user_session_backend.domain.dto.UserDto;

import java.util.Optional;

public interface BusinessLogic {

    Optional<String> authenticateUser(String email, String password);

    Optional<String> registerUser(UserDto userDto, String password);

    Optional<UserDto> findUserById(Long userId);

    boolean updateUser(String sessionToken, UserDto userDto, String password);

}
