#!/bin/bash

# Check if the correct number of arguments is provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 input_markdown_file output_pdf_file"
    exit 1
fi

INPUT_MD="$1"
OUTPUT_PDF="$2"

# Verify that the input file exists
if [ ! -f "$INPUT_MD" ]; then
    echo "Input file '$INPUT_MD' not found!"
    exit 1
fi

# Define customization files in the current directory
CHAPTER_BREAK_TEX="chapter_break.tex"
INLINE_CODE_TEX="inline_code.tex"
BULLET_STYLE_TEX="bullet_style.tex"
PDF_PROPERTIES_TEX="pdf_properties.tex"
BLOCKQUOTE_STYLE_TEX="blockquote_style.tex"
PANDOC_THEME="custom.theme"

# Create or overwrite customization files

# Chapter breaks between sections
cat > "$CHAPTER_BREAK_TEX" << 'EOF'
\usepackage{sectsty}
\sectionfont{\clearpage}
EOF

# Styling for inline code
cat > "$INLINE_CODE_TEX" << 'EOF'
\usepackage{color}
\usepackage{fancyvrb,newverbs,xcolor}
\definecolor{LightGray}{HTML}{F4F4F4}
\let\oldtexttt\texttt
\renewcommand{\texttt}[1]{\colorbox{LightGray}{\oldtexttt{#1}}}
EOF

# Bullet styling
cat > "$BULLET_STYLE_TEX" << 'EOF'
\usepackage{enumitem}
\usepackage{amsfonts}
% Level one bullets
\setlist[itemize,1]{label=$\bullet$}
% Level two bullets
\setlist[itemize,2]{label=$\circ$}
% Level three bullets
\setlist[itemize,3]{label=$\star$}
EOF

# PDF properties (metadata)
cat > "$PDF_PROPERTIES_TEX" << 'EOF'
\usepackage{hyperref}
\hypersetup{
  pdftitle={Your Document Title},
  pdfauthor={Your Name},
  pdfsubject={Subject of Document},
  pdfkeywords={keyword1, keyword2, keyword3}
}
EOF

# Blockquote styling
cat > "$BLOCKQUOTE_STYLE_TEX" << 'EOF'
\usepackage{tcolorbox}
\newtcolorbox{myquote}{
  colback=red!5!white,
  colframe=red!75!black,
  left=1em,
  right=1em,
  top=1em,
  bottom=1em
}
\renewenvironment{quote}{\begin{myquote}}{\end{myquote}}
EOF

# Ensure the custom.theme file exists
if [ ! -f "$PANDOC_THEME" ]; then
    echo "Custom theme file '$PANDOC_THEME' not found! Generating default theme."
    pandoc --print-highlight-style=pygments > "$PANDOC_THEME"
fi

# Define the Pandoc command with all customizations
pandoc "$INPUT_MD" \
    --from=gfm \
    --to=pdf \
    --pdf-engine=xelatex \
    --standalone \
    --table-of-contents \
    --toc-depth=3 \
    --include-in-header="$CHAPTER_BREAK_TEX" \
    --include-in-header="$INLINE_CODE_TEX" \
    --include-in-header="$BULLET_STYLE_TEX" \
    --include-in-header="$PDF_PROPERTIES_TEX" \
    --include-in-header="$BLOCKQUOTE_STYLE_TEX" \
    --highlight-style="$PANDOC_THEME" \
    -V linkcolor:blue \
    -V geometry:a4paper \
    -V geometry:margin=2cm \
    -V mainfont="DejaVu Serif" \
    -V monofont="DejaVu Sans Mono" \
    -V fontsize=12pt \
    -V toc-title="Table of Contents" \
    -o "$OUTPUT_PDF"

# Check if Pandoc succeeded
if [ $? -eq 0 ]; then
    echo "PDF generated successfully: $OUTPUT_PDF"
else
    echo "An error occurred during PDF generation."
    exit 1
fi

# Optional: Clean up customization files
rm -f "$CHAPTER_BREAK_TEX" "$INLINE_CODE_TEX" "$BULLET_STYLE_TEX" \
      "$PDF_PROPERTIES_TEX" "$BLOCKQUOTE_STYLE_TEX" "$PANDOC_THEME"
# Note: Do not delete custom.theme if you plan to reuse or modify it
