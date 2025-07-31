<?php

namespace app\common\tools;

use support\Redis;

class RedisLock
{

    // ロック取得
    public static function acquire($key, $value, $expire = 10): bool {
        if (Redis::setnx($key, $value)) {
            Redis::expire($key, $expire);
            return true;
        }
        return false;
    }

    // スピンロック
    public static function acquireBlocking($key, $value, $expire = 10, $timeout = 5): bool {
        $start = time();
        while ((time() - $start) < $timeout) {
            if (self::acquire($key, $value, $expire)) {
                return true;
            }
            usleep(100000); // 100ms待機してリトライ
        }
        return false;
    }

    // ロック解放
    public static function release($key, $value): bool {
        $lua = '
            if redis.call("GET", KEYS[1]) == ARGV[1] then
                return redis.call("DEL", KEYS[1])
            else
                return 0
            end';
        return Redis::eval($lua, 1, $key, $value);
    }

    // 強制解放
    public static function forceRelease($key): bool {
        return Redis::del($key) > 0;
    }
}