package cc.suvankar.moneytrail.exception;

public class AccountHasTransactionAssociatedException extends RuntimeException {
  public AccountHasTransactionAssociatedException(String msg) {
    super(msg);
  }
}
