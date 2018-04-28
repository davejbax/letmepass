import gzip
import json
import os
import sqlite3
import sys

def get_index(word):
    """Gets the non-unique index (for indexing) that should be used to represent a word"""
    return (word[0], len(word))

def gen_partitions(words):
    """Generates a dictionary of sorted word partitions by index"""
    partitions = {}

    # Add each word to its appropriate partition
    for word in words:
        index = get_index(word)
        if not index in partitions:
            partitions[index] = []

        # Omit the first character as it is contained in index
        partitions[index].append(word[1:])

    # Sort each partition
    for index in partitions:
        partitions[index].sort()

    return partitions

def write_db(parts, outfile):
    conn = sqlite3.connect(outfile)
    conn.execute('CREATE TABLE blacklist (first_letter character(1), length integer, suffix varchar(255))')
    conn.execute('CREATE INDEX idx ON blacklist (first_letter, length)')

    rows = []
    for i in parts:
        # Add all passwords in the part as a separate row
        for password in parts[i]:
            rows.append((i[0], i[1], password))

    c = conn.cursor()
    c.executemany('INSERT INTO blacklist VALUES (?,?,?)', rows)
    conn.commit()
    conn.close()

def main():
    if len(sys.argv) != 3:
        print("Usage: {} <wordlist> <output file>".format(sys.argv[0]))
        exit(0)

    # Check wordlist exists
    wordlist = sys.argv[1]
    if not os.path.isfile(wordlist):
        print("Error: wordlist '{}' not found".format(wordlist))
        exit(1)

    # Check output file doesn't exist
    outfile = sys.argv[2]
    if os.path.isfile(outfile):
        print("Error: file {} already exists".format(outfile))
        exit(1)

    # Read word list
    words = None
    with open(wordlist, 'r') as fh:
        words = fh.read()

    # Split words by line and eliminate blank lines
    words = [ x.strip() for x in words.split("\n") if x ]
    
    # Generate partitions
    parts = gen_partitions(words)

    # Write DB
    write_db(parts, outfile)
    print("Blacklist written to {}!".format(outfile))
    
if __name__ == "__main__":
    main()
