package com.example.auth.social_login_task.controller;

import com.example.auth.social_login_task.dto.SessionUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@RequiredArgsConstructor
@Controller
public class HomeController {

    private final HttpSession httpSession;

    // Spring Security를 통해 인증된 사용자 정보를 받아옵니다.
    @GetMapping("/")
    public String home(Model model) {

        SessionUser user = (SessionUser) httpSession.getAttribute("user"); // 세션에서 SessionUser 객체 로드

        String loginStatus;

        if (user != null) {
            // 로그인 상태인 경우: DTO에서 이름을 바로 가져옵니다.
            loginStatus = user.getName() + "님, 환영합니다!";
        } else {
            // 로그아웃 상태인 경우
            loginStatus = "로그인이 필요합니다.";
        }

        model.addAttribute("loginStatus", loginStatus);

        return "home";
    }
}