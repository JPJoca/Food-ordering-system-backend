package raf.rs.domaci.web3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import raf.rs.domaci.web3.model.ErrorMessage;

@Repository
public interface ErrorMessageRepository extends JpaRepository<ErrorMessage, Long> {
}
