# Drama-Go

Drama-Goは、Go言語で開発されたWebアプリケーションフレームワークです。書籍管理システムとユーザー認証機能を提供し、Redis Streamsを使用したリアルタイムデータ処理を実装しています。

## プロジェクト構造

```
drama-go/
├── cmd/                    # アプリケーションのエントリーポイント
│   ├── gen_db/            # データベース生成ツール
│   │   └── main.go
│   └── main.go            # メインアプリケーション
├── config/                # 設定ファイル
│   ├── app.yaml           # アプリケーション設定
│   ├── config.go          # 設定管理
│   ├── queues.json        # キュー設定
│   └── types.go           # 設定タイプ定義
├── internal/              # 内部パッケージ
│   ├── dao/               # データアクセスオブジェクト
│   │   ├── book.gen.go    # 書籍DAO（自動生成）
│   │   ├── gen.go         # DAO生成器
│   │   └── user_info_1.gen.go # ユーザー情報DAO
│   ├── handler/           # HTTPハンドラー
│   │   ├── book_handler.go    # 書籍関連ハンドラー
│   │   ├── index_handler.go   # インデックスハンドラー
│   │   └── user_handler.go    # ユーザー関連ハンドラー
│   ├── middleware/        # ミドルウェア
│   │   └── jwt.go         # JWT認証ミドルウェア
│   ├── model/             # データモデル
│   │   ├── book.gen.go    # 書籍モデル
│   │   ├── dto/           # データ転送オブジェクト
│   │   │   └── auth.go    # 認証DTO
│   │   └── user_info_1.gen.go # ユーザー情報モデル
│   ├── repository/        # リポジトリ層
│   ├── router/            # ルーティング
│   │   └── router.go      # ルーター設定
│   ├── service/           # ビジネスロジック
│   │   ├── stream_service.go  # ストリームサービス
│   │   └── user_service.go    # ユーザーサービス
│   ├── stream_consumer/   # Redis Streams消費者
│   │   ├── base_processor.go  # 基本プロセッサー
│   │   ├── config_manager.go  # 設定管理
│   │   ├── consumer.go        # 消費者実装
│   │   ├── processors/        # プロセッサー
│   │   │   ├── book_similarity.go      # 書籍類似性処理
│   │   │   ├── register.go             # 登録処理
│   │   │   ├── template.go             # テンプレート処理
│   │   │   └── user_login_stream.go    # ユーザーログインストリーム
│   │   ├── processors.go      # プロセッサー管理
│   │   └── registry.go        # プロセッサー登録
│   └── wire/              # 依存性注入
│       └── wire.go        # Wire設定
├── pkg/                   # 共有パッケージ
│   ├── db/                # データベース接続
│   │   └── db.go
│   ├── jwt/               # JWT認証
│   │   └── jwt.go
│   ├── logger/            # ログ機能
│   │   └── logger.go
│   ├── redis/             # Redis接続
│   │   ├── redis.go       # Redis接続管理
│   │   └── stream.go      # Redis Streams機能
│   └── response/          # HTTPレスポンス
│       └── response.go
├── go.mod                 # Goモジュール定義
├── go.sum                 # 依存関係チェックサム
├── .air.toml             # Air（ホットリロード）設定
└── test_jwt.go           # JWTテストファイル
```

## 主要機能

### 1. 書籍管理システム
- 書籍のCRUD操作
- 書籍類似性計算
- 書籍検索機能

### 2. ユーザー認証
- JWTベースの認証システム
- ユーザー登録・ログイン
- セッション管理

### 3. Redis Streams統合
- リアルタイムデータ処理
- イベント駆動アーキテクチャ
- ストリーム消費者による非同期処理

### 4. データベース統合
- GORMを使用したORM
- 自動マイグレーション
- データアクセス層の分離

## 技術スタック

- **言語**: Go 1.21+
- **Webフレームワーク**: Gin
- **データベース**: MySQL/PostgreSQL (GORM)
- **キャッシュ/メッセージング**: Redis (Streams)
- **認証**: JWT
- **依存性注入**: Wire
- **ログ**: Zap
- **開発ツール**: Air (ホットリロード)

## セットアップ

### 前提条件
- Go 1.21以上
- Redis
- MySQL/PostgreSQL

### インストール

```bash
# リポジトリをクローン
git clone <repository-url>
cd drama-go

# 依存関係をインストール
go mod download

# 設定ファイルを編集
cp config/app.yaml.example config/app.yaml
# config/app.yamlを編集してデータベースとRedisの設定を行ってください

# データベースマイグレーション
go run cmd/gen_db/main.go

# アプリケーションを起動
go run cmd/main.go
```

### 開発モード

```bash
# Airを使用したホットリロード（開発用）
air
```