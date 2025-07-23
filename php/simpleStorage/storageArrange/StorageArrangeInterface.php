<?php

namespace app\common\simpleStorage\storageArrange;

use app\common\simpleStorage\AttributeArrange;

interface StorageArrangeInterface
{

    /**
     * 落表的链接名
     * @return string
     */
    public function getConnection(): string;

    /**
     * 落表的表名
     * @return string
     */
    public function getTable(): string;

    /**
     * 是否为数组批量保存，如果是则获取区分表达式，否则为空
     * @return string
     */
    public function getArrayExpression(): string;


    /**
     * 获取唯一键属性名，如果不为空，则会覆盖这些属性名交集的数据
     * @return array<string>
     */
    public function getUniqueFields(): array;

    /**
     * 获取属性提取配置
     * @return array<AttributeArrange>
     */
    function attributes(): array;

    /**
     * 是否使用缓存解析器
     * @return mixed
     */
    function useCache(): bool;
}