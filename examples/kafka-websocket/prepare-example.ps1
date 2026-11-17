Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# Construye una copia ejecutable y conserva intactos src/, pom.xml y compose del repo.
$repoRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../..')).Path
$buildRoot = Join-Path $PSScriptRoot ('.build/' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $buildRoot -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $repoRoot 'src') -Destination (Join-Path $buildRoot 'src') -Recurse
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'pom.xml') -Destination (Join-Path $buildRoot 'pom.xml')
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'docker-compose.yml') -Destination (Join-Path $buildRoot 'docker-compose.yml')
Copy-Item -Path (Join-Path $PSScriptRoot 'src/*') -Destination (Join-Path $buildRoot 'src') -Recurse -Force
Write-Output $buildRoot
