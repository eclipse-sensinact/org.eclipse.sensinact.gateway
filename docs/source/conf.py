# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

project = "Eclipse sensiNact"
copyright = "2023, Eclipse sensiNact contributors"
author = "Eclipse sensiNact contributors"
release = "0.0.2"

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

root_doc = "index"

extensions = ["myst_parser"]

templates_path = ["_templates"]
exclude_patterns = ["Thumbs.db", ".DS_Store"]


# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = "piccolo_theme"
html_static_path = ["_static"]

# -- Options for Piccolo Theme -----------------------------------------------
# https://piccolo-theme.readthedocs.io/en/latest/configuration.html

html_theme_options = {
    "source_url": 'https://github.com/eclipse/org.eclipse.sensinact.gateway',
    "source_icon": "github"
}

# -- Options for MyST Parser ------------------------------------------------
# https://myst-parser.readthedocs.io/en/latest/syntax/optional.html

myst_heading_anchors = 6

myst_enable_extensions = [
    "amsmath",
    "attrs_inline",
    "colon_fence",
    "deflist",
    "dollarmath",
    "fieldlist",
    "html_admonition",
    "html_image",
    "replacements",
    "smartquotes",
    "strikethrough",
    "substitution",
    "tasklist",
]
