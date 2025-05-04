package raf.rs.domaci.web3.controler;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import raf.rs.domaci.web3.model.User;
import raf.rs.domaci.web3.service.UserService;
import raf.rs.domaci.web3.service.UsersService;

import java.util.List;
import java.util.Optional;


@CrossOrigin
@RestController
@RequestMapping("api/user")
public class UserController {

    private final UsersService usersService;

    public UserController(UserService userService, UsersService usersService) {
        this.usersService = usersService;

    }
    // can_create_users, can_read_users, can_update_users, can_delete_users
    @PreAuthorize("hasAuthority('can_read_users')")
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getAllUsers() {
        System.out.println("Get all users");
        return  this.usersService.findAll();
    }

    @PreAuthorize("hasAuthority('can_update_users')")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        System.out.println("Get user with id: " + id);
        Optional<User> user = this.usersService.findById(id);

        if(user.isPresent()) {
            System.out.println("User sa ID: " + id + ", je uspesno prikazan");
            return ResponseEntity.ok(user.get());
        }else{
            System.out.println("Greska");
            return ResponseEntity.notFound().build();
        }

    }

    @PreAuthorize("hasAuthority('can_create_users')")
    @PostMapping(value = "/add",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createUser(@RequestBody User user) {
        System.out.println("Add user: " + user.getEmail());
        User savedUser = usersService.save(user);
        System.out.println("User je uspesno dodat");
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PreAuthorize("hasAuthority('can_update_users')")
    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        System.out.println("Update user: " + user.getEmail());
        Optional<User> existingUser = usersService.findById(id);
        if (existingUser.isPresent()) {
            User updatedUser = usersService.save(user);
            System.out.println("User sa ID: " + id + ", je uspesno azuriran");
            return ResponseEntity.ok(updatedUser);
        } else {
            System.out.println("Greska");
            return ResponseEntity.notFound().build();
        }
    }
    @PreAuthorize("hasAuthority('can_delete_users')")
    @DeleteMapping(value ="/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {

        System.out.println("User sa ID: " + id + ", je uspesno obrisan");
        this.usersService.deleteById(id);

        return ResponseEntity.ok().build();
    }
}
