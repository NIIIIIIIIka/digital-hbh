<template>
  <body>
    <header>
      <HeaderNavbar />
    </header>
    <main class="main-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">黄宾虹作品集</h1>
        <p class="page-subtitle">
          浏览黄宾虹的艺术作品，按时期、题材分类，全面了解大师的艺术创作历程
        </p>
      </div>

      <!-- 筛选栏 -->
      <section class="filters-section">
        <div class="filters-header">
          <h2 class="filters-title">作品筛选</h2>
          <div class="view-toggle">
            <button class="view-btn active" title="网格视图">
              <i class="fas fa-th-large"></i>
            </button>
            <button class="view-btn" title="列表视图">
              <i class="fas fa-list"></i>
            </button>
          </div>
        </div>

        <div class="filter-row">
          <div class="filter-group">
            <label class="filter-label">题材：</label>
            <select
              class="filter-select"
              v-model="selectedType"
              @change="onFilterChange"
            >
              <option value="all">全部题材</option>
              <option value="画">画作</option>
              <option value="书法">书法</option>
            </select>
          </div>

          <div class="filter-group">
            <label class="filter-label">时期</label>
            <select
              class="filter-select"
              v-model="selectedPeriod"
              @change="onFilterChange"
            >
              <option value="all">全部时期</option>
              <option value="early">早期（1865-1900）</option>
              <option value="middle">中期（1901-1930）</option>
              <option value="late">晚期（1931-1955）</option>
            </select>
          </div>
        </div>

        <div class="filter-row">
          <div class="filter-group search-box">
            <label class="filter-label">搜索文物</label>
            <input
              type="text"
              class="search-input"
              v-model="searchKeyword"
              @input="onSearchInput"
              @keyup.enter="triggerSearch"
              placeholder="输入文物名称或关键词..."
            />
            <button class="search-btn" @click="triggerSearch">
              <i class="fas fa-search"></i>
            </button>
            <button 
              v-if="searchKeyword" 
              class="clear-btn" 
              @click="clearSearch"
              title="清空搜索"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>
        
        <!-- 显示当前筛选条件 -->
        <div class="active-filters" v-if="hasActiveFilters">
          <span class="filter-badge" v-if="selectedType !== 'all'">
            题材：{{ selectedType }}
            <i class="fas fa-times" @click="clearTypeFilter"></i>
          </span>
          <span class="filter-badge" v-if="selectedPeriod !== 'all'">
            时期：{{ getPeriodLabel(selectedPeriod) }}
            <i class="fas fa-times" @click="clearPeriodFilter"></i>
          </span>
          <span class="filter-badge" v-if="searchKeyword">
            搜索：{{ searchKeyword }}
            <i class="fas fa-times" @click="clearSearch"></i>
          </span>
        </div>
      </section>

      <!-- 作品网格 -->
      <div class="gallery-grid" id="galleryGrid">
        <div
          v-for="artwork in works"
          :key="artwork.id"
          class="artwork-card"
          @click="viewArtworkDetail(artwork.id)"
        >
          <div class="artwork-img-container">
            <img
              :src="artwork.thumbnail"
              :alt="artwork.title"
              class="artwork-image"
            />
          </div>
          <div class="artwork-info">
            <h3 class="artwork-title">{{ artwork.title }}</h3>
            <div class="artwork-meta">
              {{ artwork.year }}年 · {{ artwork.size }} · {{ artwork.collection }}
            </div>
            <div class="artwork-description">{{ artwork.description }}</div>
            <div class="artwork-tags">
              <span
                v-for="tag in artwork.tags"
                :key="tag"
                class="artwork-tag"
              >{{ tag }}</span>
            </div>
            <div class="artwork-actions">
              <button
                class="action-btn view-detail-btn"
                @click.stop="viewArtworkDetail(artwork.id)"
              >
                <i class="fas fa-eye"></i> 查看详情
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div class="load-more" v-if="works.length < total && works.length > 0">
        <button class="load-more-btn" @click="loadMore" :disabled="isLoading">
          <i class="fas fa-spinner fa-spin" v-if="isLoading"></i>
          <span v-else>加载更多作品</span>
        </button>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="works.length === 0 && !isLoading">
        <div class="empty-icon">
          <i class="fas fa-palette"></i>
        </div>
        <h3>未找到符合条件的作品</h3>
        <p>请尝试调整筛选条件或搜索关键词</p>
      </div>

      <!-- 加载中 -->
      <div class="loading-state" v-if="isLoading && works.length === 0">
        <div class="loading-icon">
          <i class="fas fa-spinner fa-spin"></i>
        </div>
        <p>正在加载作品...</p>
      </div>
    </main>
  </body>
</template>

<script>
import HeaderNavbar from '../components/HeaderNavbar.vue'

