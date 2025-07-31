package cn.company.soa.module.service.category.audit.task;

import cn.company.soa.module.dto.category.audit.task.CategoryAuditReferenceDto;
import cn.company.soa.module.entity.CategoryAuditTaskReferenceEntity;
import cn.company.soa.module.vo.categorytask.request.CategoryAuditReferenceSaveReq;
import cn.company.soa.module.vo.categorytask.request.SubmitCategoryInfoDataReq;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * Category Submission Task版本数据表 服务类
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
public interface CategoryAuditTaskReferenceService extends IService<CategoryAuditTaskReferenceEntity> {

    /**
     * 保存分类提报审批备注
     * @param taskId
     * @param infoList
     */
    void saveCategoryAuditTaskReference(Long taskId, List<CategoryAuditReferenceSaveReq.ReferenceInfo> infoList);

    /**
     * 查询分类提报审批备注
     * @param taskId
     * @return
     */
    List<CategoryAuditReferenceDto> getCategoryAuditReferenceDto(Long taskId);
}
