"""
NHS Medication Name Index Builder

This script creates a lightweight index of medication names and URLs only.
No detailed data is stored - just names for searching.
"""

import json
import requests
from bs4 import BeautifulSoup
import time
from pathlib import Path
import re

# Paths
SCRIPT_DIR = Path(__file__).parent
MEDICATIONS_FILE = SCRIPT_DIR.parent / "app" / "src" / "main" / "assets" / "nhs_medications.json"

# NHS URLs
NHS_BASE_URL = "https://www.nhs.uk"
NHS_MEDICINES_URL = f"{NHS_BASE_URL}/medicines/"

# Request headers
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
}

def get_medication_list():
    """
    Scrape the main medicines A-Z page to get all medication names and URLs
    """
    print(f"📡 Fetching medication list from {NHS_MEDICINES_URL}...")
    
    try:
        response = requests.get(NHS_MEDICINES_URL, headers=HEADERS, timeout=30)
        response.raise_for_status()
        
        soup = BeautifulSoup(response.text, 'html.parser')
        medications = []
        
        # Find all medication links
        med_items = soup.find_all('li', class_='nhsuk-list-panel__item')
        
        if not med_items:
            print("⚠️  Trying alternative structure...")
            med_items = soup.find_all('a', href=re.compile(r'/medicines/[a-z]'))
        
        for item in med_items:
            link = item.find('a') if item.name != 'a' else item
            
            if link and link.get('href'):
                name = link.get_text().strip()
                url = link['href']
                
                # Make URL absolute
                if not url.startswith('http'):
                    url = NHS_BASE_URL + url
                
                medications.append({
                    'name': name,
                    'url': url
                })
        
        print(f"✅ Found {len(medications)} medications")
        return medications
        
    except Exception as e:
        print(f"❌ Error fetching medication list: {e}")
        return []

def save_medications(medications):
    """Save lightweight medication index to JSON"""
    # Ensure directory exists
    MEDICATIONS_FILE.parent.mkdir(parents=True, exist_ok=True)
    
    with open(MEDICATIONS_FILE, 'w', encoding='utf-8') as f:
        json.dump(medications, f, indent=2, ensure_ascii=False)
    
    file_size = MEDICATIONS_FILE.stat().st_size / 1024  # KB
    print(f"\n💾 Saved {len(medications)} medication names to:")
    print(f"   {MEDICATIONS_FILE}")
    print(f"   File size: {file_size:.1f} KB (lightweight!)")

def main():
    print("=" * 80)
    print("NHS Medication Name Index Builder")
    print("=" * 80)
    print("\nThis creates a lightweight index with just medication names and URLs.")
    print("When users select a medication, the app will open their browser to NHS.\n")
    
    # Get medication list
    medications = get_medication_list()
    
    if not medications:
        print("\n❌ No medications found")
        return
    
    # Show sample
    print(f"\n📋 Sample medications:")
    for med in medications[:5]:
        print(f"   • {med['name']}")
        print(f"     → {med['url']}")
    print(f"   ... and {len(medications) - 5} more")
    
    # Save
    save_medications(medications)
    
    print("\n✅ Done! Rebuild your Android app to use the lightweight index.")
    print("💡 Users will see medication names, and tapping opens NHS website in browser.")

if __name__ == "__main__":
    main()
