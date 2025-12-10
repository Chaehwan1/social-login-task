package com.example.auth.social_login_task.service;

import com.example.auth.social_login_task.domain.Role;
import com.example.auth.social_login_task.domain.User;
import com.example.auth.social_login_task.dto.SessionUser;
import com.example.auth.social_login_task.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor // Lombok: final 필드를 사용하는 생성자를 자동 생성 (의존성 주입)
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final HttpSession httpSession; // 세션 관리를 위해 추가

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // [1] 기본 DefaultOAuth2UserService를 사용하여 사용자 정보를 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // [2] 로그인 서비스 이름 및 키 확인
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "kakao"
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName(); // "id" (카카오 고유 ID)

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // ----------------------------------------------------
        // [3] 카카오 사용자 정보 추출 및 DB 저장 로직 (핵심)
        // ----------------------------------------------------

        // 카카오는 사용자 정보가 한 겹 더 감싸져 있음 (response.get("id"))
        String socialId = attributes.get(userNameAttributeName).toString(); // 카카오 고유 ID

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String name = profile.get("nickname").toString();
        String email = (kakaoAccount.get("email") != null) ?
                (String) kakaoAccount.get("email") :
                "kakao-temp-no-email-" + socialId + "@test.com";

        User user = saveOrUpdate(socialId, name, email);

        httpSession.setAttribute("user", new SessionUser(user));

        // ----------------------------------------------------

        // [4] 최종적으로 Spring Security에게 제공할 User 객체 반환
        return new DefaultOAuth2User(
                // 사용자 권한 (Role)
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                // 사용자 정보 속성 (카카오에서 받은 원본)
                attributes,
                // 사용자 ID로 사용할 속성 키
                userNameAttributeName
        );
    }

    // DB에 사용자 정보가 있으면 업데이트하고, 없으면 새로 저장하는 메서드
    private User saveOrUpdate(String socialId, String name, String email) {
        User user = userRepository.findBySocialId(socialId)
                .map(entity -> entity.update(name, email)) // 기존 사용자: 이름/이메일 업데이트
                .orElse(User.builder() // 새 사용자: 새로 생성
                        .socialId(socialId)
                        .name(name)
                        .email(email)
                        .role(Role.USER)
                        .build());

        return userRepository.save(user); // DB에 저장 및 반환
    }
}