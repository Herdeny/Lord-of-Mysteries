const entries = [
  {
    type: "item",
    id: "lord_of_mysteries:spirit_herb",
    name: "灵性草药",
    summary: "基础灵性材料，后续可作为采集、掉落、交易与魔药制作输入。",
    tags: ["材料", "M0", "魔药"],
    details: [["来源", "规划中"], ["用途", "魔药材料、仪式材料"], ["注册", "ModItems.SPIRIT_HERB"]]
  },
  {
    type: "item",
    id: "lord_of_mysteries:divination_crystal",
    name: "占卜水晶",
    summary: "占卜与序列 9 魔药用材料，当前作为创造模式标签图标。",
    tags: ["材料", "占卜", "魔药"],
    details: [["用途", "占卜家魔药、仪式聚焦"], ["注册", "ModItems.DIVINATION_CRYSTAL"]]
  },
  {
    type: "item",
    id: "lord_of_mysteries:moonwater",
    name: "月华水",
    summary: "月相或夜晚相关的魔药辅助材料，提供品质加成占位。",
    tags: ["材料", "夜晚", "品质"],
    details: [["品质加成", "+0.2"], ["关联魔药", "占卜家魔药 序列 9"]]
  },
  {
    type: "item",
    id: "lord_of_mysteries:contaminated_mixture",
    name: "污染混合物",
    summary: "魔药制作失败或污染反应的产物。",
    tags: ["失败产物", "污染"],
    details: [["来源", "魔药失败结果"], ["关联系统", "污染与失控压力"]]
  },
  {
    type: "item",
    id: "lord_of_mysteries:eternal_matchbox",
    name: "永燃火柴盒",
    summary: "封印物占位物品，未来会拥有灵火、代价和封印逻辑。",
    tags: ["封印物", "危险等级 5"],
    details: [["效果", "点燃灵性火焰"], ["代价", "每次使用提高愤怒状态"], ["封印方式", "净化封印仪式"]]
  },
  {
    type: "block",
    id: "lord_of_mysteries:ritual_altar",
    name: "仪式祭坛",
    summary: "仪式结构核心方块，当前用于验证方块注册、模型和语言文件。",
    tags: ["方块", "仪式", "M0"],
    details: [["硬度", "3.0"], ["爆炸抗性", "6.0"], ["工具要求", "需要正确工具掉落"]]
  },
  {
    type: "block",
    id: "lord_of_mysteries:crucible",
    name: "坩埚",
    summary: "魔药制作系统载体，后续会绑定 CrucibleBlockEntity。",
    tags: ["方块", "魔药", "方块实体规划"],
    details: [["硬度", "3.5"], ["爆炸抗性", "6.0"], ["后续模块", "坩埚方块实体 + 基础魔药制作"]]
  },
  {
    type: "pathway",
    id: "lord_of_mysteries:seer",
    name: "占卜家途径",
    summary: "以观察、占卜和灵视见长，擅长获取信息但正面战斗能力较弱。",
    tags: ["途径", "信息获取", "灵视"],
    details: [["基础灵性", "100"], ["每序列灵性成长", "22"], ["已规划序列", "9 / 8 / 7"]]
  },
  {
    type: "sequence",
    id: "lord_of_mysteries:seer_9",
    name: "序列 9：占卜家",
    summary: "获得灵视、危险直觉与简单占卜，消化目标为 100%。",
    tags: ["序列 9", "占卜家", "能力"],
    details: [["灵性上限加成", "+22"], ["能力", "灵视、危险直觉、简单占卜"], ["关联魔药", "seer_potion_9"]]
  },
  {
    type: "potion",
    id: "lord_of_mysteries:seer_potion_9",
    name: "占卜家魔药 序列 9",
    summary: "以灵性草药、占卜水晶为必需材料，月华水可提高品质。",
    tags: ["魔药", "序列 9", "坩埚"],
    details: [["温度", "60-80"], ["制作时间", "1200 tick"], ["失败产物", "污染混合物"]]
  },
  {
    type: "ritual",
    id: "lord_of_mysteries:calm_sealing",
    name: "净化封印仪式",
    summary: "围绕仪式祭坛布置半径 3 的圆阵，用于封印异常封印物。",
    tags: ["仪式", "夜晚", "封印"],
    details: [["环境", "夜晚、晴天、Y -60 至 100"], ["材料", "纯水 x3、兰花 x5、白蜡烛 x8"], ["失败风险", "低阶灵体、污染、爆炸"]]
  },
  {
    type: "artifact",
    id: "lord_of_mysteries:eternal_matchbox",
    name: "封印物：永燃火柴盒",
    summary: "危险等级 5，可点燃能伤害灵体的灵性火焰。",
    tags: ["封印物", "灵火", "危险等级 5"],
    details: [["主动效果", "ignite_spirit_flame"], ["持续时间", "100 tick"], ["代价", "anger_buildup +15"]]
  },
  {
    type: "status",
    id: "lord_of_mysteries:pollution",
    name: "污染",
    summary: "核心风险值，25/50/75/100 分别进入更高检定区间。",
    tags: ["Buff/状态", "风险", "失控"],
    details: [["0-24", "稳定"], ["25-49", "轻度异常，每 5 分钟检定"], ["50-74", "危险，每 2 分钟检定"], ["75-99", "临界，每 30 秒检定"], ["100", "立即触发失控结局"]]
  },
  {
    type: "status",
    id: "lord_of_mysteries:spirituality",
    name: "灵性",
    summary: "能力消耗与自然恢复的核心资源，非战斗、光照充足、压力低时恢复。",
    tags: ["Buff/状态", "资源", "恢复"],
    details: [["恢复条件", "非战斗、光照 ≥ 7、失控压力 < 30"], ["默认上限", "100"], ["序列 9 恢复", "0.05/s"]]
  }
];

