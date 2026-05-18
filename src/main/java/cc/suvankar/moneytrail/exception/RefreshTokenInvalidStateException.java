package cc.suvankar.moneytrail.exception;

public class RefreshTokenInvalidStateException extends RuntimeException {
  public RefreshTokenInvalidStateException(String msg) {
    super(msg);
  }
}
