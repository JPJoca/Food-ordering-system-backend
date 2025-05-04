package raf.rs.domaci.web3.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import raf.rs.domaci.web3.model.User;
import raf.rs.domaci.web3.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsersService implements  IService<User,Long> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public UsersService(UserRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public <S extends User> S save(S var1) {
        if (var1.getPassword() != null && !var1.getPassword().startsWith("$2a$10$")) {
            var1.setPassword(passwordEncoder.encode(var1.getPassword()));
        }
        return this.userRepository.save(var1);
    }

    @Override
    public Optional<User> findById(Long var1) {
        return this.userRepository.findById(var1);
    }


    @Override
    public List<User> findAll() {
        return this.userRepository.findAll();
    }

    @Override
    public void deleteById(Long var1) {
        this.userRepository.deleteById(var1);
    }

}
