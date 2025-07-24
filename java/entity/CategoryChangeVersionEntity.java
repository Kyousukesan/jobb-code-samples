package cn.dotfashion.soa.module.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 分类版本外发表
 * </p>
 *
 * @author lizhipeng
 * @since 2023-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("module_category_change_version")
@ApiModel(value="CategoryChangeVersionEntity对象", description="分类版本外发表")
public class CategoryChangeVersionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "分类id")
    private Long categoryId;

    @ApiModelProperty(value = "分类信息")
    private String content;

    @ApiModelProperty(value = "版本号")
    private Integer version;

    @ApiModelProperty(value = "插入时间")
    private Date insertTime;

    @ApiModelProperty(value = "最后更新时间")
    private Date lastUpdateTime;

    @ApiModelProperty(value = "是否删除 1-是，0-否")
    private Boolean isDel;


}
