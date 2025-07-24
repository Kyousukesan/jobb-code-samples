package cn.dotfashion.soa.module.service.category.audit.task.impl;

import cn.dotfashion.soa.framework.util.JsonTools;
import cn.dotfashion.soa.module.dto.category.audit.task.CategoryAuditReferenceDto;
import cn.dotfashion.soa.module.entity.CategoryAuditTaskReferenceEntity;
import cn.dotfashion.soa.module.mapper.CategoryAuditTaskReferenceMapper;
import cn.dotfashion.soa.module.service.category.audit.task.CategoryAuditTaskReferenceService;
import cn.dotfashion.soa.module.vo.categorytask.request.CategoryAuditReferenceSaveReq;
import cn.dotfashion.soa.module.vo.categorytask.request.SubmitCategoryInfoDataReq;
import cn.dotfashion.soa.module.vo.request.document.SizeInfoModule;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Category Submission Task版本数据表 服务实现类
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
@Service
public class CategoryAuditTaskReferenceServiceImpl extends ServiceImpl<CategoryAuditTaskReferenceMapper, CategoryAuditTaskReferenceEntity> implements CategoryAuditTaskReferenceService {

    @Override
    public void saveCategoryAuditTaskReference(Long taskId, List<CategoryAuditReferenceSaveReq.ReferenceInfo> infoList) {
        //转换infoList
        List<CategoryAuditReferenceDto> newInfoList = infoList.stream().map(v -> {
            CategoryAuditReferenceDto referenceDto = new CategoryAuditReferenceDto();
            referenceDto.setCategoryKey(v.getCategoryKey());
            v.getReferenceList().forEach(referenceString -> {
                CategoryAuditReferenceDto.ReferenceInfo referenceInfo = new CategoryAuditReferenceDto.ReferenceInfo();
                referenceInfo.setReference(referenceString);
                referenceInfo.setOperator(v.getOperator());
                referenceDto.getInfoList().add(referenceInfo);
            });
            return referenceDto;
        }).collect(Collectors.toList());

        List<CategoryAuditReferenceDto> categoryAuditReferenceDto = getCategoryAuditReferenceDto(taskId);
        if (CollectionUtils.isEmpty(categoryAuditReferenceDto)) {
            CategoryAuditTaskReferenceEntity entity = new CategoryAuditTaskReferenceEntity();
            entity.setCategoryAuditTaskId(taskId);
            entity.setReferenceData(JsonTools.defaultMapper().toJson(newInfoList));
            save(entity);
        } else {
            newInfoList.forEach(newInfo -> {
                Optional<CategoryAuditReferenceDto> opt = categoryAuditReferenceDto.stream().filter(v -> Objects.equals(v.getCategoryKey(), newInfo.getCategoryKey())).findFirst();
                if (opt.isPresent()) {
                    CategoryAuditReferenceDto referenceDto = opt.get();
                    referenceDto.getInfoList().addAll(newInfo.getInfoList());
                } else {
                    categoryAuditReferenceDto.add(newInfo);
                }
            });
            lambdaUpdate().eq(CategoryAuditTaskReferenceEntity::getCategoryAuditTaskId, taskId)
                    .eq(CategoryAuditTaskReferenceEntity::getIsDel, false)
                    .set(CategoryAuditTaskReferenceEntity::getReferenceData, JsonTools.defaultMapper().toJson(categoryAuditReferenceDto)).update();
        }
    }

    @Override
    public List<CategoryAuditReferenceDto> getCategoryAuditReferenceDto(Long taskId) {
        CategoryAuditTaskReferenceEntity one = lambdaQuery().eq(CategoryAuditTaskReferenceEntity::getCategoryAuditTaskId, taskId)
                .eq(CategoryAuditTaskReferenceEntity::getIsDel, false).one();
        if (one == null) {
            return Collections.emptyList();
        }
        return JsonTools.defaultMapper().fromJson(one.getReferenceData(), new TypeReference<List<CategoryAuditReferenceDto>>() {});
    }
}
