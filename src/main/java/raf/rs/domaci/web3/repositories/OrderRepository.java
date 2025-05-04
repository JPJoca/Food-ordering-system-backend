package raf.rs.domaci.web3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import raf.rs.domaci.web3.model.Order;
import raf.rs.domaci.web3.model.Status;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    @Query("SELECT o FROM Order o WHERE (:userId IS NULL OR o.createdBy.id = :userId)")
    List<Order> findByUserIdOrAll(@Param("userId") Long userId);


    @Query("SELECT o FROM Order o WHERE (:userId IS NULL OR o.createdBy.id = :userId) AND (:statuses IS NULL OR o.status IN :statuses)")
    List<Order> findByStatuses(@Param("userId") Long userId, @Param("statuses") List<Status> statuses);
}
