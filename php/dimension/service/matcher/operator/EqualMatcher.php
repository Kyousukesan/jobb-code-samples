<?php

namespace app\admin\service\dimension\matcher\operator;

use app\common\enum\dimension\ConditionLogicEnum;
use app\common\enum\dimension\ConditionValueTypeEnum;
use function Aws\map;

class EqualMatcher implements MatcherInterface
{
    private ConditionLogicEnum $logicEnum;

    public function __construct(ConditionLogicEnum $logicEnum = ConditionLogicEnum::EQ)
    {
        $this->logicEnum = $logicEnum;
    }

    public function getLogicEnum(): ConditionLogicEnum
    {
        return $this->logicEnum;
    }

    public function match(mixed $actual, array $expected, ConditionValueTypeEnum $valueTypeEnum): bool
    {
        $isNeq = $this->logicEnum === ConditionLogicEnum::NEQ;
        foreach ($expected as $exp) {
            $result = false;
            switch ($valueTypeEnum) {
                case ConditionValueTypeEnum::BOOL:
                    $result = $this->simpleBoolEqual($actual, $exp);
                    break;
                case ConditionValueTypeEnum::INTEGER:
                    $result = ((int)$actual === (int)$exp);
                    break;
                case ConditionValueTypeEnum::STRING:
                    $result = ((string)$actual === (string)$exp);
                    break;
                default:
                    $result = ($actual === $exp);
            }
            if ($isNeq ? !$result : $result) {
                return true;
            }
        }
        return false;
    }

    private function simpleBoolEqual($a, $b): bool
    {
        return $this->toSimpleBool($a) === $this->toSimpleBool($b);
    }

    private function toSimpleBool($val): bool
    {
        if ($val === true || $val === 'true') return true;
        if ($val === false || $val === 'false') return false;
        return (bool)$val;
    }
}