import json

with open('app/src/main/assets/nhs_medications.json', encoding='utf-8') as f:
    data = json.load(f)

# Find Mesalazine
mes = [m for m in data if 'Mesalazine' == m['name']][0]

print('=' * 80)
print('MESALAZINE - Enhanced Data')
print('=' * 80)

print(f'\n📌 Name: {mes["name"]}')
print(f'🏷️  Generic: {mes.get("genericName", "N/A")}')
print(f'🏭 Brand Names: {", ".join(mes.get("brandNames", []))}')

print(f'\n📖 Description:')
print(f'   {mes.get("description", "N/A")}')

print(f'\n💊 Dosage Info:')
dosage = mes.get("dosageInfo", "N/A")
print(f'   {dosage[:300]}...' if len(dosage) > 300 else f'   {dosage}')

key_facts_count = len(mes.get("keyFacts", []))
print(f'\n✅ Key Facts ({key_facts_count}):')
for i, fact in enumerate(mes.get("keyFacts", []), 1):
    print(f'   {i}. {fact}')

common_se = mes.get("commonSideEffects", [])
print(f'\n⚠️  Common Side Effects ({len(common_se)}):')
for se in common_se:
    print(f'   - {se}')

serious_se = mes.get("seriousSideEffects", [])
print(f'\n🚨 Serious Side Effects ({len(serious_se)}):')
for se in serious_se:
    print(f'   - {se}')

warnings = mes.get("warnings", [])
print(f'\n⚡ Warnings ({len(warnings)}):')
for w in warnings:
    print(f'   - {w[:200]}...' if len(w) > 200 else f'   - {w}')

print(f'\n{"=" * 80}')
print('Database Statistics')
print('=' * 80)
print(f'Total medications: {len(data)}')

# Count medications with enhanced data
with_key_facts = sum(1 for m in data if m.get("keyFacts"))
with_common_se = sum(1 for m in data if m.get("commonSideEffects"))
with_serious_se = sum(1 for m in data if m.get("seriousSideEffects"))
with_warnings = sum(1 for m in data if m.get("warnings"))

print(f'With Key Facts: {with_key_facts}')
print(f'With Common Side Effects: {with_common_se}')
print(f'With Serious Side Effects: {with_serious_se}')
print(f'With Warnings: {with_warnings}')
