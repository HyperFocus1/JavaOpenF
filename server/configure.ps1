param(
    [Parameter(Mandatory = $true)]
    [string] $WildFlyHome
)

$ErrorActionPreference = 'Stop'
$serverRoot = (Resolve-Path -LiteralPath $WildFlyHome).Path
$projectRoot = Split-Path -Parent $PSScriptRoot
$driverPath = Join-Path $projectRoot 'target/drivers/mysql-connector-j.jar'
$cliPath = Join-Path $serverRoot 'bin/jboss-cli.bat'

if (!(Test-Path -LiteralPath $cliPath -PathType Leaf)) {
    throw 'WildFlyHome debe apuntar a una instalación de WildFly 42.'
}
if (!(Test-Path -LiteralPath $driverPath -PathType Leaf)) {
    throw 'Primero ejecuta mvn package para generar el driver MySQL en target/drivers.'
}
foreach ($artifact in @('jackson-core', 'jackson-databind')) {
    $jarPath = Join-Path $projectRoot "target/jackson/$artifact.jar"
    if (!(Test-Path -LiteralPath $jarPath -PathType Leaf)) {
        throw 'Primero ejecuta mvn package para generar los parches Jackson en target/jackson.'
    }
}

$moduleDirectory = Join-Path $serverRoot 'modules/com/mysql/main'
New-Item -ItemType Directory -Path $moduleDirectory -Force | Out-Null
Copy-Item -LiteralPath $driverPath -Destination (Join-Path $moduleDirectory 'mysql-connector-j.jar') -Force
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'mysql-module.xml') -Destination (Join-Path $moduleDirectory 'module.xml') -Force

# Los módulos de usuario tienen prioridad sobre modules/system/layers/base.
foreach ($artifact in @('jackson-core', 'jackson-databind')) {
    $jacksonDirectory = Join-Path $serverRoot "modules/com/fasterxml/jackson/core/$artifact/main"
    New-Item -ItemType Directory -Path $jacksonDirectory -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $projectRoot "target/jackson/$artifact.jar") -Destination (Join-Path $jacksonDirectory "$artifact.jar") -Force
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot "$artifact-module.xml") -Destination (Join-Path $jacksonDirectory 'module.xml') -Force
}

$previousNoPause = $env:NOPAUSE
try {
    $env:NOPAUSE = 'true'
    & $cliPath ('--file=' + (Join-Path $PSScriptRoot 'configure.cli'))
    $configureExit = $LASTEXITCODE
} finally {
    $env:NOPAUSE = $previousNoPause
}
if ($configureExit -ne 0) {
    throw 'No se pudo configurar WildFly. Revisa la salida del CLI y comprueba que el servidor esté detenido.'
}

Write-Host 'WildFly configurado: driver MySQL, SmallRye y parche Jackson. OpenFDS se desplegará desde META-INF/openf-ds.xml del WAR.'
