package com.example.auth.social_login_task.domain;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_account")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String socialId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public User(String socialId, String name, String email, Role role) {
        this.socialId = socialId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public User update(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }
}
