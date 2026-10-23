$ErrorActionPreference = "Stop"

function Assert-NoMatch {
    param(
        [string]$Pattern,
        [string[]]$Paths,
        [string]$Message
    )

    # Git is already required by checkout. Include untracked source files for
    # local runs, while respecting ignored files and excluding build outputs.
    $matches = & git grep -n -I -E --untracked --exclude-standard -e $Pattern -- @Paths ':(exclude)**/build/**'
    if ($LASTEXITCODE -eq 0) {
        $matches | Write-Output
        throw $Message
    }
    if ($LASTEXITCODE -gt 1) {
        throw "Could not run security check: $Pattern"
    }
}

# Android binaries are reversible. Keys and direct generative-AI clients must
# remain server-side until the authenticated gateway is delivered.
Assert-NoMatch 'BuildConfig\.GEMINI_API_KEY|com\.google\.ai\.client\.generativeai' @('app', 'gradle') 'Direct Gemini access must not be reintroduced into the Android client.'
Assert-NoMatch 'buildConfigField\("String", "[A-Z0-9_]*(KEY|TOKEN|SECRET)' @('app') 'A credential-like value must not be injected into BuildConfig.'

& .\gradlew.bat :app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
