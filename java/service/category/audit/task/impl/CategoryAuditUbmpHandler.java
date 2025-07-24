package cn.dotfashion.soa.module.service.category.audit.task.impl;

import cn.dotfashion.soa.framework.util.ExceptionTools;
import cn.dotfashion.soa.framework.util.JsonTools;
import cn.dotfashion.soa.module.enums.FileImportType;
import cn.dotfashion.soa.module.service.AsyncBatchTaskService;
import cn.dotfashion.soa.module.service.category.audit.task.CategoryAuditTaskBusinessService;
import cn.dotfashion.soa.module.ubpm.handler.AbstractUbpmWorkflowCallBackHandler;
import cn.dotfashion.soa.module.util.HttpRequestTools;
import cn.dotfashion.soa.ubpmSdk.entity.UbpmWorkflowData;
import cn.dotfashion.soa.ubpmSdk.entity.enums.WorkflowStatusEnums;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.dotfashion.soa.module.enums.UbpmCallBackHandlerCodeEnum.CATEGORY_AUDIT_TASK;

/**
 * 接收回执
 */
@Service
@Slf4j
public class CategoryAuditUbmpHandler extends AbstractUbpmWorkflowCallBackHandler {

    @Lazy
    @Resource
    private AsyncBatchTaskService asyncBatchTaskService;

    public static final String ARCHIVE = "归档";

    @Override
    public void afterHandle(UbpmWorkflowData object) {
        log.info("[ubpm回调]Category Submission Task流程归档start:{}", JsonTools.defaultMapper().toJson(object));
        if (Objects.equals(WorkflowStatusEnums.IsRemark.ARCHIVE.getCode(), object.getIsRemark()) && ARCHIVE.equals(object.getNodeName())) {
            Long requestId = object.getRequestId();
            try {
                asyncBatchTaskService.asyncBatchTask(requestId, FileImportType.ASYNC_AUDIT_TASK_AFTER_TASK, null, null, null);
            } catch (Exception e) {
                log.warn("[ubpm回调]Category Submission Task流程归档时发生异常，requestId:{},异常信息:{}", requestId, ExceptionTools.getExceptionStackTrace(e));
                throw e;
            }
        }
        log.info("[ubpm回调]Category Submission Task流程归档end:{}", JsonTools.defaultMapper().toJson(object));
    }

    @Override
    public String getHandlerCode() {
        return CATEGORY_AUDIT_TASK.name();
    }
}
