# Quick Build Fix - Manual Steps
# Run this if automatic OneDrive handling doesn't work

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "     Manual Build Fix for OneDrive Issues" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

Write-Host "STEP 1: Pause OneDrive" -ForegroundColor Yellow
Write-Host "  - Right-click OneDrive icon in system tray (bottom right)" -ForegroundColor White
Write-Host "  - Click 'Pause syncing' > '2 hours'" -ForegroundColor White
Write-Host "`nPress Enter when OneDrive is paused..." -ForegroundColor Green
Read-Host

Write-Host "`nSTEP 2: Delete build folder" -ForegroundColor Yellow
$buildDir = "$PSScriptRoot\..\app\build"

if (Test-Path $buildDir) {
    Write-Host "  Attempting to delete: $buildDir" -ForegroundColor White
    try {
        Remove-Item -Path $buildDir -Recurse -Force -ErrorAction Stop
        Write-Host "  ✅ Build folder deleted successfully!" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ Could not delete: $_" -ForegroundColor Red
        Write-Host "  Try manually deleting the folder in File Explorer" -ForegroundColor Yellow
        Write-Host "`nPress Enter when folder is deleted..." -ForegroundColor Green
        Read-Host
    }
} else {
    Write-Host "  ✅ Build folder already deleted" -ForegroundColor Green
}

Write-Host "`nSTEP 3: Build APK" -ForegroundColor Yellow
Write-Host "  Running build script..." -ForegroundColor White
Write-Host "============================================================`n" -ForegroundColor Cyan

Set-Location "$PSScriptRoot\.."
python "$PSScriptRoot\build_apk.py" debug

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "STEP 4: Resume OneDrive" -ForegroundColor Yellow
Write-Host "  - Right-click OneDrive icon in system tray" -ForegroundColor White
Write-Host "  - Click 'Resume syncing'" -ForegroundColor White
Write-Host "`nPress Enter to exit..." -ForegroundColor Green
Read-Host
