<?php

namespace app\common\simpleStorage;

class AttributeArrange
{
    private string $attribute;

    private string $tableAlias;

    private string $expression;

    private array $afterHandler;

    public function __construct(string $attribute, string $expression, string $tableAlias, callable $afterHandler = null)
    {
        $this->attribute = $attribute;
        $this->expression = $expression;
        $this->tableAlias = $tableAlias;
        $this->afterHandler = [$afterHandler, 'value'];
    }


    /**
     * 配列作成
     * @param array $array
     * @return array
     */
    public static function makeByArray(array $array): array
    {
        $arr = [];
        foreach ($array as $key => $value) {
            $arr[] = new AttributeArrange(
                $key, $value['expression'] ?? '', $value['tableAlias'] ??  '', $value['afterHandler'] ?? null
            );
        }
        return $arr;
    }

    public function getAttribute(): string
    {
        return $this->attribute;
    }

    public function getExpression(): string
    {
        return $this->expression;
    }

    public function getAfterHandler(): array
    {
        return $this->afterHandler;
    }

    public function getTableAlias(): string
    {
        return $this->tableAlias;
    }
}