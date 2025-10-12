# Health Calendar Build Script with OneDrive Handling
# This script pauses OneDrive, deletes build directory, builds APK, and resumes OneDrive

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "     Health Calendar - OneDrive-Safe Build Script" -ForegroundColor Cyan
Write-Host "============================================================`n" -ForegroundColor Cyan

$buildDir = "$PSScriptRoot\..\app\build"
$projectDir = "$PSScriptRoot\.."

# Function to pause OneDrive
function Pause-OneDrive {
    Write-Host "⏸️  Pausing OneDrive sync..." -ForegroundColor Yellow
    try {
        $oneDriveProcess = Get-Process -Name "OneDrive" -ErrorAction SilentlyContinue
        if ($oneDriveProcess) {
            # Pause OneDrive via command line
            & "$env:LOCALAPPDATA\Microsoft\OneDrive\OneDrive.exe" /pause
            Start-Sleep -Seconds 2
            Write-Host "✅ OneDrive paused" -ForegroundColor Green
            return $true
        } else {
            Write-Host "⚠️  OneDrive not running" -ForegroundColor Yellow
            return $false
        }
    } catch {
        Write-Host "⚠️  Could not pause OneDrive: $_" -ForegroundColor Yellow
        return $false
    }
}

# Function to resume OneDrive
function Resume-OneDrive {
    param([bool]$WasPaused)
    
    if ($WasPaused) {
        Write-Host "`n▶️  Resuming OneDrive sync..." -ForegroundColor Yellow
        try {
            & "$env:LOCALAPPDATA\Microsoft\OneDrive\OneDrive.exe" /resume
            Write-Host "✅ OneDrive resumed" -ForegroundColor Green
        } catch {
            Write-Host "⚠️  Could not resume OneDrive: $_" -ForegroundColor Yellow
            Write-Host "   Please resume OneDrive manually from system tray" -ForegroundColor Yellow
        }
    }
}

# Function to delete build directory
function Remove-BuildDirectory {
    Write-Host "`n🗑️  Deleting build directory..." -ForegroundColor Yellow
    
    if (Test-Path $buildDir) {
        try {
            Remove-Item -Path $buildDir -Recurse -Force -ErrorAction Stop
            Write-Host "✅ Build directory deleted" -ForegroundColor Green
            return $true
        } catch {
            Write-Host "❌ Failed to delete build directory: $_" -ForegroundColor Red
            Write-Host "   Files may still be locked. Wait a moment and try again." -ForegroundColor Yellow
            return $false
        }
    } else {
        Write-Host "ℹ️  Build directory doesn't exist (already clean)" -ForegroundColor Cyan
        return $true
    }
}

# Main execution
try {
    # Step 1: Pause OneDrive
    $oneDrivePaused = Pause-OneDrive
    Start-Sleep -Seconds 2
    
    # Step 2: Delete build directory
    $buildDeleted = Remove-BuildDirectory
    
    if ($buildDeleted) {
        # Step 3: Build APK
        Write-Host "`n🔨 Building APK..." -ForegroundColor Cyan
        Write-Host "============================================================`n" -ForegroundColor Cyan
        
        Set-Location $projectDir
        python "$PSScriptRoot\build_apk.py" debug
        
        $buildSuccess = $LASTEXITCODE -eq 0
        
        if ($buildSuccess) {
            Write-Host "`n✅ Build completed successfully!" -ForegroundColor Green
        } else {
            Write-Host "`n❌ Build failed. Check errors above." -ForegroundColor Red
        }
    } else {
        Write-Host "`n❌ Cannot proceed with build - build directory could not be deleted" -ForegroundColor Red
    }
    
} finally {
    # Step 4: Always resume OneDrive
    Resume-OneDrive -WasPaused $oneDrivePaused
}

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "Script completed. Press any key to exit..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
