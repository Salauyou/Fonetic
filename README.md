# Fonetic

Text search utils, fuzzy search algorithms etc.

----------------------

Main packages and classes:

#####`text`

- `Word` — representation of a piece of text as some string 
value, which internally "holds" mapping to source where it was
initially extracted from. Across transformations, such mapping 
remains unchanged or may change accordingly if word length changes, 
so after all operations it is clear simple to align resulting 
word to its initial source. `Word` implements `CharSequence` 
of its value, making it easy to use it in search algorithms, 
utility methods etc.

- `Words` — utility class to produce `Word`s and play with them 
(extract from `String`, join, split etc)

#####`search`

- `FoneticSearch` — original algorithm to search
for phonetically similar occurrences of `pattern` in `text`. The
main goal is to allow not only phonetic variations, but also 
**misspells**, that commonly used *Metaphone* or *Soundex* 
doesn't handle—misspelled word there are very likely to be 
encoded differently

- `LcsSearch` — search for matches using *gapped longest common
subsequence*
