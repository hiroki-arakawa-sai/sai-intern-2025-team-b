# sai-intern-2025-team-b

サイエンスアーツ 夏季インターン 2025 チーム B

# BuddyBot 最小構成での実行手順（チーム共有用）

このドキュメントは、**最小コード**で BuddyBot へ送る HTTP リクエストを実行・検証するための手順をまとめたものです。  
外部ネットワークに依存しない **ローカルのエコー API** を使った検証方法（推奨）と、本番 URL に切り替える方法を記載します。

---

## 対象リポジトリ構成（抜粋）

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
            │   │       ├── BuddyBotCall.java            # ← 最小クライアント（OkHttp）
            │   │       └── _debug/EchoController.java   # ← ローカルのエコーAPI（開発用）
            │   └── resources/
            │       └── application.properties
            └── test/
                └── java/...

**前提**

- `pom.xml` に `com.squareup.okhttp3:okhttp:4.12.0` が追加済みであること
- JDK 17 以上

---

## 0. セットアップ

依存取得とビルド：

    cd demo
    ./mvnw -U clean test

---

## 1. ローカルでの疎通検証（推奨：外部ネットワーク不要）

### 1-1) エコー API を起動

    ./mvnw spring-boot:run

- `POST http://localhost:8080/_debug/echo` を待ち受けます。
- 受け取った **multipart/form-data** の `parameter` フィールドやヘッダーを **そのまま JSON で返却**します。

### 1-2) 最小クライアントを実行

別ターミナルで：

    ./mvnw -DskipTests exec:java -Dexec.mainClass=com.example.demo.BuddyBotCall

**期待出力**  
`HTTP 200` とともに、返ってきた JSON の `parameter` に送信した JSON 文字列が表示されます。

**任意：URL を明示的に指定したい場合**

    ./mvnw -DskipTests exec:java \
      -Dexec.mainClass=com.example.demo.BuddyBotCall \
      -Dbuddybot.testUrl=http://localhost:8080/_debug/echo

---

## 2. 本番 URL へ送る（ネットワーク要件が整った後）

### 2-1) 認証トークンの設定（環境変数）

macOS / Linux（一時的）：

    export BUDDYBOT_AUTH_TOKEN='あなたのトークン'

Windows PowerShell（永続化。新しいシェルを開き直してください）：

    setx BUDDYBOT_AUTH_TOKEN "あなたのトークン"

### 2-2) 実行（URL 切り替え）

    ./mvnw -DskipTests exec:java \
      -Dexec.mainClass=com.example.demo.BuddyBotCall \
      -Dbuddybot.testUrl=https://ip0-254.science-arts.com/buddybot/peerchat

> コードは既定で `Authorization: Bearer <token>` を付与します。  
> もしトークン文字列がすでに `Bearer ...` を含むなら、ヘッダー設定を `Authorization: <token>` に変更してください。

---

## 3. プロキシ／DNS／VPN が必要な場合

- **プロキシ経由で実行（例：proxy.company.local:8080）**

      ./mvnw -DskipTests exec:java \
        -Dexec.mainClass=com.example.demo.BuddyBotCall \
        -Dbuddybot.testUrl=https://httpbin.org/post \
        -Dhttps.proxyHost=proxy.company.local -Dhttps.proxyPort=8080 \
        -Dhttp.proxyHost=proxy.company.local  -Dhttp.proxyPort=8080

  認証付きなら `-Dhttp(s).proxyUser` / `-Dhttp(s).proxyPassword` を追加。

- **DNS が引けない（NXDOMAIN）**  
  社内向けドメインの可能性があります。VPN 接続や社内 DNS を使用し、接続後に `nslookup <hostname>` で解決できるか確認してください。

- **SSL 証明書エラー**  
  社内 CA 使用時は JDK の truststore に社内ルート証明書の追加が必要です（社内手順に従ってください）。

---

## 4. よくあるエラーと対処

| 症状            | 代表メッセージ                                    | 対処                                                                        |
| --------------- | ------------------------------------------------- | --------------------------------------------------------------------------- |
| トークン未設定  | 環境変数 BUDDYBOT_AUTH_TOKEN を設定してください。 | `export/setx` で設定（またはコード側で `-Dbuddybot.authToken`、引数対応へ） |
| 名前解決不可    | Unknown host / NXDOMAIN                           | VPN / 社内 DNS / ホスト名の再確認                                           |
| タイムアウト    | timeout                                           | プロキシ必須の可能性 → JVM フラグでプロキシ設定                             |
| 407 Proxy Auth  | Proxy Authentication Required                     | `-Dhttp(s).proxyUser/Password` を付与                                       |
| 4xx/5xx（本番） | HTTP 4xx/5xx と本文                               | リクエスト項目・認可・API 仕様を確認（Echo で形が正しければ API 側要件）    |

---

## 5. セキュリティ注意点

- 認証トークンは **環境変数や秘密管理**を使用し、**リポジトリにコミットしない**でください。
- `/_debug/echo` は開発用。**本番デプロイでは無効化**（削除や開発プロファイル限定）を推奨します。

---

## 6. 参考コマンド

    # 依存の取得とビルド
    ./mvnw -U clean test

    # アプリ起動（Echo 用）
    ./mvnw spring-boot:run

    # 最小クライアント実行（既定URL or -Dbuddybot.testUrl で上書き）
    ./mvnw -DskipTests exec:java -Dexec.mainClass=com.example.demo.BuddyBotCall

    # 依存に OkHttp が入っているか確認
    ./mvnw -q dependency:tree | grep okhttp

---
