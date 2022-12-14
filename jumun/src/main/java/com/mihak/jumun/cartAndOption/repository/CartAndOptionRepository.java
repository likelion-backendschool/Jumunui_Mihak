package com.mihak.jumun.cartAndOption.repository;

import com.mihak.jumun.cart.entity.Cart;
import com.mihak.jumun.cartAndOption.entity.CartAndOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartAndOptionRepository extends JpaRepository<CartAndOption, Long> {
    List<CartAndOption> findByCart(Cart cart);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CartAndOption cao WHERE cao.cart = :cart")
    void deleteAllByCart(@Param("cart") Cart cart);
}
