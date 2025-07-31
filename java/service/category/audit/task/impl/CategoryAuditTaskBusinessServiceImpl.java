package cn.company.soa.module.service.category.audit.task.impl;

import cn.company.soa.api.constant.ErrorCodeDefine;
import cn.company.soa.api.vo.Response;
import cn.company.soa.api.vo.UploadResp;
import cn.company.soa.framework.exception.BusinessException;
import cn.company.soa.framework.util.BeanTools;
import cn.company.soa.framework.util.DateFormatTools;
import cn.company.soa.framework.util.ExceptionTools;
import cn.company.soa.framework.util.JsonTools;
import cn.company.soa.log.LtLogFormat;
import cn.company.soa.module.annotation.UlpAuthorityCheck;
import cn.company.soa.module.client.PdsFrontCategoryClient;
import cn.company.soa.module.client.vo.req.BatchChangeCategoryControlShowReq;
import cn.company.soa.module.client.vo.req.BatchChangeCategoryControlStatueReq;
import cn.company.soa.module.client.vo.req.BatchSaveCategoryControlDetailRep;
import cn.company.soa.module.client.vo.req.BatchSaveCategoryControlReq;
import cn.company.soa.module.client.vo.resp.BatchSaveCategoryControlDetailResp;
import cn.company.soa.module.dto.CategoryAuditTaskWechatDto;
import cn.company.soa.module.dto.category.audit.task.CategoryAuditReferenceDto;
import cn.company.soa.module.dto.category.audit.task.CategoryDiffChangeDto;
import cn.company.soa.module.dto.category.audit.task.CategoryTreeVersionDto;
import cn.company.soa.module.entity.CategoryAuditTaskEntity;
import cn.company.soa.module.entity.CategoryAuditTaskTimeEntity;
import cn.company.soa.module.entity.CategoryAuditTaskVersionEntity;
import cn.company.soa.module.enums.FileExportType;
import cn.company.soa.module.enums.LanguageEnum;
import cn.company.soa.module.enums.MccSensitiveSceneEnum;
import cn.company.soa.module.enums.category.audit.CategoryAuditTaskStepEnum;
import cn.company.soa.module.enums.category.audit.ChangeTagEnum;
import cn.company.soa.module.enums.category.audit.TaskStateEnum;
import cn.company.soa.module.proxy.*;
import cn.company.soa.module.service.AsyncBatchTaskService;
import cn.company.soa.module.service.AsyncExportService;
import cn.company.soa.module.service.category.audit.task.*;
import cn.company.soa.module.task.CategoryAuditNotifyDelayTask;
import cn.company.soa.module.task.CategoryAuditWechatDelayTask;
import cn.company.soa.module.util.HttpRequestTools;
import cn.company.soa.module.util.UserContextUtil;
import cn.company.soa.module.vo.categorytask.request.*;
import cn.company.soa.module.vo.categorytask.response.*;
import cn.company.soa.module.vo.request.sensitive.SensitiveWordVer3Req;
import cn.company.soa.module.vo.response.BatchQueryCategoryNameResp;
import cn.company.soa.module.vo.response.PageResponse;
import cn.company.soa.task.embedded.core.TaskParams;
import cn.company.soa.task.embedded.core.TaskScheduler;
import cn.company.soa.ubpmSdk.rpc.UbpmClientData;
import cn.company.soa.ubpmSdk.rpc.entity.dto.WorkflowDoCreateDTO;
import cn.company.soa.ubpmSdk.rpc.entity.dto.detailData.MainData;
import cn.company.soa.ubpmSdk.rpc.entity.response.DoCreateResponse;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shein.common.combine.CombineManager;
import com.shein.common.enums.ModuleTypeEnum;
import com.shein.common.exception.BusinessAssert;
import com.shein.common.response.UlpGetEnNameResp;
import com.shein.common.serivce.UlpCommonService;
import com.shein.common.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CategoryAuditTaskBusinessServiceImpl implements CategoryAuditTaskBusinessService {

    @Resource
    private PdsProxy pdsProxy;

    @Resource
    private CategoryAuditTaskService categoryAuditTaskService;

    @Resource
    private CategoryAuditTaskVersionService categoryAuditTaskVersionService;

    @Resource
    private CategoryAuditTaskBusinessService self;

    @Resource
    private CombineManager combineManager;

    @Value("${category.audit.task.apply.to.ubmp.operator.rule.list:super_admin}")
    private List<String> applyToUbmpOperatorRuleList;

    @Value("${category.audit.task.apply.to.ubmp.work.flow.id:31524}")
    private Integer applyToUbmpWorkFlowId;

    @Value("${category.audit.task.apply.to.ubmp.operator.id:10002132,10016192,10010501}")
    private List<String> applyToUbmpOperatorId;

    @Value("${category.audit.task.delay.time:10}")
    private Integer delayTime;

    @Resource
    private UbpmClientData ubpmClientData;

    @Resource
    private UlpCommonService ulpCommonService;

    @Resource
    private UbpmProxy ubpmProxy;

    @Resource
    private CategoryAuditTaskTimeService categoryAuditTaskTimeService;

    @Resource
    private CategoryAuditTaskLogService categoryAuditTaskLogService;

    @Resource
    private CategoryAuditTaskReferenceService categoryAuditTaskReferenceService;

    @Resource
    @Lazy
    private AsyncExportService asyncExportService;

    @Resource
    private TaskScheduler taskScheduler;

    @Resource
    private UlpCommonProxy ulpCommonProxy;

    @Resource
    private PdsFrontCategoryClient pdsFrontCategoryClient;

    @Value("${category.audit.task.pre_check:false}")
    private boolean preCheckSwitch;

    @Resource
    private AbcSensitiveVer3Proxy abcSensitiveVer3Proxy;

    @Lazy
    @Resource
    private AsyncBatchTaskService asyncBatchTaskService;

    public boolean checkOperatorRule() {
        return UserContextUtil.getRoleCodes().stream().anyMatch(roleCode -> applyToUbmpOperatorRuleList.contains(roleCode));
    }

    public boolean checkEmployeeId() {
        return applyToUbmpOperatorId.contains(UserContextUtil.getEmployeeId());
    }

    @Override
    public void taskFinalCheck(TaskFinalCheckReq req) {
        BusinessAssert.isTrue(checkOperatorRule(), ModuleTypeEnum.BASIC,"当前用户无权限操作");
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(req.getTaskId());
        //任务必须为已审核未核查
        BusinessAssert.isTrue(TaskStateEnum.SUBMITTED_TO_UBPM_APPROVED.getCode().equals(taskEntity.getTaskState()), ModuleTypeEnum.BASIC, "任务状态必须为已审核");
        taskEntity.setTaskState(TaskStateEnum.FINAL_CHECKED.getCode());
        categoryAuditTaskService.updateById(taskEntity);
        categoryAuditTaskLogService.saveTaskStateLog(taskEntity.getId(), TaskStateEnum.FINAL_CHECKED, getOperator());
        GetTaskListReq getTaskListReq = new GetTaskListReq();
        getTaskListReq.setTaskSnList(Collections.singletonList(taskEntity.getTaskSn()));
        getTaskListReq.setNotify(true);
        asyncExportService.initExportTask(FileExportType.EXPORT_CATEGORY_AUDIT_DETAIL, getTaskListReq, HttpRequestTools.getRequest());
    }

    @Override
    public void versionCancel(VerifyVersionReq req) {
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(req.getTaskId());
        CategoryTreeVersionDto categoryTreeVersion = categoryAuditTaskVersionService.getCategoryTreeVersion(req.getTaskId(), req.getVersion());

        BusinessAssert.isTrue(categoryTreeVersion.getVersion().equals(req.getVersion()) && Objects.equals(getOperator(), categoryTreeVersion.getApplicantName()), ModuleTypeEnum.BASIC,"当前用户无权限操作");
        //版本必须为已审核未核查
        BusinessAssert.isTrue(TaskStateEnum.applyStateList.contains(taskEntity.getTaskState()), ModuleTypeEnum.BASIC, "版本状态不支持取消");

        //删除
        categoryAuditTaskVersionService.deleteById(categoryTreeVersion.getVersionId());
        //判断是否还有版本 没有则将任务走向已取消
        List<Integer> versionList = categoryAuditTaskVersionService.getVersionList(taskEntity.getId());
        if (CollectionUtils.isEmpty(versionList)) {
            taskEntity.setTaskState(TaskStateEnum.FINAL_CANCELLED.getCode());
            categoryAuditTaskService.updateById(taskEntity);
            categoryAuditTaskLogService.saveTaskStateLog(taskEntity.getId(), TaskStateEnum.FINAL_CANCELLED, getOperator());
        }
    }

    @Override
    public void rollBackDiffUbmp(Long taskId) {
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(taskId);
        Long requestId = taskEntity.getRequestId();
        taskEntity.setRequestId(0L);
        taskEntity.setAuditVersion(0);
        taskEntity.setTaskState(TaskStateEnum.MAINTAINED_AND_SUBMIT.getCode());
        categoryAuditTaskService.updateById(taskEntity);
        ubpmProxy.deleteRequestId(requestId);
    }

    @Override
    public void auditCancel(TaskFinalCheckReq req) {
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(req.getTaskId());
        //任务必须为已审核未核查
        BusinessAssert.isTrue(TaskStateEnum.SUBMITTED_TO_UBPM.getCode().equals(taskEntity.getTaskState()), ModuleTypeEnum.BASIC, "任务状态必须为待审核");
        BusinessAssert.isTrue(checkOperatorRule(), ModuleTypeEnum.BASIC,"当前用户无权限操作");
        taskEntity.setTaskState(TaskStateEnum.FINAL_CANCELLED.getCode());
        categoryAuditTaskService.updateById(taskEntity);
        categoryAuditTaskLogService.saveTaskStateLog(taskEntity.getId(), TaskStateEnum.FINAL_CANCELLED, getOperator());
        ubpmProxy.deleteRequestId(taskEntity.getRequestId());
    }

    @Override
    public CategoryAuditTaskApplyResp categoryAuditTaskApply(CategoryAuditTaskApplyReq req) {
        //获取当前用户信息
        String userName = getOperator();

        //校验分类ID必须为顶级
        BusinessAssert.isTrue(categoryIdIsTop(req.getTopCategoryId()), ModuleTypeEnum.BASIC,"分类ID必须为顶级");

        //校验分类是否已经在提报中
        CategoryAuditTaskEntity auditTaskEntity = categoryAuditTaskService.getTopCategoryNotLock(req.getTopCategoryId());

        //开始创建任务
        CategoryAuditTaskApplyResp resp = new CategoryAuditTaskApplyResp();
        if (Objects.nonNull(auditTaskEntity)) {
            //已存在则直接复用
            resp.setTaskId(auditTaskEntity.getId());
            //并且校验有没有版本锁定
            CategoryAuditTaskVersionEntity versionEntity = categoryAuditTaskVersionService.hasProcessingVersion(auditTaskEntity.getId());
            if (Objects.nonNull(versionEntity)) {
                throw new BusinessException(ErrorCodeDefine.MessageCode.VALIDERROR, "分类已经被锁定，有进行中的版本,操作人:" + ulpCommonProxy.cnNameToEnName(versionEntity.getApplicantName()) + "，请稍后重试");
            }
        } else {
            CategoryAuditTaskEntity appCategoryAuditTask = categoryAuditTaskService.createAppCategoryAuditTask(userName, req.getTopCategoryId(), req.getSourceSystem());
            resp.setTaskId(appCategoryAuditTask.getId());
            //记录日志
            categoryAuditTaskLogService.saveTaskStateLog(appCategoryAuditTask.getId(), TaskStateEnum.getByCode(appCategoryAuditTask.getTaskState()), userName);
        }
        return resp;
    }

    @Override
    public CategoryLevelTaskGetResp getCategoryLevelVersionInfo(CategoryLevelTaskGetReq req) {
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(req.getTaskId());
        CategoryLevelTaskGetResp resp = new CategoryLevelTaskGetResp();
        resp.setTaskId(taskEntity.getId());
        resp.setTaskState(taskEntity.getTaskState());
        resp.setTaskSn(taskEntity.getTaskSn());

        //获取版本数据
        Integer version = req.getVersion();
        if (version == null) {
            version = categoryAuditTaskVersionService.latestVersion(req.getTaskId());
        }
        CategoryTreeVersionDto categoryTreeVersionDto = categoryAuditTaskVersionService.getCategoryTreeVersion(req.getTaskId(), version);
        //分类树信息
        List<Object> combineList = new ArrayList<>();
        resp.setCategoryTreeInfo(categoryAuditTaskVersionService.convertToCategoryNodeInfo(categoryTreeVersionDto.getTree(), combineList));
        combineManager.combineModelBatch(combineList.stream().filter(v -> v instanceof CategoryLevelTaskGetResp.CategoryNodeInfo.Tag).collect(Collectors.toList()), true);
        combineManager.combineModelBatch(combineList.stream().filter(v -> v instanceof CategoryLevelTaskGetResp.CategoryNodeInfo.PartRelation).collect(Collectors.toList()), true);


        Map<String, CategoryTreeVersionDto.CategoryNode> map = new HashMap<>();
        categoryAuditTaskVersionService.convertMapByNodeDto(categoryTreeVersionDto.getTree(), map);
        resp.setVersion(version);
        resp.setApplicantName(categoryTreeVersionDto.getApplicantName());
        List<CategoryLevelTaskGetResp.CategoryInfo> infoList = new ArrayList<>();
        List<CategoryLevelTaskGetResp.ApprovalReference> referenceList = new ArrayList<>();
        //参考信息和附加信息
        categoryTreeVersionDto.getChangeDataList().forEach(v -> {
            CategoryLevelTaskGetResp.CategoryInfo categoryInfo = new CategoryLevelTaskGetResp.CategoryInfo();
            categoryInfo.setKey(v.getKey());
            categoryInfo.setChangeTagList(v.getCTags());
            CategoryLevelTaskGetResp.CategoryInfo.OldParentChange oldParentChange = new CategoryLevelTaskGetResp.CategoryInfo.OldParentChange();
            oldParentChange.setOldParentKey(v.getOldParentKey());
            oldParentChange.setNewParentKey(v.getParentKey());
            oldParentChange.setOldParentName(Optional.ofNullable(map.get(v.getOldParentKey())).orElse(new CategoryTreeVersionDto.CategoryNode()).getName());
            oldParentChange.setNewParentName(Optional.ofNullable(map.get(v.getParentKey())).orElse(new CategoryTreeVersionDto.CategoryNode()).getName());
            categoryInfo.setOldParentChange(oldParentChange);
            infoList.add(categoryInfo);
            CategoryLevelTaskGetResp.ApprovalReference approvalReference = new CategoryLevelTaskGetResp.ApprovalReference();
            approvalReference.setCategoryKey(v.getKey());
            approvalReference.setReason(v.getReason());
            approvalReference.setInfo(BeanTools.copyBean(v.getReferenceInfo(), CategoryLevelTaskGetResp.ApprovalReference.Info.class));
            approvalReference.setDefinition(v.getDefinition());
            approvalReference.setDefinitionImageList(v.getDImages());
            referenceList.add(approvalReference);
        });
        resp.setCategoryInfoList(infoList);
        resp.setApprovalReferenceList(referenceList);
        resp.setTopCategoryInfo(categoryAuditTaskService.getTopCategoryInfo(taskEntity.getTopCategoryId()));
        resp.setInsertTime(categoryTreeVersionDto.getInsertTime());
        return resp;
    }

    @Override
    public CategoryLevelTaskGetResp getCategoryLevelVersionInfoOA(Long requestId) {
        CategoryAuditTaskEntity entity = categoryAuditTaskService.lambdaQuery().eq(CategoryAuditTaskEntity::getRequestId, requestId)
                .oneOpt().orElseThrow(() -> new BusinessException(ErrorCodeDefine.MessageCode.BADREQUEST, "OA请求任务不存在"));
        BusinessAssert.isTrue(entity.getAuditVersion() != 0, ModuleTypeEnum.BASIC, "OA请求的审批版本不存在");
        CategoryLevelTaskGetReq req = new CategoryLevelTaskGetReq();
        req.setTaskId(entity.getId());
        req.setVersion(entity.getAuditVersion());
        return getCategoryLevelVersionInfo(req);
    }

    @Override
    public void submitCategoryLevelVersionInfo(CategoryLevelTaskSubReq req) {
        //获取当前用户信息
        String userName = getOperator();
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(req.getTaskId());
        //校验任务状态
        BusinessAssert.isTrue(TaskStateEnum.applyStateList.contains(taskEntity.getTaskState()), ModuleTypeEnum.BASIC,"任务状态不正确:" + TaskStateEnum.convertDescription(taskEntity.getTaskState()));
        //校验是否有在进行的版本
        CategoryAuditTaskVersionEntity processingVersion = categoryAuditTaskVersionService.hasProcessingVersion(req.getTaskId());
        if (Objects.nonNull(processingVersion)) {
            //校验是否是同一个版本
            throw new BusinessException(ErrorCodeDefine.MessageCode.BADREQUEST, "存在未完成的版本:(" + processingVersion.getVersion() + "),作者:" + processingVersion.getApplicantName());
        }
        BusinessAssert.isTrue(Objects.equals(req.getCategoryTreeInfo().getCategoryId(), taskEntity.getTopCategoryId()), ModuleTypeEnum.BASIC,"分类数据和任务关联top分类不一致");

        CategoryAuditTaskVersionEntity version = categoryAuditTaskVersionService.createVersion(req.getTaskId(), userName);
        req.setVersionId(version.getId());
        req.setOperator(userName);
        //更新状态
        taskEntity.setTaskState(TaskStateEnum.MAINTAINED_AND_SUBMIT.getCode());
        categoryAuditTaskService.updateById(taskEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void asyncLevelDiffVersionInfoHandler(CategoryLevelTaskSubReq req) {
        //查询任务主表
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(req.getTaskId());

        //检查分类数据和任务关联top分类是否一致
        Long reqTopCategoryId = req.getCategoryTreeInfo().getCategoryId();
        BusinessAssert.isTrue(Objects.equals(reqTopCategoryId, taskEntity.getTopCategoryId()), ModuleTypeEnum.BASIC,"分类数据和任务关联top分类不一致");


        //查询版本表
        CategoryAuditTaskVersionEntity versionEntity = categoryAuditTaskVersionService.getById(req.getVersionId());
        BusinessAssert.isTrue(Objects.equals(versionEntity.getCategoryAuditTaskId(), req.getTaskId()), ModuleTypeEnum.BASIC,"版本数据和任务关联不一致");
        BusinessAssert.isTrue(versionEntity.getStep() == 1, ModuleTypeEnum.BASIC,"版本数据状态不正确");

        //查询最新版本数据
        Integer latestVersion = categoryAuditTaskVersionService.latestVersion(req.getTaskId()) - 1;
        CategoryTreeVersionDto latestVersionDto = categoryAuditTaskVersionService.getCategoryTreeVersion(req.getTaskId(), latestVersion);
        Map<String, CategoryTreeVersionDto.CategoryNode> oldNodeMap = new HashMap<>();
        categoryAuditTaskVersionService.convertMapByNodeDto(latestVersionDto.getTree(), oldNodeMap);
        //转换当前数据
        CategoryTreeVersionDto.CategoryNode categoryNode = categoryAuditTaskVersionService.convertToCategoryNodeReq(req.getCategoryTreeInfo(), "0", oldNodeMap);
        //对比数据
        Map<String, CategoryTreeVersionDto.ChangeData> changeDataMap = new HashMap<>();
        //层级和新增比较
        CategoryTreeVersionDto.compareTree(latestVersionDto.getTree(), categoryNode, changeDataMap);
        //转换数据
        Map<String, CategoryTreeVersionDto.CategoryNode> newNodeMap = new HashMap<>();
        categoryAuditTaskVersionService.convertMapByNodeDto(categoryNode, newNodeMap);

        if (CollectionUtils.isNotEmpty(changeDataMap.values())) {
            //需要补充oldparentKey
            changeDataMap.values().forEach(v -> {
                if (v.getCTags().contains(ChangeTagEnum.LEVEL_CHANGE)) {
                    CategoryTreeVersionDto.CategoryNode oldNode = oldNodeMap.get(v.getKey());
                    if (Objects.nonNull(oldNode)) {
                        v.setOldParentKey(oldNode.getPKey());
                    }
                }
                //新增分类需要比较名称是否重复
                if (v.getCTags().contains(ChangeTagEnum.ADD)) {
                    checkCategoryNameRepeat(newNodeMap.get(v.getKey()), newNodeMap);
                }
            });
        }
        //备注名翻译中文补充
        categoryAuditTaskVersionService.fillRemarkNameToCnName(new ArrayList<>(changeDataMap.values()), newNodeMap);
        //预校验 找到所有新增的分类
        Map<String, CategoryTreeVersionDto.ChangeData> preCheckMap = new HashMap<>(changeDataMap);
        newNodeMap.forEach((key, changeData) -> {
            if (CategoryTreeVersionDto.isNewKey(key) && !preCheckMap.containsKey(key)) {
                preCheckMap.put(key, new CategoryTreeVersionDto.ChangeData(key));
            }
        });
        preCheckCategory(new ArrayList<>(preCheckMap.values()), newNodeMap, req.getOperator(), false);

        //合并上一版本的参考信息
        mergeReferInfo(changeDataMap, latestVersionDto.getChangeDataList());
        //更新数据
        CategoryTreeVersionDto versionDto = new CategoryTreeVersionDto();
        versionDto.setChangeDataList(new ArrayList<>(changeDataMap.values()));
        versionDto.setTree(categoryNode);
        categoryAuditTaskVersionService.updateLevelEdit(versionDto, versionEntity);

        //记录日志
        categoryAuditTaskLogService.saveVersionLog(taskEntity.getId(), versionEntity.getVersion(), req.getOperator());

    }

    private void mergeReferInfo(Map<String, CategoryTreeVersionDto.ChangeData> newChangeMap, List<CategoryTreeVersionDto.ChangeData> oldChangeList) {
        oldChangeList.forEach(oldChange -> {
            CategoryTreeVersionDto.ChangeData changeData = newChangeMap.getOrDefault(oldChange.getKey(), new CategoryTreeVersionDto.ChangeData(oldChange.getKey()));
            changeData.setReferenceInfo(oldChange.getReferenceInfo());
            changeData.setReason(oldChange.getReason());
            changeData.setDImages(oldChange.getDImages());
            changeData.setDefinition(oldChange.getDefinition());
            newChangeMap.put(oldChange.getKey(), changeData);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitChangeCategoryInfo(SubmitCategoryInfoDataReq req) {

        //获取和校验
        CategoryAuditTaskVersionEntity versionEntity = checkEditInfoVersion(req.getTaskId(), req.getVersion());
        //开始处理数据
        //先保存分类主数据
        CategoryTreeVersionDto categoryTreeVersion = categoryAuditTaskVersionService.getCategoryTreeVersion(req.getTaskId(), req.getVersion());

        //转map
        Map<String, CategoryTreeVersionDto.CategoryNode> categoryNodeMap = new HashMap<>();
        categoryAuditTaskVersionService.convertMapByNodeDto(categoryTreeVersion.getTree(), categoryNodeMap);
        BusinessAssert.isTrue(categoryNodeMap.containsKey(req.getKey()), ModuleTypeEnum.BASIC,"分类数据不存在");
        CategoryTreeVersionDto.CategoryNode categoryNode = categoryNodeMap.get(req.getKey());
        List<CategoryTreeVersionDto.ChangeData> changeDataList = categoryTreeVersion.getChangeDataList();
        CategoryTreeVersionDto.ChangeData changeData = changeDataList.stream().filter(v -> Objects.equals(v.getKey(), req.getKey())).findFirst().orElse(new CategoryTreeVersionDto.ChangeData(req.getKey()));

        //更新了备注名需要验证重复
        if (!Objects.equals(categoryNode.getName(), req.getCategoryName())) {
            checkCategoryNameRepeat(categoryNode, categoryNodeMap);
        }

        //对比覆盖数据
        List<ChangeTagEnum> changeTagEnums = categoryNode.compareAndEditInfo(req.getCategoryName(), req.getCategoryCnName(), req.getIsShow(), req.getIsReturn(), req.getTagList(), req.getCategoryPartRelationList());

        changeData.getCTags().addAll(changeTagEnums);

        //保存版本审批参考信息
        changeData.setDefinition(req.getDefinition());
        changeData.setReason(req.getReason());
        changeData.setDImages(req.getDefinitionImageList());
        changeData.setReferenceInfo(BeanTools.copyBean(req.getReferenceInfo(), CategoryTreeVersionDto.ChangeData.Info.class));
        Optional<CategoryTreeVersionDto.ChangeData> first = changeDataList.stream().filter(v -> Objects.equals(v.getKey(), changeData.getKey())).findFirst();
        if (!first.isPresent()) {
            changeDataList.add(changeData);
        }

        //预校验 向上找到根节点
        List<CategoryTreeVersionDto.ChangeData> preChangeDataList = new ArrayList<>();
        preChangeDataList.add(changeData);
        putParentNode(categoryNode.getPKey(), categoryNodeMap, preChangeDataList);
        preCheckCategory(preChangeDataList, categoryNodeMap, getOperator(), true);

        //更新
        categoryAuditTaskVersionService.updateLevelEdit(categoryTreeVersion, versionEntity);

    }

    private void putParentNode(String pkey, Map<String, CategoryTreeVersionDto.CategoryNode> categoryNodeMap, List<CategoryTreeVersionDto.ChangeData> preChangeDataList) {
        if (!CategoryTreeVersionDto.isNewKey(pkey)) {
            return;
        }
        CategoryTreeVersionDto.CategoryNode categoryNode = categoryNodeMap.get(pkey);
        if (Objects.isNull(categoryNode)) {
            return;
        }
        preChangeDataList.add(new CategoryTreeVersionDto.ChangeData(categoryNode.getKey()));
        if (CategoryTreeVersionDto.isNewKey(categoryNode.getPKey())) {
            putParentNode(categoryNode.getPKey(), categoryNodeMap, preChangeDataList);
        }
    }

    @Override
    public void verifyVersion(VerifyVersionReq req) {
        CategoryAuditTaskVersionEntity versionEntity = checkEditInfoVersion(req.getTaskId(), req.getVersion());
        //校验不能重复提交
        BusinessAssert.isTrue(!Objects.equals(versionEntity.getStep(),CategoryAuditTaskStepEnum.INFO_EDIT.getCode()), ModuleTypeEnum.BASIC,"不能重复提交");
        //开始处理数据
        categoryAuditTaskVersionService.verifyVersion(versionEntity.getCategoryAuditTaskId(), versionEntity.getVersion());
        //更新任务到已提交待提报
        categoryAuditTaskService.updateTaskStatus(versionEntity.getCategoryAuditTaskId(), TaskStateEnum.MAINTAINED_AND_SUBMIT);
        //记录日志
        categoryAuditTaskLogService.saveTaskStateLog(versionEntity.getCategoryAuditTaskId(), TaskStateEnum.MAINTAINED_AND_SUBMIT, getOperator());
    }

    @Override
    public PageResponse<GetTaskListResp> getTaskList(GetTaskListReq req, Integer pageSize, Integer pageNum) {
        LambdaQueryChainWrapper<CategoryAuditTaskEntity> wrapper = categoryAuditTaskService.lambdaQuery().eq(CategoryAuditTaskEntity::getIsDel, false)
                .in(CollectionUtils.isNotEmpty(req.getTaskState()), CategoryAuditTaskEntity::getTaskState, req.getTaskState())
                .in(CollectionUtils.isNotEmpty(req.getTaskSnList()), CategoryAuditTaskEntity::getTaskSn, req.getTaskSnList())
                .in(CollectionUtils.isNotEmpty(req.getRequesterIdList()), CategoryAuditTaskEntity::getRequestId, req.getRequesterIdList())
                .eq(StringUtils.isNotEmpty(req.getApplicantName()), CategoryAuditTaskEntity::getApplicantName, req.getApplicantName())
                .in(CollectionUtils.isNotEmpty(req.getTopCategoryIdList()), CategoryAuditTaskEntity::getTopCategoryId, req.getTopCategoryIdList())
                .eq(StringUtils.isNotBlank(req.getSourceSystem()), CategoryAuditTaskEntity::getSourceSystem, req.getSourceSystem())
                .and(Objects.nonNull(req.getApplicantTimeStart()) && Objects.nonNull(req.getApplicantTimeEnd()), w -> w.between(CategoryAuditTaskEntity::getApplicantTime, req.getApplicantTimeStart(), req.getApplicantTimeEnd()))
                .and(Objects.nonNull(req.getAuditTimeStart()) && Objects.nonNull(req.getAuditTimeEnd()), w -> w.between(CategoryAuditTaskEntity::getAuditTime, req.getAuditTimeStart(), req.getAuditTimeEnd()))
                .orderByDesc(CategoryAuditTaskEntity::getId);
        Page<CategoryAuditTaskEntity> page = wrapper.page(new Page<>(pageNum, pageSize));
        List<GetTaskListResp> respList = page.getRecords().stream().map(entity -> {
            GetTaskListResp resp = new GetTaskListResp();
            resp.setTaskSn(entity.getTaskSn());
            resp.setId(entity.getId());
            resp.setTaskState(entity.getTaskState());
            resp.setRequestRemark(entity.getRequestRemark());
            resp.setRequestId(entity.getRequestId());
            TopCategoryInfoResp categoryInfoResp = new TopCategoryInfoResp();
            categoryInfoResp.setTopCategoryId(entity.getTopCategoryId());
            resp.setTopCategoryInfo(categoryInfoResp);
            return resp;
        }).collect(Collectors.toList());
        combineManager.combineModelBatch(respList, true, true);
        PageResponse pageResponse = new PageResponse();
        pageResponse.setTotal(page.getTotal());
        pageResponse.setCurrent(page.getCurrent());
        pageResponse.setRecords(respList);
        pageResponse.setSize(page.getSize());
        return pageResponse;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyToUbmp(ApplyToUbmpReq req) {

        //校验操作人角色
        BusinessAssert.isTrue(checkOperatorRule(), ModuleTypeEnum.BASIC,"当前用户无权限操作");

        req.getTaskIdList().forEach(taskId -> {
            CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(taskId);
            //校验任务状态
            BusinessAssert.isTrue(TaskStateEnum.MAINTAINED_AND_SUBMIT.getCode().equals(taskEntity.getTaskState()), ModuleTypeEnum.BASIC,"已接收待提报的才可以提报");
            //校验重复申请
            BusinessAssert.isTrue(Objects.equals(taskEntity.getRequestId(), 0L), ModuleTypeEnum.BASIC,"任务已被申请人操作,请勿重复发起");

            //检查任务版本已完成操作
            CategoryAuditTaskVersionEntity processingVersion = categoryAuditTaskVersionService.hasProcessingVersion(taskId);
            if (Objects.nonNull(processingVersion)) {
                throw new BusinessException(ErrorCodeDefine.MessageCode.VALIDERROR, "存在未完成的版本:(" + processingVersion.getVersion() + "),操作人:" + ulpCommonProxy.cnNameToEnName(processingVersion.getApplicantName()));
            }
            taskEntity.setAuditVersion(categoryAuditTaskVersionService.latestVersion(taskId));
            taskEntity.setRequestName(getOperator());
            taskEntity.setRequestTime(new Date());
            //发起提报
            taskEntity.setRequestId(doUbmpSubmit(taskEntity));
            taskEntity.setTaskState(TaskStateEnum.SUBMITTED_TO_UBPM.getCode());
            categoryAuditTaskService.updateById(taskEntity);

            //记录日志
            //categoryAuditTaskLogService.saveTaskStateLog(taskEntity.getId(), TaskStateEnum.SUBMITTED_TO_UBPM, getOperator());
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void afterHandler(Long requestId) {
        List<CategoryAuditTaskEntity> list = self.afterHandlerSetAuditTime(requestId);
        list.forEach(taskEntity -> {
            taskEntity.setTaskState(TaskStateEnum.SUBMITTED_TO_UBPM_APPROVED.getCode());
            UbpmProxyImpl.LeastAuditInfo leastAuditName = ubpmProxy.getLeastAuditName(requestId, taskEntity.getApplicantName());
            taskEntity.setAuditName(leastAuditName.getAuditName());
            taskEntity.setRequestRemark(leastAuditName.getRemark());
            categoryAuditTaskService.updateById(taskEntity);
            //对比实际版本和审批版本的数据 生成差异数据
            List<CategoryDiffChangeDto> diffChangeDtoList = categoryAuditTaskVersionService.handleDiffChangeDto(taskEntity);
            //调用批量接口保存差异数据
            if (CollectionUtils.isNotEmpty(diffChangeDtoList)) {
                List<BatchSaveCategoryControlDetailResp> detailRespList = pdsProxy.batchSaveCategoryTask(diffChangeDtoList, taskEntity.getApplicantName(), taskEntity.getAuditTime(), taskEntity.getTopCategoryId());
                //将主键反向刷入审批版本数据中
                categoryAuditTaskVersionService.updateNewCategoryId(taskEntity, detailRespList);
            }
            categoryAuditTaskLogService.saveTaskStateLog(taskEntity.getId(), TaskStateEnum.SUBMITTED_TO_UBPM_APPROVED, taskEntity.getAuditName());
            //todo 企业微信通知
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<CategoryAuditTaskEntity> afterHandlerSetAuditTime(Long requestId) {
        List<CategoryAuditTaskEntity> list = categoryAuditTaskService.lambdaQuery().eq(CategoryAuditTaskEntity::getRequestId, requestId)
                .eq(CategoryAuditTaskEntity::getIsDel, false)
                .eq(CategoryAuditTaskEntity::getTaskState, TaskStateEnum.SUBMITTED_TO_UBPM.getCode())
                .list();
        BusinessAssert.isNotEmpty(list, ModuleTypeEnum.BASIC,"未找到对应的任务");

        list.forEach(taskEntity -> {
            //判断审核时间是否大于初始年
            if (Objects.isNull(taskEntity.getAuditTime()) || taskEntity.getAuditTime().before(new Date(1))) {
                taskEntity.setAuditTime(new Date());
                categoryAuditTaskService.updateById(taskEntity);
            }
        });
        return list;
    }

    @Override
    public void batchSetUpCategoryShowTime(BatchSetUpCategoryReq req) {
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(req.getTaskId());
        //判断任务状态
        BusinessAssert.isTrue(TaskStateEnum.SUBMITTED_TO_UBPM_APPROVED.getCode().equals(taskEntity.getTaskState()), ModuleTypeEnum.BASIC,"任务状态不正确,必须为已审核待核查");
        //判断时间必须超过当前时间+1小时
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        BusinessAssert.isTrue(req.getShowTime().after(calendar.getTime()), ModuleTypeEnum.BASIC,"分类展示时间需晚于当前时间1小时后");
        //开始创建分类展示任务
        //找到分类对应key的映射
        CategoryTreeVersionDto categoryTreeVersion = categoryAuditTaskVersionService.getCategoryTreeVersion(taskEntity.getId(), taskEntity.getAuditVersion());
        Map<String, CategoryTreeVersionDto.CategoryNode> map = new HashMap<>();
        categoryAuditTaskVersionService.convertMapByNodeDto(categoryTreeVersion.getTree(), map);

        categoryAuditTaskTimeService.createCategoryAuditTaskTime(taskEntity.getId(), req.getKeyList().stream().map(key -> {
            CategoryTreeVersionDto.CategoryNode categoryNode = map.get(key);
            BusinessAssert.isTrue(Objects.nonNull(categoryNode), ModuleTypeEnum.BASIC,"分类key不存在");
            return categoryNode;
        }).collect(Collectors.toList()), req.getShowTime());
        //记录日志
        categoryAuditTaskLogService.saveTaskShowTimeLog(taskEntity.getId(), req.getKeyList(), req.getShowTime(), getOperator());
    }

    @UlpAuthorityCheck
    @Override
    public void saveCategoryAuditReference(CategoryAuditReferenceSaveReq req) {
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(req.getTaskId());
        //判断任务状态
        BusinessAssert.isTrue(TaskStateEnum.SUBMITTED_TO_UBPM.getCode().equals(taskEntity.getTaskState()), ModuleTypeEnum.BASIC,"任务状态不正确,必须为已审核待核查");

        //塞入操作人
        req.getReferenceInfoList().forEach(v -> v.setOperator(getOperator()));
        //保存参考数据
        categoryAuditTaskReferenceService.saveCategoryAuditTaskReference(req.getTaskId(), req.getReferenceInfoList());
    }

    @Override
    public List<CategoryAuditReferenceResp> getCategoryAuditReference(CategoryAuditReferenceReq req) {
        List<CategoryAuditReferenceDto> referenceDtoList = categoryAuditTaskReferenceService.getCategoryAuditReferenceDto(req.getTaskId());
        return referenceDtoList.stream().map(referenceDto -> {
            CategoryAuditReferenceResp referenceResp = new CategoryAuditReferenceResp();
            referenceResp.setCategoryKey(referenceDto.getCategoryKey());
            referenceResp.setReferenceInfoList(BeanTools.copyBeans(referenceDto.getInfoList(), CategoryAuditReferenceResp.ReferenceInfo.class));
            return referenceResp;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SetUpCategoryShowTimeResp> getSetUpCategoryShowTime(SetUpCategoryShowTimeReq req) {
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(req.getTaskId());
        return categoryAuditTaskTimeService.getSetUpCategoryShowTime(req);
    }

    @Override
    public GetCategoryAuditVersionResp getCategoryAuditVersionList(CategoryAuditReferenceReq req) {
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(req.getTaskId());
        List<Integer> versionList = categoryAuditTaskVersionService.getVersionList(taskEntity.getId());
        GetCategoryAuditVersionResp resp = new GetCategoryAuditVersionResp();
        resp.setVersionList(versionList);
        resp.setTaskId(taskEntity.getId());
        return resp;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void asyncLevelDiffVersionInfoHandler(Long taskId) {
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(taskId);

        //获取最终审批版本
        Integer auditVersion = taskEntity.getAuditVersion();
        //获取当前版本
        CategoryTreeVersionDto categoryTreeVersion = categoryAuditTaskVersionService.getCategoryTreeVersion(taskEntity.getId(), auditVersion);
        //获取生产版本
        CategoryTreeVersionDto nowCategoryTreeVersion = categoryAuditTaskVersionService.getNowCategoryTreeVersion(taskId);

        Map<String, CategoryTreeVersionDto.CategoryNode> nodeMap = new HashMap<>();
        categoryAuditTaskVersionService.convertMapByNodeDto(categoryTreeVersion.getTree(), nodeMap);

        //对比信息变更
        Map<String, CategoryTreeVersionDto.CategoryNode> oldNodeMap = new HashMap<>();
        categoryAuditTaskVersionService.convertMapByNodeDto(nowCategoryTreeVersion.getTree(), oldNodeMap);

        //对比层级变更
        Map<String, CategoryTreeVersionDto.ChangeData> changeDataMap = new HashMap<>();
        CategoryTreeVersionDto.compareTree(nowCategoryTreeVersion.getTree(), categoryTreeVersion.getTree(), changeDataMap);
        if (CollectionUtils.isNotEmpty(changeDataMap.values())) {
            //需要补充oldparentKey
            changeDataMap.values().forEach(v -> {
                if (v.getCTags().contains(ChangeTagEnum.LEVEL_CHANGE)) {
                    CategoryTreeVersionDto.CategoryNode oldNode = oldNodeMap.get(v.getKey());
                    if (Objects.nonNull(oldNode)) {
                        v.setOldParentKey(oldNode.getPKey());
                    }
                }
            });
        }

        //对比信息
        nodeMap.forEach((k, v) -> {
            CategoryTreeVersionDto.CategoryNode oldNode = oldNodeMap.get(k);
            CategoryTreeVersionDto.ChangeData changeData = changeDataMap.getOrDefault(k, new CategoryTreeVersionDto.ChangeData(k));
            if (Objects.nonNull(oldNode)) {
                List<ChangeTagEnum> changeTagEnumList = oldNode.compareAndEditInfo(v.getName(), v.getCnName(), v.getIsShow(), v.getIsReturn(), v.getTags(), v.getParts());
                changeData.getCTags().addAll(changeTagEnumList);
                changeDataMap.put(k, changeData);
            }
        });

        //创建审批版本
        CategoryAuditTaskVersionEntity versionEntity = categoryAuditTaskVersionService.createVersion(taskEntity.getId(), taskEntity.getRequestName());

        //审批信息带入
        mergeReferInfo(changeDataMap, categoryTreeVersion.getChangeDataList());

        CategoryTreeVersionDto versionDto = new CategoryTreeVersionDto();
        versionDto.setChangeDataList(new ArrayList<>(changeDataMap.values()));
        versionDto.setTree(categoryTreeVersion.getTree());
        categoryAuditTaskVersionService.updateLevelEdit(versionDto, versionEntity);

        taskEntity.setAuditVersion(versionEntity.getVersion());
        categoryAuditTaskService.updateById(taskEntity);

        //记录日志
        categoryAuditTaskLogService.saveVersionLog(taskEntity.getId(), versionEntity.getVersion(), "审批版本生成");

        categoryAuditTaskLogService.saveTaskStateLog(taskEntity.getId(), TaskStateEnum.SUBMITTED_TO_UBPM, taskEntity.getRequestName());

    }

    @Override
    public void categoryTaskAudit(categoryTaskAuditReq req) {
        Optional<CategoryAuditTaskEntity> categoryAuditTaskEntity = categoryAuditTaskService.lambdaQuery()
                .eq(CategoryAuditTaskEntity::getId, req.getId())
                .eq(CategoryAuditTaskEntity::getTaskState, TaskStateEnum.SUBMITTED_TO_UBPM_APPROVED.getCode())
                .eq(CategoryAuditTaskEntity::getIsDel, 0)
                .oneOpt();
        BusinessAssert.isTrue(categoryAuditTaskEntity.isPresent(), ModuleTypeEnum.BASIC,"没有找到状态为已审核待核查的任务");
        CategoryAuditTaskEntity categoryAuditTask = categoryAuditTaskEntity.get();
        if (req.isShowImmediately()) {
            List<Long> categoryIds = req.getCategoryInfoList().stream().map(categoryTaskAuditReq.CategoryInfo::getCategoryId).collect(Collectors.toList());
            Date date = new Date();
//            BatchChangeCategoryControlStatueReq batchChangeCategoryControlStatueReq = new BatchChangeCategoryControlStatueReq();
//            batchChangeCategoryControlStatueReq.setCategoryIdList(categoryIds);
//            batchChangeCategoryControlStatueReq.setIsEnabled((byte) 1);
//            batchChangeCategoryControlStatueReq.setOperator(UserContextUtil.getUserName());
//            log.info("立即执行分类管控定时展示分类任务,更改展示状态req:{}", JsonTools.defaultMapper().toJson(batchChangeCategoryControlStatueReq));
//            pdsFrontCategoryClient.batchChangeStatus(batchChangeCategoryControlStatueReq);
            BatchChangeCategoryControlShowReq batchChangeCategoryControlShowReq = new BatchChangeCategoryControlShowReq();
            batchChangeCategoryControlShowReq.setCategoryIdList(categoryIds);
            batchChangeCategoryControlShowReq.setIsShow((byte) 1);
            batchChangeCategoryControlShowReq.setShowChangeMode(0);
            batchChangeCategoryControlShowReq.setOperator(UserContextUtil.getUserName());
            log.info("立即执行分类管控定时展示分类任务,更改展示状态req:{}", JsonTools.defaultMapper().toJson(batchChangeCategoryControlShowReq));
            Response response = pdsFrontCategoryClient.batchChangeShow(batchChangeCategoryControlShowReq);
            BusinessAssert.isTrue(response.isSuccessResponse(), ModuleTypeEnum.BASIC, "更新是否可展示失败");
            //开始创建分类展示任务
            req.getCategoryInfoList().forEach(x -> {
                List<CategoryAuditTaskTimeEntity> list = categoryAuditTaskTimeService.lambdaQuery().eq(CategoryAuditTaskTimeEntity::getCategoryAuditTaskId, req.getId())
                        .eq(CategoryAuditTaskTimeEntity::getCategoryId, x.getCategoryId()).list();
                CategoryAuditTaskTimeEntity categoryAuditTaskTimeEntity;
                if (CollectionUtils.isNotEmpty(list)) {
                    categoryAuditTaskTimeEntity = list.get(0);
                } else {
                    categoryAuditTaskTimeEntity = new CategoryAuditTaskTimeEntity();
                }

                categoryAuditTaskTimeEntity.setCategoryAuditTaskId(categoryAuditTask.getId());
                categoryAuditTaskTimeEntity.setCategoryId(x.getCategoryId());
                categoryAuditTaskTimeEntity.setCategoryKey(x.getCategoryKey());
                categoryAuditTaskTimeEntity.setTaskState(2);
                categoryAuditTaskTimeEntity.setEnableTime(date);
                categoryAuditTaskTimeEntity.setInsertTime(date);
                categoryAuditTaskTimeEntity.setLastUpdateTime(date);
                categoryAuditTaskTimeEntity.setIsDel(0);
                categoryAuditTaskTimeService.saveOrUpdate(categoryAuditTaskTimeEntity);
                //记录日志
                categoryAuditTaskLogService.saveTaskShowTimeLog(categoryAuditTask.getId(), Collections.singletonList(x.getCategoryKey()), date, getOperator());
                categoryAuditTaskLogService.saveTaskShowTimeSuccessLog(categoryAuditTask.getId(), Collections.singletonList(x.getCategoryKey()), getOperator());
                //调用pds保存成功 发到企微群
                CategoryAuditTaskWechatDto categoryAuditTaskWechatDto = new CategoryAuditTaskWechatDto();
                categoryAuditTaskWechatDto.setAuditTaskId(categoryAuditTask.getId());
                categoryAuditTaskWechatDto.setCategoryId(x.getCategoryId());
                categoryAuditTaskWechatDto.setTime(DateFormatTools.formatDate(DateFormatTools.PATTERN_DEFAULT_ON_SECOND, date));
                combineManager.combineModel(categoryAuditTaskWechatDto, false);
                log.warn("分类展示时间到了，分类备注名:{}，分类任务标签:{}，分类展示时间:{}",
                        categoryAuditTaskWechatDto.getCategoryName(),
                        categoryAuditTaskWechatDto.getCategoryTag(),
                        categoryAuditTaskWechatDto.getTime(),
                        LtLogFormat.builder().logTag("category.audit.detail.status.change").build());
            });
        } else {
            Date date = null;
            try {
                date = DateFormatTools.pareDate(DateFormatTools.PATTERN_DEFAULT_ON_SECOND, req.getTime());
            } catch (Exception e) {
                log.warn("时间转换异常,e:{}", ExceptionTools.getExceptionStackTrace(e));
            }
            //判断时间必须超过当前时间+10分钟
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MINUTE, delayTime);
            BusinessAssert.isTrue(date.after(calendar.getTime()), ModuleTypeEnum.BASIC,"分类展示时间需晚于当前时间10分钟后");
            //开始创建分类展示任务
            for (categoryTaskAuditReq.CategoryInfo x : req.getCategoryInfoList()) {
                List<CategoryAuditTaskTimeEntity> list = categoryAuditTaskTimeService.lambdaQuery().eq(CategoryAuditTaskTimeEntity::getCategoryAuditTaskId, req.getId())
                        .eq(CategoryAuditTaskTimeEntity::getCategoryId, x.getCategoryId()).list();
                CategoryAuditTaskTimeEntity categoryAuditTaskTimeEntity;
                if (CollectionUtils.isNotEmpty(list)) {
                    categoryAuditTaskTimeEntity = list.get(0);
                } else {
                    categoryAuditTaskTimeEntity = new CategoryAuditTaskTimeEntity();
                }
                categoryAuditTaskTimeEntity.setCategoryAuditTaskId(categoryAuditTask.getId());
                categoryAuditTaskTimeEntity.setCategoryId(x.getCategoryId());
                categoryAuditTaskTimeEntity.setCategoryKey(x.getCategoryKey());
                categoryAuditTaskTimeEntity.setTaskState(1);
                categoryAuditTaskTimeEntity.setEnableTime(date);
                categoryAuditTaskTimeEntity.setInsertTime(new Date());
                categoryAuditTaskTimeEntity.setLastUpdateTime(new Date());
                categoryAuditTaskTimeEntity.setIsDel(0);
                categoryAuditTaskTimeService.saveOrUpdate(categoryAuditTaskTimeEntity);
                //记录日志
                categoryAuditTaskLogService.saveTaskShowTimeLog(categoryAuditTask.getId(), Collections.singletonList(x.getCategoryKey()), date, getOperator());
                // 创建定时任务
                String userName = UserContextUtil.getUserName();
                TaskParams.TaskParamsBuilder builder = TaskParams.builder().addParam("TIME_TASK_ID", String.valueOf(categoryAuditTaskTimeEntity.getId()))
                        .addParam("OPERATOR", userName);
                taskScheduler.scheduleAt(CategoryAuditNotifyDelayTask.class, builder.build(), date);
                // 分类展示时间提前一天要通知
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.DATE, -1);
                Date executeTime = cal.getTime();
                Date newTime = new Date();
                log.info("设置分类定时展示时间提前一天,执行时间:{},当前时间:{}", executeTime, newTime);
                if (executeTime.after(newTime)) {

                    taskScheduler.scheduleAt(CategoryAuditWechatDelayTask.class, builder.build(), cal.getTime());
                }
            }
        }

    }

    private Long doUbmpSubmit(CategoryAuditTaskEntity taskEntity) {
        WorkflowDoCreateDTO workflowDoCreateDto = new WorkflowDoCreateDTO();
        List<UlpGetEnNameResp.UserNameInfo> engUserByName = ulpCommonService.findEngUserByName(Collections.singletonList(getOperator()));
        BusinessAssert.isNotEmpty(engUserByName, ModuleTypeEnum.BASIC,"用户信息未找到:" + getOperator());
        UlpGetEnNameResp.UserNameInfo userNameInfo = engUserByName.get(0);
        workflowDoCreateDto.setCreaterEmplid(userNameInfo.getEmplid());
        workflowDoCreateDto.setRequestName("Category Submission Task-" + taskEntity.getTopCategoryId() + "-" + taskEntity.getTaskSn());
        workflowDoCreateDto.setRequestLevel("正常");
        workflowDoCreateDto.setWorkflowId(applyToUbmpWorkFlowId);
        List<MainData> mainDataList = Lists.newArrayList();
        mainDataList.add(new MainData("process_number", taskEntity.getTopCategoryId()));
        mainDataList.add(new MainData("product_category", taskEntity.getTopCategoryId()));
        workflowDoCreateDto.setMainData(mainDataList);
        log.info("调用ubpm接口,参数:{}", JsonTools.defaultMapper().toJson(workflowDoCreateDto));
        Response<DoCreateResponse> doCreateResponseResponse = ubpmClientData.doCreate(workflowDoCreateDto);
        log.info("调用ubpm接口,返回:{}", JsonTools.defaultMapper().toJson(doCreateResponseResponse));
        BusinessAssert.isTrue(ResponseUtil.isRespInfoFine(doCreateResponseResponse), ModuleTypeEnum.BASIC,"调用ubpm接口失败");
        DoCreateResponse response = doCreateResponseResponse.getInfo();
        return response.getRequestId();
    }

    /**
     * 校验修改的版本信息
     * @param taskId
     * @param version
     * @return
     */
    private CategoryAuditTaskVersionEntity checkEditInfoVersion(Long taskId, Integer version) {
        //获取当前用户信息
        String userName = getOperator();
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(taskId);
        //校验任务状态
        categoryAuditTaskService.checkTaskToEditVersion(taskEntity);
        //校验版本进程状态
        CategoryAuditTaskVersionEntity versionEntity = categoryAuditTaskVersionService.getVersionByTaskIdAndVersion(taskEntity.getId(), version);
        //必须完成层级修改
        BusinessAssert.isTrue(!Objects.equals(CategoryAuditTaskStepEnum.INIT.getCode(), versionEntity.getStep()), ModuleTypeEnum.BASIC,"版本未处于信息修改的状态");
        //作者必须为当前用户 在非审批阶段
        if (!Objects.equals(TaskStateEnum.SUBMITTED_TO_UBPM.getCode(), taskEntity.getTaskState())) {
            BusinessAssert.isTrue(Objects.equals(versionEntity.getApplicantName(), userName), ModuleTypeEnum.BASIC,"当前用户不是作者");
        }
        return versionEntity;
    }


    /**
     * 校验分类ID是否为顶级
     * @param categoryId
     * @return
     */
    private boolean categoryIdIsTop(Long categoryId) {
        List<BatchQueryCategoryNameResp> categoryNameRespList = pdsProxy.getCategoryByIdList(Collections.singletonList(categoryId), 1);
        if (CollectionUtils.isEmpty(categoryNameRespList)) {
            return false;
        }
        BatchQueryCategoryNameResp categoryNameResp = categoryNameRespList.get(0);
        return Objects.equals(categoryNameResp.getParentCategoryId(), 0L);
    }


    private String getOperator() {
        String userName = UserContextUtil.getUserName();
        BusinessAssert.isTextNotBlank(userName, ModuleTypeEnum.BASIC,"当前用户信息为空");
        return userName;
    }

    /**
     * 校验分类名称是否重复 和pdb查重逻辑一致
     * @param node
     * @param nodeMap
     */
    private void checkCategoryNameRepeat(CategoryTreeVersionDto.CategoryNode node, Map<String, CategoryTreeVersionDto.CategoryNode> nodeMap) {
       if (Objects.isNull(node)) {
           return ;
       }
       List<CategoryTreeVersionDto.CategoryNode> nodeList = new ArrayList<>();
       //查找父级分类
        CategoryTreeVersionDto.CategoryNode parentNode = nodeMap.get(node.getPKey());
       //兄弟节点
        if (CollectionUtils.isNotEmpty(parentNode.getSub())) {
            long count = parentNode.getSub().stream().map(CategoryTreeVersionDto.CategoryNode::getKey).distinct().count();
            BusinessAssert.isTrue(count == parentNode.getSub().size(), ModuleTypeEnum.BASIC,"同一层级下分类名称重复");
            nodeList.addAll(parentNode.getSub().stream().filter(n -> !Objects.equals(n.getKey(), node.getKey())).collect(Collectors.toList()));
        }
        Stack<String> searchNodeKey = new Stack<>();
        searchNodeKey.add(node.getKey());
        //查找父节点和子节点
        while (searchNodeKey.empty()) {
            CategoryTreeVersionDto.CategoryNode categoryNode = nodeMap.get(searchNodeKey.pop());
            if (Objects.nonNull(categoryNode)) {
                //添加自己
                nodeList.add(categoryNode);
                //收集子节点
                if (CollectionUtils.isNotEmpty(categoryNode.getSub())) {
                    searchNodeKey.addAll(categoryNode.getSub().stream().map(CategoryTreeVersionDto.CategoryNode::getKey).collect(Collectors.toList()));
                }
                //收集父节点
                if (StringUtils.isNotBlank(categoryNode.getPKey())) {
                    searchNodeKey.add(categoryNode.getPKey());
                }
            }
        }
        //校验名称是否重复
        if (CollectionUtils.isNotEmpty(nodeList)) {
            nodeList.forEach(n -> {
                if (Objects.equals(n.getName(), node.getName())) {
                    throw new BusinessException(ErrorCodeDefine.MessageCode.VALIDERROR, String.format("分类名称存在重复,名称:%s,唯一标识:%s", node.getName(), node.getKey()));
                }
            });
        }
    }

    private void preCheckCategory(List<CategoryTreeVersionDto.ChangeData> changeDataList,Map<String, CategoryTreeVersionDto.CategoryNode> newNodeMap, String operator, boolean skipLevelCheck) {
        if (CollectionUtils.isEmpty(changeDataList) || preCheckSwitch) {
            return ;
        }

        List<SensitiveWordVer3Req.TextInfo> textInfoList = Lists.newArrayList();
        changeDataList.forEach(changeData -> {
            if (changeData.getCTags().contains(ChangeTagEnum.INFO_CHANGE) || changeData.getCTags().contains(ChangeTagEnum.ADD)) {
                CategoryTreeVersionDto.CategoryNode node = newNodeMap.get(changeData.getKey());
                if (Objects.nonNull(node)) {
                    SensitiveWordVer3Req.TextInfo textInfo = new SensitiveWordVer3Req.TextInfo();
                    textInfo.setLang(LanguageEnum.EN.getLanguageFlag());
                    textInfo.setText(node.getName());
                    textInfoList.add(textInfo);
                    if (StringUtils.isNotBlank(node.getCnName())) {
                        SensitiveWordVer3Req.TextInfo textInfoCn = new SensitiveWordVer3Req.TextInfo();
                        textInfoCn.setLang(LanguageEnum.ZH_CN.getLanguageFlag());
                        textInfoCn.setText(node.getCnName());
                        textInfoList.add(textInfoCn);
                    }
                }
            }
        });
        //敏感词校验
        abcSensitiveVer3Proxy.checkSensitiveVersionThirdVersion(textInfoList, MccSensitiveSceneEnum.PRODUCT_CATEGORY_SENSTIVE);

        //预校验
        BatchSaveCategoryControlReq preCheckReq = new BatchSaveCategoryControlReq();
        preCheckReq.setSystemId(1);
        preCheckReq.setOperator(operator);
        preCheckReq.setSkipLevelCheck(skipLevelCheck);
        preCheckReq.setCategoryDetailList(changeDataList.stream().map(v -> {
            CategoryTreeVersionDto.CategoryNode node = newNodeMap.get(v.getKey());
            BatchSaveCategoryControlDetailRep detail = new BatchSaveCategoryControlDetailRep();
            detail.setCategoryId(node.getId());
            detail.setCategoryName(node.getName());
            detail.setCategoryCnName(node.getCnName());
            detail.setParentKey(node.getPKey());
            if (!CategoryTreeVersionDto.isNewKey(node.getPKey())) {
                detail.setParentCategoryId(Long.valueOf(node.getPKey()));
            }
            if (Objects.nonNull(node.getCnName())) {
                detail.setCategoryCnName(node.getCnName());
            } else {
                detail.setCategoryCnName(node.getName());
            }
            detail.setIsEnabled((byte) 1);
            detail.setReturnType(Objects.equals(node.getIsReturn(), 0) || Objects.isNull(node.getIsReturn()) ? null : node.getIsReturn().byteValue());
            detail.setShowInNav(Objects.equals(node.getIsShow(), 1) ? (byte) 1 : (byte) 0);
            detail.setIsShow((byte) 1);
            detail.setPartIdList(node.getParts());
            detail.setBindType(Objects.equals(node.getIsLeaf(), 1) ? (byte) 1 : (byte) 0);
            detail.setTagIds(node.getTags());
            detail.setKey(node.getKey());
            return detail;
        }).collect(Collectors.toList()));
        UploadResp uploadResp = pdsProxy.batchSaveCategoryControlPreCheck(preCheckReq);
        if (CollectionUtils.isNotEmpty(uploadResp.getFailureResults())) {
            StringBuilder err = new StringBuilder();
            err.append("分类预校验不通过:");
            uploadResp.getFailureResults().forEach(f -> {
                CategoryTreeVersionDto.CategoryNode categoryNode = newNodeMap.get(f.getIdentity());
                String name = Objects.nonNull(categoryNode) ? categoryNode.getName() : f.getIdentity();
                err.append(String.format("分类标识:%s, 错误原因:%s", name, f.getMsg()));
            });
            throw new BusinessException(ErrorCodeDefine.MessageCode.VALIDERROR, err.toString());
        }
    }
}
