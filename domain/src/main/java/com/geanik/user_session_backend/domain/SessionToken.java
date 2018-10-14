package com.geanik.user_session_backend.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class SessionToken {

    @Id
    private String token;

    private Long userId;

    private LocalDateTime expireTime;

    public SessionToken() {
    }

    public SessionToken(String token, Long userId) {
        this.token = token;
        this.userId = userId;
    }

    public SessionToken(String token, Long userId, LocalDateTime expireTime) {
        this.token = token;
        this.userId = userId;
        this.expireTime = expireTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionToken that = (SessionToken) o;
        return Objects.equals(token, that.token) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(expireTime, that.expireTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, userId, expireTime);
    }


    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

}
