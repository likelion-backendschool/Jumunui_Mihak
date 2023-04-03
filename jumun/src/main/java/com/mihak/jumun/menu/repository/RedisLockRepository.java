package com.mihak.jumun.menu.repository;

import com.mihak.jumun.menu.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLockRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Boolean lock(Menu menu) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(generateKey(menu), "lock", Duration.ofMillis(3_000));
    }

    public Boolean unLock(Menu menu) {
        return redisTemplate
                .delete(generateKey(menu));
    }

    private String generateKey(Menu menu) {
        return menu.toString();
    }
}
