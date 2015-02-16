#!/bin/bash
pdflatex main
bibtex bib
bibtex main
pdflatex main
pdflatex main
rm *.aux
rm *.bbl
rm *.blg
rm *.dvi
rm *.log
read -p "Compilation done"
