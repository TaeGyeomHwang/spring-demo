package com.boot.demo.service;

import com.boot.demo.dto.LoginDto;
import com.boot.demo.dto.TokenRequest;
import com.boot.demo.dto.TokenResponse;
import com.boot.demo.dto.UserFormDto;
import com.boot.demo.entity.RefreshToken;
import com.boot.demo.entity.User;
import com.boot.demo.jwt.TokenProvider;
import com.boot.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    private final TokenProvider tokenProvider;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public boolean validateUser(UserFormDto dto) {
        return userRepository.findById(dto.getId()).orElse(null) == null;
    }

    public void signUp(UserFormDto dto) {
        if (!validateUser(dto)) throw new RuntimeException("해당 ID는 사용할 수 없습니다.");

        User saveUser = User.createUser(dto, passwordEncoder);
        userRepository.save(saveUser);
    }

    public TokenResponse tokenRefresh(TokenRequest request) throws IllegalAccessException {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new IllegalAccessException("Unexpected token");
        }

        RefreshToken refreshToken = refreshTokenService
                .findByRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();

        String accessToken = tokenProvider.createAccessToken(user, Duration.ofHours(2));
        String newRefreshToken =
                refreshToken.update(tokenProvider
                        .createRefreshToken(Duration.ofDays(1)))
                        .getRefreshToken();

        return new TokenResponse(accessToken, newRefreshToken, user.getRole().getKey());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저입니다."));
    }

    public TokenResponse login(LoginDto dto) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(dto.getId(), dto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

//       해당 유저를 조회한다
        User user = userRepository.findById(authentication.getName())
                .orElseThrow(EntityNotFoundException::new);
//       해당 유저와 매칭된 리프레시 토큰을 조회한다
        String newRefreshToken = tokenProvider.createRefreshToken(Duration.ofDays(1));
        RefreshToken existRefreshToken = refreshTokenService.findByUser(user);
//       없다면 토큰을 생성, 저장
        if (existRefreshToken == null) {
            refreshTokenService.saveToken(new RefreshToken(user, newRefreshToken));
        } else {
//       있다면 update
            existRefreshToken.update(newRefreshToken);
        }
//       엑세스 토큰을 발급하고 TokenResponse 반환
        String accessToken = tokenProvider.createAccessToken(user, Duration.ofHours(2));

        return new TokenResponse(accessToken, newRefreshToken, user.getRole().getKey());
    }

    public void logout(TokenRequest request) {
        refreshTokenService.removeToken(request.getRefreshToken());
    }

}
