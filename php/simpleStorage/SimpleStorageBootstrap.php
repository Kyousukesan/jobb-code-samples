<?php

namespace app\common\simpleStorage;


use app\common\simpleStorage\storageArrange\StorageArrangeInterface;
use DI\Container;
use JmesPath\AstRuntime;
use JmesPath\CompilerRuntime;
use Webman\Bootstrap;
use Workerman\Worker;

class SimpleStorageBootstrap
{

    private const STORAGE_ARRANGE_CONFIG = 'simple_storage.storage_arrange';

    public static function start(): array
    {
        $allStorageArrange = [];
        $storageArrangeClasses = config(self::STORAGE_ARRANGE_CONFIG, []);
        foreach ($storageArrangeClasses as $storageArrangeClass) {
            $allStorageArrange[$storageArrangeClass] = new $storageArrangeClass();
        }

        return [
            $allStorageArrange,
            new AstRuntime(),
            new CompilerRuntime()
        ];
    }
}