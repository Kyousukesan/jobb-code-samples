<?php

namespace app\admin\service\dimension;

use app\admin\vo\req\DimensionConfigReqVo;
use app\admin\vo\res\dimension\DimensionConfigResVo;
use app\common\CacheKeyEnum;
use app\common\enum\dimension\ConditionNameEnum;
use app\common\validator\ValidatorService;
use Illuminate\Database\DatabaseTransactionsManager;
use Kujiang\ValidationPlus\Core\IntegerValidator;
use support\Db;

class DimensionConfigBusinessService
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


    /**
     * 保存数据 带主键则为更新
     * @param DimensionConfigReqVo $reqVo
     * @return int
     * @throws \Throwable
     * @throws \support\exception\MyBusinessException
     */
    public function saveInfo(DimensionConfigReqVo $reqVo): int
    {
        $conn = Db::connection("backend");
        $conn->setTransactionManager(new DatabaseTransactionsManager());
        return $conn->transaction(function ($conn) use ($reqVo) {
            $dimensionId = $this->configService->saveInfo($reqVo);
            $this->conditionService->batchSave($dimensionId, $reqVo->getConditionArr());
            $conn->afterCommit(function () use ($dimensionId) {
                //清理缓存
                CacheKeyEnum::DIMENSION_CONFIG_MATCH_DATA->newInstance($dimensionId)->delete();
            });
            return $dimensionId;
        });
    }

    /**
     * 根据ID查询详情
     * @param int[] $dimensionIds
     * @return array<int, DimensionConfigResVo>
     */
    public function getInfoMapByIds(array $dimensionIds): array
    {
        $dimensionResMap = $this->configService->getResMapByIds($dimensionIds);
        $conditionResMap = $this->conditionService->getResMapByDimensionIds($dimensionIds);
        //组装Res
        foreach ($dimensionResMap as $id => $dimensionRes) {
            if (isset($conditionResMap[$id])) {
                $dimensionRes->setConditionArr($conditionResMap[$id]);
            }
        }
        return $dimensionResMap;
    }

    public function getInfoById(int $dimensionId): DimensionConfigResVo
    {
        $dimensionConfigResVoMap = $this->getInfoMapByIds([$dimensionId]);
        if (empty($dimensionConfigResVoMap)) {
            throw_business_exception('未找到有效的维度数据,id=' . $dimensionId);
        }
        return $dimensionConfigResVoMap[$dimensionId];
    }

    public function getAdminInputConfig():array
    {
        $result = [];
        foreach (ConditionNameEnum::cases() as $enum) {
            $result[] = [
                'desc' => $enum->nameConvertToDesc(),
                'condition_name' => $enum->value,
                'is_simple' => $enum->checkIsSimple(),
                'data_type' => $enum->nameConvertToValueType(),
                'option_value' => $enum->optionValues(),
                'logic_option_value' => $enum->nameToLogicOption()
            ];
        }
        return $result;
    }

    /**
     * 删除配置
     * @param int $dimensionId
     * @return void
     * @throws \Throwable
     * @throws \support\exception\MyBusinessException
     */
    public function deleteInfoById(int $dimensionId): void
    {
        $connection = Db::connection("backend");
        $connection->beginTransaction();
        try {
            $this->configService->deleteByIds([$dimensionId]);
            $this->conditionService->deleteByDimensionIds([$dimensionId]);
            $connection->commit();
        } catch (\Exception $exception) {
            $connection->rollBack();
            throw_business_exception($exception->getMessage());
        }
    }

    public function changeIsEnable(int $dimensionId, int $isEnable): void
    {
        $rule = [
            'is_enable' => 'required|integer|in:0,1',
            'dimension_id' => 'required|integer',
        ];
        $validator = ValidatorService::make(['dimension_id' => $dimensionId, 'is_enable' => $isEnable], $rule);
        if ($validator->fails()) {
            throw_business_exception($validator->errors()->first());
        }
        $arr = $this->conditionService->getMapByDimensionIds([$dimensionId]);
        if (empty($arr)) {
            throw_business_exception('未找到有效的维度数据,id=' . $dimensionId);
        }
        $this->configService->changeIsEnable($dimensionId, $isEnable);
    }
}