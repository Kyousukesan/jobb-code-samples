<?php

namespace app\common\simpleStorage\storageArrange;

use app\common\simpleStorage\AttributeArrange;

interface StorageArrangeInterface
{

    /**
     * データベース接続名
     * @return string
     */
    public function getConnection(): string;

    /**
     * データベーステーブル名
     * @return string
     */
    public function getTable(): string;

    /**
     * 配列の一括保存かどうか、配列の場合は分割式を取得、そうでなければ空
     * @return string
     */
    public function getArrayExpression(): string;


    /**
     * ユニークキー属性名を取得、空でない場合はこれらの属性名の交差するデータを上書き
     * @return array<string>
     */
    public function getUniqueFields(): array;

    /**
     * 属性抽出設定を取得
     * @return array<AttributeArrange>
     */
    function attributes(): array;

    /**
     * キャッシュパーサーを使用するかどうか
     * @return mixed
     */
    function useCache(): bool;
}