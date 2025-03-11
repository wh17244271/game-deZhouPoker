package com.dezhou.poker.security;

import com.dezhou.poker.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 用户主体类，实现Spring Security的UserDetails接口
 */
public class UserPrincipal implements UserDetails {
    private Long id;
    private String username;
    
    @JsonIgnore
    private String password;
    
    private BigDecimal currentChips;
    private Integer totalGames;
    private Integer wins;
    
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String password, BigDecimal currentChips,
                         Integer totalGames, Integer wins, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.currentChips = currentChips;
        this.totalGames = totalGames;
        this.wins = wins;
        this.authorities = authorities;
    }

    /**
     * 创建UserPrincipal实例
     *
     * @param user 用户实体
     * @return UserPrincipal实例
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getCurrentChips(),
                user.getTotalGames(),
                user.getWins(),
                authorities
        );
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getCurrentChips() {
        return currentChips;
    }

    public Integer getTotalGames() {
        return totalGames;
    }

    public Integer getWins() {
        return wins;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
