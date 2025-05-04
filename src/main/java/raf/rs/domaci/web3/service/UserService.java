package raf.rs.domaci.web3.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import raf.rs.domaci.web3.model.User;
import raf.rs.domaci.web3.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User myUser = this.userRepository.findByEmail(username);

        if(myUser == null) {
            throw new UsernameNotFoundException("User email" + username + " not found");
        }

        List<GrantedAuthority> authorities = myUser.getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                .collect(Collectors.toList());

        System.out.println(authorities);
        return new org.springframework.security.core.userdetails.User(myUser.getEmail(), myUser.getPassword(), authorities);
    }

}
