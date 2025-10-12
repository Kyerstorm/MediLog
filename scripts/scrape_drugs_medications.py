#!/usr/bin/env python3
"""
Scrape Medications from Drugs.com
Builds a comprehensive JSON index of all medications with their URLs

This script scrapes:
1. A-Z Drug List pages (https://www.drugs.com/alpha/)
2. Extracts medication names and URLs
3. Generates drugs_medications.json for the app

Usage:
    python scrape_drugs_medications.py
    python scrape_drugs_medications.py --limit 100  # Limit total medications
    python scrape_drugs_medications.py --letters "abc"  # Only scrape A, B, C
"""

import requests
from bs4 import BeautifulSoup
import json
import time
import argparse
from pathlib import Path
from datetime import datetime

# Colors for terminal output
class Colors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'

def print_header(message):
    """Print a formatted header message"""
    print(f"\n{Colors.HEADER}{Colors.BOLD}{'=' * 60}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{message.center(60)}{Colors.ENDC}")
    print(f"{Colors.HEADER}{Colors.BOLD}{'=' * 60}{Colors.ENDC}\n")

def print_success(message):
    """Print a success message"""
    print(f"{Colors.OKGREEN}✅ {message}{Colors.ENDC}")

def print_error(message):
    """Print an error message"""
    print(f"{Colors.FAIL}❌ {message}{Colors.ENDC}")

def print_info(message):
    """Print an info message"""
    print(f"{Colors.OKCYAN}ℹ️  {message}{Colors.ENDC}")

def print_warning(message):
    """Print a warning message"""
    print(f"{Colors.WARNING}⚠️  {message}{Colors.ENDC}")

def scrape_letter_page(letter, delay=1.0):
    """
    Scrape all medications for a given letter
    
    Args:
        letter: Letter to scrape (a-z, 0-9)
        delay: Delay between requests in seconds
        
    Returns:
        List of medication dictionaries with 'name' and 'url'
    """
    medications = []
    base_url = f"https://www.drugs.com/alpha/{letter}.html"
    
    print_info(f"Scraping letter: {letter.upper()}")
    
    # Add headers to avoid being blocked
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
        'Accept-Language': 'en-US,en;q=0.5',
        'Accept-Encoding': 'gzip, deflate',
        'Connection': 'keep-alive',
    }
    
    try:
        response = requests.get(base_url, headers=headers, timeout=30)
        response.raise_for_status()
        
        soup = BeautifulSoup(response.content, 'html.parser')
        
        # Find the drug list - Drugs.com uses <ul class="ddc-list-column-2"> or similar
        # Look for all links in the main content area
        drug_links = []
        
        # Method 1: Find lists with drug links
        lists = soup.find_all(['ul', 'div'], class_=lambda x: x and ('list' in x.lower() or 'column' in x.lower()))
        for lst in lists:
            links = lst.find_all('a', href=True)
            drug_links.extend(links)
        
        # Method 2: If method 1 fails, find all links in main content
        if not drug_links:
            main_content = soup.find(['div', 'main'], class_=lambda x: x and 'content' in x.lower())
            if main_content:
                drug_links = main_content.find_all('a', href=True)
            else:
                drug_links = soup.find_all('a', href=True)
        
        # Filter and process drug links
        for link in drug_links:
            href = link.get('href', '')
            name = link.get_text(strip=True)
            
            # Filter for valid drug links
            # Drugs.com drug pages typically follow pattern: /drug-name.html or /drug-name/
            if href and name:
                # Ensure it's a drug link (not navigation, footer, etc.)
                if (href.startswith('/') and 
                    '.html' in href and 
                    not any(skip in href.lower() for skip in ['alpha', 'support', 'privacy', 'terms', 'about', 'contact'])):
                    
                    # Build full URL
                    if href.startswith('/'):
                        full_url = f"https://www.drugs.com{href}"
                    else:
                        full_url = href
                    
                    # Clean up name (remove generic suffixes, extra info)
                    clean_name = name.split('(')[0].strip()  # Remove "(brand)" etc.
                    
                    if clean_name and len(clean_name) > 1:  # Skip single char names
                        medications.append({
                            'name': clean_name,
                            'url': full_url
                        })
        
        # Remove duplicates based on name
        unique_meds = {}
        for med in medications:
            if med['name'] not in unique_meds:
                unique_meds[med['name']] = med
        
        medications = list(unique_meds.values())
        
        print_success(f"Found {len(medications)} medications for letter {letter.upper()}")
        
        # Be nice to the server
        time.sleep(delay)
        
        return medications
        
    except requests.RequestException as e:
        print_error(f"Error scraping letter {letter}: {e}")
        return []
    except Exception as e:
        print_error(f"Unexpected error for letter {letter}: {e}")
        return []

