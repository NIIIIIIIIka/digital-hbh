<template>
<div class="home-page">  <!-- 替换掉 <body> -->
    <!-- 主内容区 -->
    <main class="main-content">
        <!-- 英雄区域 -->
        <section class="hero-section">
            <img src="/images/huangbinhong.jpg" alt="黄宾虹肖像" class="artist-portrait" id="artistPortrait">
            <h1 class="hero-title">黄宾虹</h1>
            <p class="hero-subtitle">中国近现代国画家、书法家、艺术教育家，山水画一代宗师</p>
            <div class="lifespan">1865 - 1955</div>
            
            <div class="hero-quote">
                "中华大地，无山不美，无水不秀。" —— 黄宾虹
            </div>
        </section>
    </main>
</div>
</template>

<script>
export default {
  name: 'HomePage',
  mounted() {
    // 可以在这里添加组件挂载后的逻辑
  },
  methods: {  // 正确的语法：冒号 + 对象
    // 如果需要，在这里添加方法
    onImageClick() {
      console.log('图片被点击');
    }
  }
}
</script>

<style>
/* 样式保持不变，但要修正背景图片路径 */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.home-page {
    font-family: "SimSun", "STSong", serif;
    color: #333;
    min-height: 100vh;
    overflow-x: hidden;
    /* 添加蒙版和背景图 - 修正图片路径 */
    background: linear-gradient(rgba(245, 240, 230, 0.75), rgba(245, 240, 230, 0.75)), /* 蒙版 */
                url('../assets/qiushan.jpg') no-repeat center 60% fixed; /* 背景图 */
    background-size: 100% auto; /* 宽度100%填满，高度按比例自适应 */
    background-attachment: fixed; /* 背景图固定 */
    background-color: #f5f0e6; /* 顶部和底部可能会有空白，用颜色填充 */
}
    /* 主内容区 */
    .main-content {
        padding-top: 100px;
        max-width: 1400px;
        margin: 0 auto;
    }

    /* 英雄区域 */
    .hero-section {
        text-align: center;
        padding: 80px 20px;
        margin-top: 60px;
        position: relative;
    }

    .artist-portrait {
        width: 180px;
        height: 180px;
        border-radius: 50%;
        object-fit: cover;
        border: 8px solid #d2b48c;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
        margin-bottom: 30px;
        filter: sepia(0.3);
    }

    .hero-title {
        font-size: 48px;
        color: #5c4033;
        margin-bottom: 15px;
        text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
        font-weight: 700; /* 加粗标题 */
    }

    .hero-subtitle {
        font-size: 20px;
        color: #8b4513;
        max-width: 800px;
        margin: 0 auto 30px;
        line-height: 1.6;
        font-weight: 600; /* 加粗副标题 */
    }

    .lifespan {
        font-size: 18px;
        color: #a67c52;
        margin-bottom: 40px;
        font-weight: 700; /* 加粗生卒年份 */
    }

    .hero-quote {
        font-size: 18px;
        color: #8b4513;
        font-style: italic;
        max-width: 600px;
        margin: 0 auto;
        font-weight: 600; /* 加粗名言 */
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
        
        .hero-title {
            font-size: 36px;
            font-weight: 700; /* 移动端也保持加粗 */
        }
        
        .hero-subtitle {
            font-size: 18px;
            font-weight: 600; /* 移动端也保持加粗 */
        }
    
        .section-header {
            flex-direction: column;
            align-items: flex-start;
            gap: 15px;
        }
    
        .features-grid {
            grid-template-columns: 1fr;
        }
        
        .lifespan {
            font-weight: 700; /* 移动端也保持加粗 */
        }
        
        .hero-quote {
            font-weight: 600; /* 移动端也保持加粗 */
        }
    }

    /* 模态框 */
    .modal {
        display: none;
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.8);
        z-index: 2000;
        justify-content: center;
        align-items: center;
    }

    .modal-content {
        background-color: #fffbf0;
        padding: 30px;
        border-radius: 10px;
        max-width: 500px;
        width: 90%;
        border: 3px solid #d2b48c;
        position: relative;
    }

    .modal-close {
        position: absolute;
        top: 15px;
        right: 15px;
        background: none;
        border: none;
        font-size: 24px;
        color: #8b4513;
        cursor: pointer;
    }

    .modal-title {
        color: #5c4033;
        margin-bottom: 20px;
        font-size: 24px;
        font-weight: 700; /* 加粗模态框标题 */
    }

    .modal-text {
        color: #666;
        line-height: 1.6;
        margin-bottom: 20px;
    }

    .modal-buttons {
        display: flex;
        gap: 10px;
        justify-content: flex-end;
    }

    .modal-btn {
        padding: 10px 20px;
        border-radius: 5px;
        border: none;
        cursor: pointer;
        font-family: inherit;
    }

    .modal-btn.confirm {
        background-color: #d2b48c;
        color: #333;
        font-weight: 600; /* 加粗确认按钮 */
    }

    .modal-btn.cancel {
        background-color: #f0e6d6;
        color: #666;
        border: 1px solid #d2b48c;
        font-weight: 600; /* 加粗取消按钮 */
    }
</style>
