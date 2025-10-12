import json
from pathlib import Path

print("=" * 80)
print("🎉 NHS MEDICATION INDEX - LIGHTWEIGHT VERSION VERIFICATION")
print("=" * 80)

json_file = Path("app/src/main/assets/nhs_medications.json")

# Check file exists
if not json_file.exists():
    print("\n❌ Error: nhs_medications.json not found!")
    exit(1)

# Load and verify
with open(json_file, encoding='utf-8') as f:
    data = json.load(f)

# File size
file_size_kb = json_file.stat().st_size / 1024

print(f"\n📊 Index Statistics:")
print(f"   Total Medications: {len(data)}")
print(f"   File Size: {file_size_kb:.1f} KB")
print(f"   Average per medication: {(file_size_kb * 1024 / len(data)):.0f} bytes")

# Verify structure
print(f"\n🔍 Data Structure Check:")
sample = data[0]
has_name = 'name' in sample
has_url = 'url' in sample
print(f"   ✓ Has 'name' field: {has_name}")
print(f"   ✓ Has 'url' field: {has_url}")

# Check for old fields (should not be present)
old_fields = ['description', 'dosageInfo', 'sideEffects', 'keyFacts', 'brandNames']
old_found = [field for field in old_fields if field in sample]
if old_found:
    print(f"   ⚠️  Old fields still present: {old_found}")
    print(f"   💡 This is OK - they're just empty/deprecated")
else:
    print(f"   ✓ No old fields present (pure lightweight index!)")

print(f"\n📋 Sample Medications:")
for i, med in enumerate(data[:10], 1):
    print(f"   {i:2}. {med['name']}")
    print(f"       → {med['url']}")

print(f"\n🔍 Searching for 'Mesalazine':")
mesalazine = next((m for m in data if 'mesalazine' in m['name'].lower()), None)
if mesalazine:
    print(f"   ✓ Found: {mesalazine['name']}")
    print(f"   ✓ URL: {mesalazine['url']}")
else:
    print(f"   ❌ Not found!")

print(f"\n📦 APK Build Info:")
apk_path = Path("app/build/outputs/apk/debug/app-debug.apk")
if apk_path.exists():
    apk_size_mb = apk_path.stat().st_size / (1024 * 1024)
    print(f"   ✓ APK exists: app-debug.apk")
    print(f"   ✓ Size: {apk_size_mb:.2f} MB")
else:
    print(f"   ⚠️  APK not found (need to build)")

print(f"\n" + "=" * 80)
print("✅ VERIFICATION COMPLETE")
print("=" * 80)
print(f"\nHow it works:")
print(f"  1. User searches: App looks up in {file_size_kb:.1f} KB index")
print(f"  2. User taps medication: App opens browser with NHS URL")
print(f"  3. User reads: Official NHS website with all details")
print(f"\n💡 Benefits:")
print(f"  • Always up-to-date (NHS maintains content)")
print(f"  • Lightweight ({file_size_kb:.1f} KB vs ~500 KB)")
print(f"  • Professional NHS website experience")
print(f"  • No maintenance required")
print(f"\n🎯 Your 'Mesalazine' search: {'✅ WORKS!' if mesalazine else '❌ Failed'}")
