"""
NHS Medication Database Builder

This script helps you build and expand the local NHS medication database.
You can add more medications manually or scrape from NHS website.

Usage:
    python build_medication_db.py --add "Medication Name"
    python build_medication_db.py --list
    python build_medication_db.py --validate
"""

import json
import os
from pathlib import Path
from typing import List, Dict, Optional

# Path to the medication JSON file
MEDICATIONS_FILE = Path(__file__).parent.parent / "app" / "src" / "main" / "assets" / "nhs_medications.json"

def load_medications() -> List[Dict]:
    """Load existing medications from JSON file"""
    if not MEDICATIONS_FILE.exists():
        print(f"Error: File not found: {MEDICATIONS_FILE}")
        return []
    
    with open(MEDICATIONS_FILE, 'r', encoding='utf-8') as f:
        return json.load(f)

def save_medications(medications: List[Dict]):
    """Save medications to JSON file"""
    MEDICATIONS_FILE.parent.mkdir(parents=True, exist_ok=True)
    
    with open(MEDICATIONS_FILE, 'w', encoding='utf-8') as f:
        json.dump(medications, f, indent=2, ensure_ascii=False)
    
    print(f"✓ Saved {len(medications)} medications to {MEDICATIONS_FILE}")

def add_medication():
    """Interactively add a new medication"""
    print("\n=== Add New Medication ===\n")
    
    medications = load_medications()
    
    # Get medication details from user
    name = input("Medication name: ").strip()
    if not name:
        print("Error: Name is required")
        return
    
    # Check if already exists
    if any(m['name'].lower() == name.lower() for m in medications):
        print(f"Warning: Medication '{name}' already exists!")
        return
    
    generic_name = input("Generic name (press Enter if same as name): ").strip() or name
    
    brand_names_input = input("Brand names (comma-separated): ").strip()
    brand_names = [b.strip() for b in brand_names_input.split(",")] if brand_names_input else []
    
    description = input("Description: ").strip()
    dosage_info = input("Dosage information: ").strip()
    
    print("\nSide effects (one per line, empty line to finish):")
    side_effects = []
    while True:
        effect = input("  - ").strip()
        if not effect:
            break
        side_effects.append(effect)
    
    print("\nWarnings (one per line, empty line to finish):")
    warnings = []
    while True:
        warning = input("  - ").strip()
        if not warning:
            break
        warnings.append(warning)
    
    # Create medication object
    medication = {
        "name": name,
        "genericName": generic_name,
        "brandNames": brand_names,
        "description": description,
        "dosageInfo": dosage_info,
        "sideEffects": side_effects,
        "warnings": warnings
    }
    
    # Add and save
    medications.append(medication)
    medications.sort(key=lambda x: x['name'])  # Keep alphabetically sorted
    
    save_medications(medications)
    print(f"\n✓ Added '{name}' to database!")

def list_medications():
    """List all medications in the database"""
    medications = load_medications()
    
    print(f"\n=== Medication Database ({len(medications)} total) ===\n")
    
    for i, med in enumerate(medications, 1):
        brands = f" [{', '.join(med.get('brandNames', []))}]" if med.get('brandNames') else ""
        print(f"{i:3}. {med['name']}{brands}")
    
    print()

def validate_database():
    """Validate the medication database"""
    medications = load_medications()
    
    print("\n=== Validating Database ===\n")
    
    errors = []
    warnings = []
    
    required_fields = ['name', 'genericName', 'description', 'dosageInfo', 'sideEffects', 'warnings']
    
    for i, med in enumerate(medications):
        # Check required fields
        for field in required_fields:
            if field not in med:
                errors.append(f"Medication {i+1} ({med.get('name', 'Unknown')}): Missing field '{field}'")
        
        # Check for empty fields
        if med.get('name', '').strip() == '':
            errors.append(f"Medication {i+1}: Empty name")
        
        if not med.get('sideEffects') or len(med.get('sideEffects', [])) == 0:
            warnings.append(f"{med.get('name')}: No side effects listed")
        
        if not med.get('warnings') or len(med.get('warnings', [])) == 0:
            warnings.append(f"{med.get('name')}: No warnings listed")
    
    # Check for duplicates
    names = [m['name'].lower() for m in medications]
    duplicates = [name for name in names if names.count(name) > 1]
    if duplicates:
        errors.append(f"Duplicate medications: {', '.join(set(duplicates))}")
    
    # Report
    if errors:
        print("❌ ERRORS:")
        for error in errors:
            print(f"  - {error}")
    
    if warnings:
        print("\n⚠️  WARNINGS:")
        for warning in warnings:
            print(f"  - {warning}")
    
    if not errors and not warnings:
        print("✓ Database is valid!")
    
    print(f"\nTotal medications: {len(medications)}")
    print()

def search_medication():
    """Search for medications"""
    medications = load_medications()
    
    query = input("\nSearch medication: ").strip().lower()
    
    results = [
        m for m in medications
        if query in m['name'].lower() or
           query in m.get('genericName', '').lower() or
           any(query in b.lower() for b in m.get('brandNames', []))
    ]
    
    if not results:
        print(f"\n❌ No medications found matching '{query}'")
        return
    
    print(f"\n=== Found {len(results)} medication(s) ===\n")
    
    for med in results:
        print(f"Name: {med['name']}")
        print(f"Generic: {med.get('genericName', 'N/A')}")
        if med.get('brandNames'):
            print(f"Brands: {', '.join(med['brandNames'])}")
        print(f"Description: {med.get('description', 'N/A')[:100]}...")
        print()

def main():
    """Main menu"""
    while True:
        print("\n=== NHS Medication Database Builder ===")
        print("1. List all medications")
        print("2. Add new medication")
        print("3. Search medications")
        print("4. Validate database")
        print("5. Exit")
        
        choice = input("\nChoice: ").strip()
        
        if choice == '1':
            list_medications()
        elif choice == '2':
            add_medication()
        elif choice == '3':
            search_medication()
        elif choice == '4':
            validate_database()
        elif choice == '5':
            print("Goodbye!")
            break
        else:
            print("Invalid choice!")

if __name__ == '__main__':
    main()
