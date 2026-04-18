package cc.suvankar.moneytrail.user;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserReferenceById(UUID id) {
        return userRepository.getReferenceById(id);
    }
}
