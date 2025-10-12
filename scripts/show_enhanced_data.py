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

print(f'\n✅ Key Facts ({len(mes.get("keyFacts", []))}):')