const labels = {
  item: "物品",
  block: "方块",
  pathway: "途径",
  sequence: "序列",
  potion: "魔药",
  ritual: "仪式",
  artifact: "封印物",
  status: "Buff/状态"
};

const cards = document.querySelector("#cards");
const search = document.querySelector("#search");
const filterButtons = [...document.querySelectorAll(".filter")];
let activeFilter = "all";

function render() {
  const keyword = search.value.trim().toLowerCase();
  const filtered = entries.filter((entry) => {
    const matchesType = activeFilter === "all" || entry.type === activeFilter;
    const haystack = [entry.id, entry.name, entry.summary, ...entry.tags].join(" ").toLowerCase();
    return matchesType && (!keyword || haystack.includes(keyword));
  });

  cards.innerHTML = "";

  if (filtered.length === 0) {
    cards.innerHTML = '<div class="empty">没有找到匹配条目。</div>';
    return;
  }

  for (const entry of filtered) {
    const el = document.createElement("article");
    el.className = "card";
    el.id = entry.id.replace(":", "-");
    el.innerHTML = `
      <div class="card-head">
        <div class="icon ${entry.type}" aria-hidden="true">${entry.name.slice(0, 1)}</div>
        <div>
          <h3>${entry.name}</h3>
          <p class="tagline">${entry.id}</p>
        </div>
      </div>
      <p>${entry.summary}</p>
      <div class="meta">
        <span class="pill">${labels[entry.type]}</span>
        ${entry.tags.map((tag) => `<span class="pill">${tag}</span>`).join("")}
      </div>
      <ul class="details">
        ${entry.details.map(([key, value]) => `<li><strong>${key}：</strong>${value}</li>`).join("")}
      </ul>
    `;
    cards.appendChild(el);
  }
}

function updateStats() {
  const count = (type) => entries.filter((entry) => entry.type === type).length;
  document.querySelector("#stat-items").textContent = count("item");
  document.querySelector("#stat-blocks").textContent = count("block");
  document.querySelector("#stat-systems").textContent = entries.filter((entry) => !["item", "block"].includes(entry.type)).length;
  document.querySelector("#stat-data").textContent = 6;
}

filterButtons.forEach((button) => {
  button.addEventListener("click", () => {
    activeFilter = button.dataset.filter;
    filterButtons.forEach((item) => item.classList.toggle("active", item === button));
    render();
  });
});

search.addEventListener("input", render);
updateStats();
render();
