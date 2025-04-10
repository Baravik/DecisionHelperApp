initialize dictionary = { 0: "" }        // index 0 is an empty string
w = ""                                   // current prefix (starts empty)
for each character c in input:
   if (w + c) exists in dictionary:
      // extend the current prefix w by c, as (w+c) is already known
      w = w + c
   else:
      // output the pair: index of w, and the new character c
      output ( index(w), c )
      // add new entry for the new substring (w+c) with a new index
      add dictionary_entry for (w + c) with a new index
      // reset w to empty to start a new phrase
      w = ""
   // After processing all input characters:
if w is not "" (non-empty):
   // output the remaining prefix as a pair (if any leftover)
   output ( index(w), <EOF> )   // <EOF> is a special end-of-input marker
