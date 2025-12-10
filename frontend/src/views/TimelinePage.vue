<template>
  <!-- 外层 wrapper 用于垂直居中 -->
  <div class="middle-timeline-wrapper">
    <!-- 固定的图例容器 -->
    <div class="chart-legend-fixed">
      <div class="legend-item">
        <span class="legend-dot calligraphy-dot"></span>
        <span class="legend-text">书法</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot paintings-dot"></span>
        <span class="legend-text">画作</span>
      </div>
    </div>
    <!-- 左侧说明标签 -->
    <div class="event-type-labels">
      <div class="event-type-label biography-label">
        <span>生平</span>
      </div>
      <div class="event-type-label historical-label">
        <span>史事</span>
      </div>
    </div>
    <!-- 年份tooltip -->
    <div v-if="activeTooltipYear !== null && showYearTooltipFlag" class="global-tooltip"
      :style="{ left: tooltipPos.x + 'px', top: tooltipPos.y + 'px' }">
      <div class="tooltip-year">{{ activeTooltipYear }}年</div>
      <div class="tooltip-stats">
        <span class="tooltip-line">书法：{{ getCalligraphyTotal(activeTooltipYear) }}</span>
        <span class="tooltip-line">画作：{{ getPaintingsTotal(activeTooltipYear) }}</span>
      </div>
    </div>

    <div class="middle-timeline-container" ref="timelineContainer">

      <!-- 折线图容器 - 随容器滚动 -->
      <div class="chart-lines-container">
        <!-- 画作折线 - 使用单一路径并添加动画 -->
        <svg class="paintings-line-svg" :style="{ width: chartWidth + 'px', height: chartHeight + 'px' }">
          <path :d="paintingsPath" fill="none" stroke="#e67e22" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round" class="line-path" />
        </svg>

        <!-- 书法折线 - 使用单一路径并添加动画 -->
        <svg class="calligraphy-line-svg" :style="{ width: chartWidth + 'px', height: chartHeight + 'px' }">
          <path :d="calligraphyPath" fill="none" stroke="#8e682f" stroke-width="2" stroke-linecap="round"
            stroke-linejoin="round" class="line-path" />
        </svg>
      </div>

      <div class="timeline-column-wrapper" :style="{ width: chartWidth + 'px' }">
        <div class="timeline-column" v-for="yearItem in yearNodes" :key="yearItem.year"
          :class="{ shifted: shiftMap[yearItem.year] }">
          <!-- 年份节点 -->
          <div class="year-node" @mouseenter="showYearTooltip(yearItem.year, $event)" @mouseleave="hideYearTooltip()">
            <div class="year-circle" :class="{ 'decade-circle': yearItem.isDecade }"></div>
            <span v-if="yearItem.isDecade" class="decade-year">{{ yearItem.year }}</span>
          </div>

          <!-- 内容区 - 分为固定的上下两部分 -->
          <div class="content-wrapper" v-if="hasContent(yearItem.year)">
            <!-- 上方：生平事件区域 -->
            <div class="content-area biography-area" :class="{ 'no-content': !hasBiographyEvents(yearItem.year) }">
              <div class="content-list" v-if="hasBiographyEvents(yearItem.year)">
                <!-- 生平事件 -->
                <div class="content-item biography-item" v-for="event in getBiographyEventsByYear(yearItem.year)"
                  :key="'bio' + event.timeline_id"
                  @mouseenter="setActiveEvent(yearItem.year, 'bio' + event.timeline_id)"
                  @mouseleave="clearActiveEvent()">
                  <div class="content-title biography-title">{{ event.event_title }}</div>
                  <div class="detail-container" :data-event-id="'bio' + event.timeline_id"
                    :class="{ expanded: activeYear === yearItem.year && activeId === 'bio' + event.timeline_id }">
                    <!-- 外部边框 -->
                    <div class="detail-outline"></div>
                    <!-- 内部滚动容器 -->
                    <div class="detail-scroll-container">
                      <!-- 内边框 -->
                      <div class="detail-inner-border"></div>
                      <!-- 实际内容 -->
                      <div class="event-detail">{{ event.detail }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 下方：历史事件区域 -->
            <div class="content-area historical-area" :class="{ 'no-content': !hasHistoricalEvents(yearItem.year) }">
              <div class="content-list" v-if="hasHistoricalEvents(yearItem.year)">
                <!-- 历史事件 -->
                <div class="content-item historical-item" v-for="event in getHistoricalEventsByYear(yearItem.year)"
                  :key="'his' + event.historical_event_id"
                  @mouseenter="setActiveEvent(yearItem.year, 'his' + event.historical_event_id)"
                  @mouseleave="clearActiveEvent()">
                  <div class="content-title historical-title">{{ event.event_title }}</div>
                  <div class="detail-container" :data-event-id="'his' + event.historical_event_id"
                    :class="{ expanded: activeYear === yearItem.year && activeId === 'his' + event.historical_event_id }">
                    <!-- 外部边框 -->
                    <div class="detail-outline"></div>
                    <!-- 内部滚动容器 -->
                    <div class="detail-scroll-container">
                      <!-- 内边框 -->
                      <div class="detail-inner-border"></div>
                      <!-- 实际内容 -->
                      <div class="event-detail">{{ event.description }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "MiddleTimeline",
  data() {
    return {
      startYear: 1865,
      endYear: 1955,
      isScrollingToShowDetail: false,
      activeTooltipYear: null,
      activeYear: null,
      activeId: null,
      scrollAnimationFrame: null,
      showYearTooltipFlag: false,

      tooltipPos: { x: 0, y: 0 },

      // 生平事件数据
      // 在 data() 中替换现有的数据

      // 生平事件数据（黄宾虹个人事迹）
      biographyEvents: [
        { timeline_id: 1, year: 1865, event_title: "出生", detail: "1月27日（农历元旦子时）生于浙江金华铁岭头。原名黄质，字朴存" },
        { timeline_id: 2, year: 1871, event_title: "早期引导", detail: "族中翰林黄崇惺（次蓀）道经金华，为其订课程，成为早期最重要的引路人。" },
        { timeline_id: 3, year: 1876, event_title: "返乡应试", detail: "随父返安徽歙县应童子试，名列前茅，开始接触乡邦文物与收藏" },
        { timeline_id: 4, year: 1886, event_title: "补廪生成家", detail: "返歙补廪生。娶洪四果为妻。问学于经学大家汪仲伊。" },
        { timeline_id: 5, year: 1888, event_title: "游学南京扬州", detail: "游学南京、扬州。拜谒杨仁山（窥佛学、舆地学）、杨长年、甘元焕（习东汉、西汉之学），知识结构得以极大拓展。" },
        { timeline_id: 6, year: 1890, event_title: "制墨研艺", detail: "1890-1900年代：协助父亲以'易水法'制墨，深研墨性。往来于歙县、扬州、南京，广交艺友，潜心金石书画。" },
        { timeline_id: 7, year: 1907, event_title: "人生转折", detail: "因同乡革命党人牵连，被迫出走上海。结识邓实（秋枚）、黄节，加入'国学保存会'，协办《国粹学报》与《神州国光集》，从传统文人转型为现代文化出版与学者型艺术家。" },
        { timeline_id: 8, year: 1909, event_title: "定居上海", detail: "正式定居上海，寓居国学保存会藏书楼，开始其职业编辑与著述生涯。" },
        { timeline_id: 9, year: 1911, event_title: "编辑出版", detail: "与邓实合编的《美术丛书》开始出版，系统整理中国古典艺术文献" },
        { timeline_id: 10, year: 1912, event_title: "发起组织贞社", detail: "发起组织'贞社'，以研究金石书画为目的，广联中外同道。" },
        { timeline_id: 11, year: 1915, event_title: "国际艺术交流", detail: "为古玩商史德匿编撰《中华名画》图录，序言以中英文发表，开启国际艺术交流。" },
        { timeline_id: 12, year: 1919, event_title: "主编《美术周刊》", detail: "任《时报》副刊《美术周刊》主编，发表《古学复兴》等重要文章，系统宣扬其艺术思想。" },
        { timeline_id: 13, year: 1920, event_title: "任教与著述", detail: "20世纪20年代至30年代：陆续在上海各大艺术院校（如上海美专、新华艺专）任教。出版《古画微》（1925）、《画学通论》等重要著作，构建其画史与画学理论体系。" },
        { timeline_id: 14, year: 1928, event_title: "改组神州国光社", detail: "参与改组'神州国光社'，任美术部主任。" },
        { timeline_id: 15, year: 1934, event_title: "道咸画学中兴理论", detail: "发表《致治以文说》、《画法要旨》，标志其'道咸画学中兴'理论成熟，提出'民学'观，强调'内美'。" },
        { timeline_id: 16, year: 1937, event_title: "鉴定故宫藏画", detail: "应北平古物陈列所之邀，赴京鉴定故宫藏画。因抗战爆发，滞留北平十年。" },
        { timeline_id: 17, year: 1938, event_title: "黑宾虹时期", detail: "1937-1948年：闭门研求，融会贯通。画风大变，进入'黑宾虹'时期，笔墨层层积染，达于'浑厚华滋'之化境。与傅雷书信往还，深入探讨画理，互为知音。" },
        { timeline_id: 18, year: 1943, event_title: "八十寿辰画展", detail: "在北平举办八十寿辰画展（实为虚岁），傅雷在上海为其筹办'黄宾虹八秩诞辰书画展览会'。" },
        { timeline_id: 19, year: 1948, event_title: "南下任教杭州", detail: "应杭州国立艺术专科学校（现中国美术学院）之聘，南下任教，定居杭州栖霞岭。" },
        { timeline_id: 20, year: 1953, event_title: "人民艺术家称号", detail: "被授予'人民艺术家'称号。同年，任中央美术学院民族美术研究所所长（北京）。" },
        { timeline_id: 21, year: 1954, event_title: "华东美协副主席", detail: "赴上海出席华东美术家协会成立大会，当选副主席。" },
        { timeline_id: 22, year: 1955, event_title: "逝世", detail: "3月25日，在杭州病逝。遗嘱将所有藏品与作品万余件捐献给国家，现藏于浙江省博物馆。" }
      ],

      // 历史事件数据（每个年份只保留一个代表性事件）
      historicalEvents: [
        // 中国历史事件
        { historical_event_id: 1, year: 1871, event_title: "洋务运动", description: "1870年代起，清廷推行'自强''求富'改革，创办近代工业、海军、学堂（如江南制造总局、北洋水师）。" },
        { historical_event_id: 2, year: 1894, event_title: "甲午战争", description: "1894-1895年：中日甲午战争，中国战败，签订《马关条约》，民族危机空前加剧。" },
        { historical_event_id: 3, year: 1898, event_title: "戊戌变法", description: "光绪帝推行戊戌变法，试图进行政治改革，但仅持续103天，遭慈禧太后镇压而失败。" },
        { historical_event_id: 4, year: 1900, event_title: "八国联军侵华", description: "义和团运动与八国联军侵华，清廷利用义和团排外，导致八国联军攻占北京，签订《辛丑条约》。" },
        { historical_event_id: 5, year: 1905, event_title: "废除科举制度", description: "清政府宣布废除延续1300多年的科举制度，现代教育体系逐步建立。" },
        { historical_event_id: 6, year: 1910, event_title: "白话文运动", description: "1910年代起，胡适、鲁迅等推动白话文取代文言文，促进新文化传播。" },
        { historical_event_id: 7, year: 1911, event_title: "辛亥革命", description: "武昌起义爆发，标志着辛亥革命的开始，清朝统治结束。" },
        { historical_event_id: 8, year: 1912, event_title: "中华民国成立", description: "孙中山在南京就任临时大总统，中华民国正式成立。袁世凯继任后迁都北京。" },
        { historical_event_id: 9, year: 1915, event_title: "新文化运动", description: "1915-1920年代：陈独秀创办《青年杂志》（后改为《新青年》），发起思想启蒙运动，倡导'民主'与'科学'，反对旧文化。" },
        { historical_event_id: 10, year: 1919, event_title: "五四运动", description: "因巴黎和会外交失败爆发的爱国运动，成为中国新民主主义革命的开端。" },
        { historical_event_id: 11, year: 1921, event_title: "中国共产党成立", description: "中国共产党在上海召开第一次全国代表大会，宣告中国共产党成立。" },
        { historical_event_id: 12, year: 1926, event_title: "北伐战争", description: "1926-1928年：国民革命军开始北伐，旨在推翻北洋军阀统治，统一中国。" },
        { historical_event_id: 13, year: 1931, event_title: "九一八事变", description: "日本关东军发动九一八事变，开始侵占中国东北三省。" },
        { historical_event_id: 14, year: 1932, event_title: "一二八事变", description: "日本海军陆战队进攻上海闸北，中国军队奋起抵抗。" },
        { historical_event_id: 15, year: 1934, event_title: "红军长征", description: "中国工农红军开始长征，进行战略转移至陕北。" },
        { historical_event_id: 16, year: 1937, event_title: "七七事变", description: "卢沟桥事变爆发，标志着日本全面侵华战争的开始，全国抗战开始。" },
        { historical_event_id: 17, year: 1945, event_title: "抗日战争胜利", description: "日本宣布无条件投降，中国人民抗日战争取得完全胜利。" },
        { historical_event_id: 18, year: 1949, event_title: "中华人民共和国成立", description: "毛泽东在天安门城楼上宣布中华人民共和国中央人民政府成立，定都北京。" },
        { historical_event_id: 19, year: 1950, event_title: "土地改革与抗美援朝", description: "土地改革：废除封建土地所有制，实行农民土地所有制。抗美援朝：中国人民志愿军赴朝鲜作战。" },
        { historical_event_id: 20, year: 1953, event_title: "社会主义改造", description: "1953-1957年：开始对农业、手工业和资本主义工商业进行社会主义改造，逐步建立公有制经济。实施第一个五年计划，进行社会主义工业化建设。" },

        // 世界历史事件（选择代表性年份）
        // { historical_event_id: 21, year: 1871, event_title: "第二次工业革命", description: "1870年代起，电力、石油、汽车工业兴起，科学技术快速发展，推动生产力巨大进步。" },
        { historical_event_id: 22, year: 1914, event_title: "第一次世界大战", description: "1914-1918年：第一次世界大战爆发，中国后期参战，成为战胜国之一。" },
        { historical_event_id: 23, year: 1917, event_title: "俄国十月革命", description: "俄国爆发十月革命，建立世界上第一个社会主义国家，马克思主义影响全球。" },
        { historical_event_id: 24, year: 1939, event_title: "第二次世界大战", description: "1939-1945年：第二次世界大战全面爆发，全球反法西斯战争开始，中国是东方主战场。" },
        { historical_event_id: 25, year: 1947, event_title: "冷战格局形成", description: "1947-1955年：美苏对立加剧，世界逐渐分为资本主义和社会主义两大阵营，冷战格局形成。" }
      ],

      // 统计数据（保留用于折线图）
      statsTotalData: [
        { year: 1865, calligraphy: 0, paintings: 0 },
        { year: 1870, calligraphy: 0, paintings: 1 },
        { year: 1875, calligraphy: 0, paintings: 2 },
        { year: 1880, calligraphy: 0, paintings: 3 },
        { year: 1885, calligraphy: 1, paintings: 4 },
        { year: 1890, calligraphy: 1, paintings: 5 },
        { year: 1895, calligraphy: 2, paintings: 7 },
        { year: 1900, calligraphy: 3, paintings: 9 },
        { year: 1905, calligraphy: 3, paintings: 10 },
        { year: 1907, calligraphy: 3, paintings: 5 },
        { year: 1910, calligraphy: 4, paintings: 12 },
        { year: 1915, calligraphy: 4, paintings: 15 },
        { year: 1920, calligraphy: 5, paintings: 18 },
        { year: 1925, calligraphy: 6, paintings: 22 },
        { year: 1930, calligraphy: 7, paintings: 25 },
        { year: 1933, calligraphy: 5, paintings: 15 },
        { year: 1935, calligraphy: 8, paintings: 28 },
        { year: 1940, calligraphy: 9, paintings: 32 },
        { year: 1945, calligraphy: 10, paintings: 20 },
        { year: 1950, calligraphy: 12, paintings: 35 },
        { year: 1955, calligraphy: 15, paintings: 40 }
      ],
      yearSpacing: 76,
      chartHeight: 120,
      chartTop: 0,
      shiftOffset: 360,

      // 添加一个动画进度的数据
      animationProgress: 0,
      animationTimer: null,
    };
  },

  computed: {
    maxDataValue() {
      let max = 0;
      this.statsTotalData.forEach(item => {
        max = Math.max(max, item.calligraphy, item.paintings);
      });
      return max || 20;
    },

    yearNodes() {
      const nodes = [];
      for (let y = this.startYear; y <= this.endYear; y++) {
        nodes.push({
          year: y,
          isDecade: y % 10 === 0,
        });
      }
      return nodes;
    },

    shiftMap() {
      if (!this.activeYear) return {};
      const map = {};
      this.yearNodes.forEach(node => {
        map[node.year] = node.year > this.activeYear;
      });
      return map;
    },

    chartWidth() {
      return this.yearNodes.length * this.yearSpacing;
    },

    yearDataWithPositions() {
      const data = [];
      const yearToData = {};

      this.statsTotalData.forEach(item => {
        yearToData[item.year] = item;
      });

      this.yearNodes.forEach((node, index) => {
        const yearData = yearToData[node.year] || { calligraphy: 0, paintings: 0 };
        const x = (index * this.yearSpacing) + (this.yearSpacing / 2);

        data.push({
          year: node.year,
          index: index,
          calligraphy: yearData.calligraphy,
          paintings: yearData.paintings,
          x: x,
        });
      });

      return data;
    },

    /** 计算动画插值后的坐标 */
    interpolatedPoints() {
      const points = [];

      this.yearDataWithPositions.forEach(item => {
        const value = item.paintings;
        let y;
        if (value === 0) {
          y = this.chartTop + this.chartHeight - 10;
        } else {
          const usableHeight = this.chartHeight - 30;
          y = this.chartTop + (1 - value / this.maxDataValue) * usableHeight + 10;
          y = Math.max(y, this.chartTop + 10);
        }

        // 计算原始位置
        const originalX = item.x;
        // 计算平移后的位置
        const shiftedX = this.shiftMap[item.year] ? item.x + this.shiftOffset : item.x;

        // 使用动画进度进行插值
        const currentX = originalX + (shiftedX - originalX) * this.animationProgress;

        points.push({
          year: item.year,
          x: currentX,
          y: y,
          originalX: originalX,
          shiftedX: shiftedX
        });
      });

      return points;
    },

    /** 画作路径 - 使用插值后的点 */
    paintingsPath() {
      const points = this.interpolatedPoints;

      if (points.length < 2) return '';

      // 生成平滑的贝塞尔曲线路径
      let path = `M ${points[0].x},${points[0].y}`;
      for (let i = 1; i < points.length; i++) {
        const prevX = points[i - 1].x;
        const prevY = points[i - 1].y;
        const currX = points[i].x;
        const currY = points[i].y;

        const cp1x = prevX + (currX - prevX) / 3;
        const cp1y = prevY;
        const cp2x = currX - (currX - prevX) / 3;
        const cp2y = currY;

        path += ` C ${cp1x},${cp1y} ${cp2x},${cp2y} ${currX},${currY}`;
      }

      return path;
    },

    /** 书法路径 - 重新计算书法数据 */
    calligraphyPath() {
      const points = [];

      this.yearDataWithPositions.forEach(item => {
        const value = item.calligraphy;
        let y;
        if (value === 0) {
          y = this.chartTop + this.chartHeight - 10;
        } else {
          const usableHeight = this.chartHeight - 30;
          y = this.chartTop + (1 - value / this.maxDataValue) * usableHeight + 10;
          y = Math.max(y, this.chartTop + 10);
        }

        // 计算原始位置
        const originalX = item.x;
        // 计算平移后的位置
        const shiftedX = this.shiftMap[item.year] ? item.x + this.shiftOffset : item.x;

        // 使用动画进度进行插值
        const currentX = originalX + (shiftedX - originalX) * this.animationProgress;

        points.push({
          year: item.year,
          x: currentX,
          y: y,
          originalX: originalX,
          shiftedX: shiftedX
        });
      });

      if (points.length < 2) return '';

      // 生成平滑的贝塞尔曲线路径
      let path = `M ${points[0].x},${points[0].y}`;
      for (let i = 1; i < points.length; i++) {
        const prevX = points[i - 1].x;
        const prevY = points[i - 1].y;
        const currX = points[i].x;
        const currY = points[i].y;

        const cp1x = prevX + (currX - prevX) / 3;
        const cp1y = prevY;
        const cp2x = currX - (currX - prevX) / 3;
        const cp2y = currY;

        path += ` C ${cp1x},${cp1y} ${cp2x},${cp2y} ${currX},${currY}`;
      }

      return path;
    },
  },

  methods: {
    /** 检查元素是否在视口右侧 */
    isElementAtRightEdge(element) {
      if (!element) return false;

      const rect = element.getBoundingClientRect();
      const viewportWidth = window.innerWidth;

      console.log('元素右侧位置:', rect.right, '视口宽度:', viewportWidth, '差值:', rect.right - viewportWidth);

      // 检查详情容器的右侧是否超出视口（加上5px容差）
      return rect.right > viewportWidth - 5;
    },

    /** 计算需要滚动的距离以显示详情容器 */
    calculateScrollToShowDetail(container, detailContainer) {
      if (!container || !detailContainer) return 0;

      const containerRect = container.getBoundingClientRect();
      const detailRect = detailContainer.getBoundingClientRect();

      // 计算详情容器右侧超出容器右侧的距离
      const overflowRight = detailRect.right - containerRect.right;

      console.log('滚动计算:', {
        containerRight: containerRect.right,
        detailRight: detailRect.right,
        overflowRight: overflowRight,
        containerScrollLeft: container.scrollLeft,
        containerScrollWidth: container.scrollWidth,
        containerClientWidth: container.clientWidth,
        maxScrollLeft: container.scrollWidth - container.clientWidth
      });

      // 如果超出，返回需要滚动的距离（加上一些边距）
      if (overflowRight > 0) {
        return overflowRight + 50; // 增加边距到50px
      }

      return 0;
    },

    /** 平滑滚动容器以显示详情 */
    smoothScrollToShowDetail(container, scrollAmount) {
      if (!container || scrollAmount <= 0) return;

      // 如果正在滚动，先停止之前的滚动
      if (this.isScrollingToShowDetail) {
        cancelAnimationFrame(this.scrollAnimationFrame);
      }

      this.isScrollingToShowDetail = true;

      const duration = 350; // 与CSS过渡时间保持一致（0.35s）
      const startScrollLeft = container.scrollLeft;
      const targetScrollLeft = startScrollLeft + scrollAmount;
      const maxScrollLeft = container.scrollWidth - container.clientWidth;

      // 确保目标滚动位置不超过最大滚动范围
      const finalTargetScrollLeft = Math.min(targetScrollLeft, maxScrollLeft);

      const startTime = performance.now();

      const animateScroll = (currentTime) => {
        const elapsedTime = currentTime - startTime;
        const progress = Math.min(elapsedTime / duration, 1);

        // 使用与详情容器展开相同的缓动函数
        const easedProgress = this.easeInOutCubic(progress);
        const currentScrollLeft = startScrollLeft + (finalTargetScrollLeft - startScrollLeft) * easedProgress;

        container.scrollLeft = currentScrollLeft;

        if (progress < 1) {
          this.scrollAnimationFrame = requestAnimationFrame(animateScroll);
        } else {
          this.isScrollingToShowDetail = false;
          this.scrollAnimationFrame = null;
        }
      };

      this.scrollAnimationFrame = requestAnimationFrame(animateScroll);
    },
    setActiveEvent(year, id) {
      this.activeYear = year;
      this.activeId = id;
      this.showYearTooltipFlag = false;

      // 开始动画到平移状态
      this.startAnimation(1);

      // 判断是否是最后三个年份（1953, 1954, 1955）
      const isLastThreeYears = year >= 1953;

      if (isLastThreeYears) {
        // 直接触发滚动，不需要检测
        this.scrollToShowDetail();
      }
    },

    /** 滚动以显示详情容器 */
    scrollToShowDetail() {
      const timelineContainer = this.$el.querySelector('.middle-timeline-container');
      if (!timelineContainer) return;

      // 固定滚动距离，确保详情容器完全显示
      const scrollAmount = 400; // 根据您的布局调整这个值

      // 使用平滑滚动动画
      this.smoothScrollToShowDetail(timelineContainer, scrollAmount);
    },

    clearActiveEvent() {
      this.activeYear = null;
      this.activeId = null;
      // 开始动画回到原始状态
      this.startAnimation(0);
    },

    /** 开始动画 */
    startAnimation(targetProgress) {
      if (this.animationTimer) {
        clearInterval(this.animationTimer);
      }

      const duration = 300; // 450毫秒，与CSS过渡时间一致
      const steps = 30; // 动画帧数
      const stepTime = duration / steps;
      const startProgress = this.animationProgress;
      const progressDiff = targetProgress - startProgress;

      let currentStep = 0;

      this.animationTimer = setInterval(() => {
        currentStep++;
        const progress = currentStep / steps;

        // 使用缓动函数使动画更自然
        const easedProgress = this.easeInOutCubic(progress);
        this.animationProgress = startProgress + progressDiff * easedProgress;

        if (currentStep >= steps) {
          this.animationProgress = targetProgress;
          clearInterval(this.animationTimer);
          this.animationTimer = null;
        }
      }, stepTime);
    },

    /** 缓动函数 - 三次缓动 */
    easeInOutCubic(t) {
      return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    },

    showYearTooltip(year, e) {
      const rect = e.target.getBoundingClientRect();
      this.activeTooltipYear = year;
      this.showYearTooltipFlag = true;
      this.tooltipPos = {
        x: rect.left + rect.width / 2 - 80,
        y: rect.top - 100,
      };
    },

    hideYearTooltip() {
      if (!this.activeId) {
        this.showYearTooltipFlag = false;
        this.activeTooltipYear = null;
      }
    },

    hasContent(year) {
      return (
        this.biographyEvents.some(e => e.year === year) ||
        this.historicalEvents.some(e => e.year === year)
      );
    },

    hasBiographyEvents(year) {
      return this.biographyEvents.some(e => e.year === year);
    },

    hasHistoricalEvents(year) {
      return this.historicalEvents.some(e => e.year === year);
    },

    getCalligraphyTotal(year) {
      return this.statsTotalData.find(i => i.year === year)?.calligraphy || 0;
    },

    getPaintingsTotal(year) {
      return this.statsTotalData.find(i => i.year === year)?.paintings || 0;
    },

    getBiographyEventsByYear(year) {
      return this.biographyEvents.filter(e => e.year === year);
    },

    getHistoricalEventsByYear(year) {
      return this.historicalEvents.filter(e => e.year === year);
    },
    handleResize() {
      // 重置滚动状态
      this.isScrollingToShowDetail = false;
    },


  },

  mounted() {
    const container = this.$el.querySelector(".middle-timeline-container");
    if (!container) return;

    // 保存容器的引用
    this.timelineContainer = container;

    this._wheelHandler = (e) => {
      const rect = container.getBoundingClientRect();
      const inside =
        e.clientY >= rect.top &&
        e.clientY <= rect.bottom;

      if (!inside) return;

      e.preventDefault();
      container.scrollLeft += e.deltaY * 5;
    };

    window.addEventListener("wheel", this._wheelHandler, { passive: false });
  },

  beforeDestroy() {
    if (this.animationTimer) {
      clearInterval(this.animationTimer);
    }

    // 清理滚动动画
    if (this.scrollAnimationFrame) {
      cancelAnimationFrame(this.scrollAnimationFrame);
    }

    window.removeEventListener("wheel", this._wheelHandler);
    window.removeEventListener('resize', this.handleResize);
  }
};
</script>

<style scoped>
  @font-face {
  font-family: "Xing Shu Fan Ti";
  src: url('@/assets/fonts/YuWeiShuFaXingShuFanTi-1.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}

@font-face {
  font-family: "Xing Shu Jian Ti";
  src: url('@/assets/fonts/YuWeiShuFaXingShuJianTi-1.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}

@font-face {
  font-family: "Kai Ti";
  src: url('@/assets/fonts/HanYiKaiTiJian-1.ttf') format('truetype');
  font-weight: normal;
  font-style: normal;
}
/* 固定图例容器 */
.chart-legend-fixed {
  position: fixed;
  top: 100px;
  right: -30px;
  display: flex;
  gap: 20px;
  background-color: rgba(255, 255, 255, 0.8);
  padding: 8px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 9999;
  pointer-events: auto;
  transform: translateX(-100%);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

.calligraphy-dot {
  background-color: #8e682f;
  border: 2px solid rgba(142, 104, 47, 0.8);
}

.paintings-dot {
  background-color: #e67e22;
  border: 2px solid rgba(230, 126, 34, 0.8);
}

.legend-text {
  font-size: 16px;
  color: #5a3f21;
  font-weight: 500;
  font-family: "Kai Ti", "SimSun", serif;
}

/* 折线图容器 */
.chart-lines-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 140px;
  pointer-events: none;
  z-index: 150;
  overflow: visible;
}

/* SVG折线 */
.paintings-line-svg,
.calligraphy-line-svg {
  position: absolute;
  top: 0;
  left: 0;
  overflow: visible;
}

/* 路径动画效果 */
.paintings-line-svg .line-path,
.calligraphy-line-svg .line-path {
  transition: d 0.45s cubic-bezier(0.22, 0.61, 0.36, 1);
  transform-origin: center;
}

/* 增加折线宽度和悬停效果 */
.paintings-line-svg:hover .line-path {
  stroke-width: 3;
}

.calligraphy-line-svg:hover .line-path {
  stroke-width: 3;
}

/* 以下样式保持原样 */


.middle-timeline-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  background-image: url('@/assets/images/timeline.png');
  overflow: hidden;
  background-size: 170% 170%;
  background-position: center center;
  padding: 0 40px;
}

.middle-timeline-container {
  margin-top: 0;
  position: relative;
  width: 100%;
  padding: 100px 0;
  overflow-x: auto;
  overflow-y: visible;
  white-space: nowrap;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center center;
}

.middle-timeline-container.is-dragging {
  cursor: grabbing;
  user-select: none;
}

.middle-timeline-container::-webkit-scrollbar {
  height: 8px;
  background: transparent;
  border-radius: 6px;
}

.middle-timeline-container::-webkit-scrollbar-track {
  border-radius: 5px;
}

.middle-timeline-container::-webkit-scrollbar-thumb {
  background: #d2bf96;
  border-radius: 5px;
  transition: background 0.3s;
}

.middle-timeline-container::-webkit-scrollbar-thumb:hover {
  background: #b9975a;
}

.middle-timeline-container::-webkit-scrollbar-corner {
  background: transparent;
}

.timeline-column-wrapper {
  display: flex;
  gap: 40px;
  align-items: flex-start;
  min-width: max-content;
  padding-bottom: 40px;
  padding-left: 20px;
  margin-top: 140px;
}

.timeline-column {
  width: 36px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: transform 0.45s ease, opacity 0.3s ease;
  min-height: 200px;
}

.timeline-column.shifted {
  transform: translateX(360px);
  opacity: 0.35;
}

.year-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 26px;
  position: relative;
  text-align: center;
}

.year-circle {
  width: 18px;
  height: 10px;
  border: 2px solid #7a5834;
  border-radius: 10px 10px 0 0;
  background: #f6f4f0;
  transition: all 0.2s;
}

.year-circle:hover {
  background: #7a5834;
}

.decade-circle {
  width: 28px;
  height: 14px;
  border-radius: 18px 18px 0 0;
}

.decade-year {
  margin-top: 6px;
  font-size: 16px;
  font-weight: bold;
  color: #5a3f21;
  letter-spacing: 1px;
}

/* 内容区域样式 - 修改为固定位置 */
.content-wrapper {
  width: 38px;
  display: flex;
  flex-direction: column;
  gap: 0;
  /* 去掉区域之间的间隔 */
  position: relative;
  margin-top: 10px;
  /* 与年份节点的距离 */
  min-height: 170px;
  /* 固定高度：上方85px + 下方85px */
}

/* 内容区域 - 固定上下两部分 */
.content-area {
  position: absolute;
  left: 0;
  right: 0;
  width: 38px;
  box-sizing: border-box;
}

/* 生平事件区域 - 固定在上方 */
.biography-area {
  top: 0;
  height: 85px;
  z-index: 102;
}

/* 历史事件区域 - 固定在下方 */
.historical-area {
  bottom: 0;
  height: 80px;
  z-index: 101;
  transform: translateY(62px);
  /* 使用 transform 下移 */
}

/* 无内容时隐藏区域 */
.content-area.no-content {
  display: none;
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  height: 100%;
}

/* 内容项样式 */
.content-item {
  width: 38px;
  background: #f6f4f0;
  padding: 10px 10px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, .08);
  cursor: pointer;
  position: relative;
  transition: transform .25s ease, box-shadow .25s ease;
  box-sizing: border-box;
}

.content-item:hover {
  transform: translateX(6px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, .12);
  z-index: 103;
}

/* 历史事件项 - 始终在下方区域 */
.historical-item {
  margin-top: 0;
}

.content-title {
  writing-mode: vertical-rl;
  text-orientation: upright;
  font-size: 15px;
  font-weight: bold;
  font-family: "Kai Ti", "SimSun", serif;
  display: flex;
  /* 使用flex布局 */
  align-items: center;
  /* 水平居中（实际是垂直方向，因为writing-mode改变了方向） */
  justify-content: center;
  /* 垂直居中（实际是水平方向） */

  width: 100%;
  /* 占满父容器宽度 */
  min-height: 80px;
  /* 设置最小高度，避免太短的标题 */
  max-height: 140px;
  /* 设置最大高度，避免太长的标题 */
  overflow: hidden;
  /* 如果标题太长，隐藏超出部分 */
  box-sizing: border-box;
  /* 确保padding不会影响尺寸 */
}

/* 生平事件标题样式 */
.biography-title {
  color: #8e682f;
}

/* 历史事件标题样式 */
.historical-title {
  color: #654444;
}

.detail-container {
  position: absolute;
  top: 0;
  left: 100%;
  height: 100%;
  width: 360px;
  background: #f6f4f0;
  border-radius: 12px;
  box-shadow: 3px 3px 16px rgba(0, 0, 0, .1);
  border-left: 3px solid rgba(150, 110, 60, 0.6);
  transform: scaleX(0);
  transform-origin: left center;
  opacity: 0;
  z-index: 108;
  transition: transform .35s cubic-bezier(.22, .61, .36, 1),
    opacity .3s ease-in-out;
  overflow: hidden;
  /* 隐藏溢出部分 */
}

/* 为历史事件的详情容器添加不同的边框颜色 */
.historical-item .detail-container {
  border-left: 3px solid rgba(87, 55, 55, 0.6);
}

.detail-container.expanded {
  transform: scaleX(1);
  opacity: 1;
}

/* 外部边框 - 固定在容器上 */
.detail-outline {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 3px solid rgba(150, 110, 60, 0.6);
  border-radius: 12px;
  pointer-events: none;
  z-index: 1;
}

/* 为历史事件的详情容器外部边框添加不同颜色 */
.historical-item .detail-container .detail-outline {
  border: 3px solid rgba(87, 55, 55, 0.6);

}

.detail-scroll-container {
  position: relative;
  height: 100%;
  width: 100%;
  padding: 8px;
  /* 增加内边距，让内容远离边框 */
  scrollbar-width: thin;
  scrollbar-color: rgba(122, 88, 52, 0.3) rgba(255, 255, 255, 0.1);
}

/* 内边框 - 随内容一起滚动 */
.detail-inner-border {
  position: absolute;
  top: 6px;
  left: 6px;
  right: 6px;
  bottom: 6px;
  border: 2px solid rgba(200, 180, 150, 0.6);
  border-radius: 10px;
  pointer-events: none;
  z-index: 2;
}

/* 为历史事件的详情容器内边框添加不同颜色 */
.historical-item .detail-container .detail-inner-border {
  border: 2px solid rgba(87, 55, 55, 0.6);

}

.event-detail {
  position: relative;
  top: 0;
  /* 移除-5px偏移，让内容正常定位 */
  padding: 8px 24px;
  /* 减小上下内边距为8px，左右保持24px */
  margin-left: 0;
  /* 移除左边距 */
  margin-bottom: 0;
  /* 移除底部边距 */
  line-height: 1.6;
  font-size: 15px;
  color: #444;
  height: 100%;
  max-height: 300px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(122, 88, 52, 0.3) rgba(255, 255, 255, 0.1);
  font-family: "Kai Ti", "SimSun", serif;
  text-align: justify;
  word-wrap: break-word;
  word-break: break-word;
  white-space: normal;
  min-height: 100%;
  /* 移除计算，直接设为100% */
  box-sizing: border-box;
  /* 添加此属性，确保内边距包含在元素尺寸内 */
}

.event-detail::-webkit-scrollbar {
  display: none;
}

.global-tooltip {
  position: fixed;
  width: 160px;
  background: rgba(246, 244, 240, 0.98);
  border: 1px solid #e0d4c2;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 13px;
  color: #4a3a25;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 999999;
  pointer-events: none;
  animation: fadeIn 0.2s ease-out;
}

.tooltip-line {
  display: block;
  margin-bottom: 4px;

}

.year-tooltip {
  position: absolute;
  top: -130px;
  left: 50%;
  transform: translateX(-50%);
  width: 220px;
  background: rgba(255, 253, 245, 0.99);
  backdrop-filter: blur(6px);
  padding: 12px 16px;
  border: 2px solid #d2bf96;
  border-radius: 10px;
  font-size: 14px;
  color: #5a3f21;
  line-height: 1.8;
  box-shadow: 0 6px 16px rgba(0, 0, 0, .18);
  animation: fadeIn .25s ease-out;
  pointer-events: none;
  z-index: 300;
  text-align: center;
}

.tooltip-year {
  position: relative;
  font-weight: bold;
  margin-bottom: 8px;
  padding: 6px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-family: "Kai Ti", "SimSun", serif;
}

.tooltip-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 18px;
  margin: 8px 0;
  padding: 6px 0;
  border-top: 1px dashed #e0d4c2;
  border-bottom: 1px dashed #e0d4c2;
  font-family: "Kai Ti", "SimSun", serif;
}

.tooltip-line {
  font-size: 14px;
  color: #6b4f2e;
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: "Kai Ti", "SimSun", serif;
}

.event-type-labels {
  position: absolute;
  left: -10px;
  /* 调整这个值来改变标签与时间线的距离 */
  top: 33vh;     
  bottom: 0;
  width: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 100;
  padding: 140px 0 40px;
  /* 与时间线内容对齐 */
  pointer-events: none;
  /* 防止标签干扰交互 */
}

.event-type-label {
  writing-mode: vertical-rl;
  text-orientation: upright;
  font-size: 18px;
  font-weight: bold;
  font-family: "Kai Ti", "SimSun", serif;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  cursor: default;
  height: 67px;
  /* 与生平事件区域高度匹配 */
  letter-spacing: 0.5em;
  /* 增加字符间距 */
}

/* 生平标签样式 */
.biography-label {
  color: #8e682f;
  margin-bottom: 20px;
  /* 与内容区域之间的间距匹配 */
}

/* 历史标签样式 */
.historical-label {
  color: #654444;
  transform: translateY(62px);
  /* 与历史事件区域的偏移匹配 */
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
