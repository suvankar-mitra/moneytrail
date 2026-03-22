package cc.suvankar.moneytrail.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;

@Service
public class MoneyTrailUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MoneyTrailUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User foundUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Email " + username + " not found!"));

        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(foundUser.getPasswordHash())
                .authorities("ROLE_USER")
                .build();

    }
}
