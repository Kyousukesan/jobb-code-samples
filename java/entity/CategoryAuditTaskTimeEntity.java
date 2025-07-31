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
 * カテゴリ提出タスク有効化タスク
 * </p>
 *
 * @author zhoujiwei
 * @since 2023-07-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("module_category_audit_task_time")
@ApiModel(value="CategoryAuditTaskTimeEntity对象", description="カテゴリ提出タスク有効化タスク")
public class CategoryAuditTaskTimeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主キー")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "タスクメインテーブル")
    private Long categoryAuditTaskId;

    @ApiModelProperty(value = "カテゴリユニーク識別子 親ID-備考名")
    private String categoryKey;

    @ApiModelProperty(value = "有効化されたカテゴリID")
    private Long categoryId;

    @ApiModelProperty(value = "0未有効化1実行待ち2実行完了")
    private Integer taskState;

    @ApiModelProperty(value = "有効化時間")
    private Date enableTime;

    @ApiModelProperty(value = "レコード挿入時間")
    private Date insertTime;

    @ApiModelProperty(value = "レコード更新時間")
    private Date lastUpdateTime;

    @ApiModelProperty(value = "削除フラグ(0:未削除,1:削除済み)")
    private Integer isDel;


}
