<template>
  <body>
    <header><HeaderNavbar /></header>

    <main class="main-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">黄宾虹作品集</h1>
        <p class="page-subtitle">
          浏览黄宾虹的艺术作品，按时期、题材分类，全面了解大师的艺术创作历程
        </p>
      </div>

      <!-- ① sub-nav：画作 | 书法 -->
      <div class="sub-nav-wrap">
        <div class="sub-nav">
          <button
            v-for="t in typeTabs"
            :key="t.val"
            class="sub-nav-btn"
            :class="{active:selectedType===t.val}"
            @click="selectedType=t.val; selectedTag='all'"
          >{{ t.label }}</button>
        </div>
      </div>

      <!-- ② 原筛选栏（与 sub-nav 无缝） -->
      <section class="filters-section">
        <div class="filters-header">
          <h2 class="filters-title">作品筛选</h2>
        </div>

        <div class="filter-row">
          <!-- 时期 -->
          <div class="filter-group">
            <label class="filter-label">时期</label>
            <select class="filter-select" v-model="selectedPeriod">
              <option value="all">全部时期</option>
              <option value="early">早期（1865-1900）</option>
              <option value="middle">中期（1901-1930）</option>
              <option value="late">晚期（1931-1955）</option>
            </select>
          </div>

          <!-- 画作标签 -->
          <div v-if="selectedType==='画'" class="filter-group">
            <label class="filter-label">标签</label>
            <select class="filter-select" v-model="selectedTag">
              <option value="all">全部</option>
              <option value="花鸟">花鸟</option>
              <option value="山水">山水</option>
              <option value="设色山水">设色山水</option>
              <option value="访友">访友</option>
              <option value="文人画">文人画</option>
            </select>
          </div>

          <!-- 书法标签 -->
          <div v-if="selectedType==='书法'" class="filter-group">
            <label class="filter-label">标签</label>
            <select class="filter-select" v-model="selectedTag">
              <option value="all">全部</option>
              <option value="行书">行书</option>
             
              <option value="草书">草书</option>
              <option value="篆书">篆书</option>
            </select>
          </div>
        </div>

        <!-- 搜索、热门标签、已选徽章 … 保持不变 -->
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
            <button class="search-btn" @click="triggerSearch"><i class="fas fa-search"></i></button>
          </div>
        </div>

        <div class="filter-row" v-if="hotTags.length">
          <div class="filter-group">
            <label class="filter-label">热门搜索</label>
            <div class="tag-cloud">
              <span
                v-for="tag in hotTags"
                :key="tag"
                class="cloud-tag"
                :class="{active:searchKeyword===tag}"
                @click="toggleTag(tag)"
              >#{{ tag }}</span>
            </div>
          </div>
        </div>

        <button
          v-if="searchKeyword"
          class="clear-btn"
          @click="clearSearch"
          title="清空搜索"
        >
          <i class="fas fa-times"></i>
        </button>

        <div class="active-filters" v-if="hasActiveFilters">
          <span class="filter-badge" v-if="selectedPeriod!=='all'">
            时期：{{ getPeriodLabel(selectedPeriod) }}<i class="fas fa-times" @click="selectedPeriod='all'"></i>
          </span>
          <span class="filter-badge" v-if="searchKeyword">
            搜索：{{ searchKeyword }}<i class="fas fa-times" @click="clearSearch"></i>
          </span>
          <span class="filter-badge" v-if="selectedTag!=='all'">
            标签：{{  getTagLabel(selectedTag) }}<i class="fas fa-times" @click="selectedTag='all'"></i>
          </span>
        </div>
      </section>

      <!-- 作品网格、加载更多、空状态 … 以下全部原样 -->
      <div class="gallery-grid" id="galleryGrid">
        <div v-for="art in works" :key="art.id" class="artwork-card" >
          <div class="artwork-img-container">
            <img :src="art.thumbnail" :alt="art.title" class="artwork-image" />
          </div>
          <div class="artwork-info">
            <h3 class="artwork-title">{{ art.title }}</h3>
            <div class="artwork-meta">{{ art.year }}年 · {{ art.size }} · {{ art.collection }}</div>
            <div class="artwork-description">{{ art.description }}</div>
            <div class="artwork-tags">
              <span v-for="t in art.tags" :key="t" class="artwork-tag" @click="selectArtworkTag(t)">{{ t }}</span>
            </div>
            <div class="artwork-actions">
              <button class="action-btn view-detail-btn" @click.stop="viewArtworkDetail(art.id)">
                <i class="fas fa-eye"></i> 查看详情
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="load-more" v-if="works.length<total && works.length>0">
        <button class="load-more-btn" @click="loadMore" :disabled="isLoading">
          <i v-if="isLoading" class="fas fa-spinner fa-spin"></i>
          <span v-else>加载更多作品</span>
        </button>
      </div>

      <div class="empty-state" v-if="works.length===0 && !isLoading">
        <div class="empty-icon"><i class="fas fa-palette"></i></div>
        <h3>未找到符合条件的作品</h3>
        <p>请尝试调整筛选条件或搜索关键词</p>
      </div>

      <div class="loading-state" v-if="isLoading && works.length===0">
        <div class="loading-icon"><i class="fas fa-spinner fa-spin"></i></div>
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
      selectedType: '画',
      selectedTag: 'all', 
      searchKeyword: '',
      currentPage: 1,
      itemsPerPage: 12,
      works: [],
      total: 0,
      isLoading: false,
      searchTimeout: null,
      typeTabs: [
        { val: '画',   label: '画作' },
        { val: '书法', label: '书法' }
      ],
      hotTags: ['山水',  '水墨', '设色','设色山水']
    }
  },
  computed: {
    hasActiveFilters() {
      return this.selectedType!=='all' || this.selectedPeriod!=='all' || this.searchKeyword.trim()!==''
    }
  },
  watch: {
    selectedPeriod() { this.resetAndSearch() },
    selectedType()  { this.resetAndSearch() },
    selectedTag()   { this.resetAndSearch() },
    '$route.query.tag'() { this.applyRouteTag() }
  },
  mounted() {
    this.applyRouteTag()
    this.fetchWorks()
  },
  methods: {
    /* ===== 新增 URL tag 自动回显 ===== */
    applyRouteTag() {
      const tag = this.$route.query.tag
      if (!tag) return
      this.selectedTag = tag
      //this.searchKeyword = tag
      if (['行书',  '草书', '篆书'].includes(tag)) {
        this.selectedType = '书法'
      } else {
        this.selectedType = '画'
      }
      this.resetAndSearch()
    },
    selectArtworkTag(tag) {
      this.selectedTag = tag
      this.resetAndSearch()
    },
    toggleTag(tag) {
      this.searchKeyword = this.searchKeyword === tag ? '' : tag
      this.triggerSearch()
    },
    onSearchInput() {
      if (this.searchTimeout) clearTimeout(this.searchTimeout)
      this.searchTimeout = setTimeout(() => this.resetAndSearch(), 500)
    },
    triggerSearch() {
      if (this.searchTimeout) clearTimeout(this.searchTimeout)
      this.resetAndSearch()
    },
    clearSearch() {
      this.searchKeyword = ''
      this.resetAndSearch()
    },
    resetAndSearch() {
      this.currentPage = 1
      this.works = []
      this.fetchWorks(false)
    },
    async fetchWorks(isLoadMore = false) {
      if (this.isLoading) return
      this.isLoading = true

      if (!isLoadMore) {
        this.currentPage = 1
        this.works = []
      }

      try {
    const params = new URLSearchParams()
    params.append('page', this.currentPage.toString())
    params.append('size', this.itemsPerPage.toString())
    
    // 类型筛选
    if (this.selectedType !== 'all') {
      params.append('category', this.selectedType)
    }
    
    // 搜索关键词
    if (this.searchKeyword.trim()) {
      params.append('works_name', this.searchKeyword.trim())
    }
    
    // 标签筛选
    if (this.selectedTag !== 'all') {
      params.append('tags', this.selectedTag)
    }
    
    // ✅ 关键修改：传art_period而不是年份范围
    if (this.selectedPeriod !== 'all') {
      params.append('art_period', this.getPeriodLabel(this.selectedPeriod))
    }
    
    // 选择接口
    let url
    // 如果是纯时期筛选（无其他条件）- 现在不需要特殊处理了
    // 直接走通用接口
    url = `/api/works/list?${params.toString()}`
    
    console.log('📡 请求URL:', url)
    
    const res = await fetch(url)
    const json = await res.json()
    
    console.log('📦 响应数据:', {
      状态码: json.code,
      总数: json.data?.total,
      列表长度: json.data?.list?.length
    })
    
    if (json.code === 200) {
      const list = json.data.list.map(i => ({
        id: i.worksId,
        title: i.worksName,
        year: i.creationYear,
        type: i.category,
        period: i.artPeriod || this.getPeriod(i.creationYear), // 优先用数据库的art_period
        thumbnail: i.thumbnailUrl ? `http://localhost:8080${i.thumbnailUrl}` : '/images/default.jpg',
        size: i.size || '尺寸不详',
        collection: i.collectionInstitution || '收藏机构不详',
        description: i.worksDesc || '暂无简介',
        tags: i.tags || []
      }))
      
      if (isLoadMore) {
        this.works = [...this.works, ...list]
      } else {
        this.works = list
      }
      
      this.total = json.data.total || 0
      
      console.log(`✅ 加载完成，显示 ${this.works.length} 个作品`)
      
    } else {
      console.error('❌ API错误:', json.msg)
      if (!isLoadMore) this.works = []
    }
    
  } catch (e) {
    console.error('❌ 请求失败:', e)
    if (!isLoadMore) this.works = []
  } finally {
    this.isLoading = false
  }
},
    getYearRange(p) {
      switch (p) {
        case 'early':  return { startYear: 1865, endYear: 1900 }
        case 'middle': return { startYear: 1901, endYear: 1930 }
        case 'late':   return { startYear: 1931, endYear: 1955 }
        default: return null
      }
    },
    getPeriod(y) {
      if (!y) return 'unknown'
      if (y <= 1900) return 'early'
      if (y <= 1930) return 'middle'
      return 'late'
    },
    getTagLabel(y) {
      const map = {
        '花鸟': '花鸟',
        '山水': '山水',
        '设色山水': '设色山水',
        '访友': '访友',
        '文人画': '文人画',
        '行书': '行书',
        '草书': '草书',
        '篆书': '篆书'
      }
      return map[y] || y
    },
    
   getPeriodLabel(p) {
      return { early: '早期', middle: '中期', late: '晚期' }[p] || p
    },

    loadMore() {
      if (this.works.length >= this.total || this.isLoading) return
      this.currentPage++
      this.fetchWorks(true)
    },
    viewArtworkDetail(id) { this.$router.push(`/work?id=${id}`) }
  }
}
</script>


