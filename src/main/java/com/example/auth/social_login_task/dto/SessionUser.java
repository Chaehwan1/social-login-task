package com.example.auth.social_login_task.dto;

import com.example.auth.social_login_task.domain.User;
import lombok.Getter;

import java.io.Serializable;

// Serializable: 세션에 저장하기 위해 직렬화를 구현합니다.
@Getter
public class SessionUser implements Serializable {
    private String name;
    private String email;
    private String socialId;

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.socialId = user.getSocialId();
    }
}