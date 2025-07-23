<?php

namespace app\admin\service\dimension\matcher\operator;

use app\common\enum\dimension\ConditionLogicEnum;
use app\common\enum\dimension\ConditionValueTypeEnum;

interface MatcherInterface
{
    public function match(mixed $actual, array $expected, ConditionValueTypeEnum $valueTypeEnum): bool;

    public function getLogicEnum(): ConditionLogicEnum;
}