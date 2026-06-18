# GitHubアップロード前チェック

確認日: 2026-06-17

## 評価観点の確認

| 評価観点 | 確認結果 | 補足 |
| --- | --- | --- |
| 指定された技術スタックの適用 | OK | Kotlin、Spring Boot、jOOQを使用しています。 |
| フレームワークやライブラリの適切な利用 | OK | Spring MVC、Bean Validation、Flyway、jOOQを役割に合わせて使用しています。 |
| 実行可能性 | OK | `gradlew.bat test --no-daemon --rerun-tasks` でテスト成功を確認しています。 |
| 仕様に沿った動作 | OK | 著者登録・更新、書籍登録・更新、著者に紐づく本の取得、各種制約を確認しています。 |
| 変数名やクラス名、関数名が実態を明確に反映しているか | OK | `AuthorService`、`BookService`、`BookRepository`、`BookAuthorRepository` など役割が分かる命名です。 |
| Null安全性 | OK | DTOとドメインは非null中心で、DB取得時のnullは明示的に例外化しています。 |
| コードフォーマットの整合性 | OK | Kotlinの一般的なスタイルに沿っています。文字化けコメントも除去済みです。 |
| 変数に再代入しないなど、ベストプラクティスの遵守 | OK | 実装コードは `val` 中心です。テストの `lateinit var` はSpringテストのDI用途です。 |
| オーバーエンジニアリングしていないか | OK | Service、Repository、DTO、Domainのシンプルな構成です。不要なinterface分離はしていません。 |
| 適切な単体テストが作成されているか | OK | 重要要件はAPI統合テストで担保しています。課題提出としては十分な範囲です。 |

## テスト結果

直近のテスト結果:

```text
AuthorApiIntegrationTest: tests="5" skipped="0" failures="0" errors="0"
BookApiIntegrationTest: tests="7" skipped="0" failures="0" errors="0"
```

合計12件のAPI統合テストが成功しています。

## 要件に対する確認

| 要件 | 確認方法 | 確認結果 |
| --- | --- | --- |
| 書籍情報をRDBに登録できる | `BookApiIntegrationTest.creates a book` | OK |
| 書籍情報をRDBで更新できる | `BookApiIntegrationTest.updates a book with multiple authors` | OK |
| 著者情報をRDBに登録できる | テストヘルパー `createAuthor` と実HTTP確認 | OK |
| 著者情報をRDBで更新できる | `AuthorApiIntegrationTest.updates an author` | OK |
| 著者に紐づく本を取得できる | `AuthorApiIntegrationTest.gets books by author` | OK |
| 価格が0以上である | `BookApiIntegrationTest.rejects negative book price` | OK |
| 書籍が最低1人の著者を持つ | `BookApiIntegrationTest.rejects book without authors` | OK |
| 書籍が複数著者を持てる | `BookApiIntegrationTest.updates a book with multiple authors` | OK |
| 出版済みを未出版に変更できない | `BookApiIntegrationTest.rejects changing published book to unpublished` | OK |
| 著者名を持つ | `AuthorRequest` の `@NotBlank` | OK |
| 生年月日が現在日以前である | `AuthorApiIntegrationTest.rejects author birth date in future` | OK |
| 著者が複数書籍を執筆できる | 実HTTP確認で確認済み | OK |

## 補足

- APIの自然なエラー系、実HTTPでの挙動確認は実行エビデンスで確認済みです。
- `build/` 配下のログは再生成可能なため、GitHubへ含める必要はありません。
- 提出用に残す情報は `README.md` と本ファイルで確認できます。
