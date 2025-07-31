<?php

namespace app\common\simpleStorage\storageArrange;

use app\common\simpleStorage\AttributeArrange;

class AppThirdReportLogStorage implements StorageArrangeInterface
{
    /**
     * フィールドマッピング設定
     * @return array|AttributeArrange[]
     */
    function attributes(): array
    {
        return AttributeArrange::makeByArray([
            'user_id' => [//入力パラメータフィールド名
                'expression' => 'user_id',//jmespath式
                'tableAlias' => '',//データテーブルのフィールドエイリアス、あれば使用、なければkeyを使用
                'afterHandler' => null//パラメータ抽出後に実行するクロージャ関数
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
     * DB接続を指定
     * @return string
     */
    public function getConnection(): string
    {
        return 'p19_report';
    }

    /**
     * テーブル名
     * @return string
     */
    public function getTable(): string
    {
        return 'app_third_report_log_' . date('w');
    }

    /**
     * 一括保存かどうか、一括保存の場合は分割式を設定
     * @return string
     */
    public function getArrayExpression(): string
    {
        return '@';
    }

    /**
     * テーブルのユニークインデックスに基づいて上書き
     * @return string[]
     */
    public function getUniqueFields(): array
    {
        return [];
    }

    /**
     * 式解析キャッシュを有効にするかどうか
     * @return bool
     */
    function useCache(): bool
    {
        return false;
    }
}