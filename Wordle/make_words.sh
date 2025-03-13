#!/bin/bash
in="dict.gz"
out="wordle.txt"
tmp_file=$(mktemp)
# grep -v "'" words | grep -E '[a-z]{5}' | awk '{if (length($1) == 5) print tolower($1)}' | uniq >> wordle.txt
zgrep -v "'" "$in" | grep -E '^[A-Za-z]{5}$' | tr '[:upper:]' '[:lower:]' | uniq > "$tmp_file"
wc -l "$tmp_file" | cut -f1 -d' ' > "$out"
cat "$tmp_file" >> "$out"
rm "$tmp_file"

