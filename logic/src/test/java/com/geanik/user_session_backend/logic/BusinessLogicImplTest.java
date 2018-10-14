package com.geanik.user_session_backend.logic;

import com.geanik.user_session_backend.LogicConfig;
import com.geanik.user_session_backend.dal.Repositories;
import com.geanik.user_session_backend.domain.User;
import com.geanik.user_session_backend.domain.SessionToken;
import com.geanik.user_session_backend.domain.dto.UserDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@SpringBootTest(classes = LogicConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@RunWith(SpringRunner.class)
public class BusinessLogicImplTest {

    @Autowired
    Repositories repositories;

    @Autowired
    BusinessLogic businessLogic;

    private User user;
    private UserDto userDto1;
    private UserDto userDto2;
    private UserDto userDtoWithNullMember;

    @Before
    public void setUp() throws Exception {
        user = new User("geanik", "g", "eanik", "geanik@mail.at", "pw123");
        userDto1 = new UserDto(0L,"geanik", "g", "eanik", "geanik@mail.com");
        userDto2 = new UserDto(0L, "herminator", "Hermann", "Maier", "h.m@mail.com");
        userDtoWithNullMember = new UserDto(0L,"pedge", "john", null, "johnny@mail.com");
    }

    @Test
    public void testAuthenticateUserSuccessfully() {
        User savedUser = repositories.users().save(user);
        assert businessLogic.authenticateUser(savedUser.getEmail(), savedUser.getPassword()).isPresent();
    }

    @Test
    public void testAuthenticateUserWithNullParams() {
        assert !businessLogic.authenticateUser(null, "pw123").isPresent();
        assert !businessLogic.authenticateUser("geanik@mail.at", null).isPresent();
    }

    @Test
    public void testAuthenticateUserWithWrongPassword() {
        User savedUser = repositories.users().save(user);
        assert !businessLogic.authenticateUser(savedUser.getEmail(), "wrongPw").isPresent();
    }

    @Test
    public void testAuthenticateUserWithNotRegisteredEmail() {
        assert !businessLogic.authenticateUser("notRegisteredEmail", "pw123").isPresent();
    }

    @Test
    public void testRegisterUserSuccessfully() {
        // register new user
        assert businessLogic.registerUser(userDto1, "pw123").isPresent();

        // get user from DB by email
        Optional<User> userOptional = repositories.users().findByEmail(userDto1.getEmail());
        assert userOptional.isPresent();
        User registeredUser = userOptional.get();
        userDto1.setId(registeredUser.getId());

        // check if user was saved correctly to DB
        assert new UserDto(registeredUser).equals(userDto1);
    }

    @Test
    public void testRegisterUserWithNullParams() {
        assert !businessLogic.registerUser(null, "pw123").isPresent();
        assert !businessLogic.registerUser(userDto1, null).isPresent();
    }

    @Test
    public void testRegisterUserWithNullMember() {
        assert !businessLogic.registerUser(userDtoWithNullMember, "pw123").isPresent();
    }

    @Test
    public void testRegisterUserWithAlreadyRegisteredEmail() {
        businessLogic.registerUser(userDto1, "pw123");
        assert !businessLogic.registerUser(userDto1, "pw123").isPresent();
    }

    @Test
    public void testFindUserByIdSuccessfully() {
        User savedUser = repositories.users().save(user);

        Optional<UserDto> userDtoOptional = businessLogic.findUserById(savedUser.getId());
        assert userDtoOptional.isPresent();
        assert userDtoOptional.get().equals(new UserDto(savedUser));
    }

    @Test
    public void testFindUserByIdWithNotPresentId() {
        repositories.users().save(user);
        assert !businessLogic.findUserById(-1L).isPresent();
    }

    @Test
    public void testFindUserByIdWithNullParam() {
        assert !businessLogic.findUserById(null).isPresent();
    }

    @Test
    public void testUpdateUserSuccessfully() {
        // register new user
        UserDto userDto = userDto1;
        String sessionTokenString = businessLogic.registerUser(userDto, "pw123").get();

        // change sessionToken's expireTime
        SessionToken sessionToken = repositories.sessionTokens().findById(sessionTokenString).get();
        sessionToken.setExpireTime(LocalDateTime.now().plusMinutes(10));
        repositories.sessionTokens().save(sessionToken);

        // update newly created user
        UserDto modifiedUserDto = userDto2;
        assert businessLogic.updateUser(sessionTokenString, modifiedUserDto, "changedPw");

        // get user from DB
        User updatedUser = repositories.users().findById(sessionToken.getUserId()).get();
        modifiedUserDto.setId(updatedUser.getId());

        // check if fields were updated and sessionToken was renewed
        assert modifiedUserDto.equals(new UserDto(updatedUser));
        assert repositories.sessionTokens().findById(sessionTokenString).get().getExpireTime().isAfter(LocalDateTime.now().plusMinutes(25));
    }

    @Test
    public void testUpdateUserWithNullParams() {
        assert !businessLogic.updateUser(null, userDto1, "pw123");
        assert !businessLogic.updateUser("2uh52iu34523uhi", null, "pw123");
        assert !businessLogic.updateUser("2uh52iu34523uhi", userDto1, null);
    }

    @Test
    public void testUpdateUserWithNotPresentSessionToken() {
        assert !businessLogic.updateUser("123ih4hi2uh34", userDto1, "pw123");
    }

    @Test
    public void testUpdateUserWithExpiredSessionToken() {
        String sessionTokenString = businessLogic.registerUser(userDto1, "pw123").get();

        SessionToken sessionToken = repositories.sessionTokens().findById(sessionTokenString).get();
        sessionToken.setExpireTime(LocalDateTime.now().minusHours(1));

        assert !businessLogic.updateUser(sessionTokenString, userDto1, "pw123");
    }

    @Test
    public void testUpdateUserWithNotPresentUserAssociatedToSessionToken() {
        SessionToken sessionToken = new SessionToken("2i346gg45z6", -1L, LocalDateTime.now().plusMinutes(10));
        repositories.sessionTokens().save(sessionToken);

        assert !businessLogic.updateUser(sessionToken.getToken(), userDto1, "pw123");
    }

}