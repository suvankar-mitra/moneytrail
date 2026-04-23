package cc.suvankar.moneytrail.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
public class UserPrincipal implements UserDetails {

  private final @NonNull String email;
  private final @NonNull UUID userId;

  public UserPrincipal(@NonNull String email, @NonNull UUID userId) {
    this.email = email;
    this.userId = userId;
  }

  public @NonNull UUID getUserId() {
    return userId; // Null type safety: The expression of type 'UUID' needs unchecked conversion to
    // conform to '@NonNull UUID'Java(16778128)
  }

  @SuppressWarnings("null")
  @Override
  public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    throw new UnsupportedOperationException("Password not used");
  }

  @Override
  public @NonNull String getUsername() {
    return email; // Null type safety: The expression of type 'String' needs unchecked conversion
    // to conform to '@NonNull String'Java(16778128)
  }
}
