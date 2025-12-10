<template>
  <body>
    <header>
      <HeaderNavbar />
    </header>
  <div class="container">

    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">黄宾虹交友网络</h1>
      <p class="page-subtitle">探索黄宾虹与各地艺术家的交往关系，了解这位艺术大师在不同时期的重要人脉网络</p>
    </div>

    <!-- 主容器 -->
    <div class="content-container">
      <p class="intro">
        本页面展示了黄宾虹一生中在不同地点的交友网络。点击地点节点查看详细的人际关系，或点击"关键好友时间轴"了解重要交往的时间脉络。
      </p>
      
      <!-- 导航标签 -->
      <div class="nav-tabs">
        <div 
          class="nav-tab" 
          :class="{ 'active': activeView === 'overview' }"
          @click="switchView('overview')"
        >
          <i class="fas fa-users"></i> 交友网络总览
        </div>
        <div 
          class="nav-tab" 
          :class="{ 'active': activeView === 'timeline' }"
          @click="switchView('timeline')"
        >
          <i class="fas fa-history"></i> 关键好友时间轴
        </div>
      </div>
      
      <!-- 网络图总览视图 -->
      <div id="overview-view" class="view" :class="{ 'active': activeView === 'overview' }">
        <div class="hint">提示：点击地点节点查看详细信息</div>
        <svg ref="overviewSvg" class="network-svg"></svg>
      </div>
      
      <!-- 地点详情视图 -->
  <div id="map-view" class="view" :class="{ 'active': activeView === 'map' }">
    <button class="back-btn" @click="switchView('overview')">
      <i class="fas fa-arrow-left"></i> 返回总览
    </button>
    <div class="map-info">
      <div class="map-title">
        <i class="fas fa-map-marker-alt"></i>
        <!-- 修复：可选链改为兼容写法 -->
        <span id="map-place-name">{{ selectedPlace && selectedPlace.id }}</span>
      </div>
      <!-- 修复：可选链改为兼容写法 -->
      <div id="map-place-desc">{{ selectedPlace && selectedPlace.info }}</div>
      <div class="friend-info" id="map-friends">
        <strong>主要好友:</strong>
        <!-- 修复：多层可选链 -->
        {{ selectedPlace && selectedPlace.people && selectedPlace.people.map(p => p.name).join("、") }}
      </div>
    </div>
    <div class="year-label" id="map-year">
      <!-- 修复：可选链改为兼容写法 -->
      主要活动时期: {{ selectedPlace && selectedPlace.year }}
    </div>
    <div class="friend-details" :class="{ 'active': selectedFriend }" id="friend-details">
      <button class="close-btn" @click="selectedFriend = null">&times;</button>
      <!-- 修复：可选链改为兼容写法 -->
      <div class="friend-name" id="friend-name">{{ selectedFriend && selectedFriend.name }}</div>
      <div class="friend-history" id="friend-history">{{ selectedFriend && selectedFriend.history }}</div>
    </div>
    <svg ref="mapSvg" class="map-svg"></svg>
  </div>
      
      <!-- 时间轴视图 -->
      <div id="timeline-view" class="view" :class="{ 'active': activeView === 'timeline' }">
        <button class="back-btn" @click="switchView('overview')">
          <i class="fas fa-arrow-left"></i> 返回总览
        </button>
        <div class="timeline-container">
          <div class="hint">提示：点击事件点查看详细信息</div>
          <svg ref="timelineSvg" class="timeline-svg"></svg>
        </div>
        <div class="event-details"  id="event-details">
          <button class="close-btn" @click="removeActiveClass">&times;</button>
          <div class="event-title" id="event-title">
            <!-- {{ selectedEvent.year ? `${selectedEvent?.year}年 - ${selectedEvent?.title}` : '' }} -->
          </div>
          <div class="event-description" id="event-description">
            <!-- {{ selectedEvent?.description }} -->
          </div>
          <div class="event-meta" id="event-meta">
            <!-- {{ selectedEvent?.value ? `黄宾虹 ${selectedEvent?.hbhAge}岁 | ${selectedEvent?.person} ${selectedEvent?.friendAge}岁 | 重要性: ${selectedEvent?.artWeight}/10` : '' }} -->
          </div>
        </div>
      </div>
    </div>
  </div>
  </body>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as d3 from 'd3'
import HeaderNavbar from '@/components/HeaderNavbar.vue';

