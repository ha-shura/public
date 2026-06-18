# Book Management API

Kotlin、Spring Boot、jOOQ、Flyway、PostgreSQLで実装した書籍管理用のバックエンドAPIです。

## 技術スタック

- Kotlin
- Spring Boot
- jOOQ
- Flyway
- PostgreSQL
- JUnit / Spring Boot Test

## API一覧

| メソッド | URL | 内容 |
| --- | --- | --- |
| `POST` | `/api/v1/authors` | 著者を登録する |
| `PUT` | `/api/v1/authors/{id}` | 著者を更新する |
| `GET` | `/api/v1/authors/{id}/books` | 著者に紐づく書籍を取得する |
| `POST` | `/api/v1/books` | 書籍を登録する |
| `PUT` | `/api/v1/books/{id}` | 書籍を更新する |

## 主な仕様

- 書籍はタイトル、価格、出版状況、著者IDを持ちます。
- 書籍価格は0以上です。
- 書籍タイトルは255文字以内です。
- 書籍は最低1人の既存著者を持つ必要があります。
- 書籍は複数の著者を持てます。
- 著者は名前と生年月日を持ちます。
- 著者名は255文字以内です。
- 生年月日は現在日以前の日付のみ登録できます。
- 出版済みの書籍は未出版へ戻せません。

## セキュリティ・入力チェック

- SQLはjOOQで組み立て、文字列連結によるSQL実行は行っていません。
- リクエストDTOで入力値を検証しています。
- 価格、生年月日、出版状況などの重要な条件はDB制約でも保護しています。
- 不正なJSON、入力値エラー、存在しないデータ、DB制約違反はエラーレスポンスとして返します。
- 認証・認可は今回の課題範囲外として未実装です。外部公開する場合は追加が必要です。

## リクエスト・レスポンス例

### 著者登録

リクエスト:

```http
POST /api/v1/authors
Content-Type: application/json
```

```json
{
  "name": "Natsume Soseki",
  "birthDate": "1867-02-09"
}
```

レスポンス:

```json
{
  "id": 1,
  "name": "Natsume Soseki",
  "birthDate": "1867-02-09"
}
```

### 書籍登録

リクエスト:

```http
POST /api/v1/books
Content-Type: application/json
```

```json
{
  "title": "Kokoro",
  "price": 1200,
  "publicationStatus": "PUBLISHED",
  "authorIds": [1]
}
```

レスポンス:

```json
{
  "id": 1,
  "title": "Kokoro",
  "price": 1200,
  "publicationStatus": "PUBLISHED",
  "authorIds": [1]
}
```

### 著者に紐づく書籍取得

リクエスト:

```http
GET /api/v1/authors/1/books
```

レスポンス:

```json
[
  {
    "id": 1,
    "title": "Kokoro",
    "price": 1200,
    "publicationStatus": "PUBLISHED"
  }
]
```

## 起動方法

```bash
docker compose up -d
./gradlew bootRun
```

Windows:

```powershell
docker compose up -d
.\gradlew.bat bootRun
```

## テスト実行

```bash
./gradlew test
```

Windows:

```powershell
.\gradlew.bat test
```
