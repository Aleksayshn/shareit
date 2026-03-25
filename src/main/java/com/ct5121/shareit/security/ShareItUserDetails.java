package com.ct5121.shareit.security;

import com.ct5121.shareit.user.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class ShareItUserDetails implements UserDetails {
    private final Long id;
    private final String name;
    private final String email;
    private final String password;

    public ShareItUserDetails(Long id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public static ShareItUserDetails from(User user) {
        return new ShareItUserDetails(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }
}
