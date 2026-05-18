package cc.suvankar.moneytrail.exception;

public class RefreshTokenExpiredException extends RuntimeException {
  public RefreshTokenExpiredException(String msg) {
    super(msg);
  }
}
