package com.mihak.jumun.menu.service;

import com.mihak.jumun.menu.entity.Menu;
import com.mihak.jumun.menu.entity.MenuStock;
import com.mihak.jumun.menu.exception.MenuStockNotFoundException;
import com.mihak.jumun.menu.exception.StockLockTakeFailedException;
import com.mihak.jumun.menu.repository.MenuStockRepository;
import com.mihak.jumun.menu.repository.RedisLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuStockService {

    private final MenuStockRepository menuStockRepository;
    private final RedisLockRepository redisLockRepository;
    private final PlatformTransactionManager transactionManager;

    @Transactional
    public MenuStock save(Menu menu, Long quantity) {

        MenuStock menuStock = MenuStock.builder()
                .menu(menu)
                .quantity(quantity)
                .build();

        return menuStockRepository.save(menuStock);
    }

    public MenuStock findByMenu(Menu menu) {
        return menuStockRepository.findByMenu(menu).orElseThrow(MenuStockNotFoundException::new);
    }

    public Boolean remainsQuantity(Menu menu, Long count) {
        MenuStock menuStock = findByMenu(menu);

        return menuStock.isEnoughCount(count);
    }

    // 낙관적 락
    public void decreaseQuantity(Menu menu, Long count) {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        int retries = 10;
        int delay = 1000;

        while (true) {
            try {
                log.info("call decrease");
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        decrease(menu, count);
                    }
                });
                break;
            } catch (Exception e) {
                retries -= 1;
                if (retries == 0) {
                    throw new StockLockTakeFailedException("MenuStock 낙관적 락 획득 실패");
                }

                delay += 1000;
                sleep(delay);
            }
        }
    }

    // Lettuce
    @Transactional
    public void decreaseWithLettuce(Menu menu, Long count) {
        while (!redisLockRepository.lock(menu)) {
            sleep(100);
        }

        try {
            MenuStock menuStock = menuStockRepository.findByMenu(menu).orElseThrow(MenuStockNotFoundException::new);
            menuStock.decrease(count);
        } finally {
            redisLockRepository.unLock(menu);
        }
    }

    public void decrease(Menu menu, Long count) {
        MenuStock menuStock = menuStockRepository.findByMenuWithOptimisticLock(menu).orElseThrow(MenuStockNotFoundException::new);
        menuStock.decrease(count);
    }

    private void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.info("Thread {} interrupted, terminating", Thread.currentThread().getName());
            return;
        }
        log.info("Thread {} finished successfully", Thread.currentThread().getName());
    }
}
