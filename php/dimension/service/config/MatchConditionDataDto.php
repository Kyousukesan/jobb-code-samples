<?php

namespace app\admin\service\dimension\config;

use app\admin\vo\res\dimension\DimensionConfigConditionResVo;

class MatchConditionDataDto implements \JsonSerializable
{

    private string $conditionName;

    private array $conditionValue;

    private int $conditionGroup;

    private string $conditionLogic;

    public static function convert(DimensionConfigConditionResVo $resVo): self
    {
        $dto = new MatchConditionDataDto();
        $dto->setConditionName($resVo->getConditionName());
        $dto->setConditionValue($resVo->getConditionValue());
        $dto->setConditionLogic($resVo->getConditionLogic());
        $dto->setConditionGroup($resVo->getConditionGroup());
        return $dto;
    }

    public function jsonSerialize(): mixed
    {
        return [
            'conditionName' => $this->conditionName,
            'conditionValue' => $this->conditionValue,
            'conditionGroup' => $this->conditionGroup,
            'conditionLogic' => $this->conditionLogic,
        ];
    }

    public function getConditionName(): string
    {
        return $this->conditionName;
    }

    public function setConditionName(string $conditionName): void
    {
        $this->conditionName = $conditionName;
    }

    public function getConditionValue(): array
    {
        return $this->conditionValue;
    }

    public function setConditionValue(array $conditionValue): void
    {
        $this->conditionValue = $conditionValue;
    }


    public function getConditionGroup(): int
    {
        return $this->conditionGroup;
    }

    public function setConditionGroup(int $conditionGroup): void
    {
        $this->conditionGroup = $conditionGroup;
    }

    public function getConditionLogic(): string
    {
        return $this->conditionLogic;
    }

    public function setConditionLogic(string $conditionLogic): void
    {
        $this->conditionLogic = $conditionLogic;
    }
}