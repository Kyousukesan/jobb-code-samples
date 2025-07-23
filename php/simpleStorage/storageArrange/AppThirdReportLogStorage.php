<?php

namespace app\common\simpleStorage\storageArrange;

use app\common\simpleStorage\AttributeArrange;

class AppThirdReportLogStorage implements StorageArrangeInterface
{
    /**
     * 字段映射配置
     * @return array|AttributeArrange[]
     */
    function attributes(): array
    {
        return AttributeArrange::makeByArray([
            'user_id' => [//入参字段名
                'expression' => 'user_id',//jmespath表达式
                'tableAlias' => '',//数据表里的字段别名，有则使用，否则使用如key
                'afterHandler' => null//提取参数后执行的闭包函数
            ],
            'event_name' => [
                'expression' => 'event',
                'tableAlias' => '',
                'afterHandler' => null
            ],
            'request_data' => [
                'expression' => '@',
                'tableAlias' => '',
                'afterHandler' => function($value) {
                    return json_encode($value);
                }
            ],
        ]);
    }

    /**
     * 指定DB连接
     * @return string
     */
    public function getConnection(): string
    {
        return 'p19_report';
    }

    /**
     * 表名
     * @return string
     */
    public function getTable(): string
    {
        return 'app_third_report_log_' . date('w');
    }

    /**
     * 是否为批量保存，如果为批量保存则设置分割表达式
     * @return string
     */
    public function getArrayExpression(): string
    {
        return '@';
    }

    /**
     * 根据表唯一索引覆盖
     * @return string[]
     */
    public function getUniqueFields(): array
    {
        return [];
    }

    /**
     * 是否开始表达式解析缓存
     * @return bool
     */
    function useCache(): bool
    {
        return false;
    }
}