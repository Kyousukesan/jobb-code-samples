# 分類情報の承認・変更工数のワークフロー機能

## 概要

このプロジェクトは、複雑な分類データの承認・変更ワークフロー機能を実装したJavaアプリケーションです。多段階の承認フロー、ツリー構造の分類データのバージョン管理、差分比較、定時実行バッチ処理、社内システム連携、操作履歴記録などの複雑な要素を含むシステムを構築しました。

## 主要機能

### 1. 多段階承認フロー
- **タスク状態管理**: 受信済み提出待ち → 提出済み審査待ち → 審査済み更新成功/却下
- **承認者管理**: 提出者、審査者、申請者の役割分離
- **バージョン管理**: 複数バージョンの分類データを管理

### 2. ツリー構造の分類データ管理
- **階層構造**: 親子関係を持つ分類ツリーの管理
- **ノード操作**: 追加・削除・移動・変更の正確な把握
- **差分比較**: 視覚的に分かりやすい比較表示機能

### 3. 定時実行バッチ処理
- **分類展示制御**: 指定時間に分類の表示/非表示を自動制御
- **通知機能**: 企業微信（WeChat）への自動通知
- **状態管理**: タスク実行状態の追跡

### 4. 社内システム連携
- **OAシステム連携**: 外部OAシステムとのタスク同期
- **チャット連携**: 企業微信への通知送信
- **分散ロック**: 並行処理制御

### 5. 操作履歴・変更履歴記録
- **詳細ログ**: 全ての操作の詳細記録
- **変更追跡**: 分類データの変更履歴管理
- **監査証跡**: 承認プロセスの完全な追跡

## 技術アーキテクチャ

### データベース設計

#### 主要エンティティ

1. **CategoryAuditTaskEntity** - メインタスクテーブル
   - タスクの基本情報（ID、状態、申請者、審査者など）
   - 承認フローの状態管理
   - 時間情報（申請時間、審査時間、有効化時間）

2. **CategoryAuditTaskVersionEntity** - バージョンデータテーブル
   - 分類データのバージョン管理
   - 変更データとバージョンデータの保存
   - ステップ管理（初期0、階層調整1、情報調整確認2）

3. **CategoryChangeVersionEntity** - 分類変更バージョンテーブル
   - 分類情報の変更履歴
   - バージョン番号管理

4. **CategoryAuditTaskTimeEntity** - 時間制御テーブル
   - 分類の表示/非表示時間制御
   - 定時実行タスクの管理

5. **CategoryAuditTaskReferenceEntity** - 参照情報テーブル
   - 審査時の参考情報管理

### サービス層設計

#### ビジネスサービス
- **CategoryAuditTaskBusinessService**: メイン業務ロジック
- **CategoryAuditTaskService**: 基本CRUD操作
- **CategoryAuditTaskVersionService**: バージョン管理
- **CategoryAuditTaskTimeService**: 時間制御
- **CategoryAuditTaskReferenceService**: 参照情報管理
- **CategoryAuditTaskLogService**: ログ管理

#### 特殊処理
- **CategoryAuditUbmpHandler**: UBMPシステム連携処理

### コントローラー層

**CategoryTaskController** が主要なAPIエンドポイントを提供：

- `categoryReviewTaskApply()` - 分類審査タスク申請
- `getCategoryLevelVersionInfo()` - 分類レベルバージョン情報取得
- `submitCategoryLevelVersionInfo()` - 分類レベルバージョン情報提出
- `submitChangeCategoryInfo()` - 分類情報変更提出
- `verifyVersion()` - バージョン検証
- `getTaskList()` - タスク一覧取得
- `applyToUbmp()` - UBMPシステムへの申請
- `categoryTaskReview()` - 分類タスク審査
- `taskAfterHandle()` - タスク後処理
- `taskFinalCheck()` - タスク最終確認

### バッチ処理

#### CategoryAuditNotifyDelayTask
- 分類の定時展示制御
- 企業微信への通知送信
- 実行状態の管理

#### CategoryAuditWechatDelayTask
- 企業微信通知の遅延処理
- 通知失敗時の再試行

### データ転送オブジェクト（DTO）

#### CategoryTreeVersionDto
- 分類ツリーのバージョン情報
- 階層構造の管理
- 変更データの追跡

#### CategoryDiffChangeDto
- 分類変更の差分情報
- 変更タグの管理
- 親子関係の追跡

#### CategoryAuditReferenceDto
- 審査時の参考情報

## 主要な技術的課題と解決策

### 1. ツリー構造の差分比較

**課題**: 分類データがツリー構造であり、ノードの追加・削除・移動を正確に把握する必要があった。

**解決策**:
- `CategoryTreeVersionDto`で階層構造を管理
- `compareTree()`メソッドで再帰的な差分比較を実装
- 変更タグ（`ChangeTagEnum`）で変更タイプを分類
- 親子関係の変更を正確に追跡

### 2. 並行処理制御

**課題**: 複数ユーザーが同時に分類データを変更する可能性がある。

**解決策**:
- 分散ロック（`DistributedLockUtil`）を使用
- 非同期バッチ処理（`AsyncBatchTaskService`）で重い処理を分離
- 楽観的ロックでバージョン競合を防止

### 3. 複雑な承認フロー

**課題**: 多段階の承認プロセスと状態管理が複雑。

**解決策**:
- 状態マシンによる明確な状態遷移
- 各段階でのバリデーション
- ロールベースの権限管理
- 詳細な操作ログ

### 4. 外部システム連携

**課題**: OAシステムや企業微信との連携が必要。

**解決策**:
- クライアント層での外部API呼び出し
- 非同期処理による応答時間の改善
- エラーハンドリングとリトライ機能

## 開発期間と成果

- **技術スタック**: Java, Spring Boot, MyBatis-Plus, Redis, MySQL
- **主要成果**:
  - 複雑なツリー構造の差分比較機能
  - 多段階承認フローの完全実装
  - 定時実行バッチ処理の安定動作
  - 外部システムとの連携機能
  - 完全な操作履歴と監査証跡

## ファイル構成

```
java/
├── controller/
│   └── CategoryTaskController.java          # メインコントローラー
├── entity/
│   ├── CategoryAuditTaskEntity.java         # メインタスクエンティティ
│   ├── CategoryAuditTaskVersionEntity.java  # バージョンエンティティ
│   ├── CategoryChangeVersionEntity.java     # 変更バージョンエンティティ
│   ├── CategoryAuditTaskTimeEntity.java     # 時間制御エンティティ
│   └── CategoryAuditTaskReferenceEntity.java # 参照情報エンティティ
├── dto/
│   └── category/audit/task/
│       ├── CategoryTreeVersionDto.java      # ツリーバージョンDTO
│       ├── CategoryDiffChangeDto.java       # 差分変更DTO
│       └── CategoryAuditReferenceDto.java   # 審査参照DTO
├── service/
│   └── category/audit/task/
│       ├── CategoryAuditTaskBusinessService.java    # ビジネスサービス
│       ├── CategoryAuditTaskService.java            # 基本サービス
│       ├── CategoryAuditTaskVersionService.java     # バージョンサービス
│       ├── CategoryAuditTaskTimeService.java        # 時間サービス
│       ├── CategoryAuditTaskReferenceService.java   # 参照サービス
│       ├── CategoryAuditTaskLogService.java         # ログサービス
│       └── impl/                                    # 実装クラス
└── task/
    ├── CategoryAuditNotifyDelayTask.java    # 通知遅延タスク
    └── CategoryAuditWechatDelayTask.java    # 微信遅延タスク
```