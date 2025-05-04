package raf.rs.domaci.web3.service;

import org.springframework.stereotype.Service;
import raf.rs.domaci.web3.model.Dish;
import raf.rs.domaci.web3.repositories.DishRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DishService implements IService<Dish, Long> {

    private final DishRepository dishRepository;

    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    @Override
    public <S extends Dish> S save(S var1) {
        return this.dishRepository.save(var1);
    }

    @Override
    public Optional<Dish> findById(Long var1) {
        return this.dishRepository.findById(var1);
    }

    @Override
    public List<Dish> findAll() {
        return this.dishRepository.findAll();
    }

    @Override
    public void deleteById(Long var1) {
        this.dishRepository.deleteById(var1);
    }

    public List<Dish> findAllById(List<Long> ids) {
        return dishRepository.findAllById(ids);
    }


}
