<template>
  <div class="container">

    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <a href="#" @click.prevent="goHome">首页</a> /
      <a href="#" @click.prevent="goExplore">探索</a> /
      <span>交友网络</span>
    </div>

    <h1>黄宾虹交友网络</h1>

    <p class="intro">
      探索黄宾虹的交友网络与关键好友的时间线，了解这位艺术大师的人生历程与艺术成就
    </p>

    <!-- 顶部 tab -->
    <div class="nav-tabs">
      <div
        class="nav-tab"
        :class="{ active: activeView === 'overview-view' }"
        @click="switchTab('overview-view')"
      >
        交友网络
      </div>

      <div
        class="nav-tab"
        :class="{ active: activeView === 'timeline-view' }"
        @click="switchTab('timeline-view')"
      >
        关键好友时间轴
      </div>
    </div>

    <!-- 交友网络总览 -->
    <div id="overview-view" class="view" :class="{ active: activeView === 'overview-view' }">
      <svg id="overview-svg"></svg>
    </div>

    <!-- 地图视图 -->
    <div id="map-view" class="view" :class="{ active: activeView === 'map-view' }">
      <button class="back-btn" @click="backToOverview">
        <i>↩</i> 返回总览
      </button>

      <div class="map-info">
        <div class="map-title">{{ mapPlaceName }}</div>
        <div id="map-place-desc">{{ mapPlaceDesc }}</div>
        <div class="friend-info" id="map-friends">
          <strong>主要好友:</strong> {{ mapFriends }}
        </div>
      </div>

      <div class="year-label">{{ mapYear }}</div>

      <!-- 好友详情弹窗 -->
      <div class="friend-details" :class="{ active: friendDialogVisible }">
        <button class="close-btn" @click="friendDialogVisible = false">&times;</button>

        <div class="friend-name">{{ friendName }}</div>
        <div class="friend-history">{{ friendHistory }}</div>
      </div>

      <svg id="map-svg"></svg>
    </div>

    <!-- 时间轴视图 -->
    <div id="timeline-view" class="view" :class="{ active: activeView === 'timeline-view' }">
      <button class="back-btn" @click="backToOverview">
        <i>↩</i> 返回总览
      </button>

      <div class="timeline-container">
        <svg id="timeline-svg"></svg>
      </div>

      <!-- 时间事件弹窗 -->
      <div class="event-details" :class="{ active: eventDialogVisible }">
        <button class="close-btn" @click="eventDialogVisible = false">&times;</button>

        <div class="event-title">{{ eventTitle }}</div>
        <div class="event-description">{{ eventDescription }}</div>
        <div class="event-meta">{{ eventMeta }}</div>
      </div>
    </div>

  </div>
</template>
<script>

import * as d3 from "d3";

