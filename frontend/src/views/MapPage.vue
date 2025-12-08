<template>
  <div class="art-footprints-page">
    <!-- 统一导航栏 -->
    <header class="header">
      <a href="index.html" class="logo">
        <i class="fas fa-mountain logo-icon"></i>
        <div class="logo-text">数字黄宾虹</div>
      </a>
      
      <div class="mobile-menu-btn" @click="toggleMobileMenu">
        <i class="fas fa-bars"></i>
      </div>
      
      <nav class="nav-menu" :class="{ active: isMobileMenuOpen }">
        <a href="index.html#观看" class="nav-item">
          <i class="fas fa-eye"></i> <span>观看</span>
        </a>
        <a href="index.html#探索" class="nav-item active">
          <i class="fas fa-compass"></i> <span>探索</span>
        </a>
        <a href="index.html" class="nav-item">
          <i class="fas fa-home"></i> <span>返回首页</span>
        </a>
      </nav>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 地图容器 -->
      <div id="map-container" ref="mapContainer"></div>
      
      <!-- 详细信息面板 -->
      <div class="detail-panel" :class="{ active: selectedPlace !== null }">
        <div class="detail-header">
          <h3 class="detail-title">{{ selectedPlace ? selectedPlace.name : '黄宾虹艺术足迹' }}</h3>
          <button class="close-panel" @click="closeDetailPanel">&times;</button>
        </div>
        
        <div class="detail-content">
          <template v-if="selectedPlace">
            <div class="place-type" :class="selectedPlace.typeClass">{{ selectedPlace.typeLabel }}</div>
            <div class="detail-year">{{ selectedPlace.year }}</div>
            
            <div class="detail-section">
              <h4 class="section-title">
                <i class="fas fa-info-circle"></i> 地点简介
              </h4>
              <p style="color: #5c4033; line-height: 1.6;">{{ selectedPlace.description }}</p>
            </div>
            
            <div class="detail-section">
              <h4 class="section-title">
                <i class="fas fa-history"></i> 时间线事件
              </h4>
              <div class="timeline-events">
                <div 
                  v-for="(event, index) in selectedPlace.timeEvents" 
                  :key="index" 
                  class="timeline-event"
                >
                  <div class="event-year">{{ event.year }}</div>
                  <div class="event-desc">{{ event.event }}</div>
                </div>
              </div>
            </div>
            
            <div class="detail-navigation">
              <button 
                class="nav-button" 
                @click="navigateToPreviousPlace"
                :disabled="selectedPlaceIndex === 0"
              >
                <i class="fas fa-chevron-left"></i> <span>上一个地点</span>
              </button>
              <span style="color: #8b4513; font-weight: bold; display: flex; align-items: center;">
                {{ selectedPlaceIndex + 1 }} / {{ footprintData.length }}
              </span>
              <button 
                class="nav-button" 
                @click="navigateToNextPlace"
                :disabled="selectedPlaceIndex === footprintData.length - 1"
              >
                <span>下一个地点</span> <i class="fas fa-chevron-right"></i>
              </button>
            </div>
          </template>
          
          <template v-else>
            <div style="text-align: center; padding: 40px 20px;">
              <div style="font-size: 48px; color: #d2b48c; margin-bottom: 20px;">📍</div>
              <p style="color: #5c4033; font-size: 16px;">点击地图上的地点标记，查看黄宾虹在该地的艺术活动详情</p>
              <p style="color: #8b4513; font-size: 14px; margin-top: 10px;">
                使用<span style="font-weight: bold; color: #5c4033;">← →</span>方向键或导航按钮切换地点
              </p>
            </div>
          </template>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
