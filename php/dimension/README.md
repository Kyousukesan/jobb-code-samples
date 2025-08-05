# PHP ビジネスルールエンジン

## 概要

このモジュールは、柔軟な条件マッチングシステムを提供するPHPディメンション管理システムです。ユーザーの属性や条件に基づいて、動的なルールマッチングを実行できます。

## 機能

- **ディメンション設定管理**: ディメンションの作成、更新、削除
- **条件設定**: 複数の条件グループと論理演算子をサポート
- **動的マッチング**: リアルタイムでの条件マッチング
- **キャッシュ機能**: パフォーマンス向上のためのキャッシュシステム
- **柔軟なデータ型**: 文字列、整数、ブール値の条件値をサポート

## ディレクトリ構造

```
dimension/
├── db/
│   └── table.sql              # データベーステーブル定義
└── service/
    ├── config/                # 設定関連クラス
    │   ├── ConditionValueValid.php
    │   ├── MatchConditionDataDto.php
    │   ├── MatchDataDto.php
    │   └── MatchResultDto.php
    ├── matcher/               # マッチングエンジン
    │   ├── Condition.php
    │   ├── MatcherFactory.php
    │   └── operator/          # 演算子実装
    │       ├── CompareMatcher.php
    │       ├── EqualMatcher.php
    │       └── MatcherInterface.php
    ├── DimensionConfigBusinessService.php
    ├── DimensionConfigConditionService.php
    ├── DimensionConfigService.php
    └── DimensionMatchBusinessService.php
```

## データベース設計

### テーブル構造

#### dimension_config
- ディメンションの基本情報を管理
- 名前、タイプ、有効/無効状態を格納

#### dimension_config_condition
- ディメンションの条件設定を管理
- 条件グループ、論理演算子、条件名を格納

#### dimension_config_condition_value
- 条件の具体的な値を管理
- 複数選択の条件値を格納

## 使用方法

### 1. ディメンション設定の作成

```php
use app\admin\service\dimension\DimensionConfigBusinessService;

$service = new DimensionConfigBusinessService();

// ディメンション設定を作成
$dimensionId = $service->saveInfo($reqVo);
```

### 2. 条件マッチングの実行

```php
use app\admin\service\dimension\DimensionMatchBusinessService;

$matchService = new DimensionMatchBusinessService();

// 条件データを準備
$conditionData = [
    'user_age' => 25,
    'user_location' => 'Tokyo',
    'user_vip' => true
];

// マッチング実行
$result = $matchService->simpleMatch($dimensionId, $conditionData);
```

### 3. 条件の設定

```php
// 条件値の検証
$validator = new ConditionValueValid('user_age', [25, 30]);
$validator->validate('age', $value, $fail);
```

## サポートされている演算子

- **EQ**: 等しい
- **NEQ**: 等しくない
- **GTE**: 以上
- **LTE**: 以下

## サポートされているデータ型

- **STRING**: 文字列
- **INTEGER**: 整数
- **BOOL**: ブール値

## 設定例

### ユーザー年齢条件の設定

```sql
-- 年齢が25歳以上のユーザーを対象とする条件
INSERT INTO dimension_config_condition (
    dimension_config_id,
    condition_group,
    condition_logic,
    condition_name,
    condition_value
) VALUES (
    1,           -- ディメンションID
    1,           -- 条件グループ
    'GTE',       -- 論理演算子（以上）
    'user_age',  -- 条件名
    '25'         -- 条件値
);
```

### 複数条件の組み合わせ

```php
// 条件グループ1: 年齢25歳以上 AND 東京在住
// 条件グループ2: VIPユーザー
// いずれかのグループにマッチすれば対象
```

## パフォーマンス最適化

- **キャッシュ機能**: マッチングデータをキャッシュして高速化
- **インデックス**: データベースに適切なインデックスを設定
- **バッチ処理**: 大量データの一括処理に対応

## エラーハンドリング

システムは以下のエラーを適切に処理します：

- 無効なディメンションID
- 存在しない条件設定
- データ型の不一致
- 重複する条件名

## 開発者向け情報

### 新しい演算子の追加

1. `MatcherInterface`を実装した新しいクラスを作成
2. `MatcherFactory`に新しい演算子を登録
3. テストケースを追加

### 新しいデータ型の追加

1. `ConditionValueTypeEnum`に新しい型を追加
2. 対応するバリデーターを実装
3. マッチャークラスで新しい型をサポート
