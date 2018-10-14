package com.geanik.user_session_backend.dal;

import com.geanik.user_session_backend.domain.SessionToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionTokenRepository extends JpaRepository<SessionToken, String> {
}
