package com.geanik.user_session_backend.logic;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.geanik.user_session_backend.LogicConfig;
import com.geanik.user_session_backend.dal.Repositories;
import com.geanik.user_session_backend.domain.SessionToken;
import com.geanik.user_session_backend.domain.User;
import com.geanik.user_session_backend.domain.dto.UserDto;

@SpringBootTest(classes = LogicConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@RunWith(SpringRunner.class)
public class BusinessLogicImplTest {

    @Autowired
    Repositories repositories;

    @Autowired
    BusinessLogic businessLogic;

    private UserDto userDto1;
    private UserDto userDto2;
    private UserDto userDtoWithNullMember;

    @Before
    public void setUp() throws Exception {
        userDto1 = new UserDto(0L,"geanik", "g", "eanik", "geanik@mail.com");
        userDto2 = new UserDto(0L, "herminator", "Hermann", "Maier", "h.m@mail.com");
        userDtoWithNullMember = new UserDto(0L,"pedge", "john", null, "johnny@mail.com");
    }

    @Test
    public void testAuthenticateUserSuccessfully() {
        businessLogic.registerUser(userDto1, "password");
        Optional<User> savedUser = repositories.users().findByEmail(userDto1.getEmail());
        assert savedUser.isPresent();
        assert businessLogic.authenticateUser(savedUser.get().getEmail(), "password").isPresent();
    }

    @Test
    public void testAuthenticateUserWithNullParams() {
        assert !businessLogic.authenticateUser(null, "pw123").isPresent();
        assert !businessLogic.authenticateUser("geanik@mail.at", null).isPresent();
    }

    @Test
    public void testAuthenticateUserWithWrongPassword() {
        businessLogic.registerUser(userDto1, "password");
        Optional<User> savedUser = repositories.users().findByEmail(userDto1.getEmail());
        assert savedUser.isPresent();
        assert !businessLogic.authenticateUser(savedUser.get().getEmail(), "wrongPw").isPresent();
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
        businessLogic.registerUser(userDto1, "password");
        Optional<User> savedUser = repositories.users().findByEmail(userDto1.getEmail());
        assert savedUser.isPresent();

        Optional<UserDto> userDtoOptional = businessLogic.findUserById(savedUser.get().getId());
        assert userDtoOptional.isPresent();
        assert userDtoOptional.get().equals(new UserDto(savedUser.get()));
    }

    @Test
    public void testFindUserByIdWithNotPresentId() {
        businessLogic.registerUser(userDto1, "password");
        Optional<User> savedUser = repositories.users().findByEmail(userDto1.getEmail());
        assert savedUser.isPresent();
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

    @Test
    public void testOnlyUpdatesNonNullValuesForUserUpdate() {
        Optional<String> sessionTokenString = businessLogic.registerUser(userDto1, "password");

        assert sessionTokenString.isPresent();

        // username set to null
        UserDto dto = createFullUserDto();
        String expected = dto.getUsername();
        dto.setUsername(null);

        businessLogic.updateUser(sessionTokenString.get(), dto, "password");
        Optional<User> foundUser = repositories.users().findByEmail(dto.getEmail());

        assert foundUser.isPresent();
        assert foundUser.get().getUsername().equals(expected);

        // firstName set to null
        dto = createFullUserDto();
        expected = dto.getFirstName();
        dto.setFirstName(null);

        businessLogic.updateUser(sessionTokenString.get(), dto, "password");
        foundUser = repositories.users().findByEmail(dto.getEmail());

        assert foundUser.isPresent();
        assert foundUser.get().getFirstName().equals(expected);

        // lastName set to null
        dto = createFullUserDto();
        expected = dto.getLastName();
        dto.setLastName(null);

        businessLogic.updateUser(sessionTokenString.get(), dto, "password");
        foundUser = repositories.users().findByEmail(dto.getEmail());

        assert foundUser.isPresent();
        assert foundUser.get().getLastName().equals(expected);

        // email set to null
        // todo can not be tested right now as no search for user other than via email is possible
        // dto = createFullUserDto();
        // expected = dto.getEmail();
        // dto.setEmail(null);
        //
        // businessLogic.updateUser(sessionTokenString.get(), dto, "password");
        // foundUser = repositories.users().findByEmail(dto.getEmail());

        //assert foundUser.isPresent();
        // assert foundUser.get().getEmail().equals(expected);
    }

    private UserDto createFullUserDto() {
        return new UserDto(0L,"geanik", "g", "eanik", "geanik@mail.com");
    }
}