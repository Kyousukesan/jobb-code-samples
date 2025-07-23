<?php

namespace app\admin\service\dimension\config;

class MatchResultDto implements \JsonSerializable
{
    private bool $isHit;

    public static function init(bool $isHit): self
    {
        $matchResult = new MatchResultDto();
        $matchResult->setIsHit($isHit);
        return $matchResult;
    }

    public function isHit(): bool
    {
        return $this->isHit;
    }

    public function setIsHit(bool $isHit): void
    {
        $this->isHit = $isHit;
    }

    public function jsonSerialize(): mixed
    {
        return [
            'is_hit' => $this->isHit,
        ];
    }
}