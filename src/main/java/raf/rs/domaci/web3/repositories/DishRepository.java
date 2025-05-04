package raf.rs.domaci.web3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import raf.rs.domaci.web3.model.Dish;
import raf.rs.domaci.web3.model.User;



@Repository
public interface DishRepository  extends JpaRepository<Dish, Long> {

}
