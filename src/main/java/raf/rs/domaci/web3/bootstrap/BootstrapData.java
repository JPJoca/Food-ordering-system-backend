package raf.rs.domaci.web3.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import raf.rs.domaci.web3.model.Permission;
import raf.rs.domaci.web3.model.User;
import raf.rs.domaci.web3.repositories.UserRepository;

import java.util.Set;

@Component
public class BootstrapData implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Hello");
        // Provera da li korisnik već postoji
        if (userRepository.findByEmail("admin1@admin.com") == null) {
            // Kreiranje novog korisnika


            User admin1User = new User();
            admin1User.setEmail("admin1@admin.com");
            admin1User.setPassword(passwordEncoder.encode("admin"));
            admin1User.setName("Admin");
            admin1User.setSurname("Admin");
            admin1User.setPermissions(Set.of(
                    Permission.can_read_users, Permission.can_delete_users, Permission.can_update_users, Permission.can_create_users,
                    Permission.can_cancel_order, Permission.can_place_order, Permission.can_schedule_order, Permission.can_search_order,
                    Permission.can_search_order_all,
                    Permission.can_track_order
            ));
            userRepository.save(admin1User);


        } else{
            System.out.println("Postoji");
        }



    }
}
