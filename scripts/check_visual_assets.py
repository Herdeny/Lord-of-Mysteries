import json
import hashlib
import re
import struct
import sys
import zlib
from pathlib import Path

from gen_pages_catalog import ROOT, load_registry_ids


def check():
    root = ROOT
    assets = root / "src/main/resources/assets/lord_of_mysteries"
    java = root / "src/main/java/top/aurora/lordofmysteries"
    errors = []
    items, blocks, entities = load_registry_ids()
    client = (java / "client/ClientModEvents.java").read_text(encoding="utf-8")
    registered = set(re.findall(r"registerEntityRenderer\(ModEntities\.([A-Z_]+)\.get", client))
    for entity in entities:
        if entity.upper() not in registered:
            errors.append("Missing entity renderer: " + entity)
    if re.search(r"ZombieRenderer|NoopRenderer|CaveSpiderRenderer|HuskRenderer", client):
        errors.append("Vanilla placeholder renderer reintroduced")
    for item in items:
        if item.endswith("_spawn_egg"):
            continue
        model = assets / "models/item" / (item + ".json")
        payload = json.loads(model.read_text(encoding="utf-8"))
        textures = payload.get("textures", {})
        if item not in blocks and not any(value.startswith("lord_of_mysteries:item/") for value in textures.values()):
            errors.append("Missing original inventory sprite: " + item)
    for block in blocks:
        model = json.loads((assets / "models/block" / (block + ".json")).read_text(encoding="utf-8"))
        if not model.get("elements"):
            errors.append("Missing workstation geometry: " + block)
        for element in model.get("elements", []):
            if any(not 0 <= number <= 16 for key in ("from", "to") for number in element[key]):
                errors.append("Block geometry outside cell: " + block)
    manifest = json.loads((root / "docs/art-source/manifest.json").read_text(encoding="utf-8"))
    for name, expected in manifest["source_sha256"].items():
        if hashlib.sha256((root / "docs/art-source" / name).read_bytes()).hexdigest() != expected:
            errors.append("Source atlas provenance hash drift: " + name)
    for sheet in manifest["item_atlases"]:
        for item in sheet["ids"]:
            runtime = assets / "textures/item" / (item + ".png")
            web = root / "docs/assets/item-icons" / (item + ".png")
            if not runtime.exists() or not web.exists() or runtime.read_bytes() != web.read_bytes():
                errors.append("Pages/runtime sprite drift: " + item)
    particle_code = (java / "registry/ModParticles.java").read_text(encoding="utf-8")
    particles = re.findall(r'= register\("([a-z_]+)"\)', particle_code)
    if set(particles) != set(manifest["particle_atlas"]["ids"]):
        errors.append("Particle atlas and registry differ")
    for particle in particles:
        if not (assets / "particles" / (particle + ".json")).exists():
            errors.append("Missing particle definition: " + particle)
        if "ModParticles." + particle.upper() + ".get()" not in client:
            errors.append("Missing particle provider: " + particle)
    for path in (assets / "textures").rglob("*.png"):
        data = path.read_bytes()
        if data[:8] != b"\x89PNG\r\n\x1a\n":
            errors.append("Invalid PNG: " + str(path))
            continue
        width, height = struct.unpack(">II", data[16:24])
        if width != height or width not in (32, 64, 256):
            errors.append("Texture outside size budget: " + str(path))
        offset = 8
        while offset < len(data):
            length = struct.unpack(">I", data[offset:offset + 4])[0]
            chunk = data[offset + 4:offset + 8 + length]
            checksum = data[offset + 8 + length:offset + 12 + length]
            if len(checksum) != 4 or zlib.crc32(chunk) != struct.unpack(">I", checksum)[0]:
                errors.append("Corrupt PNG chunk: " + str(path))
                break
            offset += length + 12
    sprites = sum(len(sheet["ids"]) for sheet in manifest["item_atlases"])
    return errors, len(entities), sprites, len(blocks), len(particles)


def main():
    errors, entities, sprites, blocks, particles = check()
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1
    print(f"Visual coverage passed: {entities} entity renderers, {sprites} sprites, {blocks} workstations, {particles} particle providers.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
