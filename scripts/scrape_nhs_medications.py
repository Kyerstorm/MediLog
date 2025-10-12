"""
NHS Medication Web Scraper

This script scrapes medication information from the NHS website
and builds a comprehensive local JSON database.

Usage:
    pip install requests beautifulsoup4
    python scrape_nhs_medications.py
"""

import json
import requests
from bs4 import BeautifulSoup
import time
from pathlib import Path
from typing import List, Dict, Optional
import re

# Paths
SCRIPT_DIR = Path(__file__).parent
MEDICATIONS_FILE = SCRIPT_DIR.parent / "app" / "src" / "main" / "assets" / "nhs_medications.json"

# NHS URLs
NHS_BASE_URL = "https://www.nhs.uk"
NHS_MEDICINES_URL = f"{NHS_BASE_URL}/medicines/"

# Request headers to mimic browser
HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
    'Accept-Language': 'en-GB,en;q=0.5',
}

def get_medication_list() -> List[Dict[str, str]]:
    """
    Scrape the main medicines A-Z page to get all medication names and URLs
    """
    print(f"📡 Fetching medication list from {NHS_MEDICINES_URL}...")
    
    try:
        response = requests.get(NHS_MEDICINES_URL, headers=HEADERS, timeout=30)
        response.raise_for_status()
        
        soup = BeautifulSoup(response.text, 'html.parser')
        medications = []
        
        # Find all medication links in the A-Z list
        # NHS uses <li class="nhsuk-list-panel__item"> for each medication
        med_items = soup.find_all('li', class_='nhsuk-list-panel__item')
        
        if not med_items:
            print("⚠️  No medications found with standard structure, trying alternative...")
            # Try alternative structure - links in the main content
            med_items = soup.find_all('a', href=re.compile(r'/medicines/[a-z]'))
        
        for item in med_items:
            link = item.find('a') if item.name != 'a' else item
            
            if link and link.get('href'):
                name = link.get_text().strip()
                url = link['href']
                
                # Make URL absolute if needed
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

def scrape_medication_details(name: str, url: str) -> Optional[Dict]:
    """
    Scrape detailed information for a specific medication
    """
    print(f"  📄 Scraping: {name}...")
    
    try:
        response = requests.get(url, headers=HEADERS, timeout=30)
        response.raise_for_status()
        
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Extract medication information
        medication = {
            'name': name,
            'genericName': name,  # Will try to find actual generic name
            'brandNames': [],
            'description': '',
            'dosageInfo': '',
            'sideEffects': [],
            'warnings': []
        }
        
        # Get description (usually the first paragraph or intro text)
        intro = soup.find('p', class_='nhsuk-body-l')
        if intro:
            medication['description'] = intro.get_text().strip()
        else:
            # Fallback to first paragraph
            first_p = soup.find('p')
            if first_p:
                medication['description'] = first_p.get_text().strip()
        
        # Try to find brand names in the content
        content_text = soup.get_text()
        brand_pattern = r'brand name[s]?[:\s]+([A-Z][a-z]+(?:,?\s+[A-Z][a-z]+)*)'
        brand_match = re.search(brand_pattern, content_text, re.IGNORECASE)
        if brand_match:
            brands = brand_match.group(1)
            medication['brandNames'] = [b.strip() for b in brands.split(',')]
        
        # Look for sections by heading
        sections = soup.find_all(['h2', 'h3'])
        
        for section in sections:
            section_title = section.get_text().lower().strip()
            
            # Find content after this heading (up to next heading)
            content_elements = []
            sibling = section.find_next_sibling()
            while sibling and sibling.name not in ['h2', 'h3']:
                content_elements.append(sibling)
                sibling = sibling.find_next_sibling()
            
            # Extract dosage information
            if 'dose' in section_title or 'how to take' in section_title or 'how and when' in section_title:
                dosage_parts = []
                for elem in content_elements:
                    if elem.name == 'p':
                        dosage_parts.append(elem.get_text().strip())
                medication['dosageInfo'] = ' '.join(dosage_parts)[:500]  # Limit length
            
            # Extract side effects
            elif 'side effect' in section_title:
                for elem in content_elements:
                    if elem.name == 'ul':
                        items = elem.find_all('li')
                        medication['sideEffects'].extend([li.get_text().strip() for li in items[:10]])  # Limit to 10
            
            # Extract warnings
            elif 'warning' in section_title or 'caution' in section_title or 'who can' in section_title:
                for elem in content_elements:
                    if elem.name == 'ul':
                        items = elem.find_all('li')
                        medication['warnings'].extend([li.get_text().strip() for li in items[:10]])
                    elif elem.name == 'p':
                        text = elem.get_text().strip()
                        if len(text) < 200:  # Only short warnings
                            medication['warnings'].append(text)
        
        # Ensure we have at least some data
        if not medication['description']:
            medication['description'] = f"{name} is used to treat various conditions. See NHS website for full details."
        
        if not medication['dosageInfo']:
            medication['dosageInfo'] = "Dosage varies. Always follow your doctor's instructions."
        
        return medication
        
    except Exception as e:
        print(f"    ❌ Error scraping {name}: {e}")
        return None