<style>
/* ===== 仅需追加的少量样式 ===== */
.type-tabs{display:flex;gap:10px}
.type-btn{padding:6px 18px;border:1px solid #d2b48c;background:#fff;color:#5c4033;border-radius:4px;cursor:pointer;font-size:14px;transition:.3s}
.type-btn.active,.type-btn:hover{background:#d2b48c;color:#fff}
.tag-cloud{display:flex;flex-wrap:wrap;gap:8px}
.cloud-tag{padding:4px 10px;border:1px solid #d2b48c;border-radius:12px;font-size:12px;color:#5c4033;cursor:pointer;transition:.3s}
.cloud-tag.active,.cloud-tag:hover{background:#d2b48c;color:#fff}
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
  margin-top: 40px;
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
  margin: 5px;
  border-radius: 12px;
  padding: 3px 8px;
  font-size: 11px;
  color: #5c4033;
}
.artwork-tag:hover {
  background-color: #d2b48c;
  color: #333;
}
.artwork-actions {
  display: flex;
  gap: 10px;
}
.action-btn {
  margin-top: 10px;
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
/* ===== 精致清除按钮 ===== */
.clear-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #d2b48c 0%, #a67c52 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
  transition: all 0.25s ease;
}
.clear-btn:hover {
  transform: translateY(-50%) scale(1.15);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}
.clear-btn:active {
  transform: translateY(-50%) scale(0.95);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
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
.sub-nav-wrap{
  max-width:1400px;
  margin:0 auto;
  padding:0 60px;          /* 与下方 filters-section 左右对齐 */
}
.sub-nav{
  display:inline-flex;     /* 只撑开内容宽度 */
  background:#fff;
  border-radius:8px 8px 0 0;   /* 左上 + 右上圆角 */
  overflow:hidden;
  box-shadow:0 -2px 5px rgba(0,0,0,.05);
}
.sub-nav-btn{
  padding:8px 24px;
  border:none;
  background:transparent;
  color:#5c4033;
  font-size:15px;
  cursor:pointer;
  transition:.3s;
  position:relative;
}
.sub-nav-btn:hover{background:#f5f0e6}
.sub-nav-btn.active{
  background:#d2b48c;
  color:#fff;
}
/* 让 sub-nav 下边缘与 filters-section 上边缘无缝贴合 */
.filters-section{
  border-top-left-radius:0;   /* 去掉左上角圆角 */
  margin-top:-1px;            /* 叠掉 1px 缝隙 */
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