package cn.company.soa.module.dto.category.audit.task;


import cn.company.soa.module.enums.category.audit.ChangeTagEnum;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class CategoryDiffChangeDto {

    //分类树唯一标识
    private String key;

    //分类id
    private Long categoryId;

    //父级id
    private String parentKey;

    private Long parentId;

    //分类名称
    private String categoryName;

    //分类中文名
    private String categoryCnName;

    //关联标签
    private List<Long> tagIdList;

    //关联部件
    private List<Long> partIdList;

    //是否展示
    private Integer isShow;

    //是否为末级
    private Integer isLeaf;

    private Integer isEnabled;

    //是否可替换
    private Integer isReturn;

    private Set<ChangeTagEnum> cTags = new HashSet<>();

    private Date enableTime;

    private Integer level;
}
