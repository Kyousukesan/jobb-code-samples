<?php

namespace app\admin\service\dimension\config;

use app\admin\vo\res\dimension\DimensionConfigConditionResVo;
use app\model\backend\dimension\dto\DimensionConfigDto;

class MatchDataDto implements \JsonSerializable
{

    private int $dimensionId;

    private string $dimensionName;

    private array $conditionArr;


    public static function convert(DimensionConfigDto $configDto, array $conditionArr): self
    {
        $dto = new MatchDataDto();
        $dto->setDimensionId($configDto->getId());
        $dto->setDimensionName($configDto->getDimensionName());
        $matchConditionArr = [];
        /** @var DimensionConfigConditionResVo $conditionRes */
        foreach ($conditionArr as $conditionRes) {
            $matchConditionArr[] = MatchConditionDataDto::convert($conditionRes);
        }
        $dto->setConditionArr($matchConditionArr);
        return $dto;
    }

    public static function emptyData(): self
    {
        $empty = new MatchDataDto();
        $empty->setDimensionId(0);
        $empty->setDimensionName( '');
        $empty->setConditionArr([]);
        return $empty;
    }

    public function getConditionArr(): array
    {
        return $this->conditionArr;
    }

    public function setConditionArr(array $conditionArr): void
    {
        $this->conditionArr = $conditionArr;
    }

    public function jsonSerialize(): mixed
    {
        return [
            'dimensionId' => $this->dimensionId,
            'dimensionName' => $this->dimensionName,
            'conditionArr' => $this->conditionArr,
        ];
    }

    public function getDimensionId(): int
    {
        return $this->dimensionId;
    }

    public function setDimensionId(int $dimensionId): void
    {
        $this->dimensionId = $dimensionId;
    }

    public function getDimensionName(): string
    {
        return $this->dimensionName;
    }

    public function setDimensionName(string $dimensionName): void
    {
        $this->dimensionName = $dimensionName;
    }
}