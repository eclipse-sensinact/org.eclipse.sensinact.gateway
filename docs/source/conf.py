######################################################################
# Copyright (c) 2023 Contributors to the Eclipse Foundation.
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Kentyou - initial customization
######################################################################

# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

import datetime
import pathlib
import re

project = "Eclipse sensiNact™"
copyright = (
    f"{datetime.date.today().year} by Eclipse Foundation. "
    "Eclipse sensiNact™ is a trademark of Eclipse Foundation AISBL"
)
author = "Eclipse sensiNact™ contributors"
# The version lives in exactly one place: the -Drevision= line in
# .mvn/maven.config at the repository root (x.y.z, the -SNAPSHOT is the
# separate -Dchangelist= line). Read it from there so the docs can never
# drift from the build.
_maven_config = pathlib.Path(__file__).resolve().parents[2] / ".mvn" / "maven.config"
_revision = re.search(r"^-Drevision=(\S+)", _maven_config.read_text(), re.MULTILINE)
if _revision is None:
    raise RuntimeError(f"no -Drevision= line in {_maven_config}")
release = _revision.group(1)
version = release

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

root_doc = "root-toc"

extensions = [
    "myst_parser",
    "sphinxcontrib.rsvgconverter",
]

templates_path = ["_templates"]
exclude_patterns = ["Thumbs.db", ".DS_Store"]

# -- Options for LaTex output ------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#latex-options

latex_engine = "xelatex"
latex_logo = "_static/sensiNact_logo.png"
latex_show_pagerefs = True
latex_show_urls = "footnote"

# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = "piccolo_theme"
html_static_path = ["_static"]
html_favicon = "_static/sensiNact_logo.png"

# -- Options for Piccolo Theme -----------------------------------------------
# https://piccolo-theme.readthedocs.io/en/latest/configuration.html

html_theme_options = {
    "source_url": "https://github.com/eclipse-sensinact/org.eclipse.sensinact.gateway",
    "source_icon": "github",
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
