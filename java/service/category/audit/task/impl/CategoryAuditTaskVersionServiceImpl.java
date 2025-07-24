package cn.dotfashion.soa.module.service.category.audit.task.impl;

import cn.dotfashion.soa.framework.util.JsonTools;
import cn.dotfashion.soa.pdb.vo.category.resp.FrontQueryCategoryTreeRespVo;
import cn.dotfashion.soa.pdb.vo.resp.FindSeriesDetailResp;
import cn.dotfashion.soa.module.client.PdsCategoryBackClient;
import cn.dotfashion.soa.module.client.vo.resp.BatchSaveCategoryControlDetailResp;
import cn.dotfashion.soa.module.dto.category.audit.task.CategoryDiffChangeDto;
import cn.dotfashion.soa.module.dto.category.audit.task.CategoryTreeVersionDto;
import cn.dotfashion.soa.module.entity.CategoryAuditTaskEntity;
import cn.dotfashion.soa.module.entity.CategoryAuditTaskVersionEntity;
import cn.dotfashion.soa.module.enums.LanguageEnum;
import cn.dotfashion.soa.module.enums.category.audit.CategoryAuditTaskStepEnum;
import cn.dotfashion.soa.module.enums.category.audit.ChangeTagEnum;
import cn.dotfashion.soa.module.mapper.CategoryAuditTaskVersionMapper;
import cn.dotfashion.soa.module.proxy.PdsProxy;
import cn.dotfashion.soa.module.proxy.TranslateProxy;
import cn.dotfashion.soa.module.service.category.audit.task.CategoryAuditTaskService;
import cn.dotfashion.soa.module.service.category.audit.task.CategoryAuditTaskVersionService;
import cn.dotfashion.soa.module.util.ListSplitTools;
import cn.dotfashion.soa.module.vo.categorytask.request.CategoryLevelTaskSubReq;
import cn.dotfashion.soa.module.vo.categorytask.request.SubmitCategoryInfoDataReq;
import cn.dotfashion.soa.module.vo.categorytask.response.CategoryLevelTaskGetResp;
import cn.dotfashion.soa.module.vo.request.TextTranslateBatchReq;
import cn.dotfashion.soa.module.vo.response.TextTranslateBatchResp;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.shein.common.enums.ModuleTypeEnum;
import com.shein.common.exception.BusinessAssert;
import io.swagger.models.auth.In;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.dotfashion.soa.module.constant.LanguageConst.ZH_CN;

