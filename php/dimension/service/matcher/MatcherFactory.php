<?php

namespace app\admin\service\dimension\matcher;

use app\admin\service\dimension\matcher\operator\EqualMatcher;
use app\admin\service\dimension\matcher\operator\GteMatcher;
use app\admin\service\dimension\matcher\operator\LteMatcher;
use app\admin\service\dimension\matcher\operator\MatcherInterface;
use app\admin\service\dimension\matcher\operator\NotEqualMatcher;
use app\admin\service\dimension\matcher\operator\CompareMatcher;
use app\common\enum\dimension\ConditionLogicEnum;

class MatcherFactory
{
    public static function create(ConditionLogicEnum $logicEnum): MatcherInterface
    {
        return match ($logicEnum) {
            ConditionLogicEnum::EQ => new EqualMatcher(ConditionLogicEnum::EQ),
            ConditionLogicEnum::NEQ => new EqualMatcher(ConditionLogicEnum::NEQ),
            ConditionLogicEnum::GTE => new CompareMatcher(ConditionLogicEnum::GTE),
            ConditionLogicEnum::LTE => new CompareMatcher(ConditionLogicEnum::LTE),
            default => throw new \Exception('サポートされていない論理演算子'),
        };
    }
}