package cc.suvankar.moneytrail.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record UserPrincipal(String email, UUID userId) implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    throw new UnsupportedOperationException("Password not used in UserPrincipal");
  }

  @Override
  public String getUsername() {
    return email;
  }
}
