import json

with open('app/src/main/assets/nhs_medications.json', encoding='utf-8') as f:
    data = json.load(f)

print(f'✅ Total medications in database: {len(data)}\n')

print('First 10 medications:')
for i, med in enumerate(data[:10], 1):
    print(f'  {i:3}. {med["name"]}')

print('\nLast 10 medications:')
for i, med in enumerate(data[-10:], len(data)-9):
    print(f'  {i:3}. {med["name"]}')

print(f'\n📊 Sample medication details:')
sample = data[190]  # Mesalazine
print(f'\nName: {sample["name"]}')
print(f'Generic: {sample.get("genericName", "N/A")}')
print(f'Brands: {", ".join(sample.get("brandNames", []))}')
print(f'Description: {sample["description"][:100]}...')
print(f'Side Effects: {len(sample["sideEffects"])} listed')
print(f'Warnings: {len(sample["warnings"])} listed')
