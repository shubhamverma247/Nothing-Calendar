[CmdletBinding()]
param(
    [string]$NormalZip,
    [string]$MonochromeZip,
    [string]$FallbackZip,
    [string]$MonochromeFallbackZip,
    [string]$ResourceRoot
)

$ErrorActionPreference = 'Stop'
$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
if ([string]::IsNullOrWhiteSpace($NormalZip)) {
    $NormalZip = Join-Path $scriptRoot '..\Docs\base-new-normal.zip'
}
if ([string]::IsNullOrWhiteSpace($MonochromeZip)) {
    $MonochromeZip = Join-Path $scriptRoot '..\Docs\base-new-monochrome.zip'
}
if ([string]::IsNullOrWhiteSpace($FallbackZip)) {
    $FallbackZip = Join-Path $scriptRoot '..\Docs\defaul-new-normal.zip'
}
if ([string]::IsNullOrWhiteSpace($MonochromeFallbackZip)) {
    $MonochromeFallbackZip = Join-Path $scriptRoot '..\Docs\default-new-monochorme.zip'
}
if ([string]::IsNullOrWhiteSpace($ResourceRoot)) {
    $ResourceRoot = Join-Path $scriptRoot '..\app\src\main\res'
}
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem

$densities = @('mdpi', 'hdpi', 'xhdpi', 'xxhdpi', 'xxxhdpi')
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ('dotcal-icon-generator-' + [guid]::NewGuid().ToString())
$normalRoot = Join-Path $tempRoot 'normal'
$monochromeRoot = Join-Path $tempRoot 'monochrome'
$fallbackRoot = Join-Path $tempRoot 'fallback'
$monochromeFallbackRoot = Join-Path $tempRoot 'monochrome-fallback'

function New-CopyBitmap([object]$SourceInput) {
    $ownsSource = $SourceInput -is [string]
    $source = if ($ownsSource) {
        [System.Drawing.Bitmap]::new([string]$SourceInput)
    } else {
        $SourceInput
    }
    try {
        $copy = [System.Drawing.Bitmap]::new(
            $source.Width,
            $source.Height,
            [System.Drawing.Imaging.PixelFormat]::Format32bppArgb
        )
        $graphics = [System.Drawing.Graphics]::FromImage($copy)
        try {
            $graphics.DrawImage($source, 0, 0, $source.Width, $source.Height)
        } finally {
            $graphics.Dispose()
        }
        return $copy
    } finally {
        if ($ownsSource) {
            $source.Dispose()
        }
    }
}

function Save-Png([System.Drawing.Bitmap]$Bitmap, [string]$Path) {
    $directory = Split-Path -Parent $Path
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $Bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
}

function New-DateBitmap(
    [System.Drawing.Bitmap]$Base,
    [int]$Day,
    [System.Drawing.Color]$Color
) {
    $result = New-CopyBitmap $Base
    $graphics = [System.Drawing.Graphics]::FromImage($result)
    $fontSize = [Math]::Max(18, [int][Math]::Round($result.Width * 0.30))
    $font = [System.Drawing.Font]::new(
        'Arial',
        $fontSize,
        [System.Drawing.FontStyle]::Bold,
        [System.Drawing.GraphicsUnit]::Pixel
    )
    $brush = [System.Drawing.SolidBrush]::new($Color)
    try {
        $digits = $Day.ToString()
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
        $format = [System.Drawing.StringFormat]::new([System.Drawing.StringFormat]::GenericTypographic)
        $textSize = $graphics.MeasureString($digits, $font, [System.Drawing.PointF]::new(0, 0), $format)
        $left = ($result.Width - $textSize.Width) / 2
        $top = ($result.Height * 0.57) - ($textSize.Height / 2)
        $graphics.DrawString($digits, $font, $brush, $left, $top, $format)
        $format.Dispose()
    } finally {
        $brush.Dispose()
        $font.Dispose()
        $graphics.Dispose()
    }
    return $result
}

function New-CompositeBitmap(
    [System.Drawing.Bitmap]$Background,
    [System.Drawing.Bitmap]$Foreground
) {
    $result = [System.Drawing.Bitmap]::new(
        $Background.Width,
        $Background.Height,
        [System.Drawing.Imaging.PixelFormat]::Format32bppArgb
    )
    $graphics = [System.Drawing.Graphics]::FromImage($result)
    try {
        $graphics.DrawImage($Background, 0, 0, $Background.Width, $Background.Height)
        $graphics.DrawImage($Foreground, 0, 0, $Foreground.Width, $Foreground.Height)
    } finally {
        $graphics.Dispose()
    }
    return $result
}

