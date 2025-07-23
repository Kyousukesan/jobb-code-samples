package cn.dotfashion.soa.pim.dto.category.audit.task;

import cn.dotfashion.soa.pim.enums.category.audit.ChangeTagEnum;
import cn.dotfashion.soa.pim.vo.categorytask.request.SubmitCategoryInfoDataReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * 分类提报任务版本数据表
 */
@Data
public class CategoryTreeVersionDto {

    @ApiModelProperty(value = "任务ID")
    private Long taskId;

    @ApiModelProperty(value = "申请人")
    private String applicantName;

    @ApiModelProperty(value = "版本号")
    private Integer version;

    @ApiModelProperty(value = "top分类节点")
    private CategoryNode tree;

    @ApiModelProperty(value = "变更数据")
    private List<ChangeData> changeDataList;

    @ApiModelProperty(value = "版本时间")
    private Date insertTime;

    @ApiModelProperty(value = "版本id")
    private Long versionId;


    /**
     * 节点数据结构
     */
    @Data
    public static class CategoryNode {
        //分类key pkey+name
        private String key;
        //分类id 为null 为新增分类
        private Long id;
        //父级key
        private String pKey;
        //备注名
        private String name;
        //中文名
        private String cnName;
        //层级
        private Integer level;
        //是否展示
        private Integer isShow = 0;
        //是否新建
        private Integer isNew;
        //是否末级分类
        private Integer isLeaf;
        //是否是否可退换 默认null 1允许2不允许
        private Integer isReturn;
        //分类标签
        private List<Long> tags = new ArrayList<>();
        //部件信息
        private List<Long> parts = new ArrayList<>();
        //子分类
        private List<CategoryNode> sub = new ArrayList<>();

        public void genKey() {
            if (Objects.isNull(this.id) || this.id == 0) {
                this.key = this.pKey + "-" + this.name;
                this.isNew = 1;
            } else {
                this.key = this.id.toString();
                this.isNew = 0;
            }
        }

        /**
         * 比较数据并且覆盖
         * @param name
         * @param cnName
         * @param isShow
         * @param tagList
         * @param partList
         */
        public List<ChangeTagEnum> compareAndEditInfo(String name, String cnName,Integer isShow, Integer isReturn, List<Long> tagList, List<Long> partList) {
            List<ChangeTagEnum> changeTagEnumList = new ArrayList<>();
//            //是否有isShow变更
//            if (!Objects.equals(isShow, getIsShow())) {
//                changeTagEnumList.add(ChangeTagEnum.ENABLE_CHANGE);
//            }
            //判断名称等是否有变更
            if (!Objects.equals(name, getName()) ||
                    !Objects.equals(cnName, getCnName()) ||
                    compareLists(tagList, getTags()) ||
                    compareLists(partList, getParts()) ||
                    !Objects.equals(isReturn, getIsReturn()))
            {
                setTags(Objects.isNull(tagList) ? new ArrayList<>() : tagList);
                setParts(Objects.isNull(partList) ? new ArrayList<>() : partList);
                if (Objects.equals(getIsLeaf(), 1) && !Objects.equals(getIsReturn(), 0)) {
                    setIsReturn(isReturn);
                }
                if (CategoryTreeVersionDto.isNewKey(getKey())) {
                    setName(name);
                    setCnName(cnName);
                }
                //新增不加信息修改
                if (!CategoryTreeVersionDto.isNewKey(getKey())) {
                    changeTagEnumList.add(ChangeTagEnum.INFO_CHANGE);
                }
            }
            return changeTagEnumList;
        }
    }

    @Data
    public static class ChangeData {

        private String key;

        private String parentKey = "";

        private String oldParentKey = "";

        private Set<ChangeTagEnum> cTags = new HashSet<>();

        @ApiModelProperty(value = "分类定义说明图例")
        private List<String> dImages = new ArrayList<>();

        @ApiModelProperty(value = "发起原因")
        private String reason = "";

        @ApiModelProperty(value = "分类定义说明")
        private String definition = "";

        @ApiModelProperty(value = "审核参考信息")
        private Info referenceInfo;

        public ChangeData() {}

        public ChangeData(String key) {
            this.key = key;
        }

        @Data
        public static class Info {
            @ApiModelProperty(value = "是否可退换")
            private Integer isReturn;

            @ApiModelProperty(value = "开放商家业务模式")
            private List<String> openSupplierBusiness;

            @ApiModelProperty(value = "开放市场")
            private List<String> openMarket;
        }
    }

    public static boolean isNewKey(String key) {
        return !key.chars().allMatch(Character::isDigit);
    }

    /**
     * 比较两棵树的差异
     * @param oldNode
     * @param newNode
     * @param changeDataMap
     */
    public static void compareTree(CategoryNode oldNode, CategoryNode newNode, Map<String, ChangeData> changeDataMap) {
        if (oldNode == null && newNode == null) {
            return;
        }
        //新增
        if (Objects.isNull(oldNode) && Objects.equals(1, newNode.getIsNew())) {
            ChangeData changeData = changeDataMap.getOrDefault(newNode.getKey(), new ChangeData(newNode.getKey()));
            changeData.getCTags().add(ChangeTagEnum.ADD);
            changeDataMap.put(newNode.getKey(), changeData);
        }
        //节点变化
        if (Objects.isNull(oldNode) && Objects.equals(0, newNode.getIsNew())) {
            ChangeData changeData = changeDataMap.getOrDefault(newNode.getKey(), new ChangeData(newNode.getKey()));
            changeData.getCTags().add(ChangeTagEnum.LEVEL_CHANGE);
            changeData.setParentKey(newNode.getPKey());
            changeDataMap.put(newNode.getKey(), changeData);
        }
        //没有删除场景所以不考虑

        //递归子节点
        if (CollectionUtils.isNotEmpty(newNode.getSub())) {
            newNode.getSub().forEach(newChild -> {
                CategoryNode oldChild = null;
                if (Objects.nonNull(oldNode) && CollectionUtils.isNotEmpty(oldNode.getSub())) {
                    oldChild = oldNode.getSub().stream().filter(child -> Objects.equals(child.getKey(), newChild.getKey())).findFirst().orElse(null);
                }
                compareTree(oldChild, newChild, changeDataMap);
            });
        }
    }

    public static boolean compareLists(List<Long> list1, List<Long> list2) {
        if (list1.size() != list2.size()) {
            return true;
        }
        return list1.stream().anyMatch(id -> !list2.contains(id));
    }
}
