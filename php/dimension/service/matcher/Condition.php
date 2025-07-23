<?php

namespace app\admin\service\dimension\matcher;

use app\admin\service\dimension\config\MatchConditionDataDto;
use app\common\enum\dimension\ConditionValueTypeEnum;
use app\common\enum\dimension\ConditionLogicEnum;
use app\common\enum\dimension\ConditionNameEnum;

class Condition
{
    private ConditionNameEnum $conditionNameEnum;

    private array $conditionValue;

    private ConditionValueTypeEnum $conditionValueType;

    private ConditionLogicEnum $conditionLogicEnum;

    private int $conditionGroup;


    public static function fromDto(MatchConditionDataDto $dto): self
    {
        $condition = new Condition();
        $condition->setConditionNameEnum(ConditionNameEnum::match($dto->getConditionName()));
        $condition->setConditionValue($dto->getConditionValue());
        $condition->setConditionValueType($condition->getConditionNameEnum()->nameConvertToValueType());
        $condition->setConditionLogicEnum(ConditionLogicEnum::match($dto->getConditionLogic()));
        $condition->setConditionGroup($dto->getConditionGroup());
        return $condition;
    }

    public static function matchAll(array $dataMap, array $conditionList): bool
    {
        /** @var Condition $condition */
        foreach ($conditionList as $condition) {
            if (!$condition->match($dataMap)) {
                return false;
            }
        }
        return true;
    }

    public static function matchAnyGroup(array $dataMap, array $groupArr): bool
    {
        foreach ($groupArr as $group) {
            if (self::matchAll($dataMap, $group)) {
                return true;
            }
        }
        return false;
    }

    public function match(array $dataMap): bool
    {
        $match = MatcherFactory::create($this->getConditionLogicEnum());
        $actual = $dataMap[$this->getConditionNameEnum()->value] ?? null;
        if (is_null($actual)) {
            return false;
        }
        return $match->match($actual, $this->conditionValue, $this->getConditionValueType());
    }

    public function getConditionNameEnum(): ConditionNameEnum
    {
        return $this->conditionNameEnum;
    }

    public function setConditionNameEnum(ConditionNameEnum $conditionNameEnum): void
    {
        $this->conditionNameEnum = $conditionNameEnum;
    }

    public function getConditionValue(): array
    {
        return $this->conditionValue;
    }

    public function setConditionValue(array $conditionValue): void
    {
        $this->conditionValue = $conditionValue;
    }

    public function getConditionValueType(): ConditionValueTypeEnum
    {
        return $this->conditionValueType;
    }

    public function setConditionValueType(ConditionValueTypeEnum $conditionValueType): void
    {
        $this->conditionValueType = $conditionValueType;
    }

    public function getConditionLogicEnum(): ConditionLogicEnum
    {
        return $this->conditionLogicEnum;
    }

    public function setConditionLogicEnum(ConditionLogicEnum $conditionLogicEnum): void
    {
        $this->conditionLogicEnum = $conditionLogicEnum;
    }

    public function getConditionGroup(): int
    {
        return $this->conditionGroup;
    }

    public function setConditionGroup(int $conditionGroup): void
    {
        $this->conditionGroup = $conditionGroup;
    }
}