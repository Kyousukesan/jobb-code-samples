<?php

namespace app\common\tools;

use support\Redis;

class RedisLock
{

    // 加锁
    public static function acquire($key, $value, $expire = 10): bool {
        if (Redis::setnx($key, $value)) {
            Redis::expire($key, $expire);
            return true;
        }
        return false;
    }

    // 自旋锁
    public static function acquireBlocking($key, $value, $expire = 10, $timeout = 5): bool {
        $start = time();
        while ((time() - $start) < $timeout) {
            if (self::acquire($key, $value, $expire)) {
                return true;
            }
            usleep(100000); // 等待100ms重试
        }
        return false;
    }

    // 释放锁
    public static function release($key, $value): bool {
        $lua = '
            if redis.call("GET", KEYS[1]) == ARGV[1] then
                return redis.call("DEL", KEYS[1])
            else
                return 0
            end';
        return Redis::eval($lua, 1, $key, $value);
    }

    //强制释放
    public static function forceRelease($key): bool {
        return Redis::del($key) > 0;
    }
}