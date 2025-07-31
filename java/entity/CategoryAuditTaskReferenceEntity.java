package cn.company.soa.module.entity;

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
 * カテゴリ提出タスクバージョンデータテーブル
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("module_category_audit_task_reference")
@ApiModel(value="CategoryAuditTaskReferenceEntity对象", description="カテゴリ提出タスクバージョンデータテーブル")
public class CategoryAuditTaskReferenceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主キー")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "タスクメインテーブル")
    private Long categoryAuditTaskId;

    @ApiModelProperty(value = "バージョン番号")
    private Integer version;

    @ApiModelProperty(value = "関連審査参考データ")
    private String referenceData;

    @ApiModelProperty(value = "レコード挿入時間")
    private Date insertTime;

    @ApiModelProperty(value = "レコード更新時間")
    private Date lastUpdateTime;

    @ApiModelProperty(value = "削除フラグ(0:未削除,1:削除済み)")
    private Integer isDel;


}