def scrape_all_medications(letters=None, limit=None, delay=1.0):
    """
    Scrape medications from all letter pages
    
    Args:
        letters: String of letters to scrape (e.g., "abc" for A, B, C)
                If None, scrapes all letters and numbers
        limit: Maximum number of medications to scrape
        delay: Delay between requests in seconds
        
    Returns:
        List of all medications
    """
    if letters is None:
        # All letters A-Z and numbers 0-9
        letters = 'abcdefghijklmnopqrstuvwxyz0123456789'
    
    all_medications = []
    
    print_info(f"Scraping {len(letters)} pages...")
    print_info(f"Request delay: {delay}s per page")
    if limit:
        print_info(f"Limit: {limit} medications")
    
    for letter in letters.lower():
        if limit and len(all_medications) >= limit:
            print_warning(f"Reached limit of {limit} medications")
            break
        
        meds = scrape_letter_page(letter, delay)
        all_medications.extend(meds)
        
        print_info(f"Total medications so far: {len(all_medications)}")
    
    # Remove duplicates across all letters
    unique_meds = {}
    for med in all_medications:
        if med['name'] not in unique_meds:
            unique_meds[med['name']] = med
    
    all_medications = list(unique_meds.values())
    
    # Sort by name
    all_medications.sort(key=lambda x: x['name'].lower())
    
    return all_medications

def save_medications_json(medications, output_path):
    """
    Save medications to JSON file
    
    Args:
        medications: List of medication dictionaries
        output_path: Path to output JSON file
    """
    try:
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(medications, f, indent=2, ensure_ascii=False)
        
        print_success(f"Saved {len(medications)} medications to {output_path}")
        
        # Calculate file size
        size_kb = output_path.stat().st_size / 1024
        print_info(f"File size: {size_kb:.2f} KB")
        
    except Exception as e:
        print_error(f"Error saving JSON: {e}")

def generate_statistics(medications):
    """
    Generate and display statistics about scraped medications
    
    Args:
        medications: List of medication dictionaries
    """
    print_header("Statistics")
    
    print_info(f"Total medications: {len(medications)}")
    
    # Count by first letter
    letter_counts = {}
    for med in medications:
        first_letter = med['name'][0].upper()
        letter_counts[first_letter] = letter_counts.get(first_letter, 0) + 1
    
    print_info("\nMedications by first letter:")
    for letter in sorted(letter_counts.keys()):
        count = letter_counts[letter]
        bar = '█' * (count // 10)
        print(f"  {letter}: {count:4d} {bar}")
    
    # Show sample medications
    print_info("\nSample medications:")
    for med in medications[:10]:
        print(f"  • {med['name']}")
    if len(medications) > 10:
        print(f"  ... and {len(medications) - 10} more")

def main():
    """Main scraping function"""
    parser = argparse.ArgumentParser(description='Scrape medications from Drugs.com')
    parser.add_argument('--letters', type=str, help='Letters to scrape (e.g., "abc" for A, B, C)')
    parser.add_argument('--limit', type=int, help='Maximum number of medications to scrape')
    parser.add_argument('--delay', type=float, default=1.0, help='Delay between requests (seconds)')
    parser.add_argument('--output', type=str, help='Output JSON file path')
    
    args = parser.parse_args()
    
    print_header("Drugs.com Medication Scraper")
    print_info(f"Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    # Determine output path
    if args.output:
        output_path = Path(args.output)
    else:
        # Default to app assets directory
        script_dir = Path(__file__).parent
        project_root = script_dir.parent
        output_path = project_root / 'app' / 'src' / 'main' / 'assets' / 'drugs_medications.json'
    
    print_info(f"Output file: {output_path}")
    
    # Scrape medications
    medications = scrape_all_medications(
        letters=args.letters,
        limit=args.limit,
        delay=args.delay
    )
    
    if not medications:
        print_error("No medications found!")
        return 1
    
    # Save to JSON
    save_medications_json(medications, output_path)
    
    # Generate statistics
    generate_statistics(medications)
    
    print_header("Scraping Complete")
    print_success(f"Successfully scraped {len(medications)} medications")
    print_info(f"Finished at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    return 0

if __name__ == "__main__":
    try:
        exit(main())
    except KeyboardInterrupt:
        print(f"\n\n{Colors.WARNING}Scraping cancelled by user{Colors.ENDC}")
        exit(1)
    except Exception as e:
        print_error(f"Unexpected error: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
