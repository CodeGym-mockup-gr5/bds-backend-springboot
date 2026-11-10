package com.codegym.bds_backend.auth;

import java.util.List;

public record JwtPrincipal(Long userId, String username, List<String> roles) {
}
