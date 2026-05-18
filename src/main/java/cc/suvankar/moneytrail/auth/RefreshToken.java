package cc.suvankar.moneytrail.auth;

import cc.suvankar.moneytrail.user.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
  @Id
  @Column(name = "user_id")
  private UUID id;

  @MapsId
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "token", unique = true, nullable = false)
  private UUID token;

  @Column(name = "valid_from", nullable = false, updatable = false)
  private OffsetDateTime validFrom;

  @Column(name = "valid_to", nullable = false)
  private OffsetDateTime validTo;

  @Enumerated(EnumType.STRING)
  @Column(name = "token_state", nullable = false)
  private RefreshTokenState tokenState;
}