function Resize-Bitmap([System.Drawing.Bitmap]$Source, [int]$Width, [int]$Height) {
    $result = [System.Drawing.Bitmap]::new(
        $Width,
        $Height,
        [System.Drawing.Imaging.PixelFormat]::Format32bppArgb
    )
    $graphics = [System.Drawing.Graphics]::FromImage($result)
    try {
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.DrawImage($Source, 0, 0, $Width, $Height)
    } finally {
        $graphics.Dispose()
    }
    return $result
}

function Apply-AlphaMask(
    [System.Drawing.Bitmap]$Bitmap,
    [System.Drawing.Bitmap]$Mask
) {
    for ($x = 0; $x -lt $Bitmap.Width; $x++) {
        for ($y = 0; $y -lt $Bitmap.Height; $y++) {
            $pixel = $Bitmap.GetPixel($x, $y)
            $maskPixel = $Mask.GetPixel($x, $y)
            $alpha = [Math]::Min($pixel.A, $maskPixel.A)
            $Bitmap.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($alpha, $pixel.R, $pixel.G, $pixel.B))
        }
    }
}

function Normalize-Monochrome([System.Drawing.Bitmap]$Bitmap) {
    for ($x = 0; $x -lt $Bitmap.Width; $x++) {
        for ($y = 0; $y -lt $Bitmap.Height; $y++) {
            $pixel = $Bitmap.GetPixel($x, $y)
            $Bitmap.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($pixel.A, 0, 0, 0))
        }
    }
}

function Remove-GeneratedResources([string]$Root) {
    Get-ChildItem -LiteralPath $Root -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object {
            $_.Name -match '^ic_launcher_day_(0[1-9]|[12][0-9]|3[01])(_foreground)?\.png$' -or
            $_.Name -match '^ic_launcher_monochrome_day_(0[1-9]|[12][0-9]|3[01])\.png$'
        } |
        Remove-Item -Force
    Get-ChildItem -LiteralPath (Join-Path $Root 'mipmap-anydpi-v26') -Filter 'ic_launcher_day_*.xml' -File -ErrorAction SilentlyContinue |
        Remove-Item -Force
}

