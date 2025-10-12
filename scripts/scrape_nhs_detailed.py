"""
Enhanced NHS Medication Web Scraper

This script scrapes detailed medication information from NHS subpages:
- Main page: https://www.nhs.uk/medicines/{medication}/
- About page: https://www.nhs.uk/medicines/{medication}/about-{medication}/
- Side effects page: https://www.nhs.uk/medicines/{medication}/side-effects-of-{medication}/

Usage:
    pip install requests beautifulsoup4
    python scrape_nhs_detailed.py
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
        med_items = soup.find_all('li', class_='nhsuk-list-panel__item')
        
        if not med_items:
            print("⚠️  No medications found with standard structure, trying alternative...")
            med_items = soup.find_all('a', href=re.compile(r'/medicines/[a-z]'))
        
        for item in med_items:
            link = item.find('a') if item.name != 'a' else item
            
            if link and link.get('href'):
                name = link.get_text().strip()
                url = link['href']
                
                # Make URL absolute if needed
                if not url.startswith('http'):
                    url = NHS_BASE_URL + url
                
                # Extract slug from URL for building subpage URLs
                slug = url.rstrip('/').split('/')[-1]
                
                medications.append({
                    'name': name,
                    'url': url,
                    'slug': slug
                })
        
        print(f"✅ Found {len(medications)} medications")
        return medications
        
    except Exception as e:
        print(f"❌ Error fetching medication list: {e}")
        return []

def extract_text_content(element) -> str:
    """Extract clean text from an element, removing extra whitespace"""
    if not element:
        return ""
    text = element.get_text().strip()
    # Clean up multiple spaces and newlines
    text = re.sub(r'\s+', ' ', text)
    return text

def scrape_about_page(slug: str) -> Dict[str, any]:
    """
    Scrape the 'About' page for a medication
    Example: https://www.nhs.uk/medicines/aciclovir/about-aciclovir/
    """
    url = f"{NHS_MEDICINES_URL}{slug}/about-{slug}/"
    about_data = {
        'description': '',
        'genericName': '',
        'brandNames': [],
        'keyFacts': [],
        'howItWorks': '',
        'whoCanTake': [],
        'dosageInfo': ''
    }
    
    try:
        response = requests.get(url, headers=HEADERS, timeout=30)
        
        # If About page doesn't exist, try main page
        if response.status_code == 404:
            url = f"{NHS_MEDICINES_URL}{slug}/"
            response = requests.get(url, headers=HEADERS, timeout=30)
        
        response.raise_for_status()
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Get main description (intro paragraph)
        intro = soup.find('p', class_='nhsuk-body-l')
        if intro:
            about_data['description'] = extract_text_content(intro)
        
        # Look for key facts section
        key_facts_section = soup.find('h2', string=re.compile(r'Key facts', re.IGNORECASE))
        if key_facts_section:
            facts_list = key_facts_section.find_next('ul')
            if facts_list:
                facts = facts_list.find_all('li')
                about_data['keyFacts'] = [extract_text_content(fact) for fact in facts]
        
        # Look for "Who can and cannot take" section
        who_can_section = soup.find(['h2', 'h3'], string=re.compile(r'Who can.*take', re.IGNORECASE))
        if who_can_section:
            content = []
            sibling = who_can_section.find_next_sibling()
            while sibling and sibling.name not in ['h2', 'h3']:
                if sibling.name == 'ul':
                    items = sibling.find_all('li')
                    content.extend([extract_text_content(li) for li in items])
                elif sibling.name == 'p':
                    content.append(extract_text_content(sibling))
                sibling = sibling.find_next_sibling()
            about_data['whoCanTake'] = content
        
        # Look for "How and when to take" section
        dosage_section = soup.find(['h2', 'h3'], string=re.compile(r'How.*when.*take|Dosage', re.IGNORECASE))
        if dosage_section:
            dosage_parts = []
            sibling = dosage_section.find_next_sibling()
            while sibling and sibling.name not in ['h2', 'h3']:
                if sibling.name in ['p', 'div']:
                    text = extract_text_content(sibling)
                    if text:
                        dosage_parts.append(text)
                sibling = sibling.find_next_sibling()
            about_data['dosageInfo'] = ' '.join(dosage_parts)[:1000]
        
        # Look for "How it works" section
        how_works_section = soup.find(['h2', 'h3'], string=re.compile(r'How.*works', re.IGNORECASE))
        if how_works_section:
            how_parts = []
            sibling = how_works_section.find_next_sibling()
            while sibling and sibling.name not in ['h2', 'h3']:
                if sibling.name == 'p':
                    how_parts.append(extract_text_content(sibling))
                sibling = sibling.find_next_sibling()
            about_data['howItWorks'] = ' '.join(how_parts)[:500]
        
        # Try to extract brand names from text
        content_text = soup.get_text()
        brand_patterns = [
            r'brand name[s]?[:\s]+([A-Z][a-z]+(?:,?\s+(?:or\s+)?[A-Z][a-z]+)*)',
            r'also called[:\s]+([A-Z][a-z]+(?:,?\s+(?:or\s+)?[A-Z][a-z]+)*)'
        ]
        
        for pattern in brand_patterns:
            brand_match = re.search(pattern, content_text, re.IGNORECASE)
            if brand_match:
                brands_text = brand_match.group(1)
                brands = re.split(r',|\s+or\s+|\s+and\s+', brands_text)
                about_data['brandNames'] = [b.strip() for b in brands if b.strip()]
                break
        
        return about_data
        
    except Exception as e:
        print(f"    ⚠️  Could not fetch About page: {e}")
        return about_data

def scrape_side_effects_page(slug: str) -> Dict[str, any]:
    """
    Scrape the 'Side effects' page for a medication
    Example: https://www.nhs.uk/medicines/aciclovir/side-effects-of-aciclovir/
    """
    url = f"{NHS_MEDICINES_URL}{slug}/side-effects-of-{slug}/"
    side_effects_data = {
        'commonSideEffects': [],
        'seriousSideEffects': [],
        'sideEffectsSummary': '',
        'warnings': []
    }
    
    try:
        response = requests.get(url, headers=HEADERS, timeout=30)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Get summary paragraph
        intro = soup.find('p', class_='nhsuk-body-l')
        if intro:
            side_effects_data['sideEffectsSummary'] = extract_text_content(intro)
        
        # Look for "Common side effects" section
        common_section = soup.find(['h2', 'h3'], string=re.compile(r'Common side effects', re.IGNORECASE))
        if common_section:
            effects_list = common_section.find_next('ul')
            if effects_list:
                effects = effects_list.find_all('li')
                side_effects_data['commonSideEffects'] = [extract_text_content(effect) for effect in effects]
        
        # Look for "Serious side effects" section
        serious_section = soup.find(['h2', 'h3'], string=re.compile(r'Serious side effects', re.IGNORECASE))
        if serious_section:
            # Check for warning callout box
            warning_box = serious_section.find_next('div', class_=re.compile(r'warning|alert|callout'))
            if warning_box:
                warning_text = extract_text_content(warning_box)
                side_effects_data['warnings'].append(warning_text)
            
            # Get list of serious side effects
            effects_list = serious_section.find_next('ul')
            if effects_list:
                effects = effects_list.find_all('li')
                side_effects_data['seriousSideEffects'] = [extract_text_content(effect) for effect in effects]
        
        # Look for any warning boxes (NHS uses specific classes)
        warning_boxes = soup.find_all(['div', 'section'], class_=re.compile(r'nhsuk-warning-callout|nhsuk-care-card--urgent'))
        for box in warning_boxes:
            warning_text = extract_text_content(box)
            if warning_text and warning_text not in side_effects_data['warnings']:
                side_effects_data['warnings'].append(warning_text)
        
        return side_effects_data
        
    except requests.exceptions.HTTPError as e:
        if e.response.status_code == 404:
            print(f"    ℹ️  No Side Effects page found")
        else:
            print(f"    ⚠️  Could not fetch Side Effects page: {e}")
        return side_effects_data
    except Exception as e:
        print(f"    ⚠️  Error scraping Side Effects page: {e}")
        return side_effects_data

def scrape_medication_detailed(name: str, slug: str) -> Optional[Dict]:
    """
    Scrape comprehensive information for a medication from multiple pages
    """
    print(f"  📄 Scraping: {name}...")
    
    try:
        # Initialize medication data
        medication = {
            'name': name,
            'genericName': name,
            'brandNames': [],
            'description': '',
            'keyFacts': [],
            'howItWorks': '',
            'whoCanTake': [],
            'dosageInfo': '',
            'commonSideEffects': [],
            'seriousSideEffects': [],
            'sideEffectsSummary': '',
            'warnings': []
        }
        
        # Scrape About page
        print(f"    📖 Fetching About page...")
        about_data = scrape_about_page(slug)
        medication.update(about_data)
        
        # Small delay between requests
        time.sleep(0.5)
        
        # Scrape Side Effects page
        print(f"    ⚠️  Fetching Side Effects page...")
        side_effects_data = scrape_side_effects_page(slug)
        
        # Merge side effects data (don't overwrite existing data)
        for key, value in side_effects_data.items():
            if value and not medication.get(key):
                medication[key] = value
        
        # Ensure we have at least basic data
        if not medication['description']:
            medication['description'] = f"{name} - Please see NHS website for full information."
        
        if not medication['dosageInfo']:
            medication['dosageInfo'] = "Follow your doctor's instructions for dosage."
        
        print(f"    ✅ Successfully scraped {name}")
        return medication
        
    except Exception as e:
        print(f"    ❌ Error scraping {name}: {e}")
        return None

def scrape_all_medications(limit: Optional[int] = None) -> List[Dict]:
    """
    Scrape all medications from NHS website with detailed information
    
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
    
    print(f"\n🔄 Starting to scrape {len(med_list)} medications with detailed information...\n")
    print("📊 This will take longer as we're fetching multiple pages per medication\n")
    
    medications = []
    failed = []
    
    for i, med_info in enumerate(med_list, 1):
        print(f"[{i}/{len(med_list)}] ", end='')
        
        med_data = scrape_medication_detailed(med_info['name'], med_info['slug'])
        
        if med_data:
            medications.append(med_data)
        else:
            failed.append(med_info['name'])
        
        # Progress update every 10 medications
        if i % 10 == 0:
            print(f"\n📈 Progress: {i}/{len(med_list)} ({len(medications)} successful, {len(failed)} failed)\n")
        
        # Polite delay between medications (2 seconds since we're making 2-3 requests per med)
        time.sleep(2)
    
    print(f"\n✅ Scraping complete!")
    print(f"  - Successfully scraped: {len(medications)}")
    print(f"  - Failed: {len(failed)}")
    
    if failed:
        print(f"\n❌ Failed medications:")
        for name in failed[:10]:  # Show first 10
            print(f"  - {name}")
        if len(failed) > 10:
            print(f"  ... and {len(failed) - 10} more")
    
    return medications

