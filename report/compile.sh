#!/bin/bash
pdflatex main
bibtex mybibfile
bibtex main
pdflatex main
pdflatex main
rm *.aux
rm *.bbl
rm *.blg
rm *.dvi
rm *.log
rm *.out
read -p "Compilation done"
