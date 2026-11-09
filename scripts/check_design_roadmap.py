import argparse
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"
STATES = {"documented", "partial", "not_started"}


def load(name):
    return json.loads((DOCS / name).read_text(encoding="utf-8"))


def validate(audit, backlog, headings, cases):
    entries = audit["entries"]
    if audit["complete"] is not False:
        raise ValueError("Chapter audit is not a completion certificate")
    numbers = [entry["chapter"] for entry in entries]
    if len(set(numbers)) != len(numbers) or set(numbers) != set(headings):
        raise ValueError("Design chapter coverage is missing or duplicated")
    for entry in entries:
        if entry["title"] != headings[entry["chapter"]] or entry["state"] not in STATES:
            raise ValueError(f"Invalid chapter identity/state: {entry['id']}")
        if not entry["gap"] or not entry["next_action"] or not entry["evidence"]:
            raise ValueError(f"Missing evidence or remaining work: {entry['id']}")
        for reference in entry["evidence"]:
            path = (ROOT / reference).resolve()
            if not path.is_relative_to(ROOT.resolve()) or not path.exists():
                raise ValueError(f"Invalid evidence path: {reference}")
    tasks = backlog["tasks"]
    by_id = {task["id"]: task for task in tasks}
    if len(by_id) != len(tasks):
        raise ValueError("Duplicate implementation task ID")
    visited = set()
    active = set()

    def visit(task_id):
        if task_id not in by_id:
            raise ValueError(f"Unknown dependency: {task_id}")
        if task_id in active:
            raise ValueError(f"Dependency cycle: {task_id}")
        if task_id in visited:
            return
        task = by_id[task_id]
        if task["state"] != "planned":
            raise ValueError("This proposal must not silently claim runtime completion")
        if not task["deliverable"] or not task["acceptance"] or not task["v09_chapters"]:
            raise ValueError(f"Task has no delivery/acceptance/source: {task_id}")
        if not set(task["acceptance"]) <= cases or not set(task["v09_chapters"]) <= set(headings):
            raise ValueError(f"Unknown acceptance case or v0.9 chapter: {task_id}")
        active.add(task_id)
        for dependency in task["depends_on"]:
            visit(dependency)
        active.remove(task_id)
        visited.add(task_id)

    for task_id in by_id:
        visit(task_id)


def render(audit):
    labels = {"documented": "文档已记录/未代表玩法完成", "partial": "已有部分实现/仍有缺口", "not_started": "主要玩法尚未实现"}
    lines = [
        "# v0.9 全章收束审计",
        "",
        f"审计日期：{audit['audit_date']}；起点 commit：{audit['baseline_commit']}。",
        "当前运行版本由 [project-status.json](../project-status.json) 维护。",
        "",
        "**结论：v0.9 尚未全部完成。** 当前 M0–M5 的 done 是既定机器实施范围，不是原文全部功能或真人验收完成。M6–M12 仍有大量实质内容。",
        "",
        "本表覆盖原文0–81章，共82个章节入口。它不是逐个表格行的完工证明，证据路径只说明已有工作落点，不证明整个章节实现。",
        "文档标题中的“完整实现”“完整代码”是用户设计源文本，不得误读为仓库现状。",
        "",
        "## 已知规模差异",
        "",
        "- 现有五途径9–5不能算作22途径全部完成；M6四条、M8六条、M9七条仍待完整纵切。",
        "- 12组织、24保管封印物、12灵界生态实体不等于原文全部世界内容。",
        "- 2种草药/2类矿石/2原生遗迹及生存加工是扩展子集；不是36新植物、40新生物、32结构增量全部实装。",
        "- v0.9 manifest中的104物品、80配方、64状态、38粒子等是设计增量数，必须按ID/语义核对，不能拿注册总数直接比较。",
        "- 真人4人8小时、20人性能、12周世界模拟、100小时公开测试及授权/原创化审阅，不能由JUnit/GameTest计数替代。",
        "",
        "## 按章追踪",
        "",
        "| ID / 章节 | 状态 | 已有落点 | 剩余工作 |",
        "|---|---|---|---|",
    ]
    for entry in audit["entries"]:
        evidence = " · ".join(f"[{reference}](../{reference})" for reference in entry["evidence"])
        lines.append(f"| {entry['id']} · {entry['title']} | {labels[entry['state']]} | {evidence} | {entry['gap']} |")
    lines += [
        "",
        "## 收束次序",
        "",
        "先修现有缺陷和真人质量门 → M6逐途径纵切 → M7生产/兼容 → M8/M9剩余途径 → M10/M11高序列与世界 → M12可关闭终局 → 发布候选。",
        "每项需拆成获得/使用/风险/恢复/多人/持久化/美术/文案/测试九个维度；没有证据保留未完成，不关闭上层史诗Issue。",
        "",
        "## 后续设计",
        "",
        "- [1.0实施指导书](PROJECT_MYSTERY_1_0_GUIDE.md)：代表性官方资料调研、20个设计领域、生产/生态/动画/架构和发布约束。",
        "- [39项实施任务](v1-implementation-backlog.json)：依赖图、原文出处、交付和验收用例。",
        "- [48类验收矩阵](V1_ACCEPTANCE_MATRIX.md)：目标，不是已跑完的测试报告。",
        "",
        "本页由 scripts/check_design_roadmap.py 生成；源为 v09-completion-audit.json。禁止通过修改文档状态来冒充代码完成。",
    ]
    return "\n".join(lines) + "\n"


