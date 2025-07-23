<?php

namespace app\admin\service\dimension\config;

use app\common\enum\dimension\ConditionNameEnum;
use app\common\enum\dimension\ConditionValueTypeEnum;
use Closure;
use Illuminate\Contracts\Validation\ValidationRule;

class ConditionValueValid implements ValidationRule
{

    private ConditionNameEnum $conditionName;

    private ConditionValueTypeEnum $typeEnum;

    private array $conditionValue = [];

    public function __construct(string $conditionName, array $conditionValue)
    {
        $conditionName = ConditionNameEnum::match($conditionName);
        $this->setConditionName($conditionName);
        $this->setConditionValue($conditionValue);
        $this->typeEnum = $conditionName->nameConvertToValueType();
    }

    public function validate(string $attribute, mixed $value, Closure $fail): void
    {
        if (!$this->checkValueType()) {
            $fail("{$attribute} 的格式不正确");
        }
    }

    public function checkValueType():bool
    {
        return match ($this->getTypeEnum()) {
            ConditionValueTypeEnum::BOOL => ConditionValueTypeEnum::BOOL->validator()($this->conditionValue),
            ConditionValueTypeEnum::STRING => ConditionValueTypeEnum::STRING->validator()($this->conditionValue),
            ConditionValueTypeEnum::INTEGER => ConditionValueTypeEnum::INTEGER->validator()($this->conditionValue),
            default => false
        };
    }

    public function getConditionName(): ConditionNameEnum
    {
        return $this->conditionName;
    }

    public function setConditionName(ConditionNameEnum $conditionName): void
    {
        $this->conditionName = $conditionName;
    }

    public function getTypeEnum(): ConditionValueTypeEnum
    {
        return $this->typeEnum;
    }

    public function setTypeEnum(ConditionValueTypeEnum $typeEnum): void
    {
        $this->typeEnum = $typeEnum;
    }

    public function getConditionValue(): array
    {
        return $this->conditionValue;
    }

    public function setConditionValue(array $conditionValue): void
    {
        $this->conditionValue = $conditionValue;
    }
}