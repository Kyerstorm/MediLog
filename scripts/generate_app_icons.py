#!/usr/bin/env python3
"""
Generate Android app icons from a source image.
Creates all required mipmap directories and icon sizes.
"""

import os
import sys
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("❌ Pillow library not found!")
    print("📦 Installing Pillow...")
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "Pillow"])
    from PIL import Image

# Icon sizes for different densities
ICON_SIZES = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

def create_app_icons(source_image_path, output_base_path):
    """
    Generate app icons from source image.
    
    Args:
        source_image_path: Path to the source image (JPEG/PNG)
        output_base_path: Base path for app/src/main/res directory
    """
    print("=" * 60)
    print("       Android App Icon Generator")
    print("=" * 60)
    print()
    
    # Validate source image
    if not os.path.exists(source_image_path):
        print(f"❌ Source image not found: {source_image_path}")
        return False
    
    print(f"📁 Source image: {source_image_path}")
    
    # Open and validate source image
    try:
        source_img = Image.open(source_image_path)
        print(f"✅ Image loaded: {source_img.size[0]}x{source_img.size[1]} pixels")
        
        # Convert to RGBA if necessary
        if source_img.mode != 'RGBA':
            print(f"🔄 Converting from {source_img.mode} to RGBA...")
            source_img = source_img.convert('RGBA')
        
    except Exception as e:
        print(f"❌ Failed to open image: {e}")
        return False
    
    print()
    print("📦 Generating icon sizes...")
    print()
    
    # Create each icon size
    success_count = 0
    for folder_name, size in ICON_SIZES.items():
        try:
            # Create mipmap directory
            output_dir = os.path.join(output_base_path, folder_name)
            os.makedirs(output_dir, exist_ok=True)
            
            # Resize image (using LANCZOS for high quality)
            resized_img = source_img.resize((size, size), Image.Resampling.LANCZOS)
            
            # Save as PNG
            output_path = os.path.join(output_dir, 'ic_launcher.png')
            resized_img.save(output_path, 'PNG', optimize=True)
            
            # Also create round version (same for now, can be customized)
            round_output_path = os.path.join(output_dir, 'ic_launcher_round.png')
            resized_img.save(round_output_path, 'PNG', optimize=True)
            
            file_size = os.path.getsize(output_path) / 1024
            print(f"  ✅ {folder_name:20s} → {size}x{size}px ({file_size:.1f} KB)")
            success_count += 1
            
        except Exception as e:
            print(f"  ❌ {folder_name:20s} → Failed: {e}")
    
    print()
    print("=" * 60)
    if success_count == len(ICON_SIZES):
        print(f"✅ All {success_count} icon sizes generated successfully!")
        print()
        print("📝 Icons created:")
        print("   - ic_launcher.png (standard icon)")
        print("   - ic_launcher_round.png (round icon for some launchers)")
        print()
        print("🔄 Next steps:")
        print("   1. Rebuild your app to use the new icons")
        print("   2. Test on device/emulator")
        print("   3. Check app drawer and home screen appearance")
    else:
        print(f"⚠️  Generated {success_count}/{len(ICON_SIZES)} icon sizes")
    print("=" * 60)
    
    return success_count == len(ICON_SIZES)


def main():
    # Get script directory and project root
    script_dir = Path(__file__).parent
    project_root = script_dir.parent
    
    # Paths
    source_image = project_root / "app_icon.jpeg"
    res_dir = project_root / "app" / "src" / "main" / "res"
    
    print(f"🔍 Looking for icon: {source_image}")
    print(f"📂 Output directory: {res_dir}")
    print()
    
    if not source_image.exists():
        print("❌ app_icon.jpeg not found in project root!")
        print(f"   Expected location: {source_image}")
        print()
        print("💡 Please place your icon file at the project root and name it 'app_icon.jpeg'")
        return 1
    
    # Generate icons
    success = create_app_icons(str(source_image), str(res_dir))
    
    return 0 if success else 1


if __name__ == "__main__":
    sys.exit(main())
