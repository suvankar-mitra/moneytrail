package cc.suvankar.moneytrail.auth;

import cc.suvankar.moneytrail.exception.RefreshTokenExpiredException;
import cc.suvankar.moneytrail.exception.RefreshTokenInvalidStateException;
import cc.suvankar.moneytrail.user.UserService;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RefreshTokenService {
  private final RefreshTokenRepository repository;
  private final UserService userService;

  @Value("${refresh_token.validity_days}")
  private int tokenValidityDays;

  public RefreshTokenService(RefreshTokenRepository repository, UserService userService) {
    this.repository = repository;
    this.userService = userService;
  }

  /**
   * Known limitation of this method: if two threads try to access this method at the same time and
   * create a Race condition - then this method will result in inconsistent update.
   *
   * @param userId UUID
   * @return Refresh Token UUID
   */
  @Transactional
  public UUID createOrUpdateToken(@NonNull UUID userId) {
    final OffsetDateTime now = OffsetDateTime.now();

    log.debug("Deleting existing token for user {}.", userId);
    repository.deleteById(userId);

    log.debug("Creating and saving new token for user {}.", userId);
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(UUID.randomUUID());
    refreshToken.setTokenState(RefreshTokenState.ACTIVE);
    refreshToken.setUser(userService.getUserReferenceById(userId));
    refreshToken.setValidFrom(now);
    refreshToken.setValidTo(now.plusDays(tokenValidityDays));
    repository.save(refreshToken);

    log.debug("Returning valid token for user {}.", userId);
    return refreshToken.getToken();
  }

  @Transactional(readOnly = true)
  public RefreshToken getToken(@NonNull UUID token) {
    var refreshToken =
        repository
            .findByToken(token)
            .orElseThrow(() -> new RefreshTokenInvalidStateException("Token not found."));

    if (refreshToken.getTokenState() != RefreshTokenState.ACTIVE) {
      throw new RefreshTokenInvalidStateException(
          "Current state " + refreshToken.getTokenState() + " for token: " + token);
    }

    OffsetDateTime now = OffsetDateTime.now();
    boolean expired = !refreshToken.getValidTo().isAfter(now);
    boolean notYetValid = !refreshToken.getValidFrom().isBefore(now);

    if (expired || notYetValid) {
      throw new RefreshTokenExpiredException(
          "Token has expired for user: " + refreshToken.getUser().getId());
    }

    log.debug("Returning token for user {}.", refreshToken.getUser().getId());
    return refreshToken;
  }

  @Transactional
  public void invalidateToken(@NonNull UUID token) {
    var refreshTokenOptional = repository.findByToken(token);

    if (refreshTokenOptional.isEmpty()) {
      return;
    }

    var refreshToken = refreshTokenOptional.get();

    log.debug("Invalidating token for user {}.", refreshToken.getUser().getId());
    refreshToken.setTokenState(RefreshTokenState.REVOKED);
  }
}
