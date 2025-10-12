#!/usr/bin/env python3
"""
Build APK Script for Health Calendar App
Automates the Android APK build process using Gradle

Usage:
    python build_apk.py                  # Interactive mode
    python build_apk.py debug            # Build debug APK (no clean)
    python build_apk.py debug --clean    # Build debug APK with clean
    python build_apk.py release          # Build release APK (no clean)
    python build_apk.py release --clean  # Build release APK with clean
    python build_apk.py both             # Build both debug and release

Features:
    - Automatic ADB installation after successful build (if device connected)
    - OneDrive-safe build process
    - Real-time build output
    - APK size reporting
"""

import subprocess
import sys
import os
import argparse
from pathlib import Path
from datetime import datetime

# Colors for terminal output
class Colors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

def print_header(message):
    """Print a formatted header message"""
    print(f"\n{Colors.HEADER}{Colors.BOLD}{'=' * 60}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{message.center(60)}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{'=' * 60}{Colors.ENDC}\n")

def print_success(message):
    """Print a success message"""
    print(f"{Colors.OKGREEN}✅ {message}{Colors.ENDC}")

def print_error(message):
    """Print an error message"""
    print(f"{Colors.FAIL}❌ {message}{Colors.ENDC}")

def print_info(message):
    """Print an info message"""
    print(f"{Colors.OKCYAN}ℹ️  {message}{Colors.ENDC}")

def print_warning(message):
    """Print a warning message"""
    print(f"{Colors.WARNING}⚠️  {message}{Colors.ENDC}")

def check_adb_device():
    """Check if ADB device is connected"""
    try:
        result = subprocess.run(
            ["adb", "devices"],
            capture_output=True,
            text=True,
            check=True
        )
        # Parse output to see if any devices are connected
        lines = result.stdout.strip().split('\n')
        devices = [line for line in lines[1:] if line.strip() and 'device' in line]
        return len(devices) > 0
    except Exception:
        return False

def install_apk_via_adb(apk_path):
    """Install APK on connected device via ADB"""
    if not check_adb_device():
        print_warning("No ADB device connected - skipping installation")
        print_info("Connect a device and run: adb install -r app\\build\\outputs\\apk\\debug\\app-debug.apk")
        return False
    
    print_header("Installing APK via ADB")
    print_info(f"Installing: {apk_path}")
    
    try:
        result = subprocess.run(
            ["adb", "install", "-r", str(apk_path)],
            capture_output=True,
            text=True,
            check=True
        )
        
        if "Success" in result.stdout:
            print_success("APK installed successfully on device!")
            return True
        else:
            print_error(f"Installation failed: {result.stdout}")
            return False
    except subprocess.CalledProcessError as e:
        print_error(f"ADB installation failed: {e.stderr}")
        return False
    except Exception as e:
        print_error(f"Error during installation: {e}")
        return False

def check_java_installation():
    """Check if Java is installed and accessible"""
    print_info("Checking Java installation...")
    try:
        result = subprocess.run(
            ["java", "-version"],
            capture_output=True,
            text=True,
            shell=True
        )
        if result.returncode == 0:
            # Java version is in stderr for some reason
            version_info = result.stderr.split('\n')[0]
            print_success(f"Java found: {version_info}")
            return True
        else:
            print_error("Java not found or not accessible")
            return False
    except Exception as e:
        print_error(f"Error checking Java: {e}")
        return False

def clean_project():
    """Run Gradle clean"""
    print_header("Cleaning Project")
    print_info("Running: gradlew clean")
    
    try:
        result = subprocess.run(
            ["gradlew.bat", "clean"],
            shell=True,
            capture_output=True,
            text=True
        )
        
        if result.returncode == 0:
            print_success("Project cleaned successfully")
            return True
        else:
            print_error("Clean failed")
            print(result.stdout)
            print(result.stderr)
            return False
    except Exception as e:
        print_error(f"Error during clean: {e}")
        return False

def build_debug_apk():
    """Build debug APK"""
    print_header("Building Debug APK")
    print_info("Running: gradlew assembleDebug")
    print_info("This may take a few minutes...")
    
    try:
        # Run the build process with real-time output
        process = subprocess.Popen(
            ["gradlew.bat", "assembleDebug"],
            shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
            universal_newlines=True
        )
        
        # Print output in real-time
        for line in process.stdout:
            print(line, end='')
        
        process.wait()
        
        if process.returncode == 0:
            print_success("Debug APK built successfully!")
            
            # Auto-install via ADB if device is connected
            apk_path = Path("app/build/outputs/apk/debug/app-debug.apk")
            if apk_path.exists():
                install_apk_via_adb(apk_path)
            
            return True
        else:
            print_error("Build failed")
            return False
    except Exception as e:
        print_error(f"Error during build: {e}")
        return False

def build_release_apk():
    """Build release APK (unsigned)"""
    print_header("Building Release APK")
    print_info("Running: gradlew assembleRelease")
    print_warning("Note: This will be an unsigned release APK")
    print_info("This may take a few minutes...")
    
    try:
        # Run the build process with real-time output
        process = subprocess.Popen(
            ["gradlew.bat", "assembleRelease"],
            shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
            universal_newlines=True
        )
        
        # Print output in real-time
        for line in process.stdout:
            print(line, end='')
        
        process.wait()
        
        if process.returncode == 0:
            print_success("Release APK built successfully!")
            
            # Auto-install via ADB if device is connected
            apk_path = Path("app/build/outputs/apk/release/app-release-unsigned.apk")
            if apk_path.exists():
                install_apk_via_adb(apk_path)
            
            return True
        else:
            print_error("Build failed")
            return False
    except Exception as e:
        print_error(f"Error during build: {e}")
        return False

