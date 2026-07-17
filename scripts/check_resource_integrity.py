#!/usr/bin/env python3

import json
import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MAIN_RESOURCES = ROOT / "src" / "main" / "resources"
GENERATED_RESOURCES = ROOT / "src" / "generated" / "resources"
ASSETS = MAIN_RESOURCES / "assets" / "lord_of_mysteries"
LANG = ASSETS / "lang"
JAVA = ROOT / "src" / "main" / "java"
REGISTRY = JAVA / "top" / "aurora" / "lordofmysteries" / "registry"
NAMESPACE = "lord_of_mysteries"


def load_json(path, errors):
    try:
        with path.open(encoding="utf-8") as stream:
            return json.load(stream)
    except (OSError, json.JSONDecodeError) as error:
        errors.append(f"{path.relative_to(ROOT)}: invalid JSON: {error}")
        return None


def internal_reference(value):
    if not isinstance(value, str) or ":" not in value:
        return None
    namespace, path = value.split(":", 1)
    return path if namespace == NAMESPACE else None


def check_json(errors):
    files = sorted(MAIN_RESOURCES.rglob("*.json"))
    files.extend(sorted(GENERATED_RESOURCES.rglob("*.json")))
    for path in files:
        load_json(path, errors)
    return len(files)


def collect_translation_keys(value, keys, field=""):
    if isinstance(value, dict):
        for key, child in value.items():
            collect_translation_keys(child, keys, key)
    elif isinstance(value, list):
        for child in value:
            collect_translation_keys(child, keys, field)
    elif isinstance(value, str) and (
            field.endswith("_key") or field == "translate"):
        keys.add(value)


def check_languages(errors):
    languages = {}
    for locale in ("en_us", "zh_cn"):
        payload = load_json(LANG / f"{locale}.json", errors)
        if not isinstance(payload, dict):
            errors.append(f"{locale}.json must contain a JSON object")
            payload = {}
        languages[locale] = payload

    en_keys = set(languages["en_us"])
    zh_keys = set(languages["zh_cn"])
    for key in sorted(zh_keys - en_keys):
        errors.append(f"en_us.json missing translation: {key}")
    for key in sorted(en_keys - zh_keys):
        errors.append(f"zh_cn.json missing translation: {key}")

    used = set()
    pattern = re.compile(
        r'(?:Component|I18n)\.translatable\(\s*"([^"]+)"'
    )
    for path in JAVA.rglob("*.java"):
        used.update(pattern.findall(path.read_text(encoding="utf-8")))
    for root in (MAIN_RESOURCES, GENERATED_RESOURCES):
        for path in root.rglob("*.json"):
            if path.parent == LANG:
                continue
            payload = load_json(path, errors)
            if payload is not None:
                collect_translation_keys(payload, used)

    exact_keys = {
        key for key in used
        if key and not key.endswith(".") and not key.endswith("_")
    }
    for locale, payload in languages.items():
        for key in sorted(exact_keys - set(payload)):
            errors.append(f"{locale}.json missing referenced translation: {key}")
    return len(en_keys), len(exact_keys)


def require_model(reference, source, errors):
    path = internal_reference(reference)
    if path is None:
        return
    target = ASSETS / "models" / f"{path}.json"
    if not target.exists():
        errors.append(
            f"{source.relative_to(ROOT)}: missing model {reference}"
        )


def require_texture(reference, source, errors):
    if not isinstance(reference, str) or reference.startswith("#"):
        return
    path = internal_reference(reference)
    if path is None:
        return
    target = ASSETS / "textures" / f"{path}.png"
    if not target.exists():
        errors.append(
            f"{source.relative_to(ROOT)}: missing texture {reference}"
        )


def check_assets(errors):
    model_count = 0
    for path in sorted((ASSETS / "models").rglob("*.json")):
        payload = load_json(path, errors)
        if not isinstance(payload, dict):
            continue
        model_count += 1
        require_model(payload.get("parent"), path, errors)
        for reference in payload.get("textures", {}).values():
            require_texture(reference, path, errors)

    for path in sorted((ASSETS / "blockstates").rglob("*.json")):
        payload = load_json(path, errors)

        def walk(value):
            if isinstance(value, dict):
                if "model" in value:
                    require_model(value["model"], path, errors)
                for child in value.values():
                    walk(child)
            elif isinstance(value, list):
                for child in value:
                    walk(child)

        walk(payload)
    return model_count


def registrations(path, registry_name, helper=None):
    source = path.read_text(encoding="utf-8")
    names = set(re.findall(
        rf'{registry_name}\.register\(\s*"([a-z0-9_]+)"',
        source, re.DOTALL,
    ))
    if helper:
        names.update(re.findall(
            rf'{helper}\(\s*"([a-z0-9_]+)"', source, re.DOTALL,
        ))
    return names


def check_registries(errors):
    languages = load_json(LANG / "en_us.json", errors) or {}
    items = registrations(REGISTRY / "ModItems.java", "ITEMS", "simple")
    blocks = registrations(REGISTRY / "ModBlocks.java", "BLOCKS")
    entities = registrations(REGISTRY / "ModEntities.java", "ENTITIES")

    for name in sorted(items):
        if name not in blocks and f"item.{NAMESPACE}.{name}" not in languages:
            errors.append(f"missing item translation for {NAMESPACE}:{name}")
        if not (ASSETS / "models" / "item" / f"{name}.json").exists():
            errors.append(f"missing item model for {NAMESPACE}:{name}")
    for name in sorted(blocks):
        if f"block.{NAMESPACE}.{name}" not in languages:
            errors.append(f"missing block translation for {NAMESPACE}:{name}")
        if not (ASSETS / "blockstates" / f"{name}.json").exists():
            errors.append(f"missing blockstate for {NAMESPACE}:{name}")
        if not (ASSETS / "models" / "block" / f"{name}.json").exists():
            errors.append(f"missing block model for {NAMESPACE}:{name}")
    for name in sorted(entities):
        if f"entity.{NAMESPACE}.{name}" not in languages:
            errors.append(f"missing entity translation for {NAMESPACE}:{name}")
    return len(items), len(blocks), len(entities)


def main():
    errors = []
    json_count = check_json(errors)
    translation_count, referenced_count = check_languages(errors)
    model_count = check_assets(errors)
    item_count, block_count, entity_count = check_registries(errors)
    if errors:
        print("resource integrity check failed:", file=sys.stderr)
        for error in dict.fromkeys(errors):
            print(f"- {error}", file=sys.stderr)
        return 1
    print(
        "resource integrity checked: "
        f"{json_count} JSON files, {translation_count} paired translations, "
        f"{referenced_count} referenced keys, {model_count} models, "
        f"{item_count} items, {block_count} blocks, {entity_count} entities"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