New-Item -ItemType Directory -Force -Path $normalRoot, $monochromeRoot, $fallbackRoot, $monochromeFallbackRoot | Out-Null
try {
    [System.IO.Compression.ZipFile]::ExtractToDirectory((Resolve-Path $NormalZip), $normalRoot)
    [System.IO.Compression.ZipFile]::ExtractToDirectory((Resolve-Path $MonochromeZip), $monochromeRoot)
    [System.IO.Compression.ZipFile]::ExtractToDirectory((Resolve-Path $FallbackZip), $fallbackRoot)
    [System.IO.Compression.ZipFile]::ExtractToDirectory((Resolve-Path $MonochromeFallbackZip), $monochromeFallbackRoot)
    Remove-GeneratedResources $ResourceRoot

    $normalAndroidRoot = Join-Path $normalRoot 'android'
    $monochromeAndroidRoot = Join-Path $monochromeRoot 'android'
    $fallbackAndroidRoot = Join-Path $fallbackRoot 'android'
    $monochromeFallbackAndroidRoot = Join-Path $monochromeFallbackRoot 'android'
    $generatedCount = 0

    foreach ($density in $densities) {
        $normalDirectory = Join-Path $normalAndroidRoot "res\mipmap-$density"
        $monochromeDirectory = Join-Path $monochromeAndroidRoot "res\mipmap-$density"
        $outputDirectory = Join-Path $ResourceRoot "mipmap-$density"
        $normalForegroundPath = Join-Path $normalDirectory 'ic_launcher_foreground.png'
        $normalBackgroundPath = Join-Path $normalDirectory 'ic_launcher_background.png'
        $normalFlatPath = Join-Path $normalDirectory 'ic_launcher.png'
        $monochromePath = Join-Path $monochromeDirectory 'ic_launcher_monochrome.png'
        $fallbackDirectory = Join-Path $fallbackAndroidRoot "res\mipmap-$density"
        $monochromeFallbackDirectory = Join-Path $monochromeFallbackAndroidRoot "res\mipmap-$density"

        Copy-Item (Join-Path $fallbackDirectory 'ic_launcher_background.png') (Join-Path $outputDirectory 'ic_launcher_background.png') -Force
        Copy-Item (Join-Path $fallbackDirectory 'ic_launcher_foreground.png') (Join-Path $outputDirectory 'ic_launcher_foreground.png') -Force
        Copy-Item (Join-Path $fallbackDirectory 'ic_launcher.png') (Join-Path $outputDirectory 'ic_launcher.png') -Force
        Copy-Item (Join-Path $monochromeFallbackDirectory 'ic_launcher_monochrome.png') (Join-Path $outputDirectory 'ic_launcher_monochrome.png') -Force

        $baseForeground = New-CopyBitmap $normalForegroundPath
        $baseBackground = New-CopyBitmap $normalBackgroundPath
        $baseMonochrome = New-CopyBitmap $monochromePath
        $baseFlat = New-CopyBitmap $normalFlatPath
        try {
            Normalize-Monochrome $baseMonochrome
            foreach ($day in 1..31) {
                $dayName = '{0:00}' -f $day
                $normalDate = New-DateBitmap $baseForeground $day ([System.Drawing.Color]::White)
                $monochromeDate = New-DateBitmap $baseMonochrome $day ([System.Drawing.Color]::Black)
                $composite = New-CompositeBitmap $baseBackground $normalDate
                $flat = Resize-Bitmap $composite $baseFlat.Width $baseFlat.Height
                Apply-AlphaMask $flat $baseFlat

                try {
                    Save-Png $normalDate (Join-Path $outputDirectory "ic_launcher_day_$dayName`_foreground.png")
                    Save-Png $monochromeDate (Join-Path $outputDirectory "ic_launcher_monochrome_day_$dayName.png")
                    Save-Png $flat (Join-Path $outputDirectory "ic_launcher_day_$dayName.png")
                } finally {
                    $normalDate.Dispose()
                    $monochromeDate.Dispose()
                    $composite.Dispose()
                    $flat.Dispose()
                }

                $generatedCount++
            }
        } finally {
            $baseForeground.Dispose()
            $baseBackground.Dispose()
            $baseMonochrome.Dispose()
            $baseFlat.Dispose()
        }
    }

    $adaptiveDirectory = Join-Path $ResourceRoot 'mipmap-anydpi-v26'
    New-Item -ItemType Directory -Force -Path $adaptiveDirectory | Out-Null
    foreach ($day in 1..31) {
        $dayName = '{0:00}' -f $day
        $xml = @"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_day_$dayName`_foreground" />
    <monochrome android:drawable="@mipmap/ic_launcher_monochrome_day_$dayName" />
</adaptive-icon>
"@
        Set-Content -LiteralPath (Join-Path $adaptiveDirectory "ic_launcher_day_$dayName.xml") -Value $xml -Encoding utf8
    }

    foreach ($density in $densities) {
        $outputDirectory = Join-Path $ResourceRoot "mipmap-$density"
        $densityNormal = @(Get-ChildItem -LiteralPath $outputDirectory -Filter 'ic_launcher_day_*.png' -File |
                Where-Object { $_.Name -match '^ic_launcher_day_(0[1-9]|[12][0-9]|3[01])\.png$' })
        $densityForeground = @(Get-ChildItem -LiteralPath $outputDirectory -Filter 'ic_launcher_day_*_foreground.png' -File)
        $densityMonochrome = @(Get-ChildItem -LiteralPath $outputDirectory -Filter 'ic_launcher_monochrome_day_*.png' -File)
        if ($densityNormal.Count -ne 31 -or $densityForeground.Count -ne 31 -or $densityMonochrome.Count -ne 31) {
            throw "Incomplete launcher resources for $density"
        }
    }
    foreach ($day in 1..31) {
        $dayName = '{0:00}' -f $day
        $xmlPath = Join-Path $adaptiveDirectory "ic_launcher_day_$dayName.xml"
        $xmlContent = Get-Content -LiteralPath $xmlPath -Raw
        if ($xmlContent -notlike "*ic_launcher_day_${dayName}_foreground*" -or
            $xmlContent -notlike "*ic_launcher_monochrome_day_${dayName}*") {
            throw "Adaptive icon day $day references mismatched resources"
        }
    }

    if ($generatedCount -ne 155) {
        throw "Expected 155 density variants, generated $generatedCount"
    }
    Write-Output "Generated 31 normal + 31 monochrome variants across $($densities.Count) densities."
} finally {
    if (Test-Path -LiteralPath $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
