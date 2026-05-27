#!/usr/bin/env python3

import sys
import zlib
import tarfile
from io import BytesIO

if len(sys.argv) != 2:
    print(f"Usage: {sys.argv[0]} backup.ab")
    sys.exit(1)

ab_file = sys.argv[1]

with open(ab_file, "rb") as f:
    data = f.read()

# Skip Android backup header (24 bytes)
payload = data[24:]

# Decompress zlib stream
tar_data = zlib.decompress(payload)

# Extract tar contents
with tarfile.open(fileobj=BytesIO(tar_data)) as tar:
    tar.extractall()

print("Backup extracted successfully.")