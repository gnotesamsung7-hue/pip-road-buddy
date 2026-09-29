"""4-byte align uncompressed entries in an APK (same job as zipalign)."""
import sys, zipfile
src = zipfile.ZipFile(sys.argv[1]); out = zipfile.ZipFile(sys.argv[2], 'w')
for info in src.infolist():
    data = src.read(info.filename)
    ni = zipfile.ZipInfo(info.filename, date_time=(2026, 1, 1, 0, 0, 0))
    ni.compress_type = info.compress_type; ni.external_attr = info.external_attr
    if ni.compress_type == zipfile.ZIP_STORED:
        off = out.fp.tell() + 30 + len(ni.filename.encode())
        ni.extra = b'\0' * ((4 - off % 4) % 4)
    out.writestr(ni, data)
out.close()
