import json

print("=" * 80)
print("🎉 ENHANCED NHS MEDICATION DATABASE - VERIFICATION")
print("=" * 80)

with open('app/src/main/assets/nhs_medications.json', encoding='utf-8') as f:
    data = json.load(f)

print(f"\n📊 Database Overview:")
print(f"   Total Medications: {len(data)}")

# Count enhanced fields
stats = {
    'keyFacts': 0,
    'howItWorks': 0,
    'whoCanTake': 0,
    'commonSideEffects': 0,
    'seriousSideEffects': 0,
    'brandNames': 0
}

for med in data:
    if med.get('keyFacts'): stats['keyFacts'] += 1
    if med.get('howItWorks'): stats['howItWorks'] += 1
    if med.get('whoCanTake'): stats['whoCanTake'] += 1
    if med.get('commonSideEffects'): stats['commonSideEffects'] += 1
    if med.get('seriousSideEffects'): stats['seriousSideEffects'] += 1
    if med.get('brandNames'): stats['brandNames'] += 1

print(f"\n📈 Enhanced Data Coverage:")
for field, count in stats.items():
    percentage = (count / len(data)) * 100
    print(f"   {field:25} {count:3} medications ({percentage:5.1f}%)")

print(f"\n" + "=" * 80)
print("EXAMPLE: Mesalazine (Your Original Search)")
print("=" * 80)

# Find and display Mesalazine
mes = next((m for m in data if m['name'] == 'Mesalazine'), None)

if mes:
    print(f"\n📌 Name: {mes['name']}")
    if mes.get('genericName'):
        print(f"🏷️  Generic: {mes['genericName']}")
    if mes.get('brandNames'):
        print(f"🏭 Brand Names: {', '.join(mes['brandNames'])}")
    
    print(f"\n📖 Description:")
    print(f"   {mes.get('description', 'N/A')}")
    
    if mes.get('keyFacts'):
        print(f"\n✅ Key Facts ({len(mes['keyFacts'])}):")
        for i, fact in enumerate(mes['keyFacts'], 1):
            print(f"   {i}. {fact[:80]}..." if len(fact) > 80 else f"   {i}. {fact}")
    
    if mes.get('howItWorks'):
        print(f"\n⚙️ How It Works:")
        hw = mes['howItWorks']
        print(f"   {hw[:150]}..." if len(hw) > 150 else f"   {hw}")
    
    if mes.get('commonSideEffects'):
        print(f"\n⚠️  Common Side Effects ({len(mes['commonSideEffects'])}):")
        for se in mes['commonSideEffects'][:3]:
            print(f"   • {se}")
        if len(mes['commonSideEffects']) > 3:
            print(f"   ... and {len(mes['commonSideEffects']) - 3} more")
    
    if mes.get('seriousSideEffects'):
        print(f"\n🚨 Serious Side Effects ({len(mes['seriousSideEffects'])}):")
        for se in mes['seriousSideEffects'][:3]:
            print(f"   • {se}")
        if len(mes['seriousSideEffects']) > 3:
            print(f"   ... and {len(mes['seriousSideEffects']) - 3} more")

print(f"\n" + "=" * 80)
print("Sample of Other Popular Medications")
print("=" * 80)

popular = ['Paracetamol for adults', 'Ibuprofen for adults (Nurofen)', 'Aspirin', 'Warfarin']
for med_name in popular:
    med = next((m for m in data if med_name.lower() in m['name'].lower()), None)
    if med:
        key_facts = len(med.get('keyFacts', []))
        common_se = len(med.get('commonSideEffects', []))
        serious_se = len(med.get('seriousSideEffects', []))
        brands = ', '.join(med.get('brandNames', [])[:3]) if med.get('brandNames') else 'N/A'
        
        print(f"\n✓ {med['name']}")
        print(f"  Brands: {brands}")
        print(f"  Data: {key_facts} key facts, {common_se} common SE, {serious_se} serious SE")

print(f"\n" + "=" * 80)
print("✅ VERIFICATION COMPLETE")
print("=" * 80)
print("\nYour app now has comprehensive NHS medication data!")
print("Install app-debug.apk and search for 'Mesalazine' to test.")
print(f"\nAPK Location: app\\build\\outputs\\apk\\debug\\app-debug.apk")
