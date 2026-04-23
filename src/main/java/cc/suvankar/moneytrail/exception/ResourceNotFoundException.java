package cc.suvankar.moneytrail.exception;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }

  public static ResourceNotFoundException forAccount() {
    return new ResourceNotFoundException("Account not found.");
  }

  public static ResourceNotFoundException forContact() {
    return new ResourceNotFoundException("Contact not found.");
  }

  public static ResourceNotFoundException forUser() {
    return new ResourceNotFoundException("User not found.");
  }
}
