package cc.suvankar.moneytrail.exception;

public class AccountModificationNotAllowedException extends RuntimeException {
  public AccountModificationNotAllowedException(String msg) {
    super(msg);
  }
}
