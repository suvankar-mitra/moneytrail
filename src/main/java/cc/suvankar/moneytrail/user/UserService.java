package cc.suvankar.moneytrail.user;

import java.util.UUID;

import cc.suvankar.moneytrail.exception.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User getUserReferenceById(@NonNull UUID id) {
    return userRepository.getReferenceById(id);
  }

  public User getUserById(@NonNull UUID id) {
    return userRepository.findById(id).orElseThrow(ResourceNotFoundException::forUser);
  }
}