export default {
  name: 'GalleryPage',
  components: { HeaderNavbar },
  data() {
    return {
      selectedPeriod: 'all',
      selectedType: 'all',
      searchKeyword: '',
      currentPage: 1,
      itemsPerPage: 12,
      works: [],
      total: 0,
      isLoading: false,
      searchTimeout: null,
    }
  },
  computed: {
    hasActiveFilters() {
      return this.selectedType !== 'all' || 
             this.selectedPeriod !== 'all' || 
             this.searchKeyword.trim() !== ''
    }
  },
  mounted() {
    this.fetchWorks()
  },
  watch: {
    selectedPeriod() {
      this.resetAndSearch()
    },
    selectedType() {
      this.resetAndSearch()
    }
  },
  methods: {
    async fetchWorks(isLoadMore = false) {
      if (this.isLoading) return
      
      this.isLoading = true
      
      if (!isLoadMore) {
        this.currentPage = 1
      }
      
      try {
        let url
        let params
        
        // 场景1：使用时期筛选接口（当选择了时期且未选择其他条件时）
        if (this.selectedPeriod !== 'all' && 
            this.selectedType === 'all' && 
            !this.searchKeyword.trim()) {
          
          url = `/api/works/by-period?period=${this.selectedPeriod}&page=${this.currentPage}&size=${this.itemsPerPage}`
          
        } else {
          // 场景2：使用普通列表接口（支持混合筛选）
          params = new URLSearchParams({
            page: this.currentPage,
            size: this.itemsPerPage,
          })
          
          // 处理分类筛选
          if (this.selectedType !== 'all') {
            params.append('category', this.selectedType)
          }
          
          // 处理搜索关键词
          const keyword = this.searchKeyword.trim()
          if (keyword) {
            params.append('works_name', keyword)
          }
          
          // 处理时期筛选（需要在普通接口中传递年份范围）
          if (this.selectedPeriod !== 'all') {
            const yearRange = this.getYearRange(this.selectedPeriod)
            if (yearRange) {
              params.append('startYear', yearRange.startYear)
              params.append('endYear', yearRange.endYear)
            }
          }
          
          url = `/api/works/list?${params}`
        }
        
        console.log('请求URL:', url)
        
        const res = await fetch(url)
        const json = await res.json()
        console.log('API响应:', json)
        
        if (json.code === 200) {
          const list = json.data.list.map(item => ({
            id: item.worksId,
            title: item.worksName,
            year: item.creationYear,
            type: item.category,
            period: this.getPeriod(item.creationYear),
            thumbnail: item.thumbnailUrl
              ? `http://localhost:8080${item.thumbnailUrl}`
              : '/images/default.jpg',
            size: item.size || '尺寸不详',
            collection: item.collectionInstitution || '收藏机构不详',
            description: item.worksDesc || '暂无简介',
            tags: item.tags || [],
          }))
          
          if (isLoadMore) {
            this.works = [...this.works, ...list]
          } else {
            this.works = list
          }
          this.total = json.data.total
        } else {
          console.error('API返回错误:', json.message)
          if (!isLoadMore) {
            this.works = []
          }
        }
      } catch (error) {
        console.error('获取作品失败:', error)
        if (!isLoadMore) {
          this.works = []
        }
      } finally {
        this.isLoading = false
      }
    },

    // 获取时期对应的年份范围
    getYearRange(period) {
      switch(period) {
        case 'early':
          return { startYear: 1865, endYear: 1900 }
        case 'middle':
          return { startYear: 1901, endYear: 1930 }
        case 'late':
          return { startYear: 1931, endYear: 1955 }
        default:
          return null
      }
    },

    getPeriod(year) {
      if (!year) return 'unknown'
      if (year <= 1900) return 'early'
      if (year <= 1930) return 'middle'
      return 'late'
    },

    getPeriodLabel(period) {
      const labels = {
        'early': '早期（1865-1900）',
        'middle': '中期（1901-1930）',
        'late': '晚期（1931-1955）'
      }
      return labels[period] || period
    },

    // 搜索输入时触发（防抖）
    onSearchInput() {
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout)
      }
      
      this.searchTimeout = setTimeout(() => {
        this.resetAndSearch()
      }, 500)
    },

    // 手动点击搜索按钮或按回车
    triggerSearch() {
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout)
      }
      this.resetAndSearch()
    },

    // 清空搜索
    clearSearch() {
      this.searchKeyword = ''
      this.resetAndSearch()
    },

    // 清空题材筛选
    clearTypeFilter() {
      this.selectedType = 'all'
    },

    // 清空时期筛选
    clearPeriodFilter() {
      this.selectedPeriod = 'all'
    },

    // 重置并重新搜索
    resetAndSearch() {
      this.currentPage = 1
      this.fetchWorks()
    },

    // 筛选条件变化处理
    onFilterChange() {
      this.resetAndSearch()
    },

    // 加载更多
    loadMore() {
      if (this.works.length >= this.total || this.isLoading) return
      this.currentPage++
      this.fetchWorks(true)
    },

    viewArtworkDetail(id) {
      this.$router.push(`/work?id=${id}`)
    },
  }
}
</script>


