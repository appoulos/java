#!/usr/bin/env bash

ranks=(ace 2 3 4 5 6 7 8 9 10 jack queen king)
files=()
for suit in clubs diamonds hearts spades; do
	for rank in "${ranks[@]}"; do
		files+=("${rank}_of_${suit}.png")
	done
done

set -x
magick "${files[@]}" -geometry x300 +smush 0 tiles.png
