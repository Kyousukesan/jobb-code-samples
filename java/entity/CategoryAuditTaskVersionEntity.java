package cn.company.soa.module.entity;

import cn.company.soa.module.dto.category.audit.task.CategoryTreeVersionDto;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.util.ArrayList;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;

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
@TableName("module_category_audit_task_version")
@ApiModel(value="CategoryAuditTaskVersionEntity对象", description="カテゴリ提出タスクバージョンデータテーブル")
public class CategoryAuditTaskVersionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主キー")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "タスクメインテーブル")
    private Long categoryAuditTaskId;

    @ApiModelProperty(value = "申請者")
    private String applicantName;

    @ApiModelProperty(value = "バージョン番号")
    private Integer version;

    @ApiModelProperty(value = "変更データ")
    private String changeData;

    @ApiModelProperty(value = "バージョンデータ")
    private String versionData;

    @ApiModelProperty(value = "レコード挿入時間")
    private Date insertTime;

    @ApiModelProperty(value = "レコード更新時間")
    private Date lastUpdateTime;

    @ApiModelProperty(value = "削除フラグ(0:未削除,1:削除済み)")
    private Integer isDel;

    @ApiModelProperty(value = "現在のステップ 初期0 階層調整1 情報調整確認2")
    private Integer step;

}
