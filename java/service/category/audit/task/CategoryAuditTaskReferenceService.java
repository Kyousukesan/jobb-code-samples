package cn.dotfashion.soa.pim.service.category.audit.task;

import cn.dotfashion.soa.pim.dto.category.audit.task.CategoryAuditReferenceDto;
import cn.dotfashion.soa.pim.entity.CategoryAuditTaskReferenceEntity;
import cn.dotfashion.soa.pim.vo.categorytask.request.CategoryAuditReferenceSaveReq;
import cn.dotfashion.soa.pim.vo.categorytask.request.SubmitCategoryInfoDataReq;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 分类提报任务版本数据表 服务类
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
