<template>
<body>
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
</body>

</template>

<script>

export default {
  name: 'HomePage',
mounted() {
},
  methods:{

  }
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
    }

    .hero-subtitle {
        font-size: 20px;
        color: #8b4513;
        max-width: 800px;
        margin: 0 auto 30px;
        line-height: 1.6;
    }

    .lifespan {
        font-size: 18px;
        color: #a67c52;
        margin-bottom: 40px;
        font-weight: bold;
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
        }
        
        .hero-subtitle {
            font-size: 18px;
        }
    
    .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 15px;
    }
    
    .features-grid {
        grid-template-columns: 1fr;
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
    }

    .modal-btn.cancel {
        background-color: #f0e6d6;
        color: #666;
        border: 1px solid #d2b48c;
    }
</style>