def wiki_links(text):
    def convert(match):
        target = match.group(1)
        if "://" in target or target.startswith("#"):
            return match.group(0)
        path, separator, anchor = target.partition("#")
        resolved = (DOCS / path).resolve().relative_to(ROOT.resolve()).as_posix()
        return "](https://github.com/Herdeny/Lord-of-Mysteries/blob/main/" + resolved + (separator + anchor if separator else "") + ")"
    return re.sub(r"\]\(([^)]+)\)", convert, text)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    arguments = parser.parse_args()
    design = (DOCS / "Project_Mystery_Design_Doc_v0_9.md").read_text(encoding="utf-8")
    headings = {int(number): title for number, title in re.findall(r"^# (\d+)\. (.+)$", design, re.MULTILINE)}
    cases = set(re.findall(r"^\| (T\d+) \|", (DOCS / "V1_ACCEPTANCE_MATRIX.md").read_text(encoding="utf-8"), re.MULTILINE))
    audit = load("v09-completion-audit.json")
    backlog = load("v1-implementation-backlog.json")
    validate(audit, backlog, headings, cases)
    expected = render(audit)
    outputs = {DOCS / "V09_COMPLETION_AUDIT.md": expected,
               ROOT / "wiki/V09-Completion-Audit.md": wiki_links(expected)}
    for source, destination in [
        ("PROJECT_MYSTERY_1_0_GUIDE.md", "1.0-Design-Guide.md"),
        ("V1_ACCEPTANCE_MATRIX.md", "1.0-Acceptance-Matrix.md"),
        ("SURVIVAL_RESOURCE_LOOP.md", "Survival-Resource-Loop.md"),
    ]:
        outputs[ROOT / "wiki" / destination] = wiki_links((DOCS / source).read_text(encoding="utf-8"))
    for output, content in outputs.items():
        if arguments.check:
            if not output.exists() or output.read_text(encoding="utf-8") != content:
                raise SystemExit(f"Document mirror drift: {output.relative_to(ROOT)}; run python scripts/check_design_roadmap.py")
        else:
            output.write_text(content, encoding="utf-8", newline="\n")
    print(f"Design audit: {len(headings)} chapters, {len(backlog['tasks'])} tasks, {len(cases)} acceptance cases; not a completion certificate")


if __name__ == "__main__":
    main()
