# sai-intern-2025-team-b

サイエンスアーツ 夏季インターン 2025 チーム B

# BuddyBot 最小構成での実行手順（チーム共有用）

このドキュメントは、BuddyBot から送受信される HTTP リクエストを実行・検証するための手順をまとめたものです。

---

## 主要なリポジトリ構造

    hiroki-arakawa-sai-sai-intern-2025-team-b/
    └── demo/
        ├── pom.xml
        ├── mvnw / mvnw.cmd
        └── src/
            ├── main/
            │   ├── java/
            │   │   └── com/example/demo/
            │   │       ├── DemoApplication.java
            │   │       ├── HelloController.java
            │   │       ├── BuddyBotCall.java            # ← `Inbound BuddyBotAPI` に HTTP リクエストを送信する
            │   │       ├── MiniApiServer.java           # ← `Outbound BuddyBotAPI` からのレスポンスを受け取るサーバー
            │   │       └── _debug/EchoController.java   # ← ローカルのエコーAPI（デバッグ用なので気にしないでok）
            │   └── resources/
            │       └── application.properties
            └── test/
                └── java/...

## BuddyBotCall.java について

このファイルは `Inbound BuddyBotAPI` に HTTP リクエストを送信します。

**前提**

- `pom.xml` に `com.squareup.okhttp3:okhttp:4.12.0` が追加済みであること
- JDK 17 以上

---

## 1. セットアップ

依存取得とビルド：

    cd demo
    ./mvnw -U clean test

---

## 2. 本番 URL へ送る

### 2-1) 認証トークンの設定（環境変数）

macOS / Linux（一時的）：

    export BUDDYBOT_AUTH_TOKEN='BuddyBotのトークン'

Windows PowerShell（永続化。新しいシェルを開き直してください）：

    setx BUDDYBOT_AUTH_TOKEN 'BuddyBotのトークン'

### 2-2) 実行（URL 切り替え）

    ./mvnw -DskipTests exec:java \
      -Dexec.mainClass=com.example.demo.BuddyBotCall \
      -Dbuddybot.testUrl=https://ip0-254.science-arts.com/buddybot/peerchat

> コードは既定で `Authorization: Bearer <token>` を付与します。  
> もしトークン文字列がすでに `Bearer ...` を含むなら、ヘッダー設定を `Authorization: <token>` に変更してください。

---

## 3. よくあるエラーと対処

| 症状            | 代表メッセージ                                    | 対処                                                                        |
| --------------- | ------------------------------------------------- | --------------------------------------------------------------------------- |
| トークン未設定  | 環境変数 BUDDYBOT_AUTH_TOKEN を設定してください。 | `export/setx` で設定（またはコード側で `-Dbuddybot.authToken`、引数対応へ） |
| 名前解決不可    | Unknown host / NXDOMAIN                           | VPN / 社内 DNS / ホスト名の再確認                                           |
| タイムアウト    | timeout                                           | プロキシ必須の可能性 → JVM フラグでプロキシ設定                             |
| 407 Proxy Auth  | Proxy Authentication Required                     | `-Dhttp(s).proxyUser/Password` を付与                                       |
| 4xx/5xx（本番） | HTTP 4xx/5xx と本文                               | リクエスト項目・認可・API 仕様を確認（Echo で形が正しければ API 側要件）    |

---

## 4. セキュリティ注意点

- 認証トークンは **環境変数や秘密管理**を使用し、**リポジトリにコミットしない**でください。

---

## 5. 参考コマンド

    # 依存の取得とビルド
    ./mvnw -U clean test

    # アプリ起動（Echo 用）
    ./mvnw spring-boot:run

    # 最小クライアント実行（既定URL or -Dbuddybot.testUrl で上書き）
    ./mvnw -DskipTests exec:java -Dexec.mainClass=com.example.demo.BuddyBotCall

    # 依存に OkHttp が入っているか確認
    ./mvnw -q dependency:tree | grep okhttp

---

## MiniApiServer.java について

`MiniApiServer.java` は `Outbound BuddyBotAPI` からのレスポンスを受け取るサーバーを起動します。

エンドポイント：`/buddybot/outbound`（POST）

- 既定のポート: **18080**
- 既定のバインド: **0.0.0.0**（LAN からも受けられる）

## 1) コンパイル & 起動

### macOS / Linux

    cd demo
    ./mvnw -q -DskipTests compile

    # 任意: 環境変数（無ければ既定値を使用）
    export MINI_API_PORT=18080         # 既定: 18080
    export MINI_API_BIND=0.0.0.0       # 既定: 0.0.0.0（LAN受け）

    # Authorization を検証したい場合だけ設定
    export BUDDYBOT_AUTH_TOKEN='BuddyBotのトークン'

    # 起動
    java -cp target/classes com.example.demo.MiniApiServer

### Windows (PowerShell)

    cd demo
    .\mvnw -q -DskipTests compile

    # 任意: 環境変数（無ければ既定値を使用）
    $env:MINI_API_PORT = '18080'       # 既定: 18080
    $env:MINI_API_BIND = '0.0.0.0'     # 既定: 0.0.0.0（LAN受け）

    # Authorization を検証したい場合だけ設定
    $env:BUDDYBOT_AUTH_TOKEN = 'BuddyBotのトークン'

    # 起動
    java -cp target\classes com.example.demo.MiniApiServer

### 起動ログ例

    Listening on http://0.0.0.0:18080/buddybot/outbound

## 2) buddycom console での設定

buddycom console で送信先 URL の設定が必要。送信先 URL は自身の IP アドレスを調べて以下のように設定する。

    http://自分PCのIPアドレス:18080/buddybot/outbound

    例) http://172.16.1.68:18080/buddybot/outbound
