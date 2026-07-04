/* Lord of Mysteries Wiki — 交互逻辑（纯前端，GitHub Pages 直接运行） */
(function () {
  "use strict";
  var D = window.LOM || {};
  var meta = D.meta || {}, entries = D.entries || [], labels = D.labels || {};
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var el = function (t, c, h) { var e = document.createElement(t); if (c) e.className = c; if (h != null) e.innerHTML = h; return e; };

  /* ── 主题切换 ── */
  var themeToggle = $("#theme-toggle");
  var saved = null;
  try { saved = localStorage.getItem("lom-theme"); } catch (e) {}
  if (saved) document.documentElement.setAttribute("data-theme", saved);
  if (themeToggle) themeToggle.addEventListener("click", function () {
    var cur = document.documentElement.getAttribute("data-theme") === "light" ? "dark" : "light";
    document.documentElement.setAttribute("data-theme", cur);
    try { localStorage.setItem("lom-theme", cur); } catch (e) {}
  });

  /* ── 移动端汉堡菜单 ── */
  var navToggle = $("#nav-toggle"), topnav = $("#topnav");
  function setNav(open) {
    if (!topnav || !navToggle) return;
    topnav.classList.toggle("open", open);
    navToggle.setAttribute("aria-expanded", open ? "true" : "false");
    navToggle.textContent = open ? "✕" : "☰";
  }
  if (navToggle && topnav) {
    navToggle.addEventListener("click", function (ev) {
      ev.stopPropagation();
      setNav(!topnav.classList.contains("open"));
    });
    // 点击导航项后收起
    topnav.addEventListener("click", function (ev) { if (ev.target.tagName === "A") setNav(false); });
    // 点击外部收起
    document.addEventListener("click", function (ev) {
      if (topnav.classList.contains("open") && !topnav.contains(ev.target) && ev.target !== navToggle) setNav(false);
    });
    // Esc 收起
    document.addEventListener("keydown", function (ev) { if (ev.key === "Escape") setNav(false); });
    // 视口放大回桌面时清理状态
    addEventListener("resize", function () { if (innerWidth > 960) setNav(false); });
  }

  /* ── Hero meta ── */
  var metaDl = $("#meta-dl");
  if (metaDl) {
    [["Mod ID", meta.modId], ["版本", meta.version], ["Minecraft", meta.mc],
     ["加载器", meta.loader], ["Java", meta.java], ["阶段", meta.stage],
     ["最后更新", meta.lastUpdatedDisplay]]
      .forEach(function (p) { var d = el("div"); d.appendChild(el("dt", null, p[0])); d.appendChild(el("dd", null, p[1] || "—")); metaDl.appendChild(d); });
  }
  var heroTags = $("#hero-tags");
  if (heroTags) ["🜁 五条途径", "🜂 序列晋升", "🜃 魔药炼制", "🜄 扮演消化", "☾ 失控风险"].forEach(function (t) { heroTags.appendChild(el("span", "pill type", t)); });

  /* ── Stats ── */
  var statsEl = $("#stats");
  if (statsEl) {
    var cAbil = entries.filter(function (e) { return e.type === "ability"; }).length;
    var cPotion = entries.filter(function (e) { return e.type === "potion" || e.type === "item"; }).length;
    var stats = [
      [entries.length, "图鉴条目"],
      [(D.pathwaysOverview || []).length, "途径"],
      [(D.seerSequences || []).length + (D.spectatorSequences || []).length + (D.hunterSequences || []).length, "序列条目"],
      [entries.filter(function (e) { return e.type === "ability"; }).length, "能力"],
      [(D.roadmap || []).length, "开发里程碑"]
    ];
    stats.forEach(function (s) {
      var d = el("div", "stat");
      d.appendChild(el("strong", null, String(s[0])));
      d.appendChild(el("span", null, s[1]));
      statsEl.appendChild(d);
    });
  }

  /* ── Roadmap ── */
  var rmTrack = $("#roadmap-track");
  if (rmTrack) (D.roadmap || []).forEach(function (m) {
    var c = el("div", "rm-card " + m.state);
    var stateTxt = { done: "已完成", active: "进行中", planned: "规划", future: "远期" }[m.state] || m.state;
    var badge = el("span", "rm-badge", m.id + '<span class="rm-state state-' + m.state + '">' + stateTxt + "</span>");
    c.appendChild(badge);
    c.appendChild(el("b", "rm-title", m.title));
    var ul = el("ul", "rm-points");
    (m.points || []).forEach(function (p) { ul.appendChild(el("li", null, p)); });
    c.appendChild(ul);
    rmTrack.appendChild(c);
  });

  /* ── Pathways ── */
  var pwGrid = $("#pathway-grid");
  if (pwGrid) (D.pathwaysOverview || []).forEach(function (p) {
    var c = el("div", "pw-card");
    c.style.setProperty("--accent", p.accent);
    c.innerHTML =
      '<div class="pw-head"><h3>' + p.name + '</h3><span class="en">' + p.en + '</span></div>' +
      '<span class="pw-status">' + p.status + '</span>' +
      '<p class="pw-desc">' + p.desc + '</p>' +
      '<div class="pw-traits">' + (p.traits || []).map(function (t) { return '<span class="pill">' + t + '</span>'; }).join("") + '</div>' +
      '<p class="pw-spirit">' + (p.spirit
        ? '灵性上限 <b>' + p.spirit + '</b>'
        : '基础灵性 <b>' + p.baseSpirit + '</b> · 每序列成长 <b>+' + p.growth + '</b>') + '</p>';
    pwGrid.appendChild(c);
  });

  /* ── Sequence ladder ── */
  var ladder = $("#seq-ladder");
  var playableSequences = (D.seerSequences || []).map(function (s) {
    return Object.assign({ pathway: "占卜家" }, s);
  }).concat(D.spectatorSequences || [], D.hunterSequences || []);
  if (ladder) playableSequences.forEach(function (s) {
    var row = el("div", "seq-row " + (s.state === "active" ? "active" : (s.state === "future" ? "future" : "")));
    row.innerHTML =
      '<div class="seq-num">' + s.seq + '</div>' +
      '<div class="seq-info"><h4>' + s.pathway + ' · 序列 ' + s.seq + '：' + s.name + '</h4>' +
      '<span class="abil">' + (s.abilities || []).join(" · ") + '</span></div>' +
      '<div class="seq-spirit"><b>' + s.spiritMax + '</b>灵性上限</div>';
    row.title = s.desc || "";
    ladder.appendChild(row);
  });

  /* ── Core loop ── */
  var loop = $("#loop");
  if (loop) [
    ["炼制魔药", "在坩埚中按途径配方精确控温投料，材料顺序与温度共同决定成品品质。"],
    ["服用晋升", "选择占卜家、观众或猎人途径，获得对应序列能力；高阶魔药由服务端校验晋升资格。"],
    ["扮演消化", "按当前序列身份行动，通过占卜、观察、追踪、陷阱或战斗控制把消化度推向 100%。"],
    ["管理风险", "监控灵性、污染与失控压力，避免污染满值触发失控与失控体。"],
    ["晋升下一序列", "消化满额后炼制更高阶魔药，向序列 8 及更高迈进。"]
  ].forEach(function (p) {
    var li = el("li"); li.appendChild(el("h4", null, p[0])); li.appendChild(el("p", null, p[1])); loop.appendChild(li);
  });

  /* ── About ── */
  var techList = $("#tech-list");
  if (techList) [["Minecraft", meta.mc], ["加载器", meta.loader], ["Java", meta.java],
    ["映射", "official（Mojang）"], ["构建", "Gradle 8.8 + ForgeGradle 6"], ["数据", "Forge Capability + NBT"]]
    .forEach(function (p) { techList.appendChild(el("li", null, "<span>" + p[0] + "</span><b>" + (p[1] || "—") + "</b>")); });
  var teamList = $("#team-list");
  if (teamList) (meta.authors || []).forEach(function (a) {
    teamList.appendChild(el("li", null, '<span>' + a.role + '</span><b><a href="' + a.link + '" target="_blank" rel="noopener">' + a.name + '</a></b>'));
  });
  var lastUpdated = $("#last-updated");
  if (lastUpdated) {
    lastUpdated.textContent = "版本 " + meta.version + " · 最后更新 " + meta.lastUpdatedDisplay +
      " · UTC " + meta.lastUpdatedUtc;
  }

  /* ── Catalog: filters + search + cards ── */
  var filterGroup = $("#filter-group"), cardsEl = $("#cards"), searchEl = $("#search"), countEl = $("#result-count");
  var activeFilter = "all";

  var types = ["all"].concat(Object.keys(labels).filter(function (k) { return entries.some(function (e) { return e.type === k; }); }));
  if (filterGroup) types.forEach(function (t) {
    var n = t === "all" ? entries.length : entries.filter(function (e) { return e.type === t; }).length;
    var b = el("button", "filter" + (t === "all" ? " active" : ""),
      "<span>" + (t === "all" ? "全部" : (labels[t] || t)) + '</span><span class="cnt">' + n + "</span>");
    b.dataset.filter = t;
    b.addEventListener("click", function () {
      activeFilter = t;
      [].forEach.call(filterGroup.children, function (c) { c.classList.toggle("active", c === b); });
      render();
    });
    filterGroup.appendChild(b);
  });

  function iconChar(e) { return (e.name || "?").slice(0, 1); }

  function render() {
    if (!cardsEl) return;
    var kw = (searchEl && searchEl.value.trim().toLowerCase()) || "";
    var list = entries.filter(function (e) {
      var okType = activeFilter === "all" || e.type === activeFilter;
      var hay = [e.id, e.name, e.en, e.summary, (e.tags || []).join(" ")].join(" ").toLowerCase();
      return okType && (!kw || hay.indexOf(kw) >= 0);
    });
    cardsEl.innerHTML = "";
    if (countEl) countEl.textContent = "共 " + list.length + " 条" + (kw ? "（搜索：" + kw + "）" : "");
    if (!list.length) { cardsEl.appendChild(el("div", "empty", "没有找到匹配条目。")); return; }
    list.forEach(function (e, i) {
      var card = el("article", "card");
      card.style.animationDelay = Math.min(i * 30, 300) + "ms";
      card.innerHTML =
        '<div class="card-head"><div class="icon ' + e.type + '">' + iconChar(e) + '</div>' +
        '<div><h3>' + e.name + '</h3><p class="tagline">' + (e.en ? e.en + " · " : "") + e.id + '</p></div></div>' +
        '<p>' + (e.summary || "") + '</p>' +
        '<div class="meta"><span class="pill type">' + (labels[e.type] || e.type) + '</span>' +
        (e.tags || []).slice(0, 3).map(function (t) { return '<span class="pill">' + t + '</span>'; }).join("") + '</div>';
      card.addEventListener("click", function () { openModal(e); });
      cardsEl.appendChild(card);
    });
  }
  if (searchEl) searchEl.addEventListener("input", render);
  render();

  /* ── Modal ── */
  var modal = $("#modal");
  function openModal(e) {
    if (!modal) return;
    $("#modal-icon").className = "icon lg " + e.type;
    $("#modal-icon").textContent = iconChar(e);
    $("#modal-title").textContent = e.name + (e.en ? "  ·  " + e.en : "");
    $("#modal-id").textContent = e.id;
    $("#modal-tags").innerHTML = '<span class="pill type">' + (labels[e.type] || e.type) + '</span>' +
      (e.tags || []).map(function (t) { return '<span class="pill">' + t + '</span>'; }).join("");
    $("#modal-long").innerHTML = e.long || e.summary || "";
    var ul = $("#modal-details"); ul.innerHTML = "";
    (e.details || []).forEach(function (d) { ul.appendChild(el("li", null, "<strong>" + d[0] + "</strong><span>" + d[1] + "</span>")); });
    modal.hidden = false;
    document.body.style.overflow = "hidden";
  }
  function closeModal() { if (modal) { modal.hidden = true; document.body.style.overflow = ""; } }
  if (modal) modal.addEventListener("click", function (ev) { if (ev.target.hasAttribute("data-close")) closeModal(); });
  document.addEventListener("keydown", function (ev) { if (ev.key === "Escape") closeModal(); });

  /* ── Reveal on scroll ── */
  var reveals = [].slice.call(document.querySelectorAll(".reveal"));
  if ("IntersectionObserver" in window) {
    var io = new IntersectionObserver(function (ents) {
      ents.forEach(function (en) { if (en.isIntersecting) { en.target.classList.add("in"); io.unobserve(en.target); } });
    }, { threshold: 0.08 });
    reveals.forEach(function (r) { io.observe(r); });
  } else reveals.forEach(function (r) { r.classList.add("in"); });

  /* ── 灵界粒子背景 ── */
  var canvas = $("#fog-canvas");
  if (canvas && innerWidth > 620 && !matchMedia("(prefers-reduced-motion: reduce)").matches) {
    var ctx = canvas.getContext("2d"), W, H, pts = [];
    function resize() {
      W = canvas.width = innerWidth; H = canvas.height = innerHeight;
      var n = Math.min(70, Math.floor(W * H / 22000));
      pts = []; for (var i = 0; i < n; i++) pts.push({ x: Math.random() * W, y: Math.random() * H, vx: (Math.random() - .5) * .25, vy: (Math.random() - .5) * .25, r: Math.random() * 1.6 + .4 });
    }
    function tick() {
      ctx.clearRect(0, 0, W, H);
      for (var i = 0; i < pts.length; i++) {
        var p = pts[i]; p.x += p.vx; p.y += p.vy;
        if (p.x < 0 || p.x > W) p.vx *= -1; if (p.y < 0 || p.y > H) p.vy *= -1;
        ctx.beginPath(); ctx.arc(p.x, p.y, p.r, 0, 6.283);
        ctx.fillStyle = "rgba(212,175,55,.5)"; ctx.fill();
        for (var j = i + 1; j < pts.length; j++) {
          var q = pts[j], dx = p.x - q.x, dy = p.y - q.y, d = dx * dx + dy * dy;
          if (d < 13000) { ctx.beginPath(); ctx.moveTo(p.x, p.y); ctx.lineTo(q.x, q.y);
            ctx.strokeStyle = "rgba(139,92,246," + (0.10 * (1 - d / 13000)) + ")"; ctx.stroke(); }
        }
      }
      requestAnimationFrame(tick);
    }
    addEventListener("resize", resize); resize(); tick();
  }
})();
