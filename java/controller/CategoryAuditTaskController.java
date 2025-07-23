package cn.dotfashion.soa.pim.controller;


import cn.dotfashion.soa.api.vo.Response;
import cn.dotfashion.soa.api.vo.Result;
import cn.dotfashion.soa.pdb.vo.resp.AttributeSubmitResp;
import cn.dotfashion.soa.pim.annotation.UlpAuthorityCheck;
import cn.dotfashion.soa.pim.api.CategoryAuditTaskApi;
import cn.dotfashion.soa.pim.entity.ImportTaskEntity;
import cn.dotfashion.soa.pim.enums.FileImportType;
import cn.dotfashion.soa.pim.service.AsyncBatchTaskService;
import cn.dotfashion.soa.pim.service.category.audit.task.CategoryAuditTaskBusinessService;
import cn.dotfashion.soa.pim.util.HttpRequestTools;
import cn.dotfashion.soa.pim.util.ResponseTools;
import cn.dotfashion.soa.pim.vo.categorytask.request.*;
import cn.dotfashion.soa.pim.vo.categorytask.response.*;

import com.shein.common.util.DistributedLockUtil;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

import static cn.dotfashion.soa.pim.enums.FileImportType.ASYNC_SUBMIT_CATEGORY_LEVEL_VERSION;
import static cn.dotfashion.soa.pim.enums.FileImportType.ASYNC_SUBMIT_CATEGORY_TASK_DIFF;

/**
 * <p>
 * 分类提报任务主表 前端控制器
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
@RestController
public class CategoryAuditTaskController implements CategoryAuditTaskApi {

    @Resource
    private CategoryAuditTaskBusinessService businessService;

    @Resource
    private AsyncBatchTaskService asyncBatchTaskService;

    private static final String SUBMIT_CATEGORY_LEVEL_VERSION_INFO_LOCK = "SUBMIT_CATEGORY_LEVEL_VERSION_INFO_LOCK";

    @Override
    public Response<CategoryAuditTaskApplyResp> categoryAuditTaskApply(CategoryAuditTaskApplyReq req) {
        return Response.buildSuccessInfo(businessService.categoryAuditTaskApply(req));
    }

    @Override
    public Response<CategoryLevelTaskGetResp> getCategoryLevelVersionInfo(CategoryLevelTaskGetReq req) {
        return Response.buildSuccessInfo(businessService.getCategoryLevelVersionInfo(req));
    }

    @Override
    public Response<CategoryLevelTaskSubResp> submitCategoryLevelVersionInfo(CategoryLevelTaskSubReq req) {
        CategoryLevelTaskSubResp resp = new CategoryLevelTaskSubResp();
        try {
            if (DistributedLockUtil.SUCCESS_FLAG.equals(DistributedLockUtil.getAndSetDistributedLock(SUBMIT_CATEGORY_LEVEL_VERSION_INFO_LOCK))) {
                businessService.submitCategoryLevelVersionInfo(req);
                ImportTaskEntity importTaskEntity = asyncBatchTaskService.asyncBatchTask(req, ASYNC_SUBMIT_CATEGORY_LEVEL_VERSION, null, null, HttpRequestTools.getRequest());
                resp.setUploadTaskId(importTaskEntity.getId());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DistributedLockUtil.releaseDistributedLock(SUBMIT_CATEGORY_LEVEL_VERSION_INFO_LOCK);
        }

        return Response.buildSuccessInfo(resp);
    }

    @UlpAuthorityCheck
    @Override
    public Response submitChangeCategoryInfo(SubmitCategoryInfoDataReq req) {
        businessService.submitChangeCategoryInfo(req);
        return Response.buildSuccess();
    }

    @Override
    public Response verifyVersion(VerifyVersionReq req) {
        businessService.verifyVersion(req);
        return Response.buildSuccess();
    }

    @Override
    public Response<Result<GetTaskListResp>> getTaskList(GetTaskListReq req, Integer pageSize, Integer pageNum) {
        return ResponseTools.pageToResponse(businessService.getTaskList(req, pageSize, pageNum));
    }

    @Override
    public Response<CategoryLevelTaskSubResp> applyToUbmp(ApplyToUbmpReq req) {
        CategoryLevelTaskSubResp resp = new CategoryLevelTaskSubResp();
        try {
            if (DistributedLockUtil.SUCCESS_FLAG.equals(DistributedLockUtil.getAndSetDistributedLock(SUBMIT_CATEGORY_LEVEL_VERSION_INFO_LOCK))) {
                businessService.applyToUbmp(req);
                ImportTaskEntity importTaskEntity = asyncBatchTaskService.asyncBatchTask(req, ASYNC_SUBMIT_CATEGORY_TASK_DIFF, null, null, HttpRequestTools.getRequest());
                resp.setUploadTaskId(importTaskEntity.getId());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DistributedLockUtil.releaseDistributedLock(SUBMIT_CATEGORY_LEVEL_VERSION_INFO_LOCK);
        }
        return Response.buildSuccessInfo(resp);
    }

    @Override
    public Response batchSetUpCategoryShowTime(BatchSetUpCategoryReq req) {
        businessService.batchSetUpCategoryShowTime(req);
        return Response.buildSuccess();
    }

    @Override
    public Response<Result<SetUpCategoryShowTimeResp>> getSetUpCategoryShowTime(SetUpCategoryShowTimeReq req) {
        List<SetUpCategoryShowTimeResp> respList = businessService.getSetUpCategoryShowTime(req);
        return Response.buildSuccessResult(respList, respList.size());
    }

    @UlpAuthorityCheck
    @Override
    public Response saveCategoryAuditReference(CategoryAuditReferenceSaveReq req) {
        businessService.saveCategoryAuditReference(req);
        return Response.buildSuccess();
    }

    @Override
    public Response<Result<CategoryAuditReferenceResp>> getCategoryAuditReference(CategoryAuditReferenceReq req) {
        List<CategoryAuditReferenceResp> respList = businessService.getCategoryAuditReference(req);
        return Response.buildSuccessResult(respList, respList.size());
    }

    @Override
    public Response<GetCategoryAuditVersionResp> getCategoryAuditVersionList(CategoryAuditReferenceReq req) {
        return Response.buildSuccessInfo(businessService.getCategoryAuditVersionList(req));
    }

    @Override
    public Response<CategoryLevelTaskGetResp> getCategoryLevelVersionInfoOA(Long requestId) {
        return Response.buildSuccessInfo(businessService.getCategoryLevelVersionInfoOA(requestId));
    }

    @Override
    public Response<Void> categoryTaskAudit(categoryTaskAuditReq req) {
        businessService.categoryTaskAudit(req);
        return Response.buildSuccess();
    }

    @Override
    public Response<Void> taskAfterHandle(Long requestId) {
        asyncBatchTaskService.asyncBatchTask(requestId, FileImportType.ASYNC_AUDIT_TASK_AFTER_TASK, null, null, null);
        return Response.buildSuccess();
    }

    @Override
    public Response<Void> taskFinalCheck(TaskFinalCheckReq req) {
        businessService.taskFinalCheck(req);
        return Response.buildSuccess();
    }

    @Override
    public Response<Void> versionCancel(VerifyVersionReq req) {
        businessService.versionCancel(req);
        return Response.buildSuccess();
    }

    @UlpAuthorityCheck
    @Override
    public Response<Void> auditCancel(TaskFinalCheckReq req) {
        businessService.auditCancel(req);
        return Response.buildSuccess();
    }

}

