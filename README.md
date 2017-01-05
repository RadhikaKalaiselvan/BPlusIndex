# BPlusIndex
DB Design Project

B+ tree indexing :  It will read a text file containing data and build the index, treating the first 15 columns as the key.
Each of the functions and their command-line parameters are given below:

1.Create Index
IndexTree -create <InputFileName.txt> <IndexFileName.indx> 15

2.Find a record
IndexTree -find <index filename> <key>

3.Insert new record
IndexTree -insert <IndexFileName.indx> "<data to be inserted>

4.List sequential records
IndexTree -list <index filename> <starting key> <count>