export default {
  name: 'ArtFootprintsPage',
  
  data() {
    return {
      // 足迹数据
      footprintData: [
        {
          id: 1,
          name: "浙江金华",
          type: "birthplace",
          year: "1865-1890",
          coordinates: [119.6454, 29.1164],
          description: "黄宾虹出生于浙江金华，幼年至26岁主要在此生活、学画、应考，奠定学业与绘画基础。",
          importance: 10,
          typeLabel: "出生地",
          typeClass: "birthplace",
          timeEvents: [
            {
              year: "1865年",
              event: "出生于金华城西铁岭头，原名质，字朴存"
            },
            {
              year: "1875年（10岁）",
              event: "开始学画，临摹家藏沈廷瑞山水册，展示绘画天赋"
            },
            {
              year: "1886年（21岁）",
              event: "应童子试，补廪贡生，奠定传统文化基础"
            },
            {
              year: "1887-1890年",
              event: "在金华继续学画和读书，为日后艺术生涯奠定基础"
            }
          ]
        },
        {
          id: 2,
          name: "安徽歙县",
          type: "residence",
          year: "1890-1907",
          coordinates: [118.4472, 29.8147],
          description: "祖籍地，26岁后多次居住，师从汪宗沂，组织'黄社'，参与革命活动，搜集古籍书画。",
          importance: 8,
          typeLabel: "居住地",
          typeClass: "residence",
          timeEvents: [
            {
              year: "1890年",
              event: "回歙县定居，师从经学大师汪宗沂，学习经史和书画"
            },
            {
              year: "1895年",
              event: "与谭嗣同会面，受维新思想影响，参与变法活动"
            },
            {
              year: "1906年",
              event: "与许承尧等组织'黄社'，以研究诗文为名宣传革命"
            },
            {
              year: "1907年",
              event: "因革命活动遭通缉，被迫离开歙县前往上海"
            }
          ]
        },
        {
          id: 3,
          name: "上海",
          type: "residence",
          year: "1907-1937",
          coordinates: [121.4737, 31.2304],
          description: "1907年避祸赴沪，至1937年为事业核心地，编辑《美术丛书》，任教暨南大学，组织烂漫社。",
          importance: 9,
          typeLabel: "居住地",
          typeClass: "residence",
          timeEvents: [
            {
              year: "1907年",
              event: "赴上海避难，任《国粹学报》编辑，开始专业艺术生涯"
            },
            {
              year: "1912年",
              event: "与邓实合编《美术丛书》，系统整理中国美术文献"
            },
            {
              year: "1928年",
              event: "任暨南大学中国画研究会导师，开始美术教育事业"
            },
            {
              year: "1935年",
              event: "组织烂漫社，举办画展，成为海上画派重要人物"
            },
            {
              year: "1937年",
              event: "因抗战爆发离开上海，迁居北平"
            }
          ]
        },
        {
          id: 4,
          name: "北平（北京）",
          type: "residence",
          year: "1937-1948",
          coordinates: [116.4074, 39.9042],
          description: "1937-1948年寓居，鉴定故宫古画，任教北平艺专，闭门治学作画，拒绝日伪拉拢。",
          importance: 8,
          typeLabel: "居住地",
          typeClass: "residence",
          timeEvents: [
            {
              year: "1937年",
              event: "迁居北平，任北平艺术专科学校教授"
            },
            {
              year: "1940-1943年",
              event: "闭门研究古画，鉴定故宫书画，画风转向浑厚华滋"
            },
            {
              year: "1943年",
              event: "拒绝日伪北平文物研究会邀请，保持民族气节"
            },
            {
              year: "1946年",
              event: "举办八十寿辰画展，展示晚年艺术成就"
            },
            {
              year: "1948年",
              event: "接受杭州艺专聘请，离开北平"
            }
          ]
        },
        {
          id: 5,
          name: "杭州",
          type: "residence",
          year: "1948-1955",
          coordinates: [120.1551, 30.2741],
          description: "1948年定居至逝世，任教杭州艺专，当选全国政协委员，创作达到顶峰，捐赠全部作品。",
          importance: 9,
          typeLabel: "居住地",
          typeClass: "residence",
          timeEvents: [
            {
              year: "1948年",
              event: "受聘杭州艺专（今中国美术学院）教授"
            },
            {
              year: "1953年",
              event: "当选全国政协委员，参与国家文化建设"
            },
            {
              year: "1954年",
              event: "创作达到顶峰，完成大量山水画精品"
            },
            {
              year: "1955年3月25日",
              event: "在杭州逝世，享年90岁，遗嘱捐献全部作品"
            }
          ]
        },
        {
          id: 6,
          name: "黄山",
          type: "travel",
          year: "1907年、1935年",
          coordinates: [118.1684, 30.132],
          description: "多次游历黄山，实地写生，为其山水画创作积累了丰富素材，对艺术风格产生重要影响。",
          importance: 7,
          typeLabel: "游历地",
          typeClass: "travel",
          timeEvents: [
            {
              year: "1907年秋",
              event: "首次游黄山，写生数十幅，捕捉黄山云雾变幻"
            },
            {
              year: "1935年",
              event: "再次游黄山，创作大量速写和写生作品"
            },
            {
              year: "晚年多次创作",
              event: "根据黄山写生稿创作系列黄山题材作品"
            }
          ]
        }
      ],
      
      // Vue状态
      isMobileMenuOpen: false,
      selectedPlace: null,
      selectedPlaceIndex: -1,
      
      // 高德地图相关
      map: null,
      markers: [],
      currentInfoWindow: null
    };
  },
  
  computed: {
    // 计算当前选中的地点索引
    currentPlaceIndex() {
      if (!this.selectedPlace) return -1;
      return this.footprintData.findIndex(place => place.id === this.selectedPlace.id);
    }
  },
  
  mounted() {
    // 初始化地图
    this.loadMapAPI().then(() => {
      this.initMap();
    }).catch(error => {
      console.error('地图API加载失败:', error);
      this.showMapError();
    });
    
    // 绑定键盘事件
    this.bindKeyboardEvents();
    
    // 绑定滚动事件
    this.bindWheelEvents();
  },
  
  beforeDestroy() {
    // 清理事件监听
    this.unbindKeyboardEvents();
    this.unbindWheelEvents();
    
    // 清理地图
    if (this.map) {
      this.map.destroy();
    }
  },
  
  methods: {
    // 加载高德地图API
    loadMapAPI() {
      return new Promise((resolve, reject) => {
        // 检查是否已加载
        if (window.AMap) {
          resolve();
          return;
        }
        
        // 动态加载脚本
        const script = document.createElement('script');
        script.src = 'https://webapi.amap.com/maps?v=1.4.15&key=ae5e7e87a370b7b8f46e689e1efb8594';
        script.onload = () => resolve();
        script.onerror = () => reject(new Error('地图API加载失败'));
        document.head.appendChild(script);
      });
    },
    
    // 初始化地图
    initMap() {
      if (typeof AMap === 'undefined') {
        this.showMapError();
        return;
      }
      
      // 创建地图实例
      this.map = new AMap.Map(this.$refs.mapContainer, {
        viewMode: '2D',
        zoom: 5,
        center: [115.0, 35.0],
        mapStyle: 'amap://styles/whitesmoke',
        features: ['bg', 'point'],
        showLabel: false
      });
      
      // 添加控件
      this.addMapControls();
      
      // 渲染标记
      this.renderMapMarkers();
      
      // 调整地图视图
      setTimeout(() => {
        if (this.map) {
          this.map.setFitView();
        }
      }, 500);
    },
    
    // 添加地图控件
    addMapControls() {
      if (!this.map || !AMap) return;
      
      try {
        if (AMap.plugin) {
          AMap.plugin(['AMap.Scale', 'AMap.ToolBar'], () => {
            if (AMap.Scale) {
              this.map.addControl(new AMap.Scale());
            }
            if (AMap.ToolBar) {
              this.map.addControl(new AMap.ToolBar());
            }
          });
        } else if (typeof AMap.Scale === 'function') {
          this.map.addControl(new AMap.Scale());
          this.map.addControl(new AMap.ToolBar());
        }
      } catch (error) {
        console.warn('地图控件加载失败:', error);
      }
    },
    
    // 渲染地图标记
    renderMapMarkers() {
      // 清除现有标记
      this.clearMarkers();
      
      // 为每个地点创建标记
      this.footprintData.forEach((place, index) => {
        this.createMarker(place, index);
      });
      
      // 调整地图视图以显示所有标记
      if (this.markers.length > 0) {
        setTimeout(() => {
          if (this.map) {
            this.map.setFitView();
          }
        }, 300);
      }
    },
    
    // 创建标记
    createMarker(place, index) {
      const icon = this.createCustomIcon(place.type, place.importance, false);
      
      const marker = new AMap.Marker({
        position: place.coordinates,
        icon: icon,
        title: place.name,
        extData: { ...place, index }
      });
      
      // 添加事件监听
      marker.on('click', () => {
        this.showPlaceDetail(place, index);
        this.highlightMarker(marker);
      });
      
      marker.on('mouseover', () => {
        marker.setIcon(this.createCustomIcon(place.type, place.importance, true));
      });
      
      marker.on('mouseout', () => {
        marker.setIcon(this.createCustomIcon(place.type, place.importance, false));
      });
      
      this.map.add(marker);
      this.markers.push(marker);
    },
    
    // 创建自定义图标
    createCustomIcon(type, importance, isHover = false) {
      const colorMap = {
        birthplace: isHover ? '#ff7675' : '#d63031',
        residence: isHover ? '#74b9ff' : '#0984e3',
        travel: isHover ? '#55efc4' : '#00b894'
      };
      
      const color = colorMap[type] || (isHover ? '#a29bfe' : '#6c5ce7');
      const size = Math.max(isHover ? 30 : 25, importance * (isHover ? 3.5 : 3));
      
      const svg = `
        <svg width="${size}" height="${size}" xmlns="http://www.w3.org/2000/svg">
          <circle cx="${size/2}" cy="${size/2}" r="${size/2 - 2}" fill="${color}" 
                  opacity="${isHover ? 0.95 : 0.9}" 
                  stroke="#fff" stroke-width="${isHover ? 3 : 2}"/>
          <circle cx="${size/2}" cy="${size/2}" r="${size/4}" fill="#fff" 
                  opacity="${isHover ? 0.9 : 0.7}"/>
          ${isHover ? `<circle cx="${size/2}" cy="${size/2}" r="${size/6}" fill="${color}" opacity="0.7"/>` : ''}
          <text x="${size/2}" y="${size/2 + (isHover ? 3 : 2)}" text-anchor="middle" 
                font-size="${isHover ? 12 : 10}" fill="#5c4033" font-weight="bold">${importance}</text>
        </svg>
      `;
      
      return new AMap.Icon({
        size: new AMap.Size(size, size),
        image: 'data:image/svg+xml;base64,' + btoa(svg),
        imageSize: new AMap.Size(size, size)
      });
    },
    
    // 高亮标记
    highlightMarker(currentMarker) {
      // 重置所有标记
      this.markers.forEach(marker => {
        const place = marker.getExtData();
        marker.setIcon(this.createCustomIcon(place.type, place.importance, false));
      });
      
      // 高亮当前标记
      const place = currentMarker.getExtData();
      currentMarker.setIcon(this.createCustomIcon(place.type, place.importance, true));
    },
    
    // 清除标记
    clearMarkers() {
      this.markers.forEach(marker => {
        if (this.map) {
          this.map.remove(marker);
        }
      });
      this.markers = [];
    },
    
    // 显示地点详情
    showPlaceDetail(place, index) {
      this.selectedPlace = place;
      this.selectedPlaceIndex = index;
      
      // 移动地图视角到该地点
      if (this.map) {
        this.map.setCenter(place.coordinates);
        this.map.setZoom(8);
      }
      
      // 高亮对应的标记
      const marker = this.markers[index];
      if (marker) {
        this.highlightMarker(marker);
      }
      
      // 滚动到详情面板顶部
      setTimeout(() => {
        const panel = document.querySelector('.detail-panel');
        if (panel) {
          panel.scrollTop = 0;
        }
      }, 100);
    },
    
    // 关闭详情面板
    closeDetailPanel() {
      this.selectedPlace = null;
      this.selectedPlaceIndex = -1;
      
      // 重置所有标记
      this.markers.forEach(marker => {
        const place = marker.getExtData();
        marker.setIcon(this.createCustomIcon(place.type, place.importance, false));
      });
    },
    
    // 导航到上一个地点
    navigateToPreviousPlace() {
      if (this.selectedPlaceIndex > 0) {
        const prevIndex = this.selectedPlaceIndex - 1;
        const place = this.footprintData[prevIndex];
        this.showPlaceDetail(place, prevIndex);
      }
    },
    
    // 导航到下一个地点
    navigateToNextPlace() {
      if (this.selectedPlaceIndex < this.footprintData.length - 1) {
        const nextIndex = this.selectedPlaceIndex + 1;
        const place = this.footprintData[nextIndex];
        this.showPlaceDetail(place, nextIndex);
      }
    },
    
    // 切换移动端菜单
    toggleMobileMenu() {
      this.isMobileMenuOpen = !this.isMobileMenuOpen;
    },
    
    // 显示地图错误
    showMapError() {
      this.$refs.mapContainer.innerHTML = 
        '<div style="text-align: center; padding: 50px; color: #8b4513; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);">' +
        '<h3>地图加载失败</h3>' +
        '<p>请检查网络连接，或刷新页面重试</p>' +
        '</div>';
    },
    
    // 绑定键盘事件
    bindKeyboardEvents() {
      this.keyboardHandler = (e) => {
        if (e.key === 'Escape') {
          window.location.href = 'index.html';
        }
        
        if (e.key === 'ArrowLeft') {
          this.navigateToPreviousPlace();
        }
        
        if (e.key === 'ArrowRight') {
          this.navigateToNextPlace();
        }
        
        // 防止页面滚动
        if ([' ', 'PageUp', 'PageDown', 'ArrowUp', 'ArrowDown'].includes(e.key)) {
          e.preventDefault();
        }
      };
      
      window.addEventListener('keydown', this.keyboardHandler);
    },
    
    // 解绑键盘事件
    unbindKeyboardEvents() {
      if (this.keyboardHandler) {
        window.removeEventListener('keydown', this.keyboardHandler);
      }
    },
    
    // 绑定滚轮事件
    bindWheelEvents() {
      this.wheelHandler = (e) => {
        // 允许详情面板内部滚动
        if (e.target.closest('.detail-panel')) {
          return;
        }
        
        // 在地图容器上允许滚轮缩放
        if (e.target.closest('#map-container') && this.map) {
          return;
        }
        
        // 阻止其他地方的滚轮事件
        e.preventDefault();
      };
      
      document.addEventListener('wheel', this.wheelHandler, { passive: false });
    },
    
    // 解绑滚轮事件
    unbindWheelEvents() {
      if (this.wheelHandler) {
        document.removeEventListener('wheel', this.wheelHandler);
      }
    }
  },
  
  watch: {
    // 监听窗口大小变化
    '$store.state.windowWidth'() {
      if (this.map) {
        setTimeout(() => {
          this.map.setFitView();
        }, 100);
      }
    }
  }
};
</script>

