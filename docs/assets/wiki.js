/* Project Mystery Pages — progressive, accessible, dependency-free UI. */
(function () {
  "use strict";

  var data = window.LOM || {};
  var meta = data.meta || {};
  var entries = data.entries || [];
  var labels = data.labels || {};
  var PAGE_SIZE = 24;
  var $ = function (selector, root) { return (root || document).querySelector(selector); };
  var create = function (tag, className, text) {
    var node = document.createElement(tag);
    if (className) node.className = className;
    if (text != null) node.textContent = text;
    return node;
  };

  function setText(selector, value) {
    var node = $(selector);
    if (node) node.textContent = value || "—";
  }

  /* Theme */
  var themeToggle = $("#theme-toggle");
  var savedTheme = null;
  try { savedTheme = localStorage.getItem("lom-theme"); } catch (error) {}
  if (savedTheme === "dark" || savedTheme === "light") {
    document.documentElement.setAttribute("data-theme", savedTheme);
  }
  function syncThemeControl() {
    if (!themeToggle) return;
    var isLight = document.documentElement.getAttribute("data-theme") === "light";
    themeToggle.setAttribute("aria-pressed", isLight ? "true" : "false");
    themeToggle.title = isLight ? "切换到深色主题" : "切换到浅色主题";
  }
  syncThemeControl();
  if (themeToggle) {
    themeToggle.addEventListener("click", function () {
      var next = document.documentElement.getAttribute("data-theme") === "light" ? "dark" : "light";
      document.documentElement.setAttribute("data-theme", next);
      try { localStorage.setItem("lom-theme", next); } catch (error) {}
      syncThemeControl();
    });
  }

  /* Mobile navigation */
  var navToggle = $("#nav-toggle");
  var topnav = $("#topnav");
  function setNav(open) {
    if (!topnav || !navToggle) return;
    topnav.classList.toggle("open", open);
    navToggle.setAttribute("aria-expanded", open ? "true" : "false");
    navToggle.setAttribute("aria-label", open ? "收起导航菜单" : "展开导航菜单");
    navToggle.textContent = open ? "×" : "☰";
  }
  if (navToggle && topnav) {
    navToggle.addEventListener("click", function (event) {
      event.stopPropagation();
      setNav(!topnav.classList.contains("open"));
    });
    topnav.addEventListener("click", function (event) {
      if (event.target.closest("a")) setNav(false);
    });
    document.addEventListener("click", function (event) {
      if (topnav.classList.contains("open") && !topnav.contains(event.target) && event.target !== navToggle) {
        setNav(false);
      }
    });
    addEventListener("resize", function () {
      if (innerWidth > 820) setNav(false);
    });
  }

  /* Release metadata */
  setText("#hero-version", meta.version);
  setText("#hero-milestone", (window.LOM_PROJECT_META || {}).milestone || "M2");
  setText("#release-version", meta.version);
  setText("#release-stage", meta.stage);
  var metaDl = $("#meta-dl");
  if (metaDl) {
    [
      ["Minecraft", meta.mc],
      ["加载器", meta.loader],
      ["Java", meta.java],
      ["更新时间", meta.lastUpdatedDisplay]
    ].forEach(function (pair) {
      var item = create("div");
      item.appendChild(create("dt", null, pair[0]));
      item.appendChild(create("dd", null, pair[1] || "—"));
      metaDl.appendChild(item);
    });
  }
  var heroTags = $("#hero-tags");
  if (heroTags) {
    ["五条途径", "序列晋升", "魔药炼制", "调查推理", "失控风险"].forEach(function (label) {
      heroTags.appendChild(create("span", "pill type", label));
    });
  }
  var lastUpdated = $("#last-updated");
  if (lastUpdated) {
    lastUpdated.textContent = "版本 " + (meta.version || "—") + " · 最后更新 " +
      (meta.lastUpdatedDisplay || "—") + " · UTC " + (meta.lastUpdatedUtc || "—");
  }

  /* Summary statistics */
  var catalogMeta = data.catalogMeta || {};
  var registeredContent = (catalogMeta.registeredItems || 0) +
    (catalogMeta.registeredBlocks || 0) + (catalogMeta.registeredEntities || 0);
  var stats = [
    [entries.length, "图鉴条目"],
    [registeredContent, "注册内容"],
    [(data.pathwaysOverview || []).length, "首发途径"],
    [entries.filter(function (entry) { return entry.type === "ability"; }).length, "能力说明"]
  ];
  var statsRoot = $("#stats");
  if (statsRoot) {
    stats.forEach(function (item) {
      var stat = create("div", "stat");
      stat.appendChild(create("strong", null, String(item[0])));
      stat.appendChild(create("span", null, item[1]));
      statsRoot.appendChild(stat);
    });
  }

  /* Core loop */
  var loopRoot = $("#loop");
  if (loopRoot) {
    [
      ["发现线索", "从营地、城市委托和动态案件中确定目标，先理解风险再行动。"],
      ["收集材料", "调查、狩猎、工作与交易提供魔药、仪式和恢复所需资源。"],
      ["炼制魔药", "在坩埚中按途径配方控温投料，顺序与温度共同决定结果。"],
      ["服用晋升", "服务端检查同途径、消化度、序列顺序与特性守恒后完成晋升。"],
      ["扮演消化", "按当前身份原则行动，用能力、调查与生活行为推进消化。"],
      ["管理风险", "持续监控灵性、压力、污染与暴露，恢复后再进入更深案件。"]
    ].forEach(function (item) {
      var row = create("li");
      row.appendChild(create("h4", null, item[0]));
      row.appendChild(create("p", null, item[1]));
      loopRoot.appendChild(row);
    });
  }

  /* Pathway cards */
  var pathwayGrid = $("#pathway-grid");
  if (pathwayGrid) {
    (data.pathwaysOverview || []).forEach(function (pathway) {
      var card = create("article", "pw-card");
      card.style.setProperty("--accent", pathway.accent);
      var head = create("div", "pw-head");
      head.appendChild(create("h3", null, pathway.name));
      head.appendChild(create("span", "en", pathway.en));
      card.appendChild(head);
      card.appendChild(create("span", "pw-status", pathway.status));
      card.appendChild(create("p", "pw-desc", pathway.desc));
      var traits = create("div", "pw-traits");
      (pathway.traits || []).forEach(function (trait) {
        traits.appendChild(create("span", "pill", trait));
      });
      card.appendChild(traits);
      var spirit = create("p", "pw-spirit");
      spirit.textContent = pathway.spirit
        ? "灵性上限 " + pathway.spirit
        : "基础灵性 " + pathway.baseSpirit + " · 每序列成长 +" + pathway.growth;
      card.appendChild(spirit);
      pathwayGrid.appendChild(card);
    });
  }

  /* Sequence explorer */
  var sequenceSets = [
    { id: "seer", name: "占卜家", accent: "#7c5cff", entries: (data.seerSequences || []).map(function (entry) { return Object.assign({ pathway: "占卜家" }, entry); }) },
    { id: "spectator", name: "观众", accent: "#6bcad0", entries: data.spectatorSequences || [] },
    { id: "hunter", name: "猎人", accent: "#cc5b4d", entries: data.hunterSequences || [] },
    { id: "thief", name: "偷盗者", accent: "#74b86f", entries: (data.foundationSequences || []).filter(function (entry) { return entry.pathway === "偷盗者"; }) },
    { id: "apprentice", name: "学徒", accent: "#d4af37", entries: (data.foundationSequences || []).filter(function (entry) { return entry.pathway === "学徒"; }) }
  ];
  var sequenceTabs = $("#sequence-tabs");
  var sequenceLadder = $("#seq-ladder");
  var sequenceSummary = $("#sequence-summary");
  var activePathway = "seer";

  function renderSequences(pathwayId, focusPanel) {
    var group = sequenceSets.filter(function (set) { return set.id === pathwayId; })[0] || sequenceSets[0];
    activePathway = group.id;
    if (sequenceTabs) {
      [].forEach.call(sequenceTabs.children, function (tab) {
        var selected = tab.dataset.pathway === group.id;
        tab.setAttribute("aria-selected", selected ? "true" : "false");
        tab.tabIndex = selected ? 0 : -1;
      });
    }
    if (!sequenceLadder) return;
    sequenceLadder.innerHTML = "";
    var currentEntries = group.entries.filter(function (entry) { return entry.seq >= 5; });
    currentEntries.forEach(function (entry) {
      var row = create("article", "seq-row");
      row.style.setProperty("--pathway-accent", group.accent);
      row.appendChild(create("div", "seq-num", String(entry.seq)));
      var info = create("div", "seq-info");
      var heading = create("h3", null, entry.name);
      if (entry.seq <= 6) heading.appendChild(create("span", "sequence-state", " · code_ready"));
      info.appendChild(heading);
      info.appendChild(create("p", null, (entry.abilities || []).join(" · ")));
      row.appendChild(info);
      var spirit = create("div", "seq-spirit");
      spirit.appendChild(create("strong", null, String(entry.spiritMax)));
      spirit.appendChild(document.createTextNode("灵性上限"));
      row.appendChild(spirit);
      sequenceLadder.appendChild(row);
    });
    if (sequenceSummary) {
      sequenceSummary.textContent = group.name + " · 当前展示序列 9–5 · 序列 9–7 playable，序列 6–5 code_ready";
    }
    if (focusPanel) sequenceLadder.focus({ preventScroll: true });
  }

  if (sequenceTabs) {
    sequenceSets.forEach(function (group) {
      var tab = create("button", "sequence-tab", group.name);
      tab.type = "button";
      tab.role = "tab";
      tab.dataset.pathway = group.id;
      tab.setAttribute("aria-controls", "seq-ladder");
      tab.addEventListener("click", function () { renderSequences(group.id, false); });
      tab.addEventListener("keydown", function (event) {
        if (event.key !== "ArrowLeft" && event.key !== "ArrowRight") return;
        event.preventDefault();
        var currentIndex = sequenceSets.findIndex(function (set) { return set.id === activePathway; });
        var delta = event.key === "ArrowRight" ? 1 : -1;
        var nextIndex = (currentIndex + delta + sequenceSets.length) % sequenceSets.length;
        renderSequences(sequenceSets[nextIndex].id, false);
        var nextTab = sequenceTabs.querySelector('[data-pathway="' + sequenceSets[nextIndex].id + '"]');
        if (nextTab) nextTab.focus();
      });
      sequenceTabs.appendChild(tab);
    });
    renderSequences(activePathway, false);
  }

  /* Roadmap with progressive disclosure */
  var roadmapRoot = $("#roadmap-track");
  var futureRoadmapRoot = $("#future-roadmap-track");
  var roadmap = data.roadmap || [];
  var stateText = { done: "已完成", active: "进行中", planned: "规划", future: "远期" };
  roadmap.forEach(function (milestone, index) {
    if (index < 4 && roadmapRoot) {
      var card = create("article", "rm-card " + milestone.state);
      var head = create("div", "rm-card-head");
      head.appendChild(create("span", "rm-badge", milestone.id));
      head.appendChild(create("span", "rm-state", stateText[milestone.state] || milestone.state));
      card.appendChild(head);
      card.appendChild(create("h3", null, milestone.title));
      card.appendChild(create("p", null, milestone.summary));
      var details = create("details");
      details.appendChild(create("summary", null, "查看关键范围"));
      var list = create("ul");
      (milestone.points || []).slice(0, 5).forEach(function (point) {
        list.appendChild(create("li", null, point));
      });
      details.appendChild(list);
      card.appendChild(details);
      roadmapRoot.appendChild(card);
    } else if (futureRoadmapRoot) {
      var row = create("div", "future-row");
      row.appendChild(create("b", null, milestone.id));
      row.appendChild(create("strong", null, milestone.title));
      row.appendChild(create("span", null, milestone.summary));
      futureRoadmapRoot.appendChild(row);
    }
  });

  /* Catalog */
  var filterGroup = $("#filter-group");
  var cardsRoot = $("#cards");
  var searchInput = $("#search");
  var clearSearch = $("#clear-search");
  var resultCount = $("#result-count");
  var visibleCount = $("#visible-count");
  var loadMore = $("#load-more");
  var coverageRoot = $("#catalog-coverage");
  var activeFilter = "all";
  var visibleLimit = PAGE_SIZE;
  var filteredEntries = [];

  if (coverageRoot) {
    [
      "自动同步",
      "物品 " + (catalogMeta.registeredItems || 0),
      "方块 " + (catalogMeta.registeredBlocks || 0),
      "实体 " + (catalogMeta.registeredEntities || 0),
      "数据源：" + (catalogMeta.source || "仓库数据")
    ].forEach(function (label) {
      coverageRoot.appendChild(create("span", "coverage-chip", label));
    });
  }

  function entryIcon(entry) {
    return (entry.name || "?").slice(0, 1);
  }

  function normalizedSearchText(entry) {
    return [
      entry.id,
      entry.name,
      entry.en,
      entry.summary,
      (entry.tags || []).join(" ")
    ].join(" ").toLowerCase();
  }

  function renderCatalog() {
    if (!cardsRoot) return;
    var keyword = (searchInput && searchInput.value.trim().toLowerCase()) || "";
    filteredEntries = entries.filter(function (entry) {
      var matchesType = activeFilter === "all" || entry.type === activeFilter;
      return matchesType && (!keyword || normalizedSearchText(entry).indexOf(keyword) >= 0);
    });
    cardsRoot.innerHTML = "";
    var shown = filteredEntries.slice(0, visibleLimit);
    shown.forEach(function (entry) {
      var card = create("button", "card");
      card.type = "button";
      card.setAttribute("aria-label", "查看 " + entry.name + " 详情");
      var head = create("div", "card-head");
      head.appendChild(create("div", "icon " + entry.type, entryIcon(entry)));
      var title = create("div");
      title.appendChild(create("h3", null, entry.name));
      title.appendChild(create("p", "tagline", (entry.en ? entry.en + " · " : "") + entry.id));
      head.appendChild(title);
      card.appendChild(head);
      card.appendChild(create("p", null, entry.summary || ""));
      var tags = create("div", "meta");
      tags.appendChild(create("span", "pill type", labels[entry.type] || entry.type));
      (entry.tags || []).slice(0, 3).forEach(function (tag) {
        tags.appendChild(create("span", "pill", tag));
      });
      card.appendChild(tags);
      card.addEventListener("click", function () { openModal(entry, card); });
      cardsRoot.appendChild(card);
    });
    if (!shown.length) {
      cardsRoot.appendChild(create("div", "empty", "没有找到匹配条目。请尝试更短的关键词或切换分类。"));
    }
    if (resultCount) {
      resultCount.textContent = "共 " + filteredEntries.length + " 条结果";
    }
    if (visibleCount) {
      visibleCount.textContent = filteredEntries.length
        ? "已显示 " + shown.length + " / " + filteredEntries.length
        : "调整搜索条件后重试";
    }
    if (loadMore) {
      loadMore.hidden = shown.length >= filteredEntries.length;
      loadMore.textContent = "再加载 " + Math.min(PAGE_SIZE, filteredEntries.length - shown.length) + " 条";
    }
    if (clearSearch) clearSearch.hidden = !keyword;
  }

  var types = ["all"].concat(Object.keys(labels).filter(function (type) {
    return entries.some(function (entry) { return entry.type === type; });
  }));
  if (filterGroup) {
    types.forEach(function (type) {
      var count = type === "all"
        ? entries.length
        : entries.filter(function (entry) { return entry.type === type; }).length;
      var button = create("button", "filter");
      button.type = "button";
      button.dataset.filter = type;
      button.setAttribute("aria-pressed", type === "all" ? "true" : "false");
      button.appendChild(create("span", null, type === "all" ? "全部" : (labels[type] || type)));
      button.appendChild(create("span", "cnt", String(count)));
      button.addEventListener("click", function () {
        activeFilter = type;
        visibleLimit = PAGE_SIZE;
        [].forEach.call(filterGroup.children, function (child) {
          child.setAttribute("aria-pressed", child === button ? "true" : "false");
        });
        renderCatalog();
      });
      filterGroup.appendChild(button);
    });
  }
  if (searchInput) {
    searchInput.addEventListener("input", function () {
      visibleLimit = PAGE_SIZE;
      renderCatalog();
    });
  }
  if (clearSearch && searchInput) {
    clearSearch.addEventListener("click", function () {
      searchInput.value = "";
      visibleLimit = PAGE_SIZE;
      renderCatalog();
      searchInput.focus();
    });
  }
  if (loadMore) {
    loadMore.addEventListener("click", function () {
      visibleLimit += PAGE_SIZE;
      renderCatalog();
    });
  }
  renderCatalog();

  /* Accessible details dialog */
  var modal = $("#modal");
  var modalCard = modal ? $(".modal-card", modal) : null;
  var modalTrigger = null;
  function openModal(entry, trigger) {
    if (!modal || !modalCard) return;
    modalTrigger = trigger;
    var icon = $("#modal-icon");
    icon.className = "icon lg " + entry.type;
    icon.textContent = entryIcon(entry);
    setText("#modal-title", entry.name + (entry.en ? " · " + entry.en : ""));
    setText("#modal-id", entry.id);
    var tags = $("#modal-tags");
    tags.innerHTML = "";
    tags.appendChild(create("span", "pill type", labels[entry.type] || entry.type));
    (entry.tags || []).forEach(function (tag) {
      tags.appendChild(create("span", "pill", tag));
    });
    var modalLong = $("#modal-long");
    if (entry.long) {
      modalLong.innerHTML = entry.long;
    } else {
      modalLong.textContent = entry.summary || "";
    }
    var details = $("#modal-details");
    details.innerHTML = "";
    (entry.details || []).forEach(function (detail) {
      var item = create("li");
      item.appendChild(create("strong", null, detail[0]));
      item.appendChild(create("span", null, detail[1]));
      details.appendChild(item);
    });
    modal.hidden = false;
    document.body.classList.add("modal-open");
    modalCard.focus();
  }
  function closeModal() {
    if (!modal || modal.hidden) return;
    modal.hidden = true;
    document.body.classList.remove("modal-open");
    if (modalTrigger) modalTrigger.focus();
    modalTrigger = null;
  }
  if (modal) {
    modal.addEventListener("click", function (event) {
      if (event.target.hasAttribute("data-close")) closeModal();
    });
  }

  /* Global keyboard and current-section navigation */
  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape") {
      closeModal();
      setNav(false);
    }
    if (modal && !modal.hidden && event.key === "Tab") {
      var focusable = modal.querySelectorAll("button, [href], [tabindex]:not([tabindex='-1'])");
      if (!focusable.length) return;
      var first = focusable[0];
      var last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    }
  });

  var navLinks = topnav ? [].slice.call(topnav.querySelectorAll("a[href^='#']")) : [];
  if ("IntersectionObserver" in window && navLinks.length) {
    var sections = navLinks.map(function (link) {
      return document.querySelector(link.getAttribute("href"));
    }).filter(Boolean);
    var sectionObserver = new IntersectionObserver(function (observations) {
      observations.forEach(function (observation) {
        if (!observation.isIntersecting) return;
        navLinks.forEach(function (link) {
          link.setAttribute("aria-current", link.getAttribute("href") === "#" + observation.target.id ? "true" : "false");
        });
      });
    }, { rootMargin: "-28% 0px -64% 0px", threshold: 0 });
    sections.forEach(function (section) { sectionObserver.observe(section); });
  }

  function restoreHashTarget() {
    if (!location.hash) return;
    var target = document.getElementById(decodeURIComponent(location.hash.slice(1)));
    if (!target) return;
    document.documentElement.style.scrollBehavior = "auto";
    target.scrollIntoView({ block: "start" });
    requestAnimationFrame(function () {
      document.documentElement.style.removeProperty("scroll-behavior");
    });
  }
  addEventListener("hashchange", restoreHashTarget);
  requestAnimationFrame(function () {
    requestAnimationFrame(restoreHashTarget);
  });

  /* Lightweight atmosphere; never controls content visibility */
  var canvas = $("#fog-canvas");
  if (canvas && innerWidth > 820 && !matchMedia("(prefers-reduced-motion: reduce)").matches) {
    var context = canvas.getContext("2d");
    var width;
    var height;
    var points = [];
    function resizeCanvas() {
      width = canvas.width = innerWidth;
      height = canvas.height = innerHeight;
      var count = Math.min(38, Math.floor(width * height / 42000));
      points = [];
      for (var index = 0; index < count; index += 1) {
        points.push({
          x: Math.random() * width,
          y: Math.random() * height,
          vx: (Math.random() - .5) * .18,
          vy: (Math.random() - .5) * .18,
          radius: Math.random() * 1.3 + .3
        });
      }
    }
    function drawFog() {
      context.clearRect(0, 0, width, height);
      points.forEach(function (point) {
        point.x += point.vx;
        point.y += point.vy;
        if (point.x < 0 || point.x > width) point.vx *= -1;
        if (point.y < 0 || point.y > height) point.vy *= -1;
        context.beginPath();
        context.arc(point.x, point.y, point.radius, 0, Math.PI * 2);
        context.fillStyle = "rgba(223,186,67,.48)";
        context.fill();
      });
      requestAnimationFrame(drawFog);
    }
    addEventListener("resize", resizeCanvas);
    resizeCanvas();
    drawFog();
  }
})();
