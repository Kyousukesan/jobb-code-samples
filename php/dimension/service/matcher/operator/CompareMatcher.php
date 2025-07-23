<?php

namespace app\admin\service\dimension\matcher\operator;

use app\common\enum\dimension\ConditionLogicEnum;
use app\common\enum\dimension\ConditionValueTypeEnum;

class CompareMatcher implements MatcherInterface
{
    private ConditionLogicEnum $logicEnum;

    public function __construct(ConditionLogicEnum $logicEnum = ConditionLogicEnum::GTE)
    {
        $this->logicEnum = $logicEnum;
    }

    public function getLogicEnum(): ConditionLogicEnum
    {
        return $this->logicEnum;
    }

    public function match(mixed $actual, array $expected, ConditionValueTypeEnum $valueTypeEnum): bool
    {
        $isLte = $this->logicEnum === ConditionLogicEnum::LTE;
        foreach ($expected as $exp) {
            $result = false;
            switch ($valueTypeEnum) {
                case ConditionValueTypeEnum::INTEGER:
                    $result = $isLte ? ((int)$actual <= (int)$exp) : ((int)$actual >= (int)$exp);
                    break;
                case ConditionValueTypeEnum::STRING:
                    $result = $isLte ? ((string)$actual <= (string)$exp) : ((string)$actual >= (string)$exp);
                    break;
                default:
                    // 其他类型暂不支持
            }
            if ($result) {
                return true;
            }
        }
        return false;
    }
}