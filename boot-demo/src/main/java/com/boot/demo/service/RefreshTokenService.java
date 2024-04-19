package com.boot.demo.service;

import com.boot.demo.entity.RefreshToken;
import com.boot.demo.entity.User;
import com.boot.demo.repository.RefreshTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected Token"));
    }

    public RefreshToken findByUser(User user) {
        return refreshTokenRepository.findByUser(user)
                .orElse(null);
    }

    public void saveToken(RefreshToken refreshToken){
        refreshTokenRepository.save(refreshToken);
    }

    public void removeToken(String refreshToken) {
        RefreshToken findToken = refreshTokenRepository
                .findByRefreshToken(refreshToken)
                .orElseThrow(EntityNotFoundException::new);

        refreshTokenRepository.delete(findToken);
    }
}
