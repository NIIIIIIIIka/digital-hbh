<template>
  <div class="art-footprints-page">
    <!-- 统一导航栏 -->
    

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 地图容器 -->
      <div id="map-container" ref="mapContainer"></div>
      
      <!-- 加载状态 -->
      <div v-if="isLoading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <p>正在加载地图数据...</p>
      </div>
      
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
                :disabled="selectedPlaceIndex === 0 || isAnimating"
              >
                <i class="fas fa-chevron-left"></i> <span>上一个地点</span>
              </button>
              <span style="color: #8b4513; font-weight: bold; display: flex; align-items: center;">
                {{ selectedPlaceIndex + 1 }} / {{ footprintData.length }}
              </span>
              <button 
                class="nav-button" 
                @click="navigateToNextPlace"
                :disabled="selectedPlaceIndex === footprintData.length - 1 || isAnimating"
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
import axios from 'axios';

export default {
  name: 'ArtFootprintsPage',
  
  data() {
    return {
      // 足迹数据
      footprintData: [],
      
      // Vue状态
      isMobileMenuOpen: false,
      selectedPlace: null,
      selectedPlaceIndex: -1,
      isAnimating: false, // 动画状态控制
      isLoading: true,
      
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
    // 1. 先加载地图脚本
    this.loadMapAPI()
      .then(() => {
        // 2. 初始化地图
        this.initMap();
        
        // 3. 加载足迹数据
        return this.loadFootprintData();
      })
      .then(() => {
        // 4. 渲染地图标记
        if (this.map && this.footprintData.length > 0) {
          this.renderMapMarkers();
          this.isLoading = false;
        }
      })
      .catch(err => {
        console.error('初始化失败:', err);
        this.isLoading = false;
        this.showMapError();
      });

    this.bindKeyboardEvents();
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
    getTypeLabel(typeId) {
      const map = {
        1: '出生地',
        2: '居住地',
        3: '游历地'
      };
      return map[typeId] || '其他';
    },
    
    // 加载高德地图API
    loadMapAPI() {
      return new Promise((resolve, reject) => {
        if (window.AMap) {
          resolve();
          return;
        }

        const script = document.createElement('script');
        script.src = 'https://webapi.amap.com/maps?v=1.4.15&key=ae5e7e87a370b7b8f46e689e1efb8594';
        script.onload = () => {
          // 确保 AMap 完全加载
          if (window.AMap) {
            resolve();
          } else {
            reject(new Error('AMap 加载失败'));
          }
        };
        script.onerror = () => reject(new Error('地图API加载失败'));
        document.head.appendChild(script);
      });
    },
    
    async loadFootprintData() {
      try {
        const response = await axios.get('/api/huangbinhong/footprints');
        
        // 调试：查看数据结构
        console.log('📊 足迹API响应:', response.data);
        console.log('📊 数据条数:', response.data.data.length);
        
        if (response.data.data && response.data.data.length > 0) {
          console.log('📊 第一条数据字段:', Object.keys(response.data.data[0]));
          console.log('📊 第一条数据内容:', response.data.data[0]);
        }
        
        // 1. 先处理原始数据，提取年份用于排序
        let processedData = response.data.data.map((item, index) => {
          // 调试每个item
          console.log(`📍 原始数据 ${index + 1}: ${item.name}, 年份: ${item.year}, type: ${item.type}`);
          
          // 提取年份用于排序（取第一个年份）
          const sortYear = this.extractYearForSorting(item.year);
          
          return {
            id: item.id,
            name: item.name,
            type: item.type || 'travel',
            typeLabel: item.typeLabel || '其他',
            typeClass: item.typeClass || 'travel',
            year: item.year || '',
            sortYear: sortYear, // 用于排序的年份
            originalIndex: index, // 保留原始顺序用于调试
            coordinates: item.coordinates || [115, 35],
            description: item.description || '暂无描述',
            importance: item.importance || 5,
            timeEvents: item.timeEvents || []
          };
        });
        
        // 2. 按年份从小到大排序
        processedData.sort((a, b) => {
          // 如果年份相同，按重要性排序
          if (a.sortYear === b.sortYear) {
            return b.importance - a.importance; // 重要性高的在前
          }
          return a.sortYear - b.sortYear; // 年份小的在前
        });
        
        // 3. 添加按时间排序后的序号
        this.footprintData = processedData.map((item, index) => {
          return {
            ...item,
            orderNumber: index + 1, // 时间顺序编号
            timeOrder: index + 1    // 也可以添加这个字段明确表示是时间顺序
          };
        });
        
        console.log('✅ 按时间排序完成，共', this.footprintData.length, '个地点');
console.log('✅ 时间顺序列表:');
this.footprintData.forEach((place) => {
  console.log(`   ${place.orderNumber}. ${place.name} (${place.year}) - ${place.typeLabel}`);
});
        
      } catch (error) {
        console.error('❌ 加载足迹数据失败:', error);
        
        // 失败时使用示例数据
        this.useSampleData();
      }
    },

    // 辅助方法：从年份字符串中提取排序用的数字年份
    extractYearForSorting(yearStr) {
      if (!yearStr || yearStr.trim() === '') {
        return 9999; // 没有年份的排最后
      }
      
      console.log(`📅 解析年份: "${yearStr}"`);
      
      // 处理各种格式：
      // 1. "1865" - 单个年份
      // 2. "1907-1937" - 年份范围
      // 3. "约1890年" - 带文字
      
      // 先清理非数字字符 - 修正正则表达式，移除不必要的转义
      const cleaned = yearStr.replace(/[^0-9-]/g, '');
      
      if (!cleaned) {
        return 9999;
      }
      
      // 如果有"-"，取第一个年份
      if (cleaned.includes('-')) {
        const parts = cleaned.split('-');
        const firstYear = parseInt(parts[0]);
        return isNaN(firstYear) ? 9999 : firstYear;
      }
      
      // 直接解析数字
      const yearNum = parseInt(cleaned);
      return isNaN(yearNum) ? 9999 : yearNum;
    },

    // 示例数据备用
    useSampleData() {
      console.log('⚠️ 使用示例数据');
      this.footprintData = [
        {
          id: '浙江金华',
          name: '浙江金华',
          type: 'birthplace',
          typeLabel: '出生地',
          typeClass: 'birthplace',
          coordinates: [119.6495, 29.0895],
          year: '1865',
          sortYear: 1865,
          description: '黄宾虹出生地，艺术启蒙时期',
          importance: 10,
          orderNumber: 1,
          timeEvents: []
        },
        {
          id: '上海',
          name: '上海',
          type: 'residence',
          typeLabel: '居住地',
          typeClass: 'residence',
          coordinates: [121.4737, 31.2304],
          year: '1907-1937',
          sortYear: 1907,
          description: '长期居住和创作的重要城市',
          importance: 8,
          orderNumber: 2,
          timeEvents: []
        }
      ];
      
      console.log('✅ 示例数据按时间排序:', this.footprintData.map(p => `${p.orderNumber}.${p.name}(${p.year})`));
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
        showLabel: false,
        animateEnable: true, // 启用动画
        zoomEnable: true,
        doubleClickZoom: true
      });
      
      // 添加控件
      this.addMapControls();
      
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
  
  console.log('🎯 开始渲染地图标记，共', this.footprintData.length, '个地点');
  
  // 为每个地点创建标记（按orderNumber顺序）
  const sortedData = this.footprintData.sort((a, b) => a.orderNumber - b.orderNumber);
  
  // 使用传统的for循环避免ESLint报错
  for (let i = 0; i < sortedData.length; i++) {
    const place = sortedData[i];
    console.log(`🎯 创建标记 ${place.orderNumber}: ${place.name}, type: ${place.type}, color:`, 
      this.getColorByType(place.type));
    this.createMarker(place, i);
  }
  
  // 调整地图视图以显示所有标记
  if (this.markers.length > 0) {
    setTimeout(() => {
      if (this.map) {
        this.map.setFitView();
      }
    }, 300);
  }
},
    // 添加辅助方法查看颜色
    getColorByType(type) {
      const colorMap = {
        birthplace: '#d63031', // 红色
        residence: '#0984e3',  // 蓝色
        travel: '#00b894'      // 绿色
      };
      return colorMap[type] || '#6c5ce7';
    },
    
    createMarker(place, index) {
      console.log(`📍 创建标记 ${place.orderNumber}: ${place.name} (${place.year}) - ${place.typeLabel}`);
      
      const icon = this.createCustomIcon(place.type, place.orderNumber, place.importance, false);
      
      const marker = new AMap.Marker({
        position: place.coordinates,
        icon: icon,
        title: `${place.orderNumber}. ${place.name} (${place.year})`,
        extData: { ...place, index }
      });
      
      // 添加事件监听
      marker.on('click', () => {
        console.log(`点击标记: ${place.orderNumber}. ${place.name}`);
        this.showPlaceDetail(place, index);
        this.highlightMarker(marker);
      });
      
      marker.on('mouseover', () => {
        marker.setIcon(this.createCustomIcon(place.type, place.orderNumber, place.importance, true));
      });
      
      marker.on('mouseout', () => {
        marker.setIcon(this.createCustomIcon(place.type, place.orderNumber, place.importance, false));
      });
      
      this.map.add(marker);
      this.markers.push(marker);
    },
    
    createCustomIcon(type, orderNumber, importance = 5, isHover = false) {
      // 添加调试
      console.log(`🎨 创建图标: type=${type}, orderNumber=${orderNumber}, isHover=${isHover}`);
      
      const colorMap = {
        birthplace: isHover ? '#ff7675' : '#d63031',
        residence: isHover ? '#74b9ff' : '#0984e3',
        travel: isHover ? '#55efc4' : '#00b894'
      };

      const color = colorMap[type] || (isHover ? '#a29bfe' : '#6c5ce7');
      console.log(`🎨 颜色映射: type=${type} -> color=${color}`);
      
      // ✅ 根据重要性动态调整大小（最小24，最大48）
      const baseSize = isHover ? 36 : 32;
      const size = Math.max(24, Math.min(48, baseSize + importance * 2));
      
      // 计算字体大小，确保数字清晰可见
      const fontSize = Math.max(10, Math.min(16, size / 3));
      
      const svg = `
        <svg width="${size}" height="${size}" xmlns="http://www.w3.org/2000/svg">
          <circle cx="${size/2}" cy="${size/2}" r="${size/2 - 2}" fill="${color}" 
                  opacity="${isHover ? 0.95 : 0.9}" 
                  stroke="#fff" stroke-width="${isHover ? 3 : 2}"/>
          <circle cx="${size/2}" cy="${size/2}" r="${size/4}" fill="#fff" 
                  opacity="${isHover ? 0.9 : 0.7}"/>
          ${isHover ? `<circle cx="${size/2}" cy="${size/2}" r="${size/6}" fill="${color}" opacity="0.7"/>` : ''}
          <text x="${size/2}" y="${size/2 + fontSize/3}" text-anchor="middle" 
                font-size="${fontSize}" fill="#5c4033" font-weight="bold" 
                font-family="Arial, sans-serif">${orderNumber}</text>
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
        marker.setIcon(this.createCustomIcon(place.type, place.orderNumber, place.importance, false));
      });
      
      // 高亮当前标记
      const place = currentMarker.getExtData();
      currentMarker.setIcon(this.createCustomIcon(place.type, place.orderNumber, place.importance, true));
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
    
    // 平滑移动地图到指定位置
    smoothMoveMapTo(lnglat, zoom = 8, duration = 800) {
      if (!this.map || this.isAnimating) return;
      
      this.isAnimating = true;
      
      // 使用高德地图的panTo方法实现平滑移动
      this.map.panTo(lnglat, {
        duration: duration,
        easing: 'ease-in-out' // 缓动效果
      });
      
      // 平滑缩放
      this.map.setZoom(zoom, true, duration);
      
      // 动画结束后重置状态
      setTimeout(() => {
        this.isAnimating = false;
      }, duration);
    },
    
    // 显示地点详情 - 添加平滑移动
    async showPlaceDetail(place, index) {
      // 立即更新状态
      this.selectedPlace = place;
      this.selectedPlaceIndex = index;
      
      // 平滑移动地图视角到该地点
      if (this.map) {
        this.smoothMoveMapTo(place.coordinates, 8);
      }
      
      // 高亮对应的标记
      const marker = this.markers[index];
      if (marker) {
        this.highlightMarker(marker);
      }
       // ✅ 拉取该地点的人生大事
      try {
        const res = await axios.get('/api/huangbinhong/location-events', {
          params: { location: place.name }
        });
        this.selectedPlace.timeEvents = res.data.data.map(e => ({
          year: e.year,
          event: e.title
        }));
      } catch (err) {
        console.error('拉取地点事件失败:', err);
        this.selectedPlace.timeEvents = [];
      }
      // 滚动到详情面板顶部
      setTimeout(() => {
        const panel = document.querySelector('.detail-panel');
        if (panel) {
          panel.scrollTop = 0;
        }
      }, 100);
    },
    
    // 修改平滑移动到指定地点方法，调用 showPlaceDetail
    smoothMoveToPlace(place, index) {
      if (!this.map || this.isAnimating) return;
      
      this.isAnimating = true;
      
      // 1. 平滑移动地图
      this.map.panTo(place.coordinates, {
        duration: 800,
        easing: 'ease-in-out'
      });
      
      // 2. 平滑缩放
      this.map.setZoom(8, true, 800);
      
      // 3. 调用 showPlaceDetail 来更新数据和状态
      // 注意：这里使用 $nextTick 确保 DOM 更新
      this.$nextTick(() => {
        this.showPlaceDetail(place, index);
      });
      
      // 4. 动画结束后重置状态
      setTimeout(() => {
        this.isAnimating = false;
      }, 800);
    },
    
    // 关闭详情面板
    closeDetailPanel() {
      this.selectedPlace = null;
      this.selectedPlaceIndex = -1;
      
      // 重置所有标记
      this.markers.forEach(marker => {
        const place = marker.getExtData();
        marker.setIcon(this.createCustomIcon(place.type, place.orderNumber, place.importance, false));
      });
      
      // 平滑回到初始视图
      if (this.map) {
        this.smoothMoveMapTo([115.0, 35.0], 5, 800);
      }
    },
    
    // 导航到上一个地点 - 添加平滑过渡
    navigateToPreviousPlace() {
      if (this.selectedPlaceIndex > 0 && !this.isAnimating) {
        const prevIndex = this.selectedPlaceIndex - 1;
        const place = this.footprintData[prevIndex];
        
        // 使用平滑移动方法
        this.smoothMoveToPlace(place, prevIndex);
      }
    },
    
    // 导航到下一个地点 - 添加平滑过渡
    navigateToNextPlace() {
      if (this.selectedPlaceIndex < this.footprintData.length - 1 && !this.isAnimating) {
        const nextIndex = this.selectedPlaceIndex + 1;
        const place = this.footprintData[nextIndex];
        
        // 使用平滑移动方法
        this.smoothMoveToPlace(place, nextIndex);
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
        
        if (e.key === 'ArrowLeft' && !this.isAnimating) {
          this.navigateToPreviousPlace();
        }
        
        if (e.key === 'ArrowRight' && !this.isAnimating) {
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
    selectedPlaceIndex() {
      // 确保按钮状态正确
      this.$nextTick(() => {
        // 可以在这里添加一些额外的动画效果
      });
    }
  }
};
</script>

<style scoped>
/* 原有样式保持不变，只添加一些动画效果和加载状态样式 */

.art-footprints-page {
  font-family: "SimSun", "STSong", serif;
  color: #333;
  background: linear-gradient(135deg, #f5f0e6 0%, #e8dfca 100%);
  min-height: 100vh;
  overflow: hidden;
}

/* 加载状态样式 */
.loading-overlay {
  position: absolute;
  top: 80px;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 253, 248, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #d2b48c;
  border-top: 4px solid #8b4513;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 添加面板动画 */
.detail-panel {
  position: absolute;
  top: 20px;
  right: 20px;
  background-color: rgba(255, 253, 248, 0.95);
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  z-index: 100;
  width: 380px;
  max-height: calc(100% - 40px);
  overflow-y: auto;
  border: 2px solid #d2b48c;
  opacity: 0;
  transform: translateX(20px);
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.detail-panel.active {
  opacity: 1;
  transform: translateX(0);
  display: block;
}

/* 添加按钮动画 */
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
  position: relative;
  overflow: hidden;
}

.nav-button:not(:disabled):hover {
  background-color: #8b4513;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.3);
}

.nav-button:not(:disabled):active {
  transform: translateY(0);
}

.nav-button::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.2);
  transform: translate(-50%, -50%);
  transition: width 0.4s, height 0.4s;
}

.nav-button:not(:disabled):active::after {
  width: 200px;
  height: 200px;
}

.nav-button:disabled {
  background-color: #e0d9c8;
  color: #aaa;
  cursor: not-allowed;
  transform: none !important;
}

/* 地点类型标签动画 */
.place-type {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #fff;
  animation: fadeInUp 0.4s ease;
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

/* 时间线事件动画 */
.timeline-event {
  margin-bottom: 15px;
  padding-left: 20px;
  position: relative;
  animation: fadeInLeft 0.5s ease;
  animation-fill-mode: both;
}

.timeline-event:nth-child(1) { animation-delay: 0.1s; }
.timeline-event:nth-child(2) { animation-delay: 0.2s; }
.timeline-event:nth-child(3) { animation-delay: 0.3s; }
.timeline-event:nth-child(4) { animation-delay: 0.4s; }
.timeline-event:nth-child(5) { animation-delay: 0.5s; }

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 其他原有样式保持不变 */
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
  height: 80px;
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

.main-content {
  position: absolute;
  top: 80px;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
}

#map-container {
  width: 100%;
  height: 100%;
  position: relative;
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
  animation: fadeInUp 0.3s ease;
}

.detail-content {
  color: #333;
  line-height: 1.6;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0d9c8;
  animation: fadeInUp 0.4s ease;
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

.detail-navigation {
  display: flex;
  justify-content: space-between;
  margin-top: 25px;
  padding-top: 15px;
  border-top: 2px solid #d2b48c;
  animation: fadeInUp 0.5s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 15px 20px;
    height: 70px;
  }
  
  .main-content {
    top: 70px;
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
  
  .loading-overlay {
    top: 70px;
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
  
  .loading-overlay {
    top: 65px;
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