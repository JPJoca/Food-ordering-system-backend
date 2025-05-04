package raf.rs.domaci.web3.controler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import raf.rs.domaci.web3.model.Dish;
import raf.rs.domaci.web3.service.DishService;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("api/dishes")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping("/all")
    public List<Dish> getAllDishes() {
        return this.dishService.findAll();
    }
}
