
# 汎用データ挿入サービス

## 🧩 背景

プロジェクト内でクライアントからのデータ報告やログのテーブル挿入の要件が多いため、複雑なロジックなしで迅速に開発可能な**汎用インターフェースのデータ挿入サービス**を開発しました。

---

## ✅ 適用シーン

- データ報告インターフェース
- ログ収集インターフェース
- 迅速に構築するinsertインターフェース

---

## ✨ 特長

- ✅ [jmesPath.php](https://jmespath.org/)を用いたパラメータ抽出に対応し、式のキャッシュ（メモリキャッシュ）をサポート
- ✅ `StorageArrangeInterface`インターフェースを実装することでパラメータを設定可能
- ✅ データベース接続およびテーブル名を指定可能
- ✅ ユニークインデックスに基づく更新上書きロジックに対応（`Builder::upsert()`を利用）

---

## 🗂️ コード構造説明

| クラス名                          | 説明                               |
|----------------------------------|----------------------------------|
| `SimpleStorageBootstrap::class`  | モジュール起動クラス。設定とJMESPath環境を初期化      |
| `SimpleStorageController::class` | コントローラークラス。`saveData`メソッドをルートから利用可能 |
| `SimpleStorageService::class`    | コアサービスクラス。保存処理のロジックを担当        |
| `StorageArrangeInterface::class` | 設定インターフェース。すべての設定クラスはこれを実装 |
| `AttributeArrange::class`        | フィールドマッピング設定クラス                |

---

## 🛠️ 使用方法

### 1. 設定クラス例

```php
class AppThirdReportLogStorage implements StorageArrangeInterface
{
    public function attributes(): array
    {
        return AttributeArrange::makeByArray([
            'user_id' => [
                'expression' => 'user_id',
                'tableAlias' => '',
                'afterHandler' => null
            ],
            'event_name' => [
                'expression' => 'event',
                'tableAlias' => '',
                'afterHandler' => null
            ],
            'request_data' => [
                'expression' => '@',
                'tableAlias' => '',
                'afterHandler' => function ($value) {
                    return json_encode($value);
                }
            ],
        ]);
    }

    public function getConnection(): string
    {
        return 'log';
    }

    public function getTable(): string
    {
        return 'app_third_report_log_' . date('w');
    }

    public function getArrayExpression(): string
    {
        return '@';
    }

    public function getUniqueFields(): array
    {
        return [];
    }

    public function useCache(): bool
    {
        return false;
    }
}
```

---

### 2. サービス呼び出し例

```php
$this->storageService->saveData(AppThirdReportLogStorage::class, $request->all());
```

---

### 3. コントローラールート例

```php
Route::post('/push_test', [SimpleStorageController::class, 'saveData'])
    ->setParams([
        SimpleStorageController::STORAGE_ARRANGE_CLASS => AppThirdReportLogStorage::class
    ]);
```

---

### 4. 設定登録

```php
return [
    'storage_arrange' => [
        AppThirdReportLogStorage::class,
    ],
];
```

---

## 🔁 データ変換例

### ⬅️ 元の入力データ

```json
[
    {
        "event": "click_tab_home",
        "time": 1746756179646,
        "properties": {
            "aa": "11",
            "bb": "22"
        },
        "os": "1",
        "user_id": 2493,
        "package_id": "1",
        "package_type": "2",
        "version": "3.6.2"
    }
]
```

### ➡️ 変換後データ

```json
{
    "user_id": 2493,
    "event_name": "click_tab_home",
    "request_data": "{"event":"click_tab_home","time":1746756179646,"properties":{"aa":"11","bb":"22"},"os":"1","user_id":2493,"package_id":"1","package_type":"2","version":"3.6.2"}"
}
```