<style>
/* 以下样式与你原文件完全一致，无需改动 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  color: #333;
  background: linear-gradient(135deg, #f5f0e6 0%, #e8dfca 100%);
  min-height: 100vh;
  font-family: "SimSun", "STSong", "KaiTi", "STKaiti", Georgia, Times New Roman, serif;
}
.main-content {
  padding-top: 100px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}
.page-header {
  text-align: center;
  padding: 30px 20px;
  border-radius: 15px;
  margin-bottom: 30px;
}
.page-title {
  color: #8b4513;
  font-size: 36px;
  margin-bottom: 10px;
}
.page-subtitle {
  color: #5c4033;
  font-size: 18px;
  max-width: 800px;
  margin: 0 auto;
  line-height: 1.6;
}
.filters-section {
  background-color: white;
  border-radius: 8px;
  padding: 25px;
  margin: 0 60px;
  margin-bottom: 30px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
}
.filters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.filters-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a3c40;
}
.view-toggle {
  display: flex;
  gap: 10px;
}
.view-btn {
  background: none;
  border: 1px solid #ddd;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.1rem;
  color: #666;
  transition: all 0.3s;
}
.view-btn.active,
.view-btn:hover {
  background-color: #1a3c40;
  color: white;
  border-color: #1a3c40;
}
.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 15px;
}
.filter-group {
  flex: 1;
  min-width: 200px;
}
.filter-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}
.filter-select,
.search-input {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: 'Noto Serif SC', serif;
  font-size: 1rem;
  background-color: white;
}
.search-box {
  position: relative;
}
.search-btn {
  position: absolute;
  right: 10px;
  top: 70%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #666;
  font-size: 1.1rem;
  cursor: pointer;
}
.artwork-tag {
  background-color: #f0e6d6;
  border: 1px solid #d2b48c;
  border-radius: 12px;
  padding: 3px 8px;
  font-size: 11px;
  color: #5c4033;
}
.artwork-actions {
  display: flex;
  gap: 10px;
}
.action-btn {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d2b48c;
  background-color: #f8f4e6;
  border-radius: 5px;
  color: #5c4033;
  cursor: pointer;
  font-size: 12px;
  font-family: inherit;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}
.action-btn:hover {
  background-color: #d2b48c;
  color: #333;
}
.load-more {
  text-align: center;
  margin: 30px 0;
}
.load-more-btn {
  padding: 12px 40px;
  background-color: #d2b48c;
  border: none;
  border-radius: 5px;
  color: #333;
  cursor: pointer;
  font-family: inherit;
  font-size: 16px;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.load-more-btn:hover {
  background-color: #a67c52;
  color: #fff;
}
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}
.empty-icon {
  font-size: 60px;
  color: #d2b48c;
  margin-bottom: 20px;
}
.gallery-grid {
  column-count: 3;
  column-gap: 20px;
  padding: 25px;
  margin: 0 60px;
  margin-bottom: 40px;
}
.gallery-grid.list-view {
  column-count: 1;
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;
  overflow: hidden;
}
.artwork-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
  break-inside: avoid;
  margin-bottom: 25px;
  display: inline-block;
  width: 100%;
}
.gallery-grid.list-view .artwork-card {
  overflow: hidden;
  height: 100px;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 0;
  min-height: 200px;
}
.artwork-img-container {
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f8f9fa;
  width: 100%;
  height: auto;
  max-height: 550px;
}
.gallery-grid.list-view .artwork-img-container {
  overflow: hidden;
  width: 200px;
  min-height: 200px;
  padding: 20px;
  flex-shrink: 0;
  border-radius: 8px 0 0 8px;
}
.artwork-image {
  max-width: 100%;
  height: auto;
  object-fit: contain;
  display: block;
  background-color: #f8f9fa;
  margin: 0 auto;
}
.gallery-grid.list-view .artwork-image {
  overflow: hidden;
  max-height: 160px;
  max-width: 200px;
}
.artwork-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}
.gallery-grid.list-view .artwork-card:hover {
  transform: translateX(5px);
}
.gallery-grid.list-view .artwork-info {
  flex: 1;
  padding: 25px;
}
.artwork-info {
  padding: 20px;
}
.artwork-title {
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1a3c40;
}
.gallery-grid.list-view .artwork-title {
  font-size: 1.4rem;
  margin-bottom: 12px;
}
.artwork-meta {
  color: #666;
  font-size: 0.95rem;
  margin-bottom: 8px;
}
.artwork-description {
  color: #777;
  font-size: 0.95rem;
  line-height: 1.5;
  margin-bottom: 10px;
}
@media (max-width: 768px) {
  .mobile-menu-btn {
    display: block;
  }
  .page-title {
    font-size: 28px;
  }
  .filter-bar {
    flex-direction: column;
  }
}
</style>