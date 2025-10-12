"""
Quick test to see what we can extract from NHS medicines page
"""

import requests
from bs4 import BeautifulSoup

NHS_MEDICINES_URL = "https://www.nhs.uk/medicines/"

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
}

print(f"Fetching: {NHS_MEDICINES_URL}\n")

try:
    response = requests.get(NHS_MEDICINES_URL, headers=HEADERS, timeout=30)
    response.raise_for_status()
    
    print(f"✅ Status: {response.status_code}\n")
    
    soup = BeautifulSoup(response.text, 'html.parser')
    
    # Try to find medication links
    print("Looking for medication links...\n")
    
    # Method 1: Look for list items
    list_items = soup.find_all('li', class_='nhsuk-list-panel__item')
    print(f"Method 1 - List items with class 'nhsuk-list-panel__item': {len(list_items)}")
    
    # Method 2: Look for all links with /medicines/ in href
    med_links = soup.find_all('a', href=lambda x: x and '/medicines/' in x and x != '/medicines/')
    print(f"Method 2 - Links containing '/medicines/': {len(med_links)}")
    
    # Method 3: Look for alphabet navigation
    alpha_links = soup.find_all('a', href=lambda x: x and '/medicines/' in x and len(x.split('/')[-2]) == 1)
    print(f"Method 3 - Alphabet links: {len(alpha_links)}")
    
    # Show first 10 medications found
    if med_links:
        print(f"\nFirst 10 medications found:")
        for i, link in enumerate(med_links[:10], 1):
            name = link.get_text().strip()
            url = link['href']
            print(f"  {i}. {name}")
            print(f"     URL: {url}")
    
    # Save HTML to file for inspection
    with open('nhs_page_sample.html', 'w', encoding='utf-8') as f:
        f.write(soup.prettify())
    print(f"\n💾 Saved page HTML to: nhs_page_sample.html")
    print("   You can open this file to see the structure")
    
except Exception as e:
    print(f"❌ Error: {e}")