<style scoped>
.art-footprints-page {
  font-family: "SimSun", "STSong", serif;
  color: #333;
  background: linear-gradient(135deg, #f5f0e6 0%, #e8dfca 100%);
  min-height: 100vh;
  overflow: hidden; /* 防止页面滚动 */
}

/* 统一导航栏 */
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
  height: 80px; /* 固定高度 */
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
  position: absolute;
  top: 80px; /* 等于导航栏高度 */
  left: 0;
  right: 0;
  bottom: 0; /* 填满剩余空间 */
  width: 100%;
}

/* 地图容器 - 填满整个区域 */
#map-container {
  width: 100%;
  height: 100%;
  position: relative;
}

/* 详细信息面板 - 固定在右侧，调整top值避免被导航栏遮挡 */
.detail-panel {
  position: absolute;
  top: 20px; /* 相对地图容器的位置 */
  right: 20px;
  background-color: rgba(255, 253, 248, 0.95);
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  z-index: 100;
  width: 380px;
  max-height: calc(100% - 40px); /* 限制高度，留出上下边距 */
  overflow-y: auto;
  display: none;
  border: 2px solid #d2b48c;
}

.detail-panel.active {
  display: block;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #d2b48c;
}

.detail-title {
  color: #5c4033;
  font-size: 24px;
  margin: 0;
  font-weight: bold;
}

