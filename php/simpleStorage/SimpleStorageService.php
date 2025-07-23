<?php

namespace app\common\simpleStorage;

use app\common\BaseService;
use app\common\simpleStorage\storageArrange\StorageArrangeInterface;
use JmesPath\AstRuntime;
use JmesPath\CompilerRuntime;
use JmesPath\Env;
use support\Db;


class SimpleStorageService
{

    private array $allStorageArrange = [];

    private AstRuntime $astRuntime;

    private CompilerRuntime $compilerRuntime;

    public function __construct(array $initParam)
    {
        $this->allStorageArrange = $initParam[0];
        $this->astRuntime = $initParam[1];
        $this->compilerRuntime = $initParam[2];
    }

    /**
     * 执行保存数据
     * @param string $name
     * @param array|null $params
     * @return void
     */
    public function saveData(string $name, ?array $params) {
        if (!isset($this->allStorageArrange[$name])) {
            throw new \InvalidArgumentException('simple storage: StorageArrange not exists :' . $name);
        }
        if (empty($params)) {
            return ;
        }

        /** @var StorageArrangeInterface $storageArrange */
        $storageArrange = $this->allStorageArrange[$name];
        $data = [];
        $reqParams = [];
        $isArray = !empty($storageArrange->getArrayExpression());
        if ($isArray) {
            $reqParams = $this->getSearchRuntime($storageArrange->useCache())($storageArrange->getArrayExpression(), $params);
        } else {
            $reqParams[] = $params;
        }
        foreach ($reqParams as $param) {
            $data[] = $this->searchByParam($storageArrange, $param);
        }
        $dbBuilder = DB::connection($storageArrange->getConnection())
            ->table($storageArrange->getTable());
        if (!empty($storageArrange->getUniqueFields())) {
            DB::transaction(function () use ($dbBuilder, $data, $storageArrange) {
                $dbBuilder->upsert($data, $storageArrange->getUniqueFields());
            });
        } else {
            DB::transaction(function () use ($dbBuilder, $data) {
                $dbBuilder->insert($data);
            });
        }
    }

    /**
     * 提取属性值
     * @param StorageArrangeInterface $storageArrange
     * @param array $params
     * @return array
     */
    private function searchByParam(StorageArrangeInterface $storageArrange, array $params): array
    {
        $data = [];
        //提取数据
        foreach ($storageArrange->attributes() as $attribute) {
            $value = $this->getSearchRuntime($storageArrange->useCache())($attribute->getExpression(), $params);
            //after func
            if (!empty($attribute->getAfterHandler()) && $attribute->getAfterHandler()[0] !== null) {
                $value = call_user_func($attribute->getAfterHandler()[0], $value);
            }
            $data[!empty($attribute->getTableAlias()) ? $attribute->getTableAlias() : $attribute->getAttribute()] = $value;
        }
        return $data;
    }

    private function getSearchRuntime(bool $useCache): CompilerRuntime|astRuntime
    {
        return $useCache ? $this->compilerRuntime : $this->astRuntime;
    }
}