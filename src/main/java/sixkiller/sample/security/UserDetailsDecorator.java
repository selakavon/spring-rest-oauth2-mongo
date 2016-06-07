package sixkiller.sample.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sixkiller.sample.domain.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class UserDetailsDecorator implements UserDetails {

    public static final String ROLES_PREFIX = "ROLE_";

    private User user;

    public UserDetailsDecorator(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roles[] = user.getRoles();

        if (roles == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(roles).map(
                role -> (GrantedAuthority) () -> ROLES_PREFIX + role
        ).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
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

}
