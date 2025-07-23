<?php

namespace app\common\simpleStorage;

use app\common\BaseController;
use support\Request;
use support\Response;

class SimpleStorageController extends BaseController
{

    public final const STORAGE_ARRANGE_CLASS = 'storage_arrange_class';

    /**
     * @Inject
     * @var SimpleStorageService
     */
    public SimpleStorageService $storageService;

    /**
     * @param Request $request
     * @return Response
     */
    public function saveData(Request $request): Response
    {
        $className = $request->route->param(self::STORAGE_ARRANGE_CLASS);
        assert(!empty($className));
        $this->storageService->saveData($className, $request->all());
        return $this->success();
    }
}