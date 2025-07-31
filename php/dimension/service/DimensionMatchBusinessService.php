<?php

namespace app\admin\service\dimension;

use app\admin\service\dimension\config\MatchConditionDataDto;
use app\admin\service\dimension\config\MatchDataDto;
use app\admin\service\dimension\config\MatchResultDto;
use app\admin\service\dimension\matcher\Condition;
use app\common\CacheKeyEnum;
use app\common\enum\dimension\DimensionTypeEnum;
use app\model\log\DebugLog;

class DimensionMatchBusinessService
{
    /**
     * @Inject
     * @var DimensionConfigService
     */
    private DimensionConfigService $configService;

    /**
     * @Inject
     * @var DimensionConfigConditionService
     */
    private DimensionConfigConditionService $conditionService;

    public function simpleMatch(int $dimensionId, array $conditionData = [], MatchDataDto $matchDataDto = null): MatchResultDto
    {
        $matchResult = MatchResultDto::init(false);
        //まず設定を構築
        $matchConfig = !is_null($matchDataDto) ? $matchDataDto : $this->buildSimpleMatchData($dimensionId);
        if (empty($matchConfig->getConditionArr())) {
            $this->log('設定条件が存在しません,id:' . $matchConfig->getDimensionName());
            return $matchResult;
        }
        //設定をグループ化
        $group = [];
        /** @var MatchConditionDataDto $condition */
        foreach ($matchConfig->getConditionArr() as $matchConditionDto) {
            $condition = Condition::fromDto($matchConditionDto);
            $group[$condition->getConditionGroup()][] = $condition;
        }
        //マッチャーを構築してマッチング開始
        $hit = Condition::matchAnyGroup($conditionData, $group);
        $matchResult->setIsHit($hit);
        return $matchResult;
    }


    /**
     * 単一ルールマッチングデータ構築
     * @param bool $useCache
     * @return MatchDataDto
     */
    public function buildSimpleMatchData(int $dimensionId, bool $useCache = true): MatchDataDto
    {
        if ($useCache) {
            $cache = CacheKeyEnum::DIMENSION_CONFIG_MATCH_DATA->newInstance($dimensionId);
            $cache->remember(function () use ($dimensionId) {
                return $this->queryMatchSimpleData($dimensionId);
            });
        }
        return $this->queryMatchSimpleData($dimensionId);
    }

    /**
     * @param int $dimensionId
     * @return MatchDataDto
     */
    private function queryMatchSimpleData(int $dimensionId): MatchDataDto
    {
        $dimensionDtoArr = $this->configService->getDtoByIds([$dimensionId]);
        if (empty($dimensionDtoArr)) {
            $this->log('単一ルールマッチングデータ構築に失敗しました、ディメンション情報が見つかりません', ['dimensionId' => $dimensionId]);
            return MatchDataDto::emptyData();
        }
        $conditionValueResMap = $this->conditionService->getResMapByDimensionIds([$dimensionId]);
        if (empty($conditionValueResMap)) {
            $this->log('単一ルールマッチングデータ構築に失敗しました、ディメンション条件情報が見つかりません', ['dimensionId' => $dimensionId]);
            return MatchDataDto::emptyData();
        }
        return MatchDataDto::convert($dimensionDtoArr[0], $conditionValueResMap[$dimensionId]);
    }

    /**
     * @param string $message
     * @param array $params
     * @return void
     */
    public function log(string $message, array $params = []): void
    {
        DebugLog::debug("维度条件匹配器", ['error' => $message, 'params' => $params] , false);
    }
}