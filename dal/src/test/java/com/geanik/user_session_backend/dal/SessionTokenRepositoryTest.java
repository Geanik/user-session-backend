package com.geanik.user_session_backend.dal;

import com.geanik.user_session_backend.DalConfig;
import com.geanik.user_session_backend.domain.SessionToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@SpringBootTest(classes = DalConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@RunWith(SpringRunner.class)
public class SessionTokenRepositoryTest {

    @Autowired
    SessionTokenRepository sessionTokens;

    @Test
    public void testSave() {
        SessionToken sessionToken = new SessionToken("asd", 123L);
        sessionTokens.save(sessionToken);

        assert sessionToken.equals(sessionTokens.findById(sessionToken.getToken()).get());
    }
}