# Test script: login and POST payload to generation endpoint
$ErrorActionPreference = 'Stop'

$loginBody = @{ username = 'admin'; password = 'admin123' } | ConvertTo-Json
$login = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body $loginBody
$token = $login.accessToken

$payloadObj = @{
    entityName = 'tbl_users2'
    entityTableName = 'tbl_users2'
    entityColumnsCount = 3
    entityColumnNext = ''
    isColumnDropdown = $false
    isColumnCheckbox = $false
    isColumnRadio = $false
    isColumnBlob = $false
    columnType = ''
    columnLength = $null
    columnPrimary = $false
    columnIndex = $false
    columnPartOfSearch = $false
    isReferentialIntegrity = $false
    doWeNeedWorkflow = $true
    doWeNeed2LevelWorkflow = $false
    doWeNeed1LevelWorkflow = $true
    workflowStatus = 'DRAFT|SUBMITTED|APPROVED\REJECTED'
    doWeNeedAuditTable = $true
    doWeNeedArchiveRecords = $true
    criteriaFields = 'isactive'
    criteriaValues = '0'
    doWeNeedCreateView = $true
    howManyMonthsMainTable = 3
    howManyMonthsArchiveTable = 2
    criteriaToMoveFromMainToArchiveTable = 'isactive=0'
    criteriaToMoveFromArchiveToDeleteTable = 'isactive=0'
    thingsToCreate = 'sss'
    parentMenu = 'Administration'
    whichRoleIsEligible = 'ADMIN'
    columns = @(
        @{ column_seq=1; column_name='col1'; column_length=$null; column_datatype='string'; column_type='freefield'; is_dropdown=$false; is_radiobutton=$false; is_checkbox=$false; is_freefield=$true; column_index=$false; column_primary=$false; column_part_of_search=$false; column_referential_integrity=$false },
        @{ column_seq=2; column_name='col2'; column_length=$null; column_datatype='string'; column_type='freefield'; is_dropdown=$false; is_radiobutton=$false; is_checkbox=$false; is_freefield=$true; column_index=$false; column_primary=$false; column_part_of_search=$false; column_referential_integrity=$false },
        @{ column_seq=3; column_name='col3'; column_length=$null; column_datatype='string'; column_type='freefield'; is_dropdown=$false; is_radiobutton=$false; is_checkbox=$false; is_freefield=$true; column_index=$false; column_primary=$false; column_part_of_search=$false; column_referential_integrity=$false }
    )
}

$payload = $payloadObj | ConvertTo-Json -Depth 6

try {
    Write-Host 'Posting payload to generation endpoint...'
    $resp = Invoke-RestMethod -Uri 'http://localhost:8080/api/entity-management/generate-from-payload' -Method Post -Headers @{ Authorization = "Bearer $token"; 'Content-Type' = 'application/json' } -Body $payload -TimeoutSec 120
    $resp | ConvertTo-Json | Write-Host
} catch {
    Write-Host 'Request failed:'
    if ($_.Exception.Response -ne $null) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $body = $reader.ReadToEnd()
        Write-Host 'Error response body:'
        Write-Host $body
    } else {
        Write-Host $_.Exception.Message
    }
    exit 1
}

Write-Host 'Done.'
