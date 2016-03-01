# Fonetic

Text search utils, fuzzy search algorithms etc.

Main packages and classes:

####`text`

- [`Word`](src/main/java/ru/iitdgroup/lingutil/text/Word.java) 
— representation of a piece of text as some string 
value, which internally holds mapping to source where it was
initially extracted from. Across transformations, such mapping 
remains unchanged or may change accordingly if word length changes, 
so after all operations it is clear simple to align resulting 
word to its initial source. `Word` implements `CharSequence` 
of its value, making it easy to use it in search algorithms, 
utility methods etc;

- [`Words`](src/main/java/ru/iitdgroup/lingutil/text/Words.java) 
— utility class to produce `Word`s and play with them 
(extract from `String`, join, split etc).

This may be useful when you work with documents containing 
markup tags and other special entities—you first extract text 
as a collection or `Word`s from the document, then process/modify 
them, and finally apply modifications to source, leaving markup 
untouched. (For example, you need to search and highlight 
dictionary entries in html document.)

####`search`

- [`FoneticSearch`](src/main/java/ru/iitdgroup/lingutil/search/FoneticSearch.java) 
— original algorithm to search
for phonetically similar occurrences of `pattern` in `text`. The
main goal is to allow not only phonetic variations, but also 
**non-phonetic misspells**, that commonly used *Metaphone* or *Soundex* 
don't handle—misspelled word there are very likely to be 
encoded differently than original;

- [`LcsSearch`](src/main/java/ru/iitdgroup/lingutil/search/LcsSearch.java) 
— search for matches using *gapped longest common
subsequence*.

####`collect`
[*in progress, subject to change*] — char- and CharSequence-based collections,
incliding [`CharMap<V>`](src/main/java/ru/iitdgroup/lingutil/collect/CharMap.java) and [`CharTrieMap<V>`](src/main/java/ru/iitdgroup/lingutil/collect/CharTrieMap.java).

-

A few small demos to see how it works: [src/main/java/demos](src/main/java/demos) 
