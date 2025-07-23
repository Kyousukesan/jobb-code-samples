package cn.dotfashion.soa.pim.service.category.audit.task.impl;

import cn.dotfashion.soa.api.constant.ErrorCodeDefine;
import cn.dotfashion.soa.framework.exception.BusinessException;
import cn.dotfashion.soa.framework.util.DateFormatTools;
import cn.dotfashion.soa.pim.constant.DocumentConstant;
import cn.dotfashion.soa.pim.dto.SeqGenerateHolder;
import cn.dotfashion.soa.pim.entity.CategoryAuditTaskEntity;
import cn.dotfashion.soa.pim.enums.category.audit.TaskStateEnum;
import cn.dotfashion.soa.pim.mapper.CategoryAuditTaskMapper;
import cn.dotfashion.soa.pim.service.SeqAssistantService;
import cn.dotfashion.soa.pim.service.category.audit.task.CategoryAuditTaskBusinessService;
import cn.dotfashion.soa.pim.service.category.audit.task.CategoryAuditTaskService;
import cn.dotfashion.soa.pim.util.UserContextUtil;
import cn.dotfashion.soa.pim.vo.categorytask.response.TopCategoryInfoResp;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shein.common.combine.CombineManager;
import com.shein.common.exception.BusinessAssert;
import com.shein.common.util.DateUtil;
import com.shein.common.util.ResponseUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 分类提报任务主表 服务实现类
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
@Service
public class CategoryAuditTaskServiceImpl extends ServiceImpl<CategoryAuditTaskMapper, CategoryAuditTaskEntity> implements CategoryAuditTaskService {


    private static final String CATEGORY_AUDIT_TASK_PREFIX = "CATL";
    @Resource
    private CombineManager combineManager;

    @Resource
    private SeqAssistantService seqAssistantService;

    @Lazy
    @Resource
    private CategoryAuditTaskBusinessService categoryAuditTaskBusinessService;

    @Override
    public CategoryAuditTaskEntity createAppCategoryAuditTask(String applicantName, Long topCategoryId, String sourceSystem) {
        CategoryAuditTaskEntity entity = new CategoryAuditTaskEntity();
        entity.setTaskSn(generateSn(topCategoryId));
        entity.setApplicantName(applicantName);
        entity.setTopCategoryId(topCategoryId);
        entity.setSourceSystem(sourceSystem);
        entity.setTaskState(TaskStateEnum.MAINTAINED_NOT_SUBMIT.getCode());
        entity.setApplicantTime(new Date());
        entity.setAuditTime(DocumentConstant.ZERO_DATE);
        entity.setEnableTime(DocumentConstant.ZERO_DATE);
        entity.setRequestTime(DocumentConstant.ZERO_DATE);
        save(entity);
        return entity;
    }

    @Override
    public CategoryAuditTaskEntity checkTopCategoryLock(Long topCategoryId) {
        CategoryAuditTaskEntity auditTask = lambdaQuery().eq(CategoryAuditTaskEntity::getTopCategoryId, topCategoryId)
                .notIn(CategoryAuditTaskEntity::getTaskState, TaskStateEnum.finalStateList).last("limit 1").one();
        return auditTask;
    }

    @Override
    public CategoryAuditTaskEntity getTopCategoryNotLock(Long topCategoryId) {
        CategoryAuditTaskEntity auditTask = lambdaQuery().eq(CategoryAuditTaskEntity::getTopCategoryId, topCategoryId)
                .notIn(CategoryAuditTaskEntity::getTaskState, TaskStateEnum.finalStateList).eq(CategoryAuditTaskEntity::getIsDel, false).last("limit 1").one();
        if (Objects.nonNull(auditTask) && !TaskStateEnum.applyStateList.contains(auditTask.getTaskState())) {
            throw new BusinessException(ErrorCodeDefine.MessageCode.VALIDERROR, "该分类已经处于锁定中,状态为" + TaskStateEnum.convertDescription(auditTask.getTaskState()) + ",流程结束才能再次发起");
        }
        return auditTask;
    }

    @Override
    public CategoryAuditTaskEntity getById(Long id) {
        CategoryAuditTaskEntity auditTask = lambdaQuery().eq(CategoryAuditTaskEntity::getId, id).eq(CategoryAuditTaskEntity::getIsDel, false).one();
        BusinessAssert.isNotNull(auditTask, ErrorCodeDefine.MessageCode.VALIDERROR, "task id is not exist");
        return auditTask;
    }

    @Override
    public TopCategoryInfoResp getTopCategoryInfo(Long topCategoryId) {
        TopCategoryInfoResp topCategoryInfoResp = new TopCategoryInfoResp();
        topCategoryInfoResp.setTopCategoryId(topCategoryId);
        combineManager.combineModel(topCategoryInfoResp, false);
        return topCategoryInfoResp;
    }

    @Override
    public List<TopCategoryInfoResp> getTopCategoryInfoList(List<Long> topCategoryId) {
        List<TopCategoryInfoResp> respList = topCategoryId.stream().map(v -> {
            TopCategoryInfoResp topCategoryInfoResp = new TopCategoryInfoResp();
            topCategoryInfoResp.setTopCategoryId(v);
            return topCategoryInfoResp;
        }).collect(Collectors.toList());
        combineManager.combineModelBatch(respList, false);
        return respList;
    }

    @Override
    public void updateTaskStatus(Long taskId, TaskStateEnum taskStateEnum) {
        CategoryAuditTaskEntity taskEntity = getById(taskId);
        taskEntity.setTaskState(taskStateEnum.getCode());
        updateById(taskEntity);
    }

    @Override
    public void checkTaskToEditVersion(CategoryAuditTaskEntity taskEntity) {
        //不在可编辑状态
        if (!TaskStateEnum.applyStateList.contains(taskEntity.getTaskState())) {
            //判断处于审批状态并且有审批版本并且操作角色是ubmp发起角色
            if (Objects.equals(TaskStateEnum.SUBMITTED_TO_UBPM.getCode(), taskEntity.getTaskState()) &&
                    Objects.nonNull(taskEntity.getAuditVersion())) {
                if (!categoryAuditTaskBusinessService.checkEmployeeId()) {
                    throw new BusinessException(ErrorCodeDefine.MessageCode.VALIDERROR, "没有权限修改");
                }
                return ;
            }
            BusinessAssert.isTrue(TaskStateEnum.applyStateList.contains(taskEntity.getTaskState()), ErrorCodeDefine.MessageCode.VALIDERROR, "提报任务处于非提交状态,不允许提交信息");
        }
    }

    private String generateSn(Long topCategoryId) {
        SeqGenerateHolder generateHolder = seqAssistantService.batchGenerateBusinessNo("CATEGORY_AUDIT_TASK_" + DateFormatTools.formatDate(DateFormatTools.PATTERN_ISO_ON_DATE, new Date()), "PIM_CATEGORY_APPLY_AUDIT_TASK", 1L);
        return CATEGORY_AUDIT_TASK_PREFIX + topCategoryId + DateFormatTools.formatDate("yyMMdd", new Date()) +
                StringUtils.leftPad(generateHolder.genNextStr(), 6, "0");
    }
}