/**
 * <p>
 * Category Submission Task版本数据表 服务实现类
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
@Service
public class CategoryAuditTaskVersionServiceImpl extends ServiceImpl<CategoryAuditTaskVersionMapper, CategoryAuditTaskVersionEntity> implements CategoryAuditTaskVersionService {

    @Resource
    private CategoryAuditTaskService categoryAuditTaskService;

    @Resource
    private PdsProxy pdsProxy;

    @Resource
    private TranslateProxy translateProxy;

    @Override
    public CategoryAuditTaskVersionEntity getByTaskId(Long taskId) {
        return lambdaQuery().eq(CategoryAuditTaskVersionEntity::getCategoryAuditTaskId, taskId).eq(CategoryAuditTaskVersionEntity::getVersion, latestVersion(taskId))
                .eq(CategoryAuditTaskVersionEntity::getIsDel, false).one();
    }

    @Override
    public Integer latestVersion(Long taskId) {
        CategoryAuditTaskVersionEntity one = lambdaQuery().eq(CategoryAuditTaskVersionEntity::getCategoryAuditTaskId, taskId)
                .eq(CategoryAuditTaskVersionEntity::getIsDel, false).orderByDesc(CategoryAuditTaskVersionEntity::getVersion).last("limit 1")
                .select(CategoryAuditTaskVersionEntity::getVersion).one();
        if (one == null) {
            return 0;
        }
        return one.getVersion();
    }

    @Override
    public List<Integer> getVersionList(Long taskId) {
         return lambdaQuery().eq(CategoryAuditTaskVersionEntity::getCategoryAuditTaskId, taskId)
                 .eq(CategoryAuditTaskVersionEntity::getIsDel, false).orderByDesc(CategoryAuditTaskVersionEntity::getVersion).last("limit 1")
                .select(CategoryAuditTaskVersionEntity::getVersion).list()
                 .stream().map(CategoryAuditTaskVersionEntity::getVersion).collect(Collectors.toList());
    }

    public CategoryAuditTaskVersionEntity getByTaskId(Long taskId, Integer version) {
        CategoryAuditTaskVersionEntity versionEntity = lambdaQuery().eq(CategoryAuditTaskVersionEntity::getCategoryAuditTaskId, taskId).eq(CategoryAuditTaskVersionEntity::getVersion, version)
                .eq(CategoryAuditTaskVersionEntity::getIsDel, false).one();
        BusinessAssert.isNotNull(versionEntity, ModuleTypeEnum.BASIC, "版本不存在");
        return versionEntity;
    }


    @Override
    public CategoryTreeVersionDto getCategoryTreeVersion(Long taskId, Integer version) {
        if (version == 0) {
            return getNowCategoryTreeVersion(taskId);
        }
        CategoryAuditTaskVersionEntity versionEntity = getByTaskId(taskId, version);
        CategoryTreeVersionDto versionDto = new CategoryTreeVersionDto();
        versionDto.setTaskId(taskId);
        versionDto.setVersion(version);
        versionDto.setVersionId(versionEntity.getId());
        versionDto.setApplicantName(versionEntity.getApplicantName());
        versionDto.setTree(convertTreeDtoByEntity(versionEntity));
        versionDto.setChangeDataList(convertChangeDataByEntity(versionEntity));
        versionDto.setInsertTime(versionEntity.getInsertTime());
        return versionDto;
    }

    public List<CategoryTreeVersionDto.ChangeData> convertChangeDataByEntity(CategoryAuditTaskVersionEntity versionEntity) {
        return JsonTools.defaultMapper().fromJson(versionEntity.getChangeData(), new TypeReference<List<CategoryTreeVersionDto.ChangeData>>(){});
    }

    public CategoryTreeVersionDto.CategoryNode convertTreeDtoByEntity(CategoryAuditTaskVersionEntity versionEntity) {
        return JsonTools.defaultMapper().fromJson(versionEntity.getVersionData(), CategoryTreeVersionDto.CategoryNode.class);
    }

    @Override
    public void verifyVersion(Long taskId, Integer version) {
        lambdaUpdate().eq(CategoryAuditTaskVersionEntity::getCategoryAuditTaskId, taskId).eq(CategoryAuditTaskVersionEntity::getVersion, version)
                .set(CategoryAuditTaskVersionEntity::getStep, CategoryAuditTaskStepEnum.INFO_EDIT.getCode())
                .eq(CategoryAuditTaskVersionEntity::getIsDel, false).update();
    }

    @Override
    public Map<Long, List<ChangeTagEnum>> getChangeTagMapByTaskId(List<Long> taskIdList) {
        Map<Long, List<ChangeTagEnum>> changeTagEnumMap= new HashMap<>();
        ListSplitTools.toFixBatchSize(taskIdList, 5, (idList) -> {
            Map<Long, List<CategoryAuditTaskVersionEntity>> map = lambdaQuery().in(CategoryAuditTaskVersionEntity::getCategoryAuditTaskId, idList)
                    .eq(CategoryAuditTaskVersionEntity::getIsDel, false)
                    .select(CategoryAuditTaskVersionEntity::getChangeData, CategoryAuditTaskVersionEntity::getCategoryAuditTaskId)
                    .list().stream().collect(Collectors.groupingBy(CategoryAuditTaskVersionEntity::getCategoryAuditTaskId));
            map.forEach((taskId, versionList) -> {
                Set<ChangeTagEnum> tagSet = new HashSet<>();
                versionList.forEach(v -> {
                    convertChangeDataByEntity(v).forEach(changeData -> {
                        tagSet.addAll(changeData.getCTags());
                    });
                });
                changeTagEnumMap.put(taskId, new ArrayList<>(tagSet));
            });
            return ListSplitTools.ListSplitEnum.CONTINUE;
        });
        return changeTagEnumMap;
    }

    @Override
    public List<CategoryDiffChangeDto> handleDiffChangeDto(CategoryAuditTaskEntity categoryAuditTask) {
        if (categoryAuditTask.getAuditVersion() == 0) {
            return new ArrayList<>();
        }
        //获取审批版本
        CategoryTreeVersionDto categoryTreeVersion = getCategoryTreeVersion(categoryAuditTask.getId(), categoryAuditTask.getAuditVersion());

        List<CategoryTreeVersionDto.ChangeData> changeDataList = categoryTreeVersion.getChangeDataList();
        Map<String, CategoryTreeVersionDto.CategoryNode> nodeMap = new HashMap<>();
        convertMapByNodeDto(categoryTreeVersion.getTree(), nodeMap);

        return changeDataList.stream().filter(v -> CollectionUtils.isNotEmpty(v.getCTags())).map(changeData -> {
            CategoryTreeVersionDto.CategoryNode categoryNode = nodeMap.get(changeData.getKey());

            CategoryDiffChangeDto changeDto = new CategoryDiffChangeDto();
            changeDto.setCategoryId(categoryNode.getId());
            changeDto.setCategoryName(categoryNode.getName());
            changeDto.setCategoryCnName(categoryNode.getCnName());
            changeDto.setKey(changeData.getKey());
            changeDto.setTagIdList(categoryNode.getTags());
            changeDto.setPartIdList(categoryNode.getParts());
            changeDto.setParentKey(categoryNode.getPKey());
            changeDto.setIsShow(Objects.nonNull(categoryNode.getIsShow()) ? categoryNode.getIsShow(): 0);
            changeDto.setIsLeaf(categoryNode.getIsLeaf());
            changeDto.setIsReturn(categoryNode.getIsReturn());
            if (StringUtils.isBlank(changeDto.getCategoryCnName())) {
                changeDto.setCategoryCnName(changeDto.getCategoryName());
            }
            if (!CategoryTreeVersionDto.isNewKey(categoryNode.getPKey())) {
                changeDto.setParentId(Long.valueOf(categoryNode.getPKey()));
            }
            changeDto.setCTags(changeData.getCTags());
            changeDto.setEnableTime(categoryAuditTask.getEnableTime());
            changeDto.setLevel(categoryNode.getLevel());
            return changeDto;
        }).collect(Collectors.toList());
    }

    @Override
    public void convertMapByNodeDto(CategoryTreeVersionDto.CategoryNode node, Map<String, CategoryTreeVersionDto.CategoryNode> map) {
        map.put(node.getKey(), node);
        if (node.getSub() != null) {
            node.getSub().forEach(child -> {
                convertMapByNodeDto(child, map);
            });
        }
    }

    @Override
    public void deleteById(Long versionId) {
        lambdaUpdate().eq(CategoryAuditTaskVersionEntity::getId, versionId).set(CategoryAuditTaskVersionEntity::getIsDel, true).update();
    }

    @Override
    public void updateNewCategoryId(CategoryAuditTaskEntity taskEntity, List<BatchSaveCategoryControlDetailResp> detailRespList) {
        //获取审批版本
        CategoryTreeVersionDto categoryTreeVersion = getCategoryTreeVersion(taskEntity.getId(), taskEntity.getAuditVersion());

        Map<String, CategoryTreeVersionDto.CategoryNode> nodeMap = new HashMap<>();
        convertMapByNodeDto(categoryTreeVersion.getTree(), nodeMap);

        detailRespList.forEach(v -> {
            CategoryTreeVersionDto.CategoryNode categoryNode = nodeMap.get(v.getKey());
            categoryNode.setId(v.getCategoryId());
        });

        //更新
        updateLevelEdit(categoryTreeVersion, getByTaskId(taskEntity.getId(), taskEntity.getAuditVersion()));
    }

    @Override
    public void fillRemarkNameToCnName(List<CategoryTreeVersionDto.ChangeData> changeDataList, Map<String, CategoryTreeVersionDto.CategoryNode> newNodeMap) {
        List<CategoryTreeVersionDto.CategoryNode> changeNode = new ArrayList<>();
        List<String> nameList = new ArrayList<>();
        changeDataList.stream().filter(v -> v.getCTags().contains(ChangeTagEnum.ADD)).forEach(v -> {
            CategoryTreeVersionDto.CategoryNode categoryNode = newNodeMap.get(v.getKey());
            nameList.add(categoryNode.getName());
            changeNode.add(categoryNode);
        });

        TextTranslateBatchReq req = new TextTranslateBatchReq();
        req.setTargetLanguage(ZH_CN);
        req.setTexts(nameList);
        List<TextTranslateBatchResp> batchRespList = translateProxy.textTranslateBatch(req);
        batchRespList.forEach(v -> {
            List<CategoryTreeVersionDto.CategoryNode> categoryNodeList = changeNode.stream().filter(o -> Objects.equals(v.getSourceText(), o.getName())).collect(Collectors.toList());
            categoryNodeList.forEach(node -> {
                node.setCnName(v.getTranslatedText());
            });
        });
    }

    @Override
    public CategoryTreeVersionDto getNowCategoryTreeVersion(Long taskId) {
        CategoryAuditTaskEntity taskEntity = categoryAuditTaskService.getById(taskId);
        List<FrontQueryCategoryTreeRespVo> frontCategoryTree = pdsProxy.getFrontCategoryTree(taskEntity.getTopCategoryId(), Collections.singletonList(ZH_CN));
        BusinessAssert.isNotEmpty(frontCategoryTree, ModuleTypeEnum.BASIC, "分类树不存在");
        CategoryTreeVersionDto versionDto = new CategoryTreeVersionDto();
        versionDto.setTaskId(taskId);
        versionDto.setVersion(0);
        versionDto.setApplicantName("");
        versionDto.setChangeDataList(new ArrayList<>());
        versionDto.setTree(convertByNowCategoryTree(frontCategoryTree.get(0)));
        return versionDto;
    }

    @Override
    public CategoryLevelTaskGetResp.CategoryNodeInfo convertToCategoryNodeInfo(CategoryTreeVersionDto.CategoryNode categoryNode, List<Object> combineList) {
        CategoryLevelTaskGetResp.CategoryNodeInfo newNode = new CategoryLevelTaskGetResp.CategoryNodeInfo();
        newNode.setKey(categoryNode.getKey());
        newNode.setCategoryId(categoryNode.getId());
        newNode.setLevel(categoryNode.getLevel());
        newNode.setTagList(categoryNode.getTags().stream().map(tagId -> {
            CategoryLevelTaskGetResp.CategoryNodeInfo.Tag tagInfo = new CategoryLevelTaskGetResp.CategoryNodeInfo.Tag();
            tagInfo.setTagId(tagId);
            return tagInfo;
        }).collect(Collectors.toList()));
        combineList.addAll(newNode.getTagList());
        newNode.setCategoryPartRelationList(categoryNode.getParts().stream().map(partId -> {
            CategoryLevelTaskGetResp.CategoryNodeInfo.PartRelation partRelation = new CategoryLevelTaskGetResp.CategoryNodeInfo.PartRelation();
            partRelation.setPartId(partId);
            return partRelation;
        }).collect(Collectors.toList()));
        combineList.addAll(newNode.getCategoryPartRelationList());
        newNode.setCategoryCnName(categoryNode.getCnName());
        newNode.setCategoryName(categoryNode.getName());
        newNode.setIsLeaf(categoryNode.getIsLeaf());
        newNode.setIsReturn(categoryNode.getIsReturn());
        List<CategoryLevelTaskGetResp.CategoryNodeInfo> newChildren = new ArrayList<>();
        List<CategoryTreeVersionDto.CategoryNode> children = categoryNode.getSub();
        if (children != null) { // 如果该节点有子节点，则递归转换其子节点
            for (CategoryTreeVersionDto.CategoryNode childNode : children) {
                newChildren.add(convertToCategoryNodeInfo(childNode, combineList));
            }
        }
        newNode.setSub(newChildren);
        return newNode;
    }

    @Override
    public CategoryAuditTaskVersionEntity createVersion(Long taskId, String applicantName) {
        CategoryAuditTaskVersionEntity versionEntity = new CategoryAuditTaskVersionEntity();
        versionEntity.setCategoryAuditTaskId(taskId);
        versionEntity.setVersion(latestVersion(taskId) + 1);
        versionEntity.setStep(CategoryAuditTaskStepEnum.LEVEL_EDIT.getCode());
        versionEntity.setVersionData("{}");
        versionEntity.setChangeData("[]");
        versionEntity.setApplicantName(applicantName);
        save(versionEntity);
        return versionEntity;
    }

    @Override
    public CategoryAuditTaskVersionEntity getById(Long id) {
        CategoryAuditTaskVersionEntity one = lambdaQuery().eq(CategoryAuditTaskVersionEntity::getId, id)
                .eq(CategoryAuditTaskVersionEntity::getIsDel, false).one();
        BusinessAssert.isNotNull(one, ModuleTypeEnum.BASIC, "版本不存在");
        return one;
    }

    @Override
    public CategoryTreeVersionDto.CategoryNode convertToCategoryNodeReq(CategoryLevelTaskSubReq.CategoryNodeInfo categoryNodeInfo, String Pkey, Map<String, CategoryTreeVersionDto.CategoryNode> oldNodeMap) {
        CategoryTreeVersionDto.CategoryNode newNode = new CategoryTreeVersionDto.CategoryNode();
        newNode.setId(categoryNodeInfo.getCategoryId());
        newNode.setPKey(Pkey);
        newNode.setName(categoryNodeInfo.getCategoryName());
        newNode.genKey();
        newNode.setIsLeaf(categoryNodeInfo.getIsLeaf());
        //末级默认不允许
        if (Objects.equals(categoryNodeInfo.getIsLeaf(), 1)) {
            newNode.setIsReturn(2);
        }
        CategoryTreeVersionDto.CategoryNode categoryNode = oldNodeMap.get(newNode.getKey());
        if (categoryNode != null) {
            newNode.setTags(categoryNode.getTags());
            newNode.setParts(categoryNode.getParts());
            newNode.setIsShow(categoryNode.getIsShow());
            newNode.setCnName(categoryNode.getCnName());
            newNode.setName(categoryNode.getName());
            newNode.genKey();
            newNode.setIsReturn(categoryNode.getIsReturn());
        }
        newNode.setLevel(categoryNodeInfo.getLevel());
        newNode.setPKey(Pkey);


        if (newNode.getTags() == null) {
            newNode.setTags(new ArrayList<>());
        }
        if (newNode.getParts() == null) {
            newNode.setParts(new ArrayList<>());
        }

        if (categoryNodeInfo.getSub() != null) {
            categoryNodeInfo.getSub().forEach(sub -> newNode.getSub().add(convertToCategoryNodeReq(sub, newNode.getKey(), oldNodeMap)));
        }
        return newNode;
    }

    @Override
    public CategoryAuditTaskVersionEntity hasProcessingVersion(Long taskId) {
        return lambdaQuery().eq(CategoryAuditTaskVersionEntity::getCategoryAuditTaskId, taskId)
                .notIn(CategoryAuditTaskVersionEntity::getStep, Collections.singletonList(CategoryAuditTaskStepEnum.INFO_EDIT.getCode()))
                .eq(CategoryAuditTaskVersionEntity::getIsDel, false).one();
    }

    @Override
    public void updateLevelEdit(CategoryTreeVersionDto dto, CategoryAuditTaskVersionEntity entity) {
        entity.setStep(CategoryAuditTaskStepEnum.LEVEL_EDIT.getCode());
        entity.setChangeData(JsonTools.defaultMapper().toJson(dto.getChangeDataList()));
        entity.setVersionData(JsonTools.defaultMapper().toJson(dto.getTree()));
        updateById(entity);
    }

    @Override
    public CategoryAuditTaskVersionEntity getVersionByTaskIdAndVersion(Long taskId, Integer version) {
        CategoryAuditTaskVersionEntity versionEntity = lambdaQuery().eq(CategoryAuditTaskVersionEntity::getCategoryAuditTaskId, taskId).eq(CategoryAuditTaskVersionEntity::getVersion, version)
                .eq(CategoryAuditTaskVersionEntity::getIsDel, false).one();
        BusinessAssert.isNotNull(versionEntity, ModuleTypeEnum.BASIC, "版本不存在");
        return versionEntity;
    }

    private CategoryTreeVersionDto.CategoryNode convertByNowCategoryTree(FrontQueryCategoryTreeRespVo nowNode) {
        //转换节点
        CategoryTreeVersionDto.CategoryNode newNode = new CategoryTreeVersionDto.CategoryNode();
        newNode.setKey(nowNode.getCategoryId().toString());
        newNode.setId(nowNode.getCategoryId());
        newNode.setPKey(nowNode.getParentId().toString());
        newNode.setIsShow(Objects.equals((byte) 1 , nowNode.getIsShow()) ? 1 : 0);
        newNode.setName(nowNode.getCategoryName());
        newNode.setIsReturn(Integer.valueOf(nowNode.getReturnType()));
        newNode.setLevel(nowNode.getCategoryLevel());
        newNode.setIsLeaf(Objects.equals(nowNode.getBindType(), (byte) 1) ? 1 : 0);
        newNode.setParts(ListUtils.emptyIfNull(nowNode.getPartInfoList()).stream().map(FrontQueryCategoryTreeRespVo.PartInfoVO::getPartId).collect(Collectors.toList()));
        newNode.setCnName(nowNode.getCategoryNameMulti().stream().filter(v -> Objects.equals(v.getLanguageFlag(), LanguageEnum.ZH_CN.getLanguageFlag())).findFirst().orElse(new FrontQueryCategoryTreeRespVo.NameMultiRespVo()).getCategoryName());
        newNode.setTags(ListUtils.emptyIfNull(nowNode.getTags()).stream().map(FrontQueryCategoryTreeRespVo.TagRespVo::getTagId).collect(Collectors.toList()));
        List<CategoryTreeVersionDto.CategoryNode> newChildren = new ArrayList<>();
        List<FrontQueryCategoryTreeRespVo> children = nowNode.getSubCategory();
        if (children != null) { // 如果该节点有子节点，则递归转换其子节点
            for (FrontQueryCategoryTreeRespVo childNode : children) {
                newChildren.add(convertByNowCategoryTree(childNode));
            }
        }
        newNode.setSub(newChildren);
        return newNode;
    }
}
