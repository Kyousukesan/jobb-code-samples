<?php

namespace app\admin\service\dimension;

use app\admin\model\backend\dimension\DimensionConfigModel;
use app\admin\vo\req\DimensionConfigReqVo;
use app\admin\vo\res\dimension\DimensionConfigResVo;
use app\model\backend\dimension\dto\DimensionConfigDto;
use Carbon\Carbon;
use support\exception\BusinessException;
use support\Request;
use support\Response;
use Workerman\Crontab\Crontab;

/**
 * Service
 * @author zhoujiwei
 * @date 2025-07-11 08:19:17
 */
class DimensionConfigService
{
    /**
     * @Inject
     * @var DimensionConfigModel
     */
    private DimensionConfigModel $dimensionConfigModel;

    public function saveInfo(DimensionConfigReqVo $reqVo): int
    {
        $reqVo->validation();
        //校验名称
        $isRepeat = $this->dimensionConfigModel->checkNameRepeat($reqVo->getDimensionName(), $reqVo->getType(), $reqVo->getId());
        if ($isRepeat) {
            throw_business_exception('名称不能重复');
        }

        $id = 0;
        if (!empty($reqVo->getId())) {
            //更新
            $old = $this->dimensionConfigModel->getById($reqVo->getId());
            $old->setDimensionName($reqVo->getDimensionName());
            $old->setIsEnable($reqVo->getIsEnable());
            $old->setUpdatedAt(Carbon::now()->toDateTimeString());
            $id = $this->dimensionConfigModel->saveInfo($old);
        } else {
            //新增
            $dto = DimensionConfigDto::convertByReq($reqVo);
            $id = $this->dimensionConfigModel->saveInfo($dto);
        }
        return $id;
    }

    /**
     * @param array $ids
     * @return array<DimensionConfigResVo>
     */
    public function getResMapByIds(array $ids): array
    {
        $dtoArr = $this->dimensionConfigModel->getArrByIds($ids);
        $resMap = [];
        foreach ($dtoArr as $dto) {
            $resMap[$dto->getId()] = DimensionConfigResVo::convertByDto($dto);
        }
        return $resMap;
    }

    /**
     * @param array $ids
     * @return array
     */
    public function getDtoByIds(array $ids): array
    {
        return $this->dimensionConfigModel->getArrByIds($ids);
    }

    /**
     * @param int $dimensionId
     * @param int $isEnable
     * @return void
     */
    public function changeIsEnable(int $dimensionId, int $isEnable): void
    {
        $this->dimensionConfigModel->where('id', $dimensionId)->update(['is_enable' => $isEnable]);
    }

    public function deleteByIds(array $ids): void
    {
        $this->dimensionConfigModel->deleteById($ids);
    }
}
