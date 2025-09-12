package org.example.expert.domain.common.dto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public class AuthUser {

    private final Long id;
    private final String email;
    private final UserRole userRole;
    private final String nickname;

    public AuthUser(Long id, String email, UserRole userRole, String nickname) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
        this.nickname = nickname;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRole.getUserRole()));
    }
}
