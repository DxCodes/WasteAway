package csd.app.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository users;

    @Autowired
    private UserInfoRepository userInfos;

    public List<User> listUsers() {
        return users.findAll();
    }

    public User getUser(Long id) {
        if (users.existsById(id)) {
            return users.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        }
        return null;
    }

    public User getUserByUsername(String username) {
        if (users.existsByUsername(username)) {
            return users.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Error: User not found."));
        }
        return null;
    }

    public User getUserByEmail(String email) {
        if (users.existsByEmail(email)) {
            return users.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Error: User not found."));
        }
        return null;
    }

    public User addUser(User user) {
        if (users.existsByUsername(user.getUsername()) || users.existsByEmail(user.getEmail())) {
            return null;
        }
        return users.save(user);
    }

    public UserInfo addUserInfo(UserInfo userInfo) {
        if (userInfos.findById(userInfo.getId()).orElse(null) != null) {
            return null;
        }
        return userInfos.save(userInfo);
    }
}