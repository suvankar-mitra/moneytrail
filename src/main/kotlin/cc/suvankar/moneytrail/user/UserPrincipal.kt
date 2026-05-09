package cc.suvankar.moneytrail.user

import java.util.UUID
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(val email: String, val userId: UUID) : UserDetails {
  override fun getAuthorities(): Collection<GrantedAuthority?>? {
    return listOf()
  }

  override fun getPassword(): String? {
    throw UnsupportedOperationException("Password not used")
  }

  override fun getUsername(): String? {
    return email
  }
}
