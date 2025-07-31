package cn.company.module.controller;


import cn.company.api.vo.Response;
import cn.company.api.vo.Result;
import cn.company.pdb.vo.resp.AttributeSubmitResp;
import cn.company.module.annotation.UlpAuthorityCheck;
import cn.company.module.api.CategoryAuditTaskApi;
import cn.company.module.entity.ImportTaskEntity;
import cn.company.module.enums.FileImportType;
import cn.company.module.service.AsyncBatchTaskService;
import cn.company.module.service.category.audit.task.CategoryAuditTaskBusinessService;
import cn.company.module.util.HttpRequestTools;
import cn.company.module.util.ResponseTools;
import cn.company.module.vo.categorytask.request.*;
import cn.company.module.vo.categorytask.response.*;

import com.common.util.DistributedLockUtil;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.List;

import static cn.company.module.enums.FileImportType.ASYNC_SUBMIT_CATEGORY_LEVEL_VERSION;
import static cn.company.module.enums.FileImportType.ASYNC_SUBMIT_CATEGORY_TASK_DIFF;

/**
 * <p>
 * Category Submission Task Main Controller
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
@RestController
public class CategoryTaskController implements CategoryReviewTaskApi {

    @Resource
    private CategoryReviewTaskBusinessService businessService;

    @Resource
    private AsyncBatchTaskService asyncBatchTaskService;

    private static final String SUBMIT_CATEGORY_LEVEL_VERSION_INFO_LOCK = "SUBMIT_CATEGORY_LEVEL_VERSION_INFO_LOCK";

    @Override
    public Response<CategoryReviewTaskApplyResp> categoryReviewTaskApply(CategoryReviewTaskApplyReq req) {
        return Response.buildSuccessInfo(businessService.categoryReviewTaskApply(req));
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

    @Override
    public Response saveCategoryReviewReference(CategoryReviewReferenceSaveReq req) {
        businessService.saveCategoryReviewReference(req);
        return Response.buildSuccess();
    }

    @Override
    public Response<Result<CategoryReviewReferenceResp>> getCategoryReviewReference(CategoryReviewReferenceReq req) {
        List<CategoryReviewReferenceResp> respList = businessService.getCategoryReviewReference(req);
        return Response.buildSuccessResult(respList, respList.size());
    }

    @Override
    public Response<GetCategoryReviewVersionResp> getCategoryReviewVersionList(CategoryReviewReferenceReq req) {
        return Response.buildSuccessInfo(businessService.getCategoryReviewVersionList(req));
    }

    @Override
    public Response<CategoryLevelTaskGetResp> getCategoryLevelVersionInfoOA(Long requestId) {
        return Response.buildSuccessInfo(businessService.getCategoryLevelVersionInfoOA(requestId));
    }

    @Override
    public Response<Void> categoryTaskReview(categoryTaskReviewReq req) {
        businessService.categoryTaskReview(req);
        return Response.buildSuccess();
    }

    @Override
    public Response<Void> taskAfterHandle(Long requestId) {
        asyncBatchTaskService.asyncBatchTask(requestId, FileImportType.ASYNC_REVIEW_TASK_AFTER_TASK, null, null, null);
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

    @Override
    public Response<Void> reviewCancel(TaskFinalCheckReq req) {
        businessService.reviewCancel(req);
        return Response.buildSuccess();
    }

}

