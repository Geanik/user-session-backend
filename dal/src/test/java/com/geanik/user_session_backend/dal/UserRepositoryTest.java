package com.geanik.user_session_backend.dal;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.geanik.user_session_backend.DalConfig;
import com.geanik.user_session_backend.domain.User;

@SpringBootTest(classes = DalConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@RunWith(SpringRunner.class)
public class UserRepositoryTest {

    @Autowired
    UserRepository users;

    @Test
    public void testSave() {
        User user = new User("geanik", "g", "eanik", "geanik@mail.com");
        users.save(user);

        assert user.getId() != 0;
        assert user.equals(users.findById(user.getId()).get());
    }
}