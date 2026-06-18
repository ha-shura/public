$ErrorActionPreference = "Stop"

$port = 18080
$baseUrl = "http://localhost:$port"
$buildDir = Join-Path (Get-Location) "build"
$serverOut = Join-Path $buildDir "final-bootrun-output.txt"
$serverErr = Join-Path $buildDir "final-bootrun-error.txt"
$resultJson = Join-Path $buildDir "final-http-results.json"

Remove-Item -LiteralPath $serverOut, $serverErr, $resultJson -ErrorAction SilentlyContinue

$process = Start-Process `
    -FilePath "cmd.exe" `
    -ArgumentList "/c", ".\gradlew.bat bootRun --args=""--server.port=$port"" --no-daemon" `
    -RedirectStandardOutput $serverOut `
    -RedirectStandardError $serverErr `
    -PassThru `
    -WindowStyle Hidden

function Convert-Body($content) {
    if ([string]::IsNullOrWhiteSpace($content)) {
        return $null
    }
    try {
        return $content | ConvertFrom-Json
    } catch {
        return $content
    }
}

function Invoke-Api {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Path,
        [object]$RequestBody = $null,
        [string]$RawBody = $null
    )

    $requestBodyForEvidence = $RequestBody
    $bodyToSend = $null

    if ($null -ne $RawBody) {
        $bodyToSend = $RawBody
        $requestBodyForEvidence = $RawBody
    } elseif ($null -ne $RequestBody) {
        $bodyToSend = $RequestBody | ConvertTo-Json -Depth 20 -Compress
    }

    $curlArgs = @("-sS", "-w", "`n%{http_code}", "-X", $Method, "$baseUrl$Path")
    $bodyFile = $null
    if ($null -ne $bodyToSend) {
        $bodyFile = Join-Path $buildDir "curl-body-$([guid]::NewGuid()).json"
        Set-Content -LiteralPath $bodyFile -Value $bodyToSend -Encoding utf8 -NoNewline
        $curlArgs += @("-H", "Content-Type: application/json", "--data-binary", "@$bodyFile")
    }

    try {
        $curlOutput = & curl.exe @curlArgs 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "curl failed for ${Method} ${Path}: $curlOutput"
        }
    } finally {
        if ($bodyFile) {
            Remove-Item -LiteralPath $bodyFile -ErrorAction SilentlyContinue
        }
    }

    $responseText = ($curlOutput -join "`n").TrimEnd()
    $lastBreak = $responseText.LastIndexOf("`n")
    if ($lastBreak -lt 0) {
        throw "curl response did not include a status code for $Method $Path"
    }

    $content = $responseText.Substring(0, $lastBreak)
    $status = [int]$responseText.Substring($lastBreak + 1).Trim()
    $parsedOutput = Convert-Body $content
    $evidenceInput = if ($null -eq $requestBodyForEvidence) { "none" } else { $requestBodyForEvidence }

    [ordered]@{
        name = $Name
        method = $Method
        path = $Path
        input = $evidenceInput
        status = $status
        outputRaw = $content
        output = $parsedOutput
    }
}

