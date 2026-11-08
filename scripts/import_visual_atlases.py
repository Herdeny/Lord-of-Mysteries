import json
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/art-source"
ASSETS = ROOT / "src/main/resources/assets/lord_of_mysteries"
MANIFEST = json.loads((SOURCE / "manifest.json").read_text(encoding="utf-8"))
MATERIALS = ["cloth", "parchment", "iron", "brass", "porcelain", "scales", "thorns",
             "wing", "feathers", "wood", "runestone", "spirit_glass", "ash", "embers",
             "void", "sigil"]


def cell(image, index, columns):
    column, row = index % columns, index // columns
    width, height = image.size
    return image.crop((round(column * width / columns), round(row * height / columns),
                       round((column + 1) * width / columns), round((row + 1) * height / columns)))


def save_image(image, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, optimize=True)


def save_json(data, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def element(start, end, texture):
    return {"from": start, "to": end, "faces": {
        direction: {"texture": "#" + texture}
        for direction in ("north", "south", "east", "west", "up", "down")}}


def workstation_models():
    return {
        "crucible": [
            ([2, 0, 2], [5, 3, 5], "iron"), ([11, 0, 2], [14, 3, 5], "iron"),
            ([2, 0, 11], [5, 3, 14], "iron"), ([11, 0, 11], [14, 3, 14], "iron"),
            ([1, 3, 1], [15, 5, 15], "iron"), ([1, 5, 1], [3, 14, 15], "iron"),
            ([13, 5, 1], [15, 14, 15], "iron"), ([3, 5, 1], [13, 14, 3], "iron"),
            ([3, 5, 13], [13, 14, 15], "iron"), ([0, 9, 6], [1, 12, 10], "brass"),
            ([15, 9, 6], [16, 12, 10], "brass")],
        "ritual_altar": [
            ([0, 0, 0], [16, 3, 16], "runestone"), ([3, 3, 3], [13, 12, 13], "runestone"),
            ([0, 12, 0], [16, 15, 16], "iron"), ([1, 15, 1], [15, 16, 15], "sigil")],
        "characteristic_separator": [
            ([1, 0, 1], [15, 4, 15], "iron"), ([2, 4, 6], [4, 15, 10], "brass"),
            ([12, 4, 6], [14, 15, 10], "brass"), ([2, 14, 6], [14, 16, 10], "brass"),
            ([6, 6, 6], [10, 12, 10], "spirit_glass"), ([4, 4, 4], [12, 6, 12], "sigil")],
        "imprint_washing_altar": [
            ([1, 0, 1], [15, 3, 15], "runestone"), ([3, 3, 3], [13, 9, 13], "porcelain"),
            ([0, 9, 0], [16, 11, 16], "porcelain"), ([0, 11, 0], [2, 16, 16], "brass"),
            ([14, 11, 0], [16, 16, 16], "brass"), ([2, 11, 0], [14, 16, 2], "brass"),
            ([2, 11, 14], [14, 16, 16], "brass"), ([2, 11, 2], [14, 12, 14], "spirit_glass")],
        "commission_board": [
            ([2, 0, 3], [14, 2, 13], "wood"), ([3, 2, 6], [5, 8, 9], "wood"),
            ([11, 2, 6], [13, 8, 9], "wood"), ([0, 7, 6], [16, 16, 10], "wood"),
            ([2, 8, 5.8], [7, 14, 6], "parchment"), ([8, 9, 5.8], [14, 15, 6], "parchment"),
            ([2, 8, 10], [14, 15, 10.2], "parchment")],
        "hunter_snare": [
            ([1, 0, 1], [15, 1, 3], "iron"), ([1, 0, 13], [15, 1, 15], "iron"),
            ([1, 0, 3], [3, 2, 13], "iron"), ([13, 0, 3], [15, 2, 13], "iron"),
            ([6, 0, 6], [10, 1, 10], "brass"), ([3, 0, 7], [13, 1, 9], "iron")],
        "ritual_chalk_mark": [([0, 0, 0], [16, 1, 16], "sigil")],
    }


def main():
    for atlas in MANIFEST["item_atlases"]:
        image = Image.open(SOURCE / atlas["file"]).convert("RGBA")
        for index, item_id in enumerate(atlas["ids"]):
            sprite = cell(image, index, 8)
            sprite = sprite.resize((56, 56), Image.Resampling.NEAREST)
            target = Image.new("RGBA", (64, 64))
            target.paste(sprite, (4, 4))
            save_image(target, ASSETS / "textures/item" / (item_id + ".png"))
            save_image(target, ROOT / "docs/assets/item-icons" / (item_id + ".png"))
            save_json({"parent": "minecraft:item/generated", "textures": {
                "layer0": "lord_of_mysteries:item/" + item_id}},
                ASSETS / "models/item" / (item_id + ".json"))
    material = Image.open(SOURCE / MANIFEST["material_atlas"]["file"]).convert("RGB")
    save_image(material.resize((256, 256), Image.Resampling.NEAREST),
               ASSETS / "textures/entity/occult_atlas.png")
    for index, name in enumerate(MATERIALS):
        save_image(cell(material, index, 4).resize((64, 64), Image.Resampling.NEAREST),
                   ASSETS / "textures/block" / (name + ".png"))
    for name, boxes in workstation_models().items():
        textures = {texture: "lord_of_mysteries:block/" + texture
                    for _, _, texture in boxes}
        textures["particle"] = next(iter(textures.values()))
        save_json({"parent": "minecraft:block/block", "textures": textures,
                   "elements": [element(start, end, texture) for start, end, texture in boxes]},
                  ASSETS / "models/block" / (name + ".json"))
    particles = Image.open(SOURCE / MANIFEST["particle_atlas"]["file"]).convert("RGBA")
    for index, particle_id in enumerate(MANIFEST["particle_atlas"]["ids"]):
        save_image(cell(particles, index, 4).resize((32, 32), Image.Resampling.NEAREST),
                   ASSETS / "textures/particle" / (particle_id + ".png"))
        save_json({"textures": ["lord_of_mysteries:" + particle_id]},
                  ASSETS / "particles" / (particle_id + ".json"))
    print("Imported 123 inventory sprites, 16 material tiles, 7 workstation models, 16 particles.")


if __name__ == "__main__":
    main()
