import re
import sys

def fix_escaped_quotes(file_path):
    with open(file_path, 'r') as f:
        content = f.read()
    
    # Replace escaped quotes with regular quotes
    content = content.replace('\\"', '"')
    
    with open(file_path, 'w') as f:
        f.write(content)
    
    print(f"Fixed escaped quotes in {file_path}")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python fix_quotes.py <file_path>")
        sys.exit(1)
    
    file_path = sys.argv[1]
    fix_escaped_quotes(file_path)