try {
    $ready = $false
    for ($i = 0; $i -lt 90; $i++) {
        Start-Sleep -Seconds 1
        try {
            Invoke-WebRequest -Uri "$baseUrl/api/v1/authors/999999/books" -UseBasicParsing -TimeoutSec 2 | Out-Null
            $ready = $true
            break
        } catch {
            if ($_.Exception.Response) {
                $ready = $true
                break
            }
        }
        if ($process.HasExited) {
            break
        }
    }

    if (-not $ready) {
        throw "API server did not become ready. See $serverOut and $serverErr"
    }

    $suffix = Get-Date -Format "yyyyMMddHHmmss"
    $steps = New-Object System.Collections.Generic.List[object]

    $author1Body = [ordered]@{ name = "Author One $suffix"; birthDate = "1980-01-01" } | ConvertTo-Json -Compress
    $author1 = Invoke-Api -Name "author_create_1" -Method "POST" -Path "/api/v1/authors" -RawBody $author1Body
    $steps.Add($author1)

    $author2Body = [ordered]@{ name = "Author Two $suffix"; birthDate = "1981-02-03" } | ConvertTo-Json -Compress
    $author2 = Invoke-Api -Name "author_create_2" -Method "POST" -Path "/api/v1/authors" -RawBody $author2Body
    $steps.Add($author2)

    $updatedAuthorBody = [ordered]@{ name = "Author One Updated $suffix"; birthDate = "1979-12-31" } | ConvertTo-Json -Compress
    $updatedAuthor = Invoke-Api -Name "author_update" -Method "PUT" -Path "/api/v1/authors/$($author1.output.id)" -RawBody $updatedAuthorBody
    $steps.Add($updatedAuthor)

    $book1Body = [ordered]@{ title = "Shared Book $suffix"; price = 1200; publicationStatus = "UNPUBLISHED"; authorIds = @($author1.output.id, $author2.output.id) } | ConvertTo-Json -Compress
    $book1 = Invoke-Api -Name "book_create_multiple_authors" -Method "POST" -Path "/api/v1/books" -RawBody $book1Body
    $steps.Add($book1)

    $book1UpdatedBody = [ordered]@{ title = "Shared Book Updated $suffix"; price = 1500; publicationStatus = "PUBLISHED"; authorIds = @($author1.output.id, $author2.output.id) } | ConvertTo-Json -Compress
    $book1Updated = Invoke-Api -Name "book_update_to_published" -Method "PUT" -Path "/api/v1/books/$($book1.output.id)" -RawBody $book1UpdatedBody
    $steps.Add($book1Updated)

    $book2Body = [ordered]@{ title = "Second Book $suffix"; price = 800; publicationStatus = "PUBLISHED"; authorIds = @($author1.output.id) } | ConvertTo-Json -Compress
    $book2 = Invoke-Api -Name "book_create_second_book_for_author" -Method "POST" -Path "/api/v1/books" -RawBody $book2Body
    $steps.Add($book2)

    $booksByAuthor = Invoke-Api -Name "books_by_author" -Method "GET" -Path "/api/v1/authors/$($author1.output.id)/books"
    $steps.Add($booksByAuthor)

    $invalidPriceBody = [ordered]@{ title = "Invalid Price $suffix"; price = -1; publicationStatus = "UNPUBLISHED"; authorIds = @($author1.output.id) } | ConvertTo-Json -Compress
    $invalidPrice = Invoke-Api -Name "error_negative_price" -Method "POST" -Path "/api/v1/books" -RawBody $invalidPriceBody
    $steps.Add($invalidPrice)

    $emptyAuthorsBody = [ordered]@{ title = "No Author $suffix"; price = 100; publicationStatus = "UNPUBLISHED"; authorIds = @() } | ConvertTo-Json -Compress
    $emptyAuthors = Invoke-Api -Name "error_no_authors" -Method "POST" -Path "/api/v1/books" -RawBody $emptyAuthorsBody
    $steps.Add($emptyAuthors)

    $futureAuthorBody = [ordered]@{ name = "Future Author $suffix"; birthDate = "2999-01-01" } | ConvertTo-Json -Compress
    $futureAuthor = Invoke-Api -Name "error_future_birth_date" -Method "POST" -Path "/api/v1/authors" -RawBody $futureAuthorBody
    $steps.Add($futureAuthor)

    $dateTimeAuthorBody = [ordered]@{ name = "DateTime Author $suffix"; birthDate = "1990-01-01T10:30:00" } | ConvertTo-Json -Compress
    $dateTimeAuthor = Invoke-Api -Name "error_birth_date_with_time" -Method "POST" -Path "/api/v1/authors" -RawBody $dateTimeAuthorBody
    $steps.Add($dateTimeAuthor)

    $rollbackBody = [ordered]@{ title = "Shared Book Updated $suffix"; price = 1500; publicationStatus = "UNPUBLISHED"; authorIds = @($author1.output.id, $author2.output.id) } | ConvertTo-Json -Compress
    $rollback = Invoke-Api -Name "error_published_to_unpublished" -Method "PUT" -Path "/api/v1/books/$($book1.output.id)" -RawBody $rollbackBody
    $steps.Add($rollback)

    $notFoundAuthorBooks = Invoke-Api -Name "error_missing_author_books" -Method "GET" -Path "/api/v1/authors/999999/books"
    $steps.Add($notFoundAuthorBooks)

    $notFoundBookUpdateBody = [ordered]@{ title = "Missing Book $suffix"; price = 100; publicationStatus = "UNPUBLISHED"; authorIds = @($author1.output.id) } | ConvertTo-Json -Compress
    $notFoundBookUpdate = Invoke-Api -Name "error_missing_book_update" -Method "PUT" -Path "/api/v1/books/999999" -RawBody $notFoundBookUpdateBody
    $steps.Add($notFoundBookUpdate)

    $missingAuthorInBookBody = [ordered]@{ title = "Missing Author $suffix"; price = 100; publicationStatus = "UNPUBLISHED"; authorIds = @(999999) } | ConvertTo-Json -Compress
    $missingAuthorInBook = Invoke-Api -Name "error_missing_author_id" -Method "POST" -Path "/api/v1/books" -RawBody $missingAuthorInBookBody
    $steps.Add($missingAuthorInBook)

    $invalidStatusBody = [ordered]@{ title = "Invalid Status $suffix"; price = 100; publicationStatus = "ARCHIVED"; authorIds = @($author1.output.id) } | ConvertTo-Json -Compress
    $invalidStatus = Invoke-Api -Name "error_invalid_publication_status" -Method "POST" -Path "/api/v1/books" -RawBody $invalidStatusBody
    $steps.Add($invalidStatus)

    $invalidJson = Invoke-Api -Name "error_invalid_json" -Method "POST" -Path "/api/v1/authors" -RawBody '{"name":"Broken"'
    $steps.Add($invalidJson)

    $checks = [ordered]@{
        authorCreateAndUpdate = ($author1.status -eq 201 -and $author2.status -eq 201 -and $updatedAuthor.status -eq 200)
        bookCreateAndUpdate = ($book1.status -eq 201 -and $book1Updated.status -eq 200)
        booksByAuthor = ($booksByAuthor.status -eq 200 -and $booksByAuthor.output.Count -ge 2)
        bookHasMultipleAuthors = ($book1.output.authorIds.Count -eq 2)
        authorHasMultipleBooks = ($booksByAuthor.output.Count -ge 2)
        priceMustBeZeroOrMore = ($invalidPrice.status -eq 400)
        bookMustHaveAuthor = ($emptyAuthors.status -eq 400)
        birthDateMustBePastOrPresent = ($futureAuthor.status -eq 400)
        birthDateRejectsDateTime = ($dateTimeAuthor.status -eq 400)
        publishedCannotBecomeUnpublished = ($rollback.status -eq 400)
        notFoundAuthorReturns404 = ($notFoundAuthorBooks.status -eq 404)
        notFoundBookReturns404 = ($notFoundBookUpdate.status -eq 404)
        missingAuthorIdReturns400 = ($missingAuthorInBook.status -eq 400)
        invalidPublicationStatusReturns400 = ($invalidStatus.status -eq 400)
        invalidJsonReturns400 = ($invalidJson.status -eq 400)
    }

    [ordered]@{
        executedAt = (Get-Date -Format "yyyy-MM-dd HH:mm:ss zzz")
        server = "bootRun + PostgreSQL docker compose"
        baseUrl = $baseUrl
        requirementsChecked = $checks
        steps = $steps
    } | ConvertTo-Json -Depth 40 | Set-Content -LiteralPath $resultJson -Encoding utf8

    Write-Output "Final HTTP verification succeeded. Results: $resultJson"
} finally {
    $ErrorActionPreference = "SilentlyContinue"
    if ($process -and -not $process.HasExited) {
        Stop-Process -Id $process.Id -Force
    }

    Start-Sleep -Seconds 2
    Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue |
        ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
}
