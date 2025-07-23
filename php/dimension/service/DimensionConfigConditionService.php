<?php

namespace app\admin\service\dimension;

use app\admin\model\backend\dimension\DimensionConfigConditionModel;
use app\admin\model\backend\dimension\DimensionConfigConditionValueModel;
use app\admin\vo\req\DimensionConfigConditionReqVo;
use app\admin\vo\res\dimension\DimensionConfigConditionResVo;
use app\model\backend\dimension\dto\DimensionConfigConditionDto;
use support\Request;
use support\Response;

/**
 * Service
 * @author zhoujiwei
 * @date 2025-07-11 08:19:17
 */
class DimensionConfigConditionService
{
    /**
     * @Inject
     * @var DimensionConfigConditionModel
     */
    private DimensionConfigConditionModel $conditionModel;

    /**
     * @Inject
     * @var DimensionConfigConditionValueModel
     */
    private DimensionConfigConditionValueModel $conditionValueModel;

    /**
     * @param int $dimensionId
     * @param array $conditionValueReqList
     * @return void
     */
    public function batchSave(int $dimensionId, array $conditionValueReqList): void
    {
        //删除旧数据
       $this->deleteByDimensionIds([$dimensionId]);

       //校验新数据是否存在重复
        $unqName = [];
        /** @var DimensionConfigConditionReqVo$conditionValueReq */
        foreach ($conditionValueReqList as $conditionValueReq) {
            if (isset($unqName[$conditionValueReq->getConditionGroup()][$conditionValueReq->getConditionName()])) {
                throw_business_exception('条件名称不能重复:' . $conditionValueReq->getConditionName());
            }
            $unqName[$conditionValueReq->getConditionGroup()][$conditionValueReq->getConditionName()] = 1;
        }
        //保存新数据
        /** @var DimensionConfigConditionReqVo $conditionValueReq */
        foreach ($conditionValueReqList as $conditionValueReq) {
            $conditionValueReq->validation();
            $dto = DimensionConfigConditionDto::convertByReq($dimensionId, $conditionValueReq);
            if (count($conditionValueReq->getConditionValue()) == 1) {
                $dto->setConditionValue($conditionValueReq->getConditionValue()[0]);
            } else {
                $dto->setConditionValue('');
            }
            $conditionId = $this->conditionModel->createToId($dto);
            if (count($conditionValueReq->getConditionValue()) > 1) {
                $this->conditionValueModel->batchInsert($conditionId, $conditionValueReq->getConditionValue());
            }
        }
    }

    /**
     * @param int[] $dimensionIds
     * @return array [int,DimensionConfigConditionDto]
     */
    public function getMapByDimensionIds(array $dimensionIds): array
    {
        $conditionArr = $this->conditionModel->getArrByDimensionIds($dimensionIds);
        $map = [];
        /** @var DimensionConfigConditionDto $conditionDto */
        foreach ($conditionArr as $conditionDto) {
            $map[$conditionDto->getDimensionConfigId()] = $conditionDto;
        }
        return $map;
    }

    /**
     * @param array $dimensionIds
     * @return array<int,  array<DimensionConfigConditionResVo>>
     */
    public function getResMapByDimensionIds(array $dimensionIds): array
    {
        $conditionArr = $this->conditionModel->getArrByDimensionIds($dimensionIds);
        $conditionIds = [];
        /** @var DimensionConfigConditionDto $conditionDto */
        foreach ($conditionArr as $conditionDto) {
            $conditionIds[] = $conditionDto->getId();
        }
        $valueMap = $this->conditionValueModel->getValueMapByConditionIds($conditionIds);

        $map = [];
        /** @var DimensionConfigConditionDto $conditionDto */
        foreach ($conditionArr as $conditionDto) {
            $resVo = DimensionConfigConditionResVo::convertByDto($conditionDto);
            $map[$conditionDto->getDimensionConfigId()][] = $resVo;
            if (!empty($valueMap[$resVo->getId()])) {
                $resVo->setConditionValue($valueMap[$resVo->getId()]);
            }
        }
        return $map;
    }

    public function deleteByDimensionIds(array $dimensionIds): void
    {
        //删除旧数据
        $conditionIds = $this->conditionModel->getIdsByDimensionIds($dimensionIds);
        if (!empty($conditionIds)) {
            $this->conditionValueModel->deleteByConditionIds($conditionIds);
        }
        $this->conditionModel->deleteByIds($dimensionIds);
    }
}
