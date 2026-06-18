# API動作確認エビデンス

実行日時: 2026-06-17 20:02:38 +09:00

## 実行環境

- アプリケーション: Spring Boot `bootRun`
- データベース: Docker Compose上のPostgreSQL
- ベースURL: `http://localhost:18080`
- HTTP実行結果JSON: `docs/FINAL_HTTP_RESULTS.json`
- bootRunログ: `build/final-bootrun-output.txt`, `build/final-bootrun-error.txt`
- ビルドログ: `final-build-output.txt`

## ビルド・自動テスト結果

実行コマンド:

```powershell
docker compose up -d
cmd /c ".\gradlew.bat build --no-daemon --rerun-tasks > final-build-output.txt 2>&1"
```

結果:

```text
BUILD SUCCESSFUL in 36s
10 actionable tasks: 10 executed
```

テスト結果:

| テストクラス | 件数 | 失敗 | エラー |
| --- | ---: | ---: | ---: |
| `AuthorApiIntegrationTest` | 5 | 0 | 0 |
| `BookApiIntegrationTest` | 7 | 0 | 0 |

## 要件確認結果

| 確認項目 | 結果 |
| --- | --- |
| 著者情報をRDBに登録・更新できる | OK |
| 書籍情報をRDBに登録・更新できる | OK |
| 著者に紐づく本を取得できる | OK |
| 書籍が複数著者を持てる | OK |
| 著者が複数書籍を持てる | OK |
| 価格は0以上である | OK |
| 書籍は最低1人の著者を持つ | OK |
| 生年月日は現在日以前である | OK |
| 生年月日は日付形式のみ受け付ける | OK |
| 出版済みから未出版へ変更できない | OK |
| 存在しない著者・書籍を適切にエラーにする | OK |
| 存在しない著者IDを指定した書籍登録をエラーにする | OK |
| 不正な出版状況をエラーにする | OK |
| 不正JSONをエラーにする | OK |

## 実HTTP確認結果

### 1. 著者登録

- Request: `POST /api/v1/authors`
- Input:

```json
{"name":"Author One 20260617200237","birthDate":"1980-01-01"}
```

- Status: `201`
- Output:

```json
{"id":14,"name":"Author One 20260617200237","birthDate":"1980-01-01"}
```

### 2. 著者登録 2人目

- Request: `POST /api/v1/authors`
- Input:

```json
{"name":"Author Two 20260617200237","birthDate":"1981-02-03"}
```

- Status: `201`
- Output:

```json
{"id":15,"name":"Author Two 20260617200237","birthDate":"1981-02-03"}
```

### 3. 著者更新

- Request: `PUT /api/v1/authors/14`
- Input:

```json
{"name":"Author One Updated 20260617200237","birthDate":"1979-12-31"}
```

- Status: `200`
- Output:

```json
{"id":14,"name":"Author One Updated 20260617200237","birthDate":"1979-12-31"}
```

### 4. 複数著者を持つ書籍登録

- Request: `POST /api/v1/books`
- Input:

```json
{"title":"Shared Book 20260617200237","price":1200,"publicationStatus":"UNPUBLISHED","authorIds":[14,15]}
```

- Status: `201`
- Output:

```json
{"id":14,"title":"Shared Book 20260617200237","price":1200,"publicationStatus":"UNPUBLISHED","authorIds":[14,15]}
```

### 5. 書籍更新、未出版から出版済みへ変更

- Request: `PUT /api/v1/books/14`
- Input:

```json
{"title":"Shared Book Updated 20260617200237","price":1500,"publicationStatus":"PUBLISHED","authorIds":[14,15]}
```

- Status: `200`
- Output:

```json
{"id":14,"title":"Shared Book Updated 20260617200237","price":1500,"publicationStatus":"PUBLISHED","authorIds":[14,15]}
```

### 6. 同じ著者に2冊目の書籍を登録

- Request: `POST /api/v1/books`
- Input:

```json
{"title":"Second Book 20260617200237","price":800,"publicationStatus":"PUBLISHED","authorIds":[14]}
```

