package cn.dotfashion.soa.pim.service.category.audit.task;

import cn.dotfashion.soa.api.vo.Result;
import cn.dotfashion.soa.pim.entity.CategoryAuditTaskEntity;
import cn.dotfashion.soa.pim.vo.categorytask.request.*;
import cn.dotfashion.soa.pim.vo.categorytask.response.*;
import cn.dotfashion.soa.pim.vo.response.PageResponse;

import java.util.List;

public interface CategoryAuditTaskBusinessService {

    /**
     * 发起提报申请
     * @param attrCheckInfoReq
     * @return
     */
    CategoryAuditTaskApplyResp categoryAuditTaskApply(CategoryAuditTaskApplyReq attrCheckInfoReq);


    /**
     * 查询版本分类层级信息
     * @param req
     * @return
     */
    CategoryLevelTaskGetResp getCategoryLevelVersionInfo(CategoryLevelTaskGetReq req);

    CategoryLevelTaskGetResp getCategoryLevelVersionInfoOA(Long requestId);

    /**
     * 前置校验创建版本分类信息
     * @param req
     */
    void submitCategoryLevelVersionInfo(CategoryLevelTaskSubReq req);

    /**
     * 异步处理创建版本分类信息
     * @param req
     */
    void asyncLevelDiffVersionInfoHandler(CategoryLevelTaskSubReq req);


    /**
     * 提交分类变更信息
     * @param req
     */
    void submitChangeCategoryInfo(SubmitCategoryInfoDataReq req);

    /**
     * 确认版本结果
     * @param req
     */
    void verifyVersion(VerifyVersionReq req);


    /**
     * 获取任务列表
     * @param req
     * @param pageSize
     * @param pageNum
     * @return
     */
    PageResponse<GetTaskListResp> getTaskList(GetTaskListReq req, Integer pageSize, Integer pageNum);

    /**
     * 申请UBMP
     * @param req
     */
    void applyToUbmp(ApplyToUbmpReq req);

    /**
     * 异步处理分类层级版本信息
     * @param requestId
     */
    void afterHandler(Long requestId);

    /**
     * 异步处理分类层级版本信息
     * @param requestId
     * @return
     */
    List<CategoryAuditTaskEntity> afterHandlerSetAuditTime(Long requestId);

    /**
     * 批量设置分类展示时间
     * @param req
     */
    void batchSetUpCategoryShowTime(BatchSetUpCategoryReq req);


    /**
     * 保存审批说明
     * @param req
     */
    void saveCategoryAuditReference(CategoryAuditReferenceSaveReq req);

    /**
     * 获取审批说明
     * @param req
     * @return
     */
    List<CategoryAuditReferenceResp> getCategoryAuditReference(CategoryAuditReferenceReq req);

    /**
     * 获取分类展示时间
     * @param req
     * @return
     */
    List<SetUpCategoryShowTimeResp> getSetUpCategoryShowTime(SetUpCategoryShowTimeReq req);

    /**
     * 获取分类审批说明版本
     * @param req
     * @return
     */
    GetCategoryAuditVersionResp getCategoryAuditVersionList(CategoryAuditReferenceReq req);

    /**
     * 异步对比审批信息生成审批页数据
     * @param taskId
     */
    void asyncLevelDiffVersionInfoHandler(Long taskId);

    /**
     *
     * 分类任务核查
     * @param req
     */
    void categoryTaskAudit(categoryTaskAuditReq req);


    /**
     * 分类任务检查当前操作人是否为中台业务角色
     * @return
     */
    boolean checkOperatorRule();

    /**
     * 检查工号ID
     * @return
     */
    boolean checkEmployeeId();


    void taskFinalCheck(TaskFinalCheckReq req);

    /**
     * 取消版本
     * @param req
     */
    void versionCancel(VerifyVersionReq req);

    /**
     *
     * @param taskId
     */
    void rollBackDiffUbmp(Long taskId);


    void auditCancel(TaskFinalCheckReq req);
}