def save_medications(medications: List[Dict]):
    """Save medications to JSON file"""
    # Ensure directory exists
    MEDICATIONS_FILE.parent.mkdir(parents=True, exist_ok=True)
    
    with open(MEDICATIONS_FILE, 'w', encoding='utf-8') as f:
        json.dump(medications, f, indent=2, ensure_ascii=False)
    
    print(f"\n💾 Saved {len(medications)} medications to:")
    print(f"   {MEDICATIONS_FILE}")

def main():
    """Main function"""
    print("=" * 80)
    print("NHS Medication Detailed Scraper")
    print("=" * 80)
    print("\nThis scraper fetches detailed information from multiple NHS pages:")
    print("  1. About page (description, dosage, how it works)")
    print("  2. Side Effects page (common/serious side effects, warnings)")
    print("\n⚠️  NOTE: This takes ~2 seconds per medication due to multiple pages")
    print("   Estimated time: ~10 minutes for 297 medications\n")
    
    print("Select scraping mode:")
    print("  1. Test mode (3 medications)")
    print("  2. Small batch (10 medications)")
    print("  3. Medium batch (50 medications)")
    print("  4. Full scrape (ALL medications)")
    
    choice = input("\nEnter your choice (1-4): ").strip()
    
    limit_map = {
        '1': 3,
        '2': 10,
        '3': 50,
        '4': None
    }
    
    limit = limit_map.get(choice)
    
    if choice == '4':
        confirm = input("\n⚠️  This will scrape ALL medications (~10 minutes). Continue? (yes/no): ").strip().lower()
        if confirm != 'yes':
            print("Cancelled.")
            return
    
    # Scrape medications
    medications = scrape_all_medications(limit=limit)
    
    if not medications:
        print("\n❌ No medications were scraped successfully")
        return
    
    # Ask to save
    save_choice = input("\n💾 Save medications to database? (yes/no): ").strip().lower()
    
    if save_choice == 'yes':
        save_medications(medications)
        print("\n✅ Done! You can now rebuild your Android app.")
    else:
        print("\n⚠️  Medications not saved.")

if __name__ == "__main__":
    main()