- Status: `201`
- Output:

```json
{"id":15,"title":"Second Book 20260617200237","price":800,"publicationStatus":"PUBLISHED","authorIds":[14]}
```

### 7. 著者に紐づく本を取得

- Request: `GET /api/v1/authors/14/books`
- Input: なし
- Status: `200`
- Output:

```json
[{"id":14,"title":"Shared Book Updated 20260617200237","price":1500,"publicationStatus":"PUBLISHED"},{"id":15,"title":"Second Book 20260617200237","price":800,"publicationStatus":"PUBLISHED"}]
```

### 8. 価格が0未満の場合のエラー

- Request: `POST /api/v1/books`
- Input:

```json
{"title":"Invalid Price 20260617200237","price":-1,"publicationStatus":"UNPUBLISHED","authorIds":[14]}
```

- Status: `400`
- Output:

```json
{"message":"price: 価格は0以上で入力してください"}
```

### 9. 著者なし書籍登録のエラー

- Request: `POST /api/v1/books`
- Input:

```json
{"title":"No Author 20260617200237","price":100,"publicationStatus":"UNPUBLISHED","authorIds":[]}
```

- Status: `400`
- Output:

```json
{"message":"authorIds: 著者を1人以上指定してください"}
```

### 10. 生年月日が未来日の場合のエラー

- Request: `POST /api/v1/authors`
- Input:

```json
{"name":"Future Author 20260617200237","birthDate":"2999-01-01"}
```

- Status: `400`
- Output:

```json
{"message":"生年月日は現在日以前の日付を入力してください"}
```

### 11. 生年月日に時刻が含まれる場合のエラー

- Request: `POST /api/v1/authors`
- Input:

```json
{"name":"DateTime Author 20260617200237","birthDate":"1990-01-01T10:30:00"}
```

- Status: `400`
- Output:

```json
{"message":"birthDate: 生年月日はyyyy-MM-dd形式で入力してください"}
```

### 12. 出版済みから未出版へ戻す場合のエラー

- Request: `PUT /api/v1/books/14`
- Input:

```json
{"title":"Shared Book Updated 20260617200237","price":1500,"publicationStatus":"UNPUBLISHED","authorIds":[14,15]}
```

- Status: `400`
- Output:

```json
{"message":"出版済みの書籍は未出版に変更できません"}
```

### 13. 存在しない著者に紐づく本を取得する場合のエラー

- Request: `GET /api/v1/authors/999999/books`
- Input: なし
- Status: `404`
- Output:

```json
{"message":"著者が見つかりません: 999999"}
```

### 14. 存在しない書籍更新のエラー

- Request: `PUT /api/v1/books/999999`
- Input:

```json
{"title":"Missing Book 20260617200237","price":100,"publicationStatus":"UNPUBLISHED","authorIds":[14]}
```

- Status: `404`
- Output:

```json
{"message":"書籍が見つかりません: 999999"}
```

### 15. 存在しない著者IDを指定した書籍登録のエラー

- Request: `POST /api/v1/books`
- Input:

```json
{"title":"Missing Author 20260617200237","price":100,"publicationStatus":"UNPUBLISHED","authorIds":[999999]}
```

- Status: `400`
- Output:

```json
{"message":"存在しない著者IDが指定されています: 999999"}
```

### 16. 不正な出版状況のエラー

- Request: `POST /api/v1/books`
- Input:

```json
{"title":"Invalid Status 20260617200237","price":100,"publicationStatus":"ARCHIVED","authorIds":[14]}
```

- Status: `400`
- Output:

```json
{"message":"リクエストボディの形式が不正です"}
```

### 17. 不正JSONのエラー

- Request: `POST /api/v1/authors`
- Input:

```json
{"name":"Broken"
```

- Status: `400`
- Output:

```json
{"message":"リクエストボディの形式が不正です"}
```

## 補足

自動テストでは主要な正常系・制約違反を担保し、実HTTP確認ではAPIとして自然に発生する入力不正、存在しないID、JSON形式不正、GETの実レスポンスまで確認しています。