export default {
  name: "JiaoyouIndex",

  data() {
    return {
      activeView: "overview-view",

      // 地图界面信息
      mapPlaceName: "",
      mapPlaceDesc: "",
      mapFriends: "",
      mapYear: "",

      // 好友弹窗
      friendDialogVisible: false,
      friendName: "",
      friendHistory: "",

      // 时间轴弹窗
      eventDialogVisible: false,
      eventTitle: "",
      eventDescription: "",
      eventMeta: "",

      // 数据（从原 HTML 中完整迁移）
      data: {
        core: {
          id: "黄宾虹",
          birth: 1865,
          death: 1955
        },
        locations: [
          {
            id: "浙江金华",
            year: "1865-1890",
            info: "1865年诞生于此，幼年至26岁主要在此生活、学画、应考，奠定学业与绘画基础",
            people: [
              {name: "邵赋清", history: "黄宾虹早年好友，共同探讨绘画艺术"},
              {name: "陈春帆", history: "黄宾虹的同乡好友，交流绘画心得"},
              {name: "方良英", history: "黄宾虹的同窗好友，共同研习书画"},
              {name: "黄崇惺", history: "黄宾虹的亲属，支持其艺术创作"},
              {name: "程健行", history: "黄宾虹的师长，对其艺术风格影响深远"}
            ],
            coords: [119.6454, 29.1164]
          },
          {
            id: "安徽歙县",
            year: "1890-1907",
            info: "祖籍地，26岁后多次居住，师从汪宗沂，组织'黄社'，参与革命活动，搜集古籍书画",
            people: [
              {name: "汪宗沂", history: "黄宾虹的恩师，传授其绘画技艺"},
              {name: "许承尧", history: "黄宾虹的挚友，共同参与革命活动"},
              {name: "汪吉修", history: "黄宾虹的同乡好友，交流古籍书画"},
              {name: "汪采白", history: "黄宾虹的晚辈，受其艺术影响"}
            ],
            coords: [118.4472, 29.8147]
          },
          {
            id: "上海",
            year: "1907-1937",
            info: "1907年避祸赴沪，至1937年为事业核心地，编辑《美术丛书》，任教暨南大学，组织烂漫社",
            people: [
              {name: "邓实", history: "黄宾虹的挚友，共同编辑《美术丛书》"},
              {name: "傅雷", history: "黄宾虹的晚辈，受其艺术影响"},
              {name: "张大千", history: "黄宾虹的同道好友，交流绘画艺术"},
              {name: "张善孖", history: "黄宾虹的同道好友，共同探讨艺术"},
              {name: "吴昌硕", history: "黄宾虹的同道好友，交流书画艺术"}
            ],
            coords: [121.4737, 31.2304]
          },
          {
            id: "北平",
            year: "1937-1948",
            info: "1937-1948年寓居，鉴定故宫古画，任教北平艺专，闭门治学作画，拒绝日伪拉拢",
            people: [
              {name: "齐白石", history: "黄宾虹的挚友，共同探讨艺术"},
              {name: "周怀民", history: "黄宾虹的晚辈，受其艺术影响"},
              {name: "郭味蕖", history: "黄宾虹的晚辈，受其艺术影响"},
              {name: "李可染", history: "黄宾虹的晚辈，受其艺术影响"}
            ],
            coords: [116.4074, 39.9042]
          },
          {
            id: "浙江杭州",
            year: "1948-1955",
            info: "1948年定居至逝世，任教杭州艺专，当选全国政协委员，创作达到顶峰，捐赠全部作品",
            people: [
              {name: "潘天寿", history: "黄宾虹的挚友，共同探讨艺术"},
              {name: "夏承焘", history: "黄宾虹的同道好友，交流书画艺术"},
              {name: "诸乐三", history: "黄宾虹的晚辈，受其艺术影响"},
              {name: "王伯敏", history: "黄宾虹的晚辈，受其艺术影响"}
            ],
            coords: [120.1551, 30.2741]
          }
        ],

        timelineEvents: [
          {
            year: 1907,
            person: "邓实",
            title: "共同编辑《美术丛书》",
            description: "黄宾虹与邓实开始合作编辑《美术丛书》，这是中国近代美术史上重要的出版物，收录了大量古代书画理论著作。",
            artWeight: 9,
            hbhAge: 42,
            friendAge: 32
          },
          {
            year: 1912,
            person: "邓实",
            title: "创立贞社",
            description: "黄宾虹与邓实共同创立贞社，这是一个以研究金石书画为主的学术团体，对推动近代美术研究有重要影响。",
            artWeight: 8,
            hbhAge: 47,
            friendAge: 37
          },
          {
            year: 1929,
            person: "傅雷",
            title: "初次相识",
            description: "傅雷初次拜访黄宾虹，两人一见如故，开始了长达二十多年的深厚友谊。傅雷后来成为黄宾虹艺术最重要的阐释者。",
            artWeight: 7,
            hbhAge: 64,
            friendAge: 21
          },
          {
            year: 1931,
            person: "傅雷",
            title: "艺术理论交流",
            description: "傅雷与黄宾虹频繁通信，讨论中国画理论与创作，这些通信后来成为研究黄宾虹艺术思想的重要文献。",
            artWeight: 8,
            hbhAge: 66,
            friendAge: 23
          },
          {
            year: 1937,
            person: "傅雷",
            title: "《黄宾虹画集》出版",
            description: "在傅雷的推动下，《黄宾虹画集》出版，这是黄宾虹第一本个人画集，标志着他的艺术成就得到广泛认可。",
            artWeight: 9,
            hbhAge: 72,
            friendAge: 29
          },
          {
            year: 1943,
            person: "傅雷",
            title: "八十寿辰展览",
            description: "傅雷为黄宾虹筹办八十寿辰画展，并撰写长篇序言，系统阐述黄宾虹的艺术成就与历史地位。",
            artWeight: 10,
            hbhAge: 78,
            friendAge: 35
          },
          {
            year: 1925,
            person: "陈柱",
            title: "学术交流",
            description: "黄宾虹与陈柱开始密切交往，共同探讨金石学与文字学，这些交流对黄宾虹的绘画理论产生重要影响。",
            artWeight: 7,
            hbhAge: 60,
            friendAge: 35
          },
          {
            year: 1930,
            person: "陈柱",
            title: "合作出版",
            description: "黄宾虹与陈柱合作出版《金石书画丛刊》，收录了大量珍贵的金石拓片与书画作品。",
            artWeight: 8,
            hbhAge: 65,
            friendAge: 40
          }
        ]
      }
    };
  },

  mounted() {
    // 初始化视图
    this.initOverview();
    this.initTimeline();

    // 监听窗口变化
    window.addEventListener("resize", this.onResize);
  },

  beforeDestroy() {
    window.removeEventListener("resize", this.onResize);
  },

  methods: {
    /* -----------------------------
       导航相关
    ----------------------------- */
    goHome() {
      alert("跳转到首页");
    },
    goExplore() {
      alert("跳转到黄宾虹研究页面");
    },
    switchTab(view) {
      this.activeView = view;
      if (view === "timeline-view") {
        // 确保 DOM 更新后再绘制
        this.$nextTick(() => {
          this.initTimeline();
        });
      }
    },
    backToOverview() {
      this.activeView = "overview-view";
      this.friendDialogVisible = false;
      this.eventDialogVisible = false;
    },

    /* -----------------------------
       Overview 总览图（D3）
    ----------------------------- */
    initOverview() {
      const width = document.getElementById("overview-view").clientWidth;
      const height = document.getElementById("overview-view").clientHeight;

      const svg = d3.select("#overview-svg")
        .attr("width", width)
        .attr("height", height);

      svg.selectAll("*").remove();

      const nodes = [this.data.core];
      this.data.locations.forEach(loc => nodes.push({ id: loc.id, type: "place" }));

      const people = new Set();
      this.data.locations.forEach(loc => loc.people.forEach(p => people.add(p.name)));
      people.forEach(p => nodes.push({ id: p, type: "person" }));

      const links = [];
      this.data.locations.forEach(loc => {
        links.push({ source: "黄宾虹", target: loc.id });
        loc.people.forEach(person => {
          links.push({ source: loc.id, target: person.name });
        });
      });

      const simulation = d3.forceSimulation()
        .force("link", d3.forceLink().id(d => d.id).distance(100))
        .force("charge", d3.forceManyBody().strength(-300))
        .force("center", d3.forceCenter(width / 2, height / 2))
        .force("collide", d3.forceCollide().radius(50));

      const link = svg.append("g")
        .selectAll("line")
        .data(links)
        .enter().append("line")
        .attr("class", "link");

      const node = svg.append("g")
        .selectAll("circle")
        .data(nodes)
        .enter().append("circle")
        .attr("class", d => {
          if (d.id === "黄宾虹") return "node core";
          if (d.type === "place") return "node place";
          return "node person no-hover";
        })
        .attr("r", d => {
          if (d.id === "黄宾虹") return 15;
          if (d.type === "place") return 12;
          return 8;
        })
        .call(d3.drag()
          .on("start", this._dragStarted(simulation))
          .on("drag", this._dragged)
          .on("end", this._dragEnded(simulation))
        );

      // 地点节点点击跳转地图视图
      node.filter(d => d.type === "place")
        .on("click", (event, d) => {
          this.showMapView(d.id);
        });

      const label = svg.append("g")
        .selectAll("text")
        .data(nodes)
        .enter().append("text")
        .attr("class", d => d.type === "place" ? "label place-label" : "label")
        .attr("dy", d => d.type === "place" ? 25 : 20)
        .text(d => d.id);

      simulation.nodes(nodes).on("tick", () => {
        link
          .attr("x1", d => d.source.x)
          .attr("y1", d => d.source.y)
          .attr("x2", d => d.target.x)
          .attr("y2", d => d.target.y);

        node
          .attr("cx", d => d.x = Math.max(50, Math.min(width - 50, d.x)))
          .attr("cy", d => d.y = Math.max(50, Math.min(height - 50, d.y)));

        label
          .attr("x", d => d.x)
          .attr("y", d => d.y);
      });

      simulation.force("link").links(links);
    },

    _dragStarted(simulation) {
      return function (event, d) {
        if (!event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
      };
    },
    _dragged(event, d) {
      d.fx = event.x;
      d.fy = event.y;
    },
    _dragEnded(simulation) {
      return function (event, d) {
        if (!event.active) simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
      };
    },

    /* -----------------------------
       Map 地图视图（D3）
    ----------------------------- */
    showMapView(placeId) {
      const place = this.data.locations.find(loc => loc.id === placeId);
      if (!place) return;

      this.activeView = "map-view";
      this.mapPlaceName = place.id;
      this.mapPlaceDesc = place.info;
      this.mapYear = `主要活动时期: ${place.year}`;
      this.mapFriends = place.people.map(p => p.name).join("、");

      this.$nextTick(() => {
        this.drawMap(place);
      });
    },

    drawMap(place) {
      const width = document.getElementById("map-view").clientWidth;
      const height = document.getElementById("map-view").clientHeight;

      const svg = d3.select("#map-svg")
        .attr("width", width)
        .attr("height", height);

      svg.selectAll("*").remove();

      const centerX = width / 2;
      const centerY = height / 2;
      const placeRadius = 18;
      const personRadius = 12;
      const connectRadius = 160;

      // 地点节点
      svg.append("circle")
        .attr("cx", centerX)
        .attr("cy", centerY)
        .attr("r", placeRadius)
        .attr("fill", "#ebd976");

      // 地点名称
      svg.append("text")
        .attr("x", centerX)
        .attr("y", centerY - placeRadius - 12)
        .text(place.id)
        .attr("class", "label place-label")
        .attr("text-anchor", "middle")
        .attr("font-size", "14px");

      const peopleCount = place.people.length;
      place.people.forEach((person, index) => {
        const angle = (index / peopleCount) * Math.PI * 2;
        const personX = centerX + connectRadius * Math.cos(angle);
        const personY = centerY + connectRadius * Math.sin(angle);

        const startX = centerX + placeRadius * Math.cos(angle);
        const startY = centerY + placeRadius * Math.sin(angle);
        const endX = personX - personRadius * Math.cos(angle);
        const endY = personY - personRadius * Math.sin(angle);

        svg.append("line")
          .attr("x1", startX)
          .attr("y1", startY)
          .attr("x2", endX)
          .attr("y2", endY)
          .attr("class", "link")
          .attr("stroke-width", 1.5);

        svg.append("circle")
          .attr("cx", personX)
          .attr("cy", personY)
          .attr("r", personRadius)
          .attr("fill", "#abc08d")
          .style("cursor", "pointer")
          .on("click", () => this.showFriendDetails(person));

        svg.append("text")
          .attr("x", personX)
          .attr("y", personY + personRadius + 15)
          .text(person.name)
          .attr("class", "label")
          .attr("text-anchor", "middle")
          .style("cursor", "pointer")
          .on("click", () => this.showFriendDetails(person));
      });
    },

    showFriendDetails(friend) {
      this.friendDialogVisible = true;
      this.friendName = friend.name;
      this.friendHistory = friend.history;
    },

    /* -----------------------------
       Timeline 时间轴（D3）
    ----------------------------- */
    initTimeline() {
      const width = document.querySelector(".timeline-container").clientWidth;
      const height = document.querySelector(".timeline-container").clientHeight;

      const svg = d3.select("#timeline-svg")
        .attr("width", width)
        .attr("height", height);

      svg.selectAll("*").remove();

      const margin = {top: 50, right: 50, bottom: 50, left: 100};
      const innerWidth = width - margin.left - margin.right;
      const innerHeight = height - margin.top - margin.bottom;

      const xScale = d3.scaleLinear()
        .domain([1865, 1955])
        .range([0, innerWidth]);

      const friends = ["黄宾虹", "邓实", "傅雷", "陈柱"];
      const yScale = d3.scalePoint()
        .domain(friends)
        .range([0, innerHeight])
        .padding(0.5);

      const g = svg.append("g")
        .attr("transform", `translate(${margin.left}, ${margin.top})`);

      // 主轴线
      g.append("line")
        .attr("x1", 0)
        .attr("y1", yScale("黄宾虹"))
        .attr("x2", innerWidth)
        .attr("y2", yScale("黄宾虹"))
        .attr("class", "timeline-axis");

      friends.forEach(friend => {
        if (friend !== "黄宾虹") {
          g.append("line")
            .attr("x1", 0)
            .attr("y1", yScale(friend))
            .attr("x2", innerWidth)
            .attr("y2", yScale(friend))
            .attr("class", "timeline-axis")
            .style("stroke-dasharray", "5,5")
            .style("stroke", "#8b7d6b");
        }
      });

      const years = d3.range(1870, 1956, 10);
      years.forEach(year => {
        g.append("line")
          .attr("x1", xScale(year))
          .attr("y1", yScale("黄宾虹") - 5)
          .attr("x2", xScale(year))
          .attr("y2", yScale("黄宾虹") + 5)
          .attr("class", "timeline-axis");

        g.append("text")
          .attr("x", xScale(year))
          .attr("y", yScale("黄宾虹") - 10)
          .text(year)
          .attr("class", "timeline-label")
          .attr("text-anchor", "middle");
      });

      friends.forEach(friend => {
        g.append("text")
          .attr("x", -10)
          .attr("y", yScale(friend))
          .text(friend)
          .attr("class", "timeline-label")
          .attr("text-anchor", "end")
          .attr("dominant-baseline", "middle");
      });

      // 使用真实文本测量 (getBBox) 来避免重叠：逐步搜索可用位置直到不与其他标签或节点重叠
      var placedLabels = [];

      // 收集所有事件节点的位置（用于避免标签与节点重叠）
      var nodesPositions = [];
      for (var ii = 0; ii < this.data.timelineEvents.length; ii++) {
        var ev = this.data.timelineEvents[ii];
        var nx = xScale(ev.year);
        var ny = yScale(ev.person);
        var nr = ev.artWeight || 6;
        nodesPositions.push({ x: nx, y: ny, r: nr });
      }

      // 用于检测两个矩形是否近似重叠
      var rectsOverlap = function(x1, y1, w1, h1, x2, y2, w2, h2, gap) {
        return Math.abs(x1 - x2) < (w1/2 + w2/2 + gap) && Math.abs(y1 - y2) < (h1/2 + h2/2 + gap);
      };

      // 检查标签矩形是否与任一节点圆心/半径发生碰撞（近似）
      var labelOverlapsNode = function(labelX, labelY, lw, lh) {
        for (var ni = 0; ni < nodesPositions.length; ni++) {
          var n = nodesPositions[ni];
          var dx = Math.abs(n.x - labelX);
          var dy = Math.abs(n.y - labelY);
          if (dx < (n.r + lw/2 + 6) && dy < (n.r + lh/2 + 6)) return true;
        }
        return false;
      };

      // 为每个事件寻找一个不冲突的位置
      for (var i = 0; i < this.data.timelineEvents.length; i++) {
        let event = this.data.timelineEvents[i];
        let ex = xScale(event.year);
        let ey = yScale(event.person);

        g.append("line")
          .attr("x1", ex)
          .attr("y1", yScale("黄宾虹"))
          .attr("x2", ex)
          .attr("y2", ey)
          .attr("class", "timeline-event-line");

        g.append("circle")
          .attr("cx", ex)
          .attr("cy", ey)
          .attr("r", event.artWeight)
          .attr("class", "timeline-event-circle timeline-event")
          .on("click", () => this.showEventDetails(event));

        // 初始候选位置（置于圆点上方）
        let candX = ex;
        let candY = ey - event.artWeight - 8;

        let text = g.append("text")
          .attr("x", candX)
          .attr("y", candY)
          .text(event.title)
          .attr("class", "timeline-event-details")
          .attr("text-anchor", "middle");

        let bbox = text.node().getBBox();
        let lw = bbox.width;
        let lh = bbox.height;

        var gap = 6;
        var maxAttempts = 80;
        var attempt = 0;
        var stepIndex = 0;

        // 搜索策略：优先在竖直方向（上下）寻找空位，若失败则尝试水平错开，逐步扩大搜索半径
        while (attempt < maxAttempts) {
          // 检查是否与已有标签重叠或与节点重叠
          var collided = false;
          for (var pi = 0; pi < placedLabels.length; pi++) {
            var pl = placedLabels[pi];
            if (rectsOverlap(pl.x, pl.y, pl.w, pl.h, candX, candY, lw, lh, gap)) {
              collided = true;
              break;
            }
          }
          if (!collided && !labelOverlapsNode(candX, candY, lw, lh)) break;

          // 否则扩展搜索：交替上下移动，再交替左右移动，步长逐步放大
          stepIndex += 1;
          var vertSteps = Math.ceil(stepIndex / 2);
          var vertDir = (stepIndex % 2 === 0) ? -1 : 1; // 上/下交替
          candY = ey - event.artWeight - 8 + vertDir * vertSteps * (lh + gap);

          // 每隔若干步尝试左右偏移
          if (stepIndex > 6) {
            var horSteps = Math.ceil((stepIndex - 6) / 4);
            var horDir = (stepIndex % 2 === 0) ? -1 : 1;
            candX = ex + horDir * horSteps * (lw + gap);
          } else {
            candX = ex;
          }

          // 更新文本位置并重新测量
          text.attr("x", candX).attr("y", candY);
          bbox = text.node().getBBox();
          lw = bbox.width;
          lh = bbox.height;

          attempt += 1;
        }

        // 记录已放置标签
        placedLabels.push({ x: candX, y: candY, w: lw, h: lh });
      }
    },

    showEventDetails(event) {
      this.eventDialogVisible = true;
      this.eventTitle = `${event.year}年 - ${event.title}`;
      this.eventDescription = event.description;
      this.eventMeta = `黄宾虹 ${event.hbhAge}岁，${event.person} ${event.friendAge}岁，艺术重要性: ${event.artWeight}/10`;
    },

    /* -----------------------------
       窗口大小变化处理
    ----------------------------- */
    onResize() {
      if (document.getElementById("overview-view") && this.activeView === "overview-view") {
        d3.select("#overview-svg").selectAll("*").remove();
        this.initOverview();
      }
      if (document.getElementById("timeline-view") && this.activeView === "timeline-view") {
        this.initTimeline();
      }
      if (document.getElementById("map-view") && this.activeView === "map-view") {
        const activePlace = this.mapPlaceName;
        if (activePlace) {
          const place = this.data.locations.find(loc => loc.id === activePlace);
          if (place) this.drawMap(place);
        }
      }
    }
  }
};
</script>
<style>
    * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
    }

    body {
        font-family: "SimSun", "STSong", serif;
        color: #333;
        background: linear-gradient(135deg, #f5f0e6 0%, #e8dfca 100%);
        min-height: 100vh;
        overflow-x: hidden;
    }
    .container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 30px 20px;
        background-color: rgba(248, 244, 230, 0.85);
        border-radius: 8px;
        box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
    }
    h1 {
        text-align: center;
        color: #8b4513;
        border-bottom: 2px solid #d2b48c;
        padding-bottom: 10px;
        margin: 20px 0 30px 0;
    }
    .breadcrumb {
        font-size: 14px;
        color: #6b553e;
        margin-bottom: 20px;
        padding-bottom: 10px;
        border-bottom: 1px dashed #b8a48c;
    }
    .breadcrumb a {
        color: #6b553e;
        text-decoration: none;
    }
    .breadcrumb a:hover {
        text-decoration: underline;
    }
    .view {
        display: none;
        margin-top: 20px;
        height: 600px;
        border: 1px solid #d2b48c;
        border-radius: 5px;
        background-color: #fffbf0;
        position: relative;
    }
    .view.active {
        display: block;
    }
    #overview-view {
        overflow: hidden;
    }
    .node {
        cursor: pointer;
    }
    .node.place {
        fill: #ebd976; /* 地点节点颜色 */
    }
    .node.person {
        fill: #abc08d; /* 人物节点颜色 */
    }
    .node.core {
        fill: #80996a;
    }
    .link {
        stroke: #8b7d6b;
        stroke-opacity: 0.6;
    }
    .label {
        font-size: 12px;
        text-anchor: middle;
        pointer-events: none;
    }
    .place-label {
        font-weight: bold;
    }
    /* 仅保留总览页面人物节点的无hover效果样式 */
    .node.person.no-hover:hover {
        fill: #abc08d; /* 保持原颜色，无变化 */
        cursor: default; /* 鼠标样式不变 */
    }
    .back-btn {
        position: absolute;
        top: 10px;
        left: 10px;
        background-color: #d2b48c;
        border: none;
        color: #333;
        padding: 8px 15px;
        border-radius: 3px;
        cursor: pointer;
        font-family: "SimSun", serif;
        display: flex;
        align-items: center;
        gap: 5px;
    }
    .back-btn:hover {
        background-color: #a67c52;
    }
    .map-info {
        position: absolute;
        top: 10px;
        right: 10px;
        background-color: rgba(255, 251, 240, 0.8);
        padding: 10px;
        border: 1px solid #d2b48c;
        border-radius: 3px;
        max-width: 250px;
    }
    .map-title {
        font-weight: bold;
        margin-bottom: 5px;
        color: #8b4513;
    }
    .friend-info {
        font-size: 13px;
        margin-top: 5px;
    }
    .year-label {
        position: absolute;
        bottom: 10px;
        right: 10px;
        background-color: rgba(255, 251, 240, 0.8);
        padding: 3px 8px;
        border: 1px solid #d2b48c;
        border-radius: 3px;
        font-size: 14px;
    }
    .intro {
        text-align: center;
        margin: 10px 0 20px 0;
        color: #5c4033;
        line-height: 1.6;
    }
    .friend-details {
        display: none;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background-color: rgba(255, 251, 240, 0.95);
        padding: 20px;
        border: 2px solid #d2b48c;
        border-radius: 8px;
        width: 300px;
        text-align: center;
        box-shadow: 0 0 15px rgba(0,0,0,0.2);
        z-index: 100;
    }
    .friend-details.active {
        display: block;
    }
    .friend-name {
        font-size: 18px;
        font-weight: bold;
        color: #8b4513;
        margin-bottom: 10px;
    }
    .friend-history {
        font-size: 14px;
        line-height: 1.5;
    }
    .close-btn {
        position: absolute;
        top: 5px;
        right: 10px;
        background: none;
        border: none;
        font-size: 18px;
        cursor: pointer;
        color: #8b4513;
    }
    .nav-tabs {
        display: flex;
        justify-content: center;
        margin-bottom: 20px;
        border-bottom: 1px solid #d2b48c;
        padding-bottom: 5px;
    }
    .nav-tab {
        padding: 8px 15px;
        cursor: pointer;
        background-color: #f8f4e6;
        border: 1px solid #d2b48c;
        border-bottom: none;
        border-radius: 5px 5px 0 0;
        margin: 0 5px;
        display: flex;
        align-items: center;
        gap: 5px;
    }
    .nav-tab.active {
        background-color: #fffbf0;
        font-weight: bold;
    }
    .timeline-container {
        padding: 20px;
        height: 550px;
        overflow-y: auto;
    }
    .timeline-axis {
        stroke: #8b4513;
        stroke-width: 2;
    }
    .timeline-label {
        font-size: 12px;
        fill: #5c4033;
    }
    .timeline-event {
        cursor: pointer;
    }
    .timeline-event-circle {
        fill: #c1272d;
        stroke: #8b4513;
        stroke-width: 1;
    }
    .timeline-event-line {
        stroke: #8b7d6b;
        stroke-width: 1;
        stroke-dasharray: 5,5;
    }
    .timeline-event-details {
        font-size: 12px;
        fill: #333;
        text-anchor: middle;
    }
    .event-details {
        display: none;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background-color: rgba(255, 251, 240, 0.95);
        padding: 20px;
        border: 2px solid #d2b48c;
        border-radius: 8px;
        width: 400px;
        text-align: center;
        box-shadow: 0 0 15px rgba(0,0,0,0.2);
        z-index: 100;
    }
    .event-details.active {
        display: block;
    }
    .event-title {
        font-size: 18px;
        font-weight: bold;
        color: #8b4513;
        margin-bottom: 10px;
    }
    .event-description {
        font-size: 14px;
        line-height: 1.5;
        margin-bottom: 10px;
    }
    .event-meta {
        font-size: 12px;
        color: #666;
    }

    /* 让 svg 在容器内适配 */
    svg {
        width: 100%;
        height: 100%;
        display: block;
    }
</style>
