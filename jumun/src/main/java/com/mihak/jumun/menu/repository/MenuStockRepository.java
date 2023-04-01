package com.mihak.jumun.menu.repository;

import com.mihak.jumun.menu.entity.Menu;
import com.mihak.jumun.menu.entity.MenuStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface MenuStockRepository extends JpaRepository<MenuStock, Long> {

    Optional<MenuStock> findByMenu(Menu menu);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("select ms from MenuStock ms where ms.menu = :menu")
    Optional<MenuStock> findByMenuWithPessimisticLock(Menu menu);

    @Lock(value = LockModeType.OPTIMISTIC)
    @Query("select ms from MenuStock ms where ms.menu = :menu")
    Optional<MenuStock> findByMenuWithOptimisticLock(Menu menu);
}