def find_apk_files():
    """Find all APK files in the build directory"""
    print_header("Locating APK Files")
    
    apk_dir = Path("app/build/outputs/apk")
    
    if not apk_dir.exists():
        print_error("APK output directory not found")
        return []
    
    apk_files = list(apk_dir.rglob("*.apk"))
    
    if apk_files:
        print_success(f"Found {len(apk_files)} APK file(s):")
        for apk in apk_files:
            size_mb = apk.stat().st_size / (1024 * 1024)
            print(f"\n  📦 {Colors.BOLD}{apk.name}{Colors.ENDC}")
            print(f"     Location: {apk}")
            print(f"     Size: {size_mb:.2f} MB")
    else:
        print_warning("No APK files found")
    
    return apk_files

def copy_apk_to_root(apk_files):
    """Copy APK files to root directory with timestamp"""
    if not apk_files:
        return
    
    print_header("Copying APK to Root Directory")
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    
    for apk in apk_files:
        # Create new filename with timestamp
        build_type = "debug" if "debug" in apk.name.lower() else "release"
        new_name = f"HealthCalendar_{build_type}_{timestamp}.apk"
        destination = Path(new_name)
        
        try:
            import shutil
            shutil.copy2(apk, destination)
            print_success(f"Copied to: {destination}")
        except Exception as e:
            print_error(f"Error copying APK: {e}")

def main():
    """Main build script"""
    # Parse command line arguments
    parser = argparse.ArgumentParser(description='Build Health Calendar APK')
    parser.add_argument('build_type', nargs='?', choices=['debug', 'release', 'both'], 
                        help='Build type: debug, release, or both')
    parser.add_argument('--clean', action='store_true', help='Clean before building')
    parser.add_argument('--copy', action='store_true', help='Copy APK to root directory')
    
    args = parser.parse_args()
    
    print_header("Health Calendar APK Builder")
    print_info(f"Build started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    # Check current directory
    if not Path("gradlew.bat").exists():
        print_error("gradlew.bat not found. Please run this script from the project root.")
        sys.exit(1)
    
    # Check Java installation
    if not check_java_installation():
        print_error("Java is required to build Android apps.")
        print_info("Please install Java JDK 11 or later.")
        sys.exit(1)
    
    # Determine build mode
    if args.build_type:
        # Non-interactive mode
        choice = args.build_type
        clean_first = args.clean
        copy_apk = args.copy
    else:
        # Interactive mode
        print("\nBuild Options:")
        print("  1. Debug APK (faster, for testing)")
        print("  2. Release APK (optimized, unsigned)")
        print("  3. Both Debug and Release")
        print("  4. Clean only (no build)")
        
        choice_input = input(f"\n{Colors.OKCYAN}Select option (1-4): {Colors.ENDC}").strip()
        
        # Map choice to build type
        choice_map = {"1": "debug", "2": "release", "3": "both", "4": "clean"}
        choice = choice_map.get(choice_input)
        
        if not choice:
            print_error("Invalid choice")
            sys.exit(1)
        
        clean_first = input(f"{Colors.OKCYAN}Clean project before building? (y/n): {Colors.ENDC}").strip().lower() == 'y'
        copy_apk = False  # Will ask later if APKs are built
    
    # Clean if requested
    if clean_first:
        if not clean_project():
            print_warning("Clean failed, continuing with build anyway...")
    
    # Handle build choice
    success = False
    
    if choice == "debug":
        success = build_debug_apk()
    elif choice == "release":
        success = build_release_apk()
    elif choice == "both":
        success = build_debug_apk()
        if success:
            success = build_release_apk()
    elif choice == "clean":
        print_info("Clean only - no build performed")
        success = True
    else:
        print_error("Invalid choice")
        sys.exit(1)
    
    # Find and display APK files
    if success and choice != "clean":
        apk_files = find_apk_files()
        
        # Ask if user wants to copy to root (interactive mode only)
        if apk_files and not args.build_type:
            copy_input = input(f"\n{Colors.OKCYAN}Copy APK(s) to root directory? (y/n): {Colors.ENDC}").strip().lower()
            copy_apk = copy_input == 'y'
        
        if copy_apk and apk_files:
            copy_apk_to_root(apk_files)
    
    # Final summary
    print_header("Build Summary")
    if success:
        print_success("Build completed successfully! 🎉")
        print_info("\nNext steps:")
        print("  1. Install APK on your Android device/emulator")
        print("  2. Test all three fixes:")
        print("     - Responsive tablet/foldable UI")
        print("     - Calendar '+' button (add appointments)")
        print("     - Document scanner with save dialog")
    else:
        print_error("Build failed. Check the error messages above.")
        sys.exit(1)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print(f"\n\n{Colors.WARNING}Build cancelled by user{Colors.ENDC}")
        sys.exit(1)
    except Exception as e:
        print_error(f"Unexpected error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