def scrape_all_medications(limit: Optional[int] = None) -> List[Dict]:
    """
    Scrape all medications from NHS website
    
    Args:
        limit: Maximum number of medications to scrape (for testing)
    """
    # Get list of all medications
    med_list = get_medication_list()
    
    if not med_list:
        print("❌ No medications found to scrape")
        return []
    
    if limit:
        med_list = med_list[:limit]
        print(f"⚠️  Limiting to {limit} medications for testing")
    
    print(f"\n🔄 Starting to scrape {len(med_list)} medications...\n")
    
    medications = []
    failed = []
    
    for i, med_info in enumerate(med_list, 1):
        print(f"[{i}/{len(med_list)}] ", end='')
        
        med_data = scrape_medication_details(med_info['name'], med_info['url'])
        
        if med_data:
            medications.append(med_data)
        else:
            failed.append(med_info['name'])
        
        # Be polite - don't hammer the server
        time.sleep(1)  # 1 second delay between requests
        
        # Progress update every 10 medications
        if i % 10 == 0:
            print(f"\n  ✅ Progress: {i}/{len(med_list)} | Success: {len(medications)} | Failed: {len(failed)}\n")
    
    print(f"\n✅ Scraping complete!")
    print(f"  - Successfully scraped: {len(medications)}")
    print(f"  - Failed: {len(failed)}")
    
    if failed:
        print(f"\n❌ Failed medications: {', '.join(failed)}")
    
    return medications

def save_medications(medications: List[Dict]):
    """Save medications to JSON file"""
    MEDICATIONS_FILE.parent.mkdir(parents=True, exist_ok=True)
    
    # Sort alphabetically
    medications.sort(key=lambda x: x['name'].lower())
    
    with open(MEDICATIONS_FILE, 'w', encoding='utf-8') as f:
        json.dump(medications, f, indent=2, ensure_ascii=False)
    
    print(f"\n💾 Saved {len(medications)} medications to:")
    print(f"   {MEDICATIONS_FILE}")

def test_scraper():
    """Test the scraper with just a few medications"""
    print("🧪 Testing scraper with 3 medications...\n")
    
    medications = scrape_all_medications(limit=3)
    
    if medications:
        print("\n📋 Sample data:")
        for med in medications:
            print(f"\n  {med['name']}")
            print(f"    Description: {med['description'][:100]}...")
            print(f"    Side Effects: {len(med['sideEffects'])} items")
            print(f"    Warnings: {len(med['warnings'])} items")
    
    return medications

def main():
    """Main function"""
    print("=" * 70)
    print("NHS Medication Database Scraper")
    print("=" * 70)
    print()
    
    print("Options:")
    print("1. Test scraper (scrape 3 medications)")
    print("2. Scrape 10 medications")
    print("3. Scrape 50 medications")
    print("4. Scrape ALL medications (may take 30+ minutes)")
    print("5. Exit")
    print()
    
    choice = input("Choose option: ").strip()
    
    medications = []
    
    if choice == '1':
        medications = test_scraper()
    elif choice == '2':
        medications = scrape_all_medications(limit=10)
    elif choice == '3':
        medications = scrape_all_medications(limit=50)
    elif choice == '4':
        confirm = input("⚠️  This will take 30+ minutes. Continue? (yes/no): ").strip().lower()
        if confirm == 'yes':
            medications = scrape_all_medications()
        else:
            print("Cancelled.")
            return
    elif choice == '5':
        print("Goodbye!")
        return
    else:
        print("Invalid choice!")
        return
    
    if medications:
        save = input("\n💾 Save to database? (yes/no): ").strip().lower()
        if save == 'yes':
            save_medications(medications)
            print("\n✅ Database updated successfully!")
            print(f"\nNext steps:")
            print(f"1. Rebuild the app: .\\gradlew assembleDebug")
            print(f"2. Install the new APK")
            print(f"3. Test medication search in the app")
        else:
            print("Not saved.")

if __name__ == '__main__':
    main()
