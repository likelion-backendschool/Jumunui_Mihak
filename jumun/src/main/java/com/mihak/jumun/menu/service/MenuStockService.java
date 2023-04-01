package com.mihak.jumun.menu.service;

import com.mihak.jumun.menu.entity.Menu;
import com.mihak.jumun.menu.entity.MenuStock;
import com.mihak.jumun.menu.exception.MenuStockNotFoundException;
import com.mihak.jumun.menu.repository.MenuStockRepository;
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

    public void decreaseQuantity(Menu menu, Long count) {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

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
                sleep();
            }
        }
    }

    public void decrease(Menu menu, Long count) {
        MenuStock menuStock = menuStockRepository.findByMenuWithOptimisticLock(menu).orElseThrow(MenuStockNotFoundException::new);
        menuStock.decrease(count);
    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.info("Thread {} interrupted, terminating", Thread.currentThread().getName());
            return;
        }
        log.info("Thread {} finished successfully", Thread.currentThread().getName());
    }
}
