package cc.suvankar.moneytrail.exception;

public class IllegalCurrencyCodeException extends RuntimeException {
  public IllegalCurrencyCodeException(String msg) {
    super(msg);
  }
}
