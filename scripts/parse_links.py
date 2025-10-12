from bs4 import BeautifulSoup

with open('nhs_page_sample.html', encoding='utf-8') as f:
    soup = BeautifulSoup(f, 'html.parser')

# Find all medication links (exclude navigation/alphabet links)
links = [a for a in soup.find_all('a', href=True) 
         if '/medicines/' in a['href'] 
         and a['href'] != '/medicines/' 
         and '#' not in a['href']]

print(f'Found {len(links)} medication links\n')
print('First 20 medications:')
for i, link in enumerate(links[:20], 1):
    name = link.text.strip()
    url = link['href']
    print(f'{i:3}. {name[:50]:<50} -> {url}')