export default {
  name: 'HuangBinhongNetwork',
  components: {
    HeaderNavbar
  },

  setup() {

    const  showEventDetails=(event) =>{
            const eventDetails = document.getElementById("event-details");
            document.getElementById("event-details").classList.add("active");
            document.getElementById("event-title").textContent = `${event.year}年 - ${event.title}`;
            document.getElementById("event-description").textContent = event.description;
            document.getElementById("event-meta").textContent = 
                `黄宾虹 ${event.hbhAge}岁 | ${event.person} ${event.friendAge}岁 | 重要性: ${event.artWeight}/10`;
            eventDetails.classList.add("active");
            selectedEvent.value = event;
            console.log(selectedEvent.value);
        }
    // 数据定义
    const data = reactive({
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
            { name: "邵赋清", history: "黄宾虹早年好友，共同探讨绘画艺术，对黄宾虹早期艺术风格有一定影响" },
            { name: "陈春帆", history: "黄宾虹的同乡好友，两人经常交流绘画心得，相互学习" },
            { name: "方良英", history: "黄宾虹的同窗好友，共同研习书画，探讨传统技法" },
            { name: "黄崇惺", history: "黄宾虹的亲属，在其艺术道路上给予了许多支持和建议" },
            { name: "程健行", history: "黄宾虹的师长，对其早期艺术风格的塑造产生了深远影响" }
          ],
          coords: [119.6454, 29.1164]
        },
        {
          id: "安徽歙县",
          year: "1890-1907",
          info: "祖籍地，26岁后多次居住，师从汪宗沂，组织'黄社'，参与革命活动，搜集古籍书画",
          people: [
            { name: "汪宗沂", history: "黄宾虹的恩师，传授其绘画技艺和文人修养，影响深远" },
            { name: "许承尧", history: "黄宾虹的挚友，共同参与革命活动，志同道合" },
            { name: "汪吉修", history: "黄宾虹的同乡好友，经常一起交流古籍书画鉴赏" },
            { name: "汪采白", history: "黄宾虹的晚辈，受其艺术影响，后来也成为著名画家" }
          ],
          coords: [118.4472, 29.8147]
        },
        {
          id: "上海",
          year: "1907-1937",
          info: "1907年避祸赴沪，至1937年为事业核心地，编辑《美术丛书》，任教暨南大学，组织烂漫社",
          people: [
            { name: "邓实", history: "黄宾虹的挚友，共同编辑《美术丛书》，对中国美术文献整理有重要贡献" },
            { name: "傅雷", history: "黄宾虹的晚辈，艺术上的知音，对其艺术成就的传播起到关键作用" },
            { name: "张大千", history: "黄宾虹的同道好友，两人经常交流绘画艺术，相互欣赏" },
            { name: "张善孖", history: "黄宾虹的同道好友，共同探讨艺术理论与创作实践" },
            { name: "吴昌硕", history: "海上画派领袖，黄宾虹的同道好友，交流书画艺术心得" }
          ],
          coords: [121.4737, 31.2304]
        },
        {
          id: "北平",
          year: "1937-1948",
          info: "1937-1948年寓居，鉴定故宫古画，任教北平艺专，闭门治学作画，拒绝日伪拉拢",
          people: [
            { name: "齐白石", history: "黄宾虹的挚友，两位大师互相尊重，在艺术理念上有许多共鸣" },
            { name: "周怀民", history: "黄宾虹的晚辈，受其艺术影响，致力于传统山水画的传承" },
            { name: "郭味蕖", history: "黄宾虹的晚辈，在艺术理论和实践上受到黄宾虹的重要影响" },
            { name: "李可染", history: "黄宾虹的晚辈，受其'浑厚华滋'艺术理念的深刻影响" }
          ],
          coords: [116.4074, 39.9042]
        },
        {
          id: "浙江杭州",
          year: "1948-1955",
          info: "1948年定居至逝世，任教杭州艺专，当选全国政协委员，创作达到顶峰，捐赠全部作品",
          people: [
            { name: "潘天寿", history: "黄宾虹的挚友，共同推动中国画教学改革，影响深远" },
            { name: "夏承焘", history: "黄宾虹的同道好友，两人经常交流书画艺术和诗词创作" },
            { name: "诸乐三", history: "黄宾虹的晚辈，在艺术上受到黄宾虹的指导和影响" },
            { name: "王伯敏", history: "黄宾虹的晚辈，后来成为研究黄宾虹艺术的重要学者" }
          ],
          coords: [120.1551, 30.2741]
        }
      ],
      timelineEvents: [
        {
          year: 1907,
          person: "邓实",
          title: "共同编辑《美术丛书》",
          description: "黄宾虹与邓实开始合作编辑《美术丛书》，这是中国近代美术史上重要的出版物，收录了大量古代书画理论著作，对整理和传承中国传统美术文献起到了重要作用。",
          artWeight: 9,
          hbhAge: 42,
          friendAge: 32
        },
        {
          year: 1912,
          person: "邓实",
          title: "创立贞社",
          description: "黄宾虹与邓实共同创立贞社，这是一个以研究金石书画为主的学术团体，聚集了当时一批有志于传统文化研究的学者，对推动近代美术研究有重要影响。",
          artWeight: 8,
          hbhAge: 47,
          friendAge: 37
        },
        {
          year: 1929,
          person: "傅雷",
          title: "初次相识",
          description: "傅雷初次拜访黄宾虹，两人一见如故，开始了长达二十多年的深厚友谊。傅雷后来成为黄宾虹艺术最重要的阐释者和推广者，两人的艺术对话成为中国现代美术史上的佳话。",
          artWeight: 7,
          hbhAge: 64,
          friendAge: 21
        },
        {
          year: 1931,
          person: "傅雷",
          title: "艺术理论交流",
          description: "傅雷与黄宾虹频繁通信，讨论中国画理论与创作，这些通信后来成为研究黄宾虹艺术思想的重要文献。通过交流，傅雷深刻理解了黄宾虹的艺术理念并为其传播奠定了基础。",
          artWeight: 8,
          hbhAge: 66,
          friendAge: 23
        },
        {
          year: 1937,
          person: "傅雷",
          title: "《黄宾虹画集》出版",
          description: "在傅雷的推动下，《黄宾虹画集》出版，这是黄宾虹第一本个人画集，收录了其各个时期的代表作品，标志着黄宾虹的艺术成就得到艺术界的广泛认可。",
          artWeight: 9,
          hbhAge: 72,
          friendAge: 29
        },
        {
          year: 1943,
          person: "傅雷",
          title: "八十寿辰展览",
          description: "傅雷为黄宾虹筹办八十寿辰画展，并撰写长篇序言，系统阐述黄宾虹的艺术成就与历史地位。这次展览在艺术界引起巨大反响，确立了黄宾虹作为一代宗师的地位。",
          artWeight: 10,
          hbhAge: 78,
          friendAge: 35
        },
        {
          year: 1925,
          person: "陈柱",
          title: "学术交流",
          description: "黄宾虹与陈柱开始密切交往，共同探讨金石学与文字学，这些交流对黄宾虹的绘画理论产生重要影响。两人的学术合作为传统金石学研究注入了新的活力。",
          artWeight: 7,
          hbhAge: 60,
          friendAge: 35
        },
        {
          year: 1930,
          person: "陈柱",
          title: "合作出版",
          description: "黄宾虹与陈柱合作出版《金石书画丛刊》，收录了大量珍贵的金石拓片与书画作品，为金石书画研究提供了重要的文献资料。",
          artWeight: 8,
          hbhAge: 65,
          friendAge: 40
        }
      ]
    })

    // 响应式数据
    const activeView = ref('overview')
    const selectedPlace = ref(null)
    const selectedFriend = ref(null)
    const selectedEvent = ref(null)
    
    // DOM 引用
    const overviewSvg = ref(null)
    const timelineSvg = ref(null)
    const mapSvg = ref(null)
    
    // 模拟变量
    let overviewSimulation = null
    let resizeTimer = null

    // 初始化函数
    const init = () => {
      initOverview()
      initTimeline()
      setupEventListeners()
    }

    // 初始化网络总览图
    const initOverview = () => {
      if (!overviewSvg.value) return
      
      const width = overviewSvg.value.parentElement.clientWidth
      const height = overviewSvg.value.parentElement.clientHeight

      // 清除现有内容
      d3.select(overviewSvg.value).selectAll("*").remove()

      const svg = d3.select(overviewSvg.value)
        .attr("width", width)
        .attr("height", height)

      // 创建节点数据
      const nodes = [data.core]
      data.locations.forEach(loc => nodes.push({ id: loc.id, type: "place" }))
      
      // 添加人物节点
      const peopleSet = new Set()
      data.locations.forEach(loc => loc.people.forEach(p => peopleSet.add(p.name)))
      peopleSet.forEach(p => nodes.push({ id: p, type: "person" }))

      // 创建连接数据
      const links = []
      data.locations.forEach(loc => {
        links.push({ source: "黄宾虹", target: loc.id })
      })
      data.locations.forEach(loc => {
        loc.people.forEach(person => {
          links.push({ source: loc.id, target: person.name })
        })
      })

      // 创建力导向图模拟
      overviewSimulation = d3.forceSimulation(nodes)
        .force("link", d3.forceLink(links).id(d => d.id).distance(100).strength(0.5))
        .force("charge", d3.forceManyBody().strength(-300))
        .force("center", d3.forceCenter(width / 2, height / 2))
        .force("collide", d3.forceCollide().radius(d => {
          if (d.id === "黄宾虹") return 30
          if (d.type === "place") return 25
          return 20
        }))

      // 绘制连接线
      const link = svg.append("g")
        .selectAll("line")
        .data(links)
        .enter().append("line")
        .attr("class", "link")

      // 绘制节点
      const node = svg.append("g")
        .selectAll("circle")
        .data(nodes)
        .enter().append("circle")
        .attr("class", d => {
          if (d.id === "黄宾虹") return "node core"
          if (d.type === "place") return "node place"
          return "node person"
        })
        .attr("r", d => {
          if (d.id === "黄宾虹") return 15
          if (d.type === "place") return 12
          return 8
        })
        .style("cursor", d => d.type === "place" ? "pointer" : "default")
        .call(d3.drag()
          .on("start", dragstarted)
          .on("drag", dragged)
          .on("end", dragended))

      // 节点点击事件
      node.filter(d => d.type === "place")
        .on("click", function(event, d) {
          showMapView(d.id)
        })

      // 绘制标签
      const label = svg.append("g")
        .selectAll("text")
        .data(nodes)
        .enter().append("text")
        .attr("class", d => d.type === "place" ? "label place-label" : "label")
        .attr("dy", d => d.type === "place" ? 20 : 15)
        .text(d => d.id)
        .style("font-size", d => d.id === "黄宾虹" ? "14px" : "12px")

      // 力导向图更新函数
      overviewSimulation.on("tick", () => {
        link
          .attr("x1", d => d.source.x)
          .attr("y1", d => d.source.y)
          .attr("x2", d => d.target.x)
          .attr("y2", d => d.target.y)

        node
          .attr("cx", d => d.x = Math.max(30, Math.min(width - 30, d.x)))
          .attr("cy", d => d.y = Math.max(30, Math.min(height - 30, d.y)))

        label
          .attr("x", d => d.x)
          .attr("y", d => d.y)
      })

      // 拖动函数
      function dragstarted(event, d) {
        if (!event.active) overviewSimulation.alphaTarget(0.3).restart()
        d.fx = d.x
        d.fy = d.y
      }

      function dragged(event, d) {
        d.fx = event.x
        d.fy = event.y
      }

      function dragended(event, d) {
        if (!event.active) overviewSimulation.alphaTarget(0)
        d.fx = null
        d.fy = null
      }
    }

    // 显示地点详情视图
    const showMapView = (placeId) => {
      // 获取地点数据
      const place = data.locations.find(loc => loc.id === placeId)
      if (!place) return
      
      selectedPlace.value = place
      selectedFriend.value = null
      activeView.value = 'map'
      
      nextTick(() => {
        initMapView()
      })
    }

    // 初始化地图视图
    const initMapView = () => {
      if (!mapSvg.value || !selectedPlace.value) return
      
      const width = mapSvg.value.parentElement.clientWidth
      const height = mapSvg.value.parentElement.clientHeight

      // 清除现有地图
      d3.select(mapSvg.value).selectAll("*").remove()

      const svg = d3.select(mapSvg.value)
        .attr("width", width)
        .attr("height", height)

      const centerX = width / 2
      const centerY = height / 2
      const placeRadius = 25
      const personRadius = 15
      const connectRadius = Math.min(width, height) * 0.3

      // 绘制中心地点节点
      svg.append("circle")
        .attr("cx", centerX)
        .attr("cy", centerY)
        .attr("r", placeRadius)
        .attr("class", "node place")
        .style("fill", "#ebd976")

      // 地点名称
      svg.append("text")
        .attr("x", centerX)
        .attr("y", centerY - placeRadius - 15)
        .text(selectedPlace.value.id)
        .attr("class", "label place-label")
        .attr("text-anchor", "middle")
        .style("font-size", "16px")

      // 绘制人物节点和连接线
      const peopleCount = selectedPlace.value.people.length
      selectedPlace.value.people.forEach((person, index) => {
        const angle = (index / peopleCount) * Math.PI * 2
        const personX = centerX + connectRadius * Math.cos(angle)
        const personY = centerY + connectRadius * Math.sin(angle)

        // 计算连接线的起点和终点
        const startX = centerX + placeRadius * Math.cos(angle)
        const startY = centerY + placeRadius * Math.sin(angle)
        const endX = personX - personRadius * Math.cos(angle)
        const endY = personY - personRadius * Math.sin(angle)

        // 绘制连接线
        svg.append("line")
          .attr("x1", startX)
          .attr("y1", startY)
          .attr("x2", endX)
          .attr("y2", endY)
          .attr("class", "link")
          .attr("stroke-width", 2)

        // 绘制人物节点
        svg.append("circle")
          .attr("cx", personX)
          .attr("cy", personY)
          .attr("r", personRadius)
          .attr("class", "node person")
          .style("cursor", "pointer")
          .on("click", () => {
            selectedFriend.value = person
          })

        // 人物姓名
        svg.append("text")
          .attr("x", personX)
          .attr("y", personY + personRadius + 18)
          .text(person.name)
          .attr("class", "label")
          .attr("text-anchor", "middle")
          .style("cursor", "pointer")
          .on("click", () => {
            selectedFriend.value = person
          })
      })
    }

    // 初始化时间轴
    const initTimeline = () => {
      if (!timelineSvg.value) return
      
      const width = timelineSvg.value.parentElement.clientWidth
      const height = timelineSvg.value.parentElement.clientHeight

      // 清除现有内容
      d3.select(timelineSvg.value).selectAll("*").remove()

      const svg = d3.select(timelineSvg.value)
        .attr("width", width)
        .attr("height", height)

      const margin = { top: 50, right: 50, bottom: 50, left: 100 }
      const innerWidth = width - margin.left - margin.right
      const innerHeight = height - margin.top - margin.bottom

      // 创建时间轴比例尺
      const xScale = d3.scaleLinear()
        .domain([1865, 1955])
        .range([0, innerWidth])

      // 创建好友纵坐标比例尺
      const friends = ["黄宾虹", "邓实", "傅雷", "陈柱"]
      const yScale = d3.scalePoint()
        .domain(friends)
        .range([0, innerHeight])
        .padding(0.5)

      // 创建画布组
      const g = svg.append("g")
        .attr("transform", `translate(${margin.left}, ${margin.top})`)

      // 绘制主线
      g.append("line")
        .attr("x1", 0)
        .attr("y1", yScale("黄宾虹"))
        .attr("x2", innerWidth)
        .attr("y2", yScale("黄宾虹"))
        .attr("class", "timeline-axis")
        .attr("stroke-width", 3)

      // 绘制其他好友的时间线
      friends.forEach(friend => {
        if (friend !== "黄宾虹") {
          g.append("line")
            .attr("x1", 0)
            .attr("y1", yScale(friend))
            .attr("x2", innerWidth)
            .attr("y2", yScale(friend))
            .attr("class", "timeline-axis")
            .style("stroke-dasharray", "5,5")
            .style("stroke", "#8b7d6b")
            .style("stroke-width", 1.5)
        }
      })

      // 绘制年份刻度
      const years = d3.range(1870, 1956, 10)
      years.forEach(year => {
        // 刻度线
        g.append("line")
          .attr("x1", xScale(year))
          .attr("y1", yScale("黄宾虹") - 8)
          .attr("x2", xScale(year))
          .attr("y2", yScale("黄宾虹") + 8)
          .attr("class", "timeline-axis")
          .attr("stroke-width", 2)

        // 年份标签
        g.append("text")
          .attr("x", xScale(year))
          .attr("y", yScale("黄宾虹") - 15)
          .text(year)
          .attr("class", "timeline-label")
          .attr("text-anchor", "middle")
          .style("font-size", "13px")
          .style("font-weight", "bold")
      })

      // 绘制好友姓名标签
      friends.forEach(friend => {
        g.append("text")
          .attr("x", -15)
          .attr("y", yScale(friend))
          .text(friend)
          .attr("class", "timeline-label")
          .attr("text-anchor", "end")
          .attr("dominant-baseline", "middle")
          .style("font-size", "14px")
          .style("font-weight", friend === "黄宾虹" ? "bold" : "normal")
      })

      // 绘制事件点
      data.timelineEvents.forEach(event => {
        const x = xScale(event.year)
        const y = yScale(event.person)
        
        // 事件连接线
        g.append("line")
          .attr("x1", x)
          .attr("y1", yScale("黄宾虹"))
          .attr("x2", x)
          .attr("y2", y)
          .attr("class", "timeline-event-line")

        // 事件点
        // 事件点 - 创建一个闭包来捕获当前事件
        const circle = g.append("circle")
          .attr("cx", x)
          .attr("cy", y)
          .attr("r", event.artWeight)
          .attr("class", "timeline-event-circle")
          .style("cursor", "pointer")
        
        // 使用闭包确保每个事件都有自己的点击处理函数
        circle.on("click", function() {
          console.log('点击事件点:', event)
          // 直接设置响应式变量
          selectedEvent.value = {...event}
          showEventDetails(event)
        })

        // 事件标题
        g.append("text")
          .attr("x", x)
          .attr("y", y - event.artWeight - 8)
          .text(event.title)
          .attr("class", "timeline-label")
          .attr("text-anchor", "middle")
          .style("font-size", "11px")
          .style("cursor", "pointer")
          .on("click", () => {
             selectedEvent.value = { ...event }
          })
      })
    }

    // 切换视图
    const switchView = (view) => {
      activeView.value = view
      
      if (view === 'overview') {
        selectedPlace.value = null
        selectedFriend.value = null
        selectedEvent.value = null
      }
      
      nextTick(() => {
        if (view === 'overview') {
          initOverview()
        } else if (view === 'timeline') {
          initTimeline()
        } else if (view === 'map' && selectedPlace.value) {
          initMapView()
        }
      })
    }

    // 窗口大小调整处理函数
    const handleResize = () => {
      clearTimeout(resizeTimer)
      resizeTimer = setTimeout(() => {
        if (activeView.value === 'overview') {
          initOverview()
        } else if (activeView.value === 'timeline') {
          initTimeline()
        } else if (activeView.value === 'map' && selectedPlace.value) {
          initMapView()
        }
      }, 250)
    }

    // 设置事件监听器
    const setupEventListeners = () => {
      window.addEventListener('resize', handleResize)
      
      // 提示信息自动隐藏
      setTimeout(() => {
        const hints = document.querySelectorAll('.hint')
        hints.forEach(hint => {
          hint.style.opacity = '0.7'
        })
      }, 3000)
    }

    // 清理函数
    const cleanup = () => {
      if (overviewSimulation) {
        overviewSimulation.stop()
      }
      window.removeEventListener('resize', handleResize)
    }

    // 生命周期钩子
    onMounted(() => {
      init()
    })

    onUnmounted(() => {
      cleanup()
    })

    // 监听视图变化
    watch(activeView, (newView) => {
      if (newView === 'overview') {
        initOverview()
      } else if (newView === 'timeline') {
        nextTick(() => {
          initTimeline()
        })
      }
    })

    // 点击弹窗外部关闭
    const handleClickOutside = (event) => {
      const friendDetails = document.getElementById('friend-details')
      const eventDetails = document.getElementById('event-details')
      
      if (friendDetails && friendDetails.classList.contains('active') && 
          !friendDetails.contains(event.target)) {
        selectedFriend.value = null
      }
      
      if (eventDetails && eventDetails.classList.contains('active') && 
          !eventDetails.contains(event.target)) {
        selectedEvent.value = null
      }
    }

    // 初始化点击外部关闭监听
    onMounted(() => {
      document.addEventListener('click', handleClickOutside)
    })

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })

    // 暴露给模板
    return {
      // 数据
      data,
      
      // 响应式状态
      activeView,
      selectedPlace,
      selectedFriend,
      selectedEvent,
      
      // DOM 引用
      overviewSvg,
      timelineSvg,
      mapSvg,
      
      // 方法
      switchView,
      showMapView,
      
    }
  },
  methods: { 
    removeActiveClass() {
      const eventDetails = document.getElementById('event-details')
      if (eventDetails) {
        eventDetails.classList.remove('active')
      }
    }
  },
}
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

        /* 统一导航栏样式 - 与主页保持一致 */
        .header {
            background-color: rgba(139, 69, 19, 0.95);
            backdrop-filter: blur(10px);
            padding: 15px 40px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            position: fixed;
            top: 0;
            width: 100%;
            z-index: 1000;
        }

        .logo {
            display: flex;
            align-items: center;
            gap: 15px;
            text-decoration: none;
        }

        .logo-icon {
            color: #d2b48c;
            font-size: 28px;
        }

        .logo-text {
            color: #fff;
            font-size: 24px;
            font-weight: bold;
            letter-spacing: 2px;
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
        }

        .nav-menu {
            display: flex;
            gap: 30px;
        }

        .nav-item {
            color: #f8f4e6;
            text-decoration: none;
            font-size: 16px;
            padding: 8px 15px;
            border-radius: 20px;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .nav-item:hover {
            background-color: rgba(210, 180, 140, 0.3);
            color: #fff;
            transform: translateY(-2px);
        }

        .nav-item.active {
            background-color: #d2b48c;
            color: #5c4033;
        }

        .mobile-menu-btn {
            display: none;
            color: #fff;
            font-size: 24px;
            cursor: pointer;
        }

        /* 主内容区 */
        .main-content {
            padding-top: 120px;
            max-width: 1400px;
            margin: 0 auto;
            padding-left: 20px;
            padding-right: 20px;
        }

        /* 页面标题样式 */
        .page-header {
            text-align: center;
            margin-bottom: 40px;
            padding: 0 20px;
        }

        .page-title {
            font-size: 36px;
            color: #8b4513;
            margin-bottom: 15px;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
        }

        .page-subtitle {
            font-size: 18px;
            color: #5c4033;
            max-width: 800px;
            margin: 0 auto;
            line-height: 1.6;
        }

        .container {
            
            max-width: 1200px;
            margin: 0 auto;
            padding: 30px;
            background-color: rgba(255, 251, 240, 0.9);
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
            border: 2px solid #d2b48c;
        }

        .intro {
            text-align: center;
            margin: 20px 0 30px 0;
            color: #5c4033;
            line-height: 1.6;
            font-size: 16px;
            padding: 0 20px;
        }

        .nav-tabs {
            display: flex;
            justify-content: center;
            margin-bottom: 30px;
            border-bottom: 2px solid #d2b48c;
            padding-bottom: 5px;
            flex-wrap: wrap;
            gap: 10px;
        }

        .nav-tab {
            padding: 12px 25px;
            cursor: pointer;
            background-color: #f8f4e6;
            border: 2px solid #d2b48c;
            border-bottom: none;
            border-radius: 8px 8px 0 0;
            margin: 0 5px;
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 16px;
            color: #5c4033;
            font-weight: 600;
            transition: all 0.3s ease;
            min-width: 180px;
            justify-content: center;
        }

        .nav-tab:hover {
            background-color: rgba(210, 180, 140, 0.2);
            transform: translateY(-2px);
        }

        .nav-tab.active {
            background-color: #d2b48c;
            color: #5c4033;
            border-color: #8b4513;
            font-weight: bold;
        }

        .view {
            display: none;
            margin-top: 20px;
            height: 600px;
            border: 2px solid #d2b48c;
            border-radius: 10px;
            background-color: #fffbf0;
            position: relative;
            box-shadow: 0 5px 15px rgba(0,0,0,0.05);
            overflow: hidden;
        }

        .view.active {
            display: block;
        }

        #overview-view {
            overflow: hidden;
        }

        /* 网络图样式 */
        .node {
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .node.place {
            fill: #ebd976;
        }

        .node.person {
            fill: #abc08d;
        }

        .node.core {
            fill: #80996a;
        }

        .node:hover {
            filter: drop-shadow(0 0 8px rgba(139, 69, 19, 0.3));
            transform: scale(1.1);
        }

        .link {
            stroke: #8b7d6b;
            stroke-opacity: 0.6;
            stroke-width: 1.5;
        }

        .label {
            font-size: 12px;
            text-anchor: middle;
            pointer-events: none;
            font-weight: 600;
        }

        .place-label {
            font-weight: bold;
            font-size: 13px;
        }

        /* 返回按钮样式 */
        .back-btn {
            position: absolute;
            top: 15px;
            left: 15px;
            background-color: #d2b48c;
            border: none;
            color: #333;
            padding: 10px 20px;
            border-radius: 5px;
            cursor: pointer;
            font-family: "SimSun", serif;
            font-size: 15px;
            display: flex;
            align-items: center;
            gap: 8px;
            transition: all 0.3s ease;
            z-index: 10;
            box-shadow: 0 3px 8px rgba(0,0,0,0.2);
        }

        .back-btn:hover {
            background-color: #a67c52;
            color: #fff;
            transform: translateX(-3px);
        }

        /* 地图信息框 */
        .map-info {
            position: absolute;
            top: 15px;
            right: 15px;
            background-color: rgba(255, 251, 240, 0.95);
            padding: 15px;
            border: 2px solid #d2b48c;
            border-radius: 8px;
            max-width: 300px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            z-index: 10;
        }

        .map-title {
            font-weight: bold;
            margin-bottom: 10px;
            color: #8b4513;
            font-size: 18px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .friend-info {
            font-size: 14px;
            margin-top: 10px;
            color: #5c4033;
        }

        .year-label {
            position: absolute;
            bottom: 15px;
            right: 15px;
            background-color: rgba(255, 251, 240, 0.95);
            padding: 8px 15px;
            border: 2px solid #d2b48c;
            border-radius: 5px;
            font-size: 14px;
            font-weight: 600;
            color: #5c4033;
            box-shadow: 0 3px 8px rgba(0,0,0,0.1);
            z-index: 10;
        }

        /* 详情弹窗 */
        .friend-details, .event-details {
            display: none;
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background-color: rgba(255, 251, 240, 0.98);
            padding: 30px;
            border: 3px solid #d2b48c;
            border-radius: 10px;
            width: 400px;
            text-align: center;
            box-shadow: 0 15px 40px rgba(0,0,0,0.2);
            z-index: 100;
            backdrop-filter: blur(10px);
        }

        .friend-details.active, .event-details.active {
            display: block;
            animation: modalSlideIn 0.3s ease-out;
        }

        @keyframes modalSlideIn {
            from {
                opacity: 0;
                transform: translate(-50%, -50%) translateY(30px);
            }
            to {
                opacity: 1;
                transform: translate(-50%, -50%);
            }
        }

        .friend-name, .event-title {
            font-size: 22px;
            font-weight: bold;
            color: #8b4513;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 2px solid #d2b48c;
        }

        .friend-history, .event-description {
            font-size: 16px;
            line-height: 1.6;
            color: #5c4033;
            margin-bottom: 20px;
        }

        .close-btn {
            position: absolute;
            top: 10px;
            right: 15px;
            background: none;
            border: none;
            font-size: 24px;
            cursor: pointer;
            color: #8b4513;
            transition: all 0.3s;
            width: 30px;
            height: 30px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
        }

        .close-btn:hover {
            background-color: rgba(139, 69, 19, 0.1);
            transform: rotate(90deg);
        }

        /* 时间轴容器 */
        .timeline-container {
            padding: 20px;
            height: 550px;
            overflow-y: auto;
        }

        .timeline-axis {
            stroke: #8b4513;
            stroke-width: 2;
        }

        .timeline-event-circle {
            fill: #d2b48c;
            stroke: #8b4513;
            stroke-width: 2;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .timeline-event-circle:hover {
            fill: #8b4513;
            transform: scale(1.2);
        }

        .timeline-event-line {
            stroke: #8b7d6b;
            stroke-width: 1.5;
            stroke-dasharray: 5,5;
        }

        .event-meta {
            font-size: 14px;
            color: #666;
            margin-top: 10px;
            padding-top: 10px;
            border-top: 1px solid #d2b48c;
        }


        .copyright {
            margin-top: 20px;
            color: #b8a48c;
            font-size: 14px;
        }

        /* 响应式设计 */
        @media (max-width: 768px) {

            
            .mobile-menu-btn {
                display: block;
            }
            
            .main-content {
                padding-top: 100px;
            }
            
            .page-title {
                font-size: 28px;
            }
            
            .page-subtitle {
                font-size: 16px;
            }
            
            .container {
                padding: 20px;
            }
            
            .nav-tabs {
                flex-direction: column;
                align-items: center;
            }
            
            .nav-tab {
                width: 100%;
                max-width: 300px;
                margin: 5px 0;
                border-radius: 8px;
            }
            
            .view {
                height: 500px;
            }
            
            .friend-details, .event-details {
                width: 90%;
                max-width: 350px;
                padding: 20px;
            }
            
            .map-info {
                position: relative;
                top: auto;
                right: auto;
                margin: 15px;
                max-width: none;
            }
            
            .year-label {
                position: relative;
                top: auto;
                right: auto;
                margin: 15px;
                display: inline-block;
            }
        }

        @media (max-width: 480px) {
            .page-title {
                font-size: 24px;
            }
            
            .view {
                height: 400px;
            }
            
            .timeline-container {
                height: 350px;
            }
            
            .friend-details, .event-details {
                padding: 15px;
            }
            
            .friend-name, .event-title {
                font-size: 20px;
            }
        }

        /* 加载提示 */
        .loading {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            color: #5c4033;
            font-size: 18px;
            font-weight: 600;
            z-index: 5;
        }

        /* 提示信息 */
        .hint {
            position: absolute;
            bottom: 15px;
            left: 15px;
            background-color: rgba(91, 64, 51, 0.9);
            color: #f8f4e6;
            padding: 8px 15px;
            border-radius: 5px;
            font-size: 12px;
            max-width: 300px;
            z-index: 10;
            border: 1px solid #d2b48c;
        }
    </style>