.detail-year {
  color: #8b4513;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
}

.detail-content {
  color: #333;
  line-height: 1.6;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0d9c8;
}

.section-title {
  color: #8b4513;
  font-size: 16px;
  margin-bottom: 10px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.timeline-events {
  margin-top: 15px;
}

.timeline-event {
  margin-bottom: 15px;
  padding-left: 20px;
  position: relative;
}

.timeline-event:before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 10px;
  height: 10px;
  background-color: #d2b48c;
  border-radius: 50%;
  border: 2px solid #8b4513;
}

.event-year {
  color: #8b4513;
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 5px;
}

.event-desc {
  color: #5c4033;
  font-size: 14px;
  line-height: 1.5;
}

/* 导航按钮 */
.detail-navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 25px;
  padding-top: 15px;
  border-top: 2px solid #d2b48c;
}

.nav-button {
  background-color: #d2b48c;
  color: #5c4033;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-family: inherit;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 120px;
}

.nav-button:hover:not(:disabled) {
  background-color: #8b4513;
  color: #fff;
  transform: translateY(-2px);
}

.nav-button:disabled {
  background-color: #e0d9c8;
  color: #aaa;
  cursor: not-allowed;
}

/* 地点类型标签 */
.place-type {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #fff;
}

.birthplace {
  background-color: #d63031;
}

