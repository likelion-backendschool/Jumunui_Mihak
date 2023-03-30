package com.mihak.jumun.menu.repository;

import com.mihak.jumun.menu.entity.Menu;
import com.mihak.jumun.menu.entity.MenuStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuStockRepository extends JpaRepository<MenuStock, Long> {

    Optional<MenuStock> findByMenu(Menu menu);
}
