package cc.suvankar.moneytrail.exception;

public class AccountCreationForInvalidAccountTypeException extends RuntimeException {
  public AccountCreationForInvalidAccountTypeException(String msg) {
    super(msg);
  }
}
