import os
import re
from pathlib import Path

# =========================
# CONFIGURATION
# =========================

INCLUDE_PATTERNS = [
    # r".*\.py$",
    r".*\.html$",
    # r".*\.ts$",
    r".*\.java$",
    # r".*superadmin.*",
    # r".*Super.*",
    # r".*\.kt$",
    # r".*\.xml$",
    # r".*\.yml$",
    # r".*enum\.ts$",
    # r".*entity\.ts$",
    # r".*dto\.ts$",
    # r".*service\.ts$",
    # r".*controller\.ts$",
]

IGNORE_DIRS = {
    ".git",
    "node_modules",
    "build",
    "dist",
    "__pycache__",
    ".expo",
    ".gradle",
    "scripts",
    ".vscode",
    ".angular",
    ".idea",
    # "frontend-web",
    "frontend-mobile",
    ".github",
    "gradle",
    "docs",
    "public"
}

# Known binary extensions (quick filter)
BINARY_EXTENSIONS = {
    ".png", ".jpg", ".jpeg", ".gif", ".bmp",
    ".exe", ".dll", ".so", ".bin",
    ".class", ".jar",
    ".zip", ".tar", ".gz", ".7z",
    ".mp3", ".mp4", ".avi"
}

INPUT_DIR = "."
OUTPUT_FILE = "current-parser.txt"

# =========================
# FILTERS
# =========================

def matches_patterns(filename: str) -> bool:
    return any(re.match(pattern, filename) for pattern in INCLUDE_PATTERNS)


def should_ignore_dir(dirname: str) -> bool:
    return dirname in IGNORE_DIRS


def is_binary_file(filepath: Path) -> bool:
    # Fast check using extension
    if filepath.suffix.lower() in BINARY_EXTENSIONS:
        return True

    # Deeper check: look for null bytes
    try:
        with open(filepath, "rb") as f:
            chunk = f.read(1024)
            if b"\x00" in chunk:
                return True
    except Exception:
        return True  # if unreadable, treat as binary

    return False


# =========================
# CORE LOGIC
# =========================

def collect_files(root_dir: Path):
    collected_files = []

    for dirpath, dirnames, filenames in os.walk(root_dir):
        dirnames[:] = [d for d in dirnames if not should_ignore_dir(d)]

        for filename in filenames:
            if not matches_patterns(filename):
                continue

            full_path = Path(dirpath) / filename

            if is_binary_file(full_path):
                continue

            collected_files.append(full_path)

    return collected_files


def read_file_safe(filepath: Path) -> str:
    try:
        with open(filepath, "r", encoding="utf-8") as f:
            return f.read()
    except Exception:
        try:
            with open(filepath, "r", encoding="latin-1") as f:
                return f.read()
        except Exception as e:
            return f"[ERROR READING FILE: {e}]"


def write_output(files, output_path: Path):
    with open(output_path, "w", encoding="utf-8") as out:
        for file in files:
            out.write(f"\n{'='*80}\n")
            out.write(f"FILE: {file}\n")
            out.write(f"{'='*80}\n\n")

            content = read_file_safe(file)
            out.write(content)
            out.write("\n\n")


# =========================
# ENTRY POINT
# =========================

def main():
    root = Path(INPUT_DIR).resolve()
    output_path = root / OUTPUT_FILE

    print(f"Scanning directory: {root}")

    files = collect_files(root)

    print(f"Found {len(files)} text files matching patterns")

    write_output(files, output_path)

    print(f"Output written to: {output_path}")


if __name__ == "__main__":
    main()