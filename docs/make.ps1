Push-Location $PSScriptRoot

# Command file for Sphinx documentation

if (!$Env:SPHINXBUILD) {
	$Env:SPHINXBUILD = "sphinx-build"
}
$SOURCEDIR = "source"
$BUILDDIR = "build"

try {
	& $Env:SPHINXBUILD > $null 2> $null
} catch {
	Write-Output ""
	Write-Output "The 'sphinx-build' command was not found. Make sure you have Sphinx"
	Write-Output "installed, then set the SPHINXBUILD environment variable to point"
	Write-Output "to the full path of the 'sphinx-build' executable. Alternatively you"
	Write-Output "may add the Sphinx directory to PATH."
	Write-Output ""
	Write-Output "If you don't have Sphinx installed, grab it from"
	Write-Output "https://www.sphinx-doc.org/"
	exit 1
}

if (!$args[0]) {
	& $Env:SPHINXBUILD -M help $SOURCEDIR $BUILDDIR $Env:SPHINXOPTS $Env:O
} else {
	& $Env:SPHINXBUILD -M $args[0] $SOURCEDIR $BUILDDIR $Env:SPHINXOPTS $Env:O
}

Pop-Location