.residence {
  background-color: #0984e3;
}

.travel {
  background-color: #00b894;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 15px 20px;
    height: 70px; /* 移动端导航栏高度 */
  }
  
  .main-content {
    top: 70px; /* 调整主内容区位置 */
  }
  
  .logo-text {
    font-size: 20px;
  }
  
  .nav-menu {
    display: none;
    position: absolute;
    top: 70px;
    left: 0;
    width: 100%;
    background-color: rgba(139, 69, 19, 0.98);
    flex-direction: column;
    padding: 20px;
    gap: 15px;
  }
  
  .nav-menu.active {
    display: flex;
  }
  
  .mobile-menu-btn {
    display: block;
  }
  
  .detail-panel {
    top: 10px;
    right: 10px;
    left: 10px;
    width: calc(100% - 20px);
    max-height: calc(100% - 20px);
  }
  
  .nav-button {
    min-width: 100px;
    padding: 8px 12px;
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 12px 15px;
    height: 65px;
  }
  
  .main-content {
    top: 65px;
  }
  
  .logo-text {
    font-size: 18px;
    letter-spacing: 1px;
  }
  
  .detail-panel {
    width: calc(100% - 20px);
    padding: 15px;
  }
  
  .detail-title {
    font-size: 20px;
  }
  
  .nav-button {
    min-width: 80px;
    padding: 6px 10px;
    font-size: 11px;
  }
  
  .nav-button span {
    display: none;
  }
  
  .nav-button i {
    margin: 0;
  }
}

/* 隐藏高德地图logo */
:deep(.amap-logo),
:deep(.amap-copyright) {
  display: none !important;
}

/* 滚动条样式 */
.detail-panel::-webkit-scrollbar {
  width: 8px;
}

.detail-panel::-webkit-scrollbar-track {
  background: #f0e6d6;
  border-radius: 4px;
}

.detail-panel::-webkit-scrollbar-thumb {
  background: #d2b48c;
  border-radius: 4px;
}

.detail-panel::-webkit-scrollbar-thumb:hover {
  background: #8b4513;
}
</style>