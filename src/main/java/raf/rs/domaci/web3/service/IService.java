package raf.rs.domaci.web3.service;

import java.util.List;
import java.util.Optional;

public interface IService <T,ID>{
    <S extends T> S save(S var1);

    Optional<T> findById(ID var1);

    List<T> findAll();

    void deleteById(ID var1);
}
