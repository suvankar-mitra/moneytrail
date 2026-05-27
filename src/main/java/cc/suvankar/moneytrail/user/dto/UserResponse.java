package cc.suvankar.moneytrail.user.dto;

import cc.suvankar.moneytrail.user.User;

public record UserResponse(String name, String email) {
  public static UserResponse from(User user) {
    return new UserResponse(user.getName(), user.getEmail());
  }
}
