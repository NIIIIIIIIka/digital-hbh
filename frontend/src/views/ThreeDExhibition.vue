<template>
  <div class="corridor-container">
    <!-- 美化加载页面 -->
    <div class="loading-overlay" v-if="loading">
      <div class="loading-content">
        <div class="loader">
          <div class="loader-spinner"></div>
          <div class="loader-text">加载中...</div>
        </div>
        <div class="loading-tip">
          <p>🎨 黄宾虹艺术长廊</p>
          <p>使用 W/S 或 ↑/↓ 键前后移动</p>
          <p>鼠标左键拖动可旋转视角</p>
        </div>
      </div>
    </div>

    <!-- 标签关闭按钮 -->
    <div v-if="activeIntro" class="intro-close-btn" @click="closeIntro">
      ×
    </div>

    <div ref="sceneContainer" id="scene-container"></div>

    <!-- 操作提示 -->
    <div class="controls-hint">
      <div class="hint-item">W/S 或 ↑/↓：前后移动</div>
      <div class="hint-item">鼠标拖动：旋转视角</div>
    </div>
  </div>
</template>

<script>
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls';
import { CSS2DRenderer } from 'three/examples/jsm/renderers/CSS2DRenderer';
import createPersonIntroduction from './charactor.js';
import { IntroManager } from './charactor.js';

export default {
  name: 'CorridorScene',
  data() {
    return {
      loading: true,
      scene: null,
      camera: null,
      renderer: null,
      controls: null,
      labelRenderer: null,
      introManager: null,
      moveSpeed: 0.15,
      keys: { w: false, s: false, ArrowUp: false, ArrowDown: false },
      corridorParams: {
        length: 200,
        width: 14,
        height: 14,
        wallThickness: 0.1
      },
      paintingParams: {
        width: 3.8,
        height: 4.4,
        spacing: 12,
        offsetY: 4.5,
        thickness: 0.05
      },
      paintingImages: [

    { id: 1,  url: '/images/春山红树.jpg' },
    { id: 2,  url: '/images/青山古寺.jpg' },
    { id: 3,  url: '/images/夏花.jpg' },
    { id: 4,  url: '/images/牡丹海棠.jpg' },
    { id: 5,  url: '/images/紫藤绣球.jpg' },
    { id: 6,  url: '/images/春花.jpg' },
    { id: 7,  url: '/images/夜色轻舟.jpg' },
    { id: 8,  url: '/images/浅绛山水45.jpg' },
    { id: 9,  url: '/images/山游归来.jpg' },
    { id: 10, url: '/images/设色山水135.jpg' },
    { id: 11, url: '/images/惠林写生.jpg' },
    { id: 12, url: '/images/溪山访友.jpg' },
    { id: 13, url: '/images/山水.jpg' },
    { id: 14, url: '/images/清水湾写生.jpg' },
    { id: 15, url: '/images/浅绛山水108.jpg' },
    { id: 16, url: '/images/水墨山水92.jpg' }
      ],
      paintingSizes: [],
      animationId: null,
      activeIntro: null, // 当前显示的介绍
      loadingProgress: 0,
      totalResources: 0,
      loadedResources: 0
    };
  },
  mounted() {
    this.initScene();
    this.initListeners();
    
    // 模拟加载进度
    this.simulateLoading();
  },
  beforeDestroy() {
    this.cleanup();
  },
  methods: {
    simulateLoading() {
      const interval = setInterval(() => {
        this.loadingProgress += 5;
        if (this.loadingProgress >= 100) {
          clearInterval(interval);
          setTimeout(() => {
            this.loading = false;
            this.animate();
          }, 500);
        }
      }, 100);
    },

    initScene() {
      // 1. 初始化基础组件
      this.scene = new THREE.Scene();
      this.scene.background = new THREE.Color(0xF0E6D2);
this.initInteraction();
      // 透视相机
      this.camera = new THREE.PerspectiveCamera(
        75,
        window.innerWidth / window.innerHeight,
        0.1,
        1000
      );
      this.camera.position.set(0, 10.6, 0);
      this.camera.lookAt(0, 0, 100);

      // 渲染器
      this.renderer = new THREE.WebGLRenderer({ 
        antialias: true,
        alpha: true 
      });
      this.renderer.setSize(window.innerWidth, window.innerHeight);
      this.renderer.shadowMap.enabled = true;
      this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
      this.$refs.sceneContainer.appendChild(this.renderer.domElement);

      // 创建走廊结构
      this.createCorridor();
      
      // 添加光照系统
      this.addLighting();
      
      // 初始化CSS2DRenderer
      this.initCSS2DRenderer();
      
      // 创建人物介绍
      this.createPersonIntros();
      
      // 加载画作
      this.loadAndCreatePaintings();
      
      // 添加相机控制器
      this.initControls();
    },

    createCorridor() {
      const wallMaterial = new THREE.MeshStandardMaterial({
        color: 0xFfF5F0,
        roughness: 0.6,
        metalness: 0.1
      });
      
      const floorMaterial = new THREE.MeshStandardMaterial({
        color: 0xF0E6D2,
        roughness: 0.2,
        metalness: 0.7,
        opacity: 0.9,
        transparent: true
      });
      
      const ceilMaterial = new THREE.MeshStandardMaterial({ 
        color: 0xFfF5F0,
        roughness: 0.8,
        metalness: 0.5
      });

      // 左侧墙体
      const leftWall = new THREE.Mesh(
        new THREE.BoxGeometry(this.corridorParams.wallThickness, this.corridorParams.height, this.corridorParams.length),
        wallMaterial
      );
      leftWall.position.set(
        -this.corridorParams.width / 2 + this.corridorParams.wallThickness / 2,
        this.corridorParams.height / 2,
        this.corridorParams.length / 2
      );
      leftWall.receiveShadow = true;
      leftWall.castShadow = true;
      this.scene.add(leftWall);

      // 右侧墙体
      const rightWall = new THREE.Mesh(
        new THREE.BoxGeometry(this.corridorParams.wallThickness, this.corridorParams.height, this.corridorParams.length),
        wallMaterial
      );
      rightWall.position.set(
        this.corridorParams.width / 2 - this.corridorParams.wallThickness / 2,
        this.corridorParams.height / 2,
        this.corridorParams.length / 2
      );
      rightWall.receiveShadow = true;
      rightWall.castShadow = true;
      this.scene.add(rightWall);

      // 地面
      const floor = new THREE.Mesh(
        new THREE.BoxGeometry(this.corridorParams.width, this.corridorParams.wallThickness, this.corridorParams.length),
        floorMaterial
      );
      floor.position.set(0, this.corridorParams.wallThickness / 2, this.corridorParams.length / 2);
      floor.receiveShadow = true;
      this.scene.add(floor);

      // 天花板
      const ceiling = new THREE.Mesh(
        new THREE.BoxGeometry(this.corridorParams.width, this.corridorParams.wallThickness, this.corridorParams.length),
        ceilMaterial
      );
      ceiling.position.x = 0;
      ceiling.position.y = this.corridorParams.height - (this.corridorParams.wallThickness / 2);
      ceiling.position.z = this.corridorParams.length / 2;
      ceiling.receiveShadow = true;
      this.scene.add(ceiling);

      // 走廊尽头
      const endWall = new THREE.Mesh(
        new THREE.BoxGeometry(this.corridorParams.width, this.corridorParams.height, this.corridorParams.wallThickness),
        wallMaterial
      );
      endWall.position.set(0, this.corridorParams.height / 2, this.corridorParams.length - this.corridorParams.wallThickness / 2);
      endWall.receiveShadow = true;
      this.scene.add(endWall);
    },

    addLighting() {
      // 环境光
      const ambientLight = new THREE.AmbientLight(0xfffff0, 0.8);
      this.scene.add(ambientLight);

      // 左侧墙壁灯光
      const leftWallLight = new THREE.DirectionalLight(0xfff8e1, 2.5);
      leftWallLight.position.set(
        -this.corridorParams.width / 2 - 1,
        this.corridorParams.height / 2 - 2,
        this.corridorParams.length / 2
      );
      leftWallLight.target.position.set(
        0,
        this.corridorParams.height / 2 + 0.5,
        this.corridorParams.length / 2
      );
      this.scene.add(leftWallLight.target);
      leftWallLight.castShadow = false;
      leftWallLight.shadow.mapSize.width = 2048;
      leftWallLight.shadow.mapSize.height = 2048;
      this.scene.add(leftWallLight);

      // 右侧墙壁灯光
      const rightWallLight = new THREE.DirectionalLight(0xfff8e1, 2.5);
      rightWallLight.position.set(
        this.corridorParams.width / 2 + 4,
        this.corridorParams.height / 2 - 2,
        this.corridorParams.length / 2
      );
      rightWallLight.target.position.set(
        0,
        this.corridorParams.height / 2 + 0.5,
        this.corridorParams.length / 2
      );
      this.scene.add(rightWallLight.target);
      rightWallLight.castShadow = false;
      rightWallLight.shadow.mapSize.width = 2048;
      rightWallLight.shadow.mapSize.height = 2048;
      this.scene.add(rightWallLight);

      // 走廊氛围光
      const corridorLight = new THREE.DirectionalLight(0xffffff, 1.5);
      corridorLight.position.set(0, 15, this.corridorParams.length / 2);
      corridorLight.target.position.set(0, 0, this.corridorParams.length / 2);
      this.scene.add(corridorLight.target);
      this.scene.add(corridorLight);
    },

    initCSS2DRenderer() {
      this.labelRenderer = new CSS2DRenderer();
      this.labelRenderer.setSize(window.innerWidth, window.innerHeight);
      this.labelRenderer.domElement.style.position = 'absolute';
      this.labelRenderer.domElement.style.top = '0';
      this.labelRenderer.domElement.style.left = '0';
      this.labelRenderer.domElement.style.pointerEvents = 'none';
      this.labelRenderer.domElement.id = 'css2d-renderer';
      document.body.appendChild(this.labelRenderer.domElement);
    },

    createPersonIntros() {
      this.introManager = new IntroManager(this.camera);
      
      // 修改IntroManager以支持关闭功能
      const originalShowIntro = this.introManager.showIntro.bind(this.introManager);
      const originalHideIntro = this.introManager.hideIntro.bind(this.introManager);
      
      this.introManager.showIntro = (intro) => {
        originalShowIntro(intro);
        this.activeIntro = intro;
        
        // 添加关闭按钮到文字元素
        if (intro.textElement && intro.textElement.element) {
          const element = intro.textElement.element;
          
          // 检查是否已有关闭按钮
          if (!element.querySelector('.intro-close-button')) {
            const closeBtn = document.createElement('div');
            closeBtn.className = 'intro-close-button';   // 统一类名
            closeBtn.innerHTML = '×';
            closeBtn.style.cssText = `
              position: absolute;
              top: 10px;
              right: 10px;
              width: 30px;
              height: 30px;
              background: rgba(139, 69, 19, 0.9);
              color: white;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 24px;
              cursor: pointer;
              z-index: 1000;
              pointer-events: auto;   /* 关键：允许点击 */
              transition: all 0.3s;
            `;
            closeBtn.addEventListener('click', (e) => {
              e.stopPropagation();     // 防止冒泡
              this.closeIntro();
            });
            closeBtn.addEventListener('mouseenter', () => {
              closeBtn.style.background = 'rgba(139, 69, 19, 1)';
              closeBtn.style.transform = 'scale(1.1)';
            });
            closeBtn.addEventListener('mouseleave', () => {
              closeBtn.style.background = 'rgba(139, 69, 19, 0.9)';
              closeBtn.style.transform = 'scale(1)';
            });
            element.style.position = 'relative';
            element.appendChild(closeBtn);
          }
        }
      };
      
      this.introManager.hideIntro = (intro) => {
        originalHideIntro(intro);
        if (this.activeIntro === intro) {
          this.activeIntro = null;
        }
      };

// 1. 白宾虹阶段（1865-1930 左右）
const introPanel = createPersonIntroduction(this.scene, {
  position: new THREE.Vector3(0, 5, 10),
  corridorLength: 80,
  corridorHeight: 12,
  panelWidth: 7,
  panelHeight: 8,
  content: {
    name: "白宾虹",
    lifespan: "1865-1930",
    title: "早期·白宾虹",
    honor: "“白宾虹”时期",
    detail: "以疏淡清逸的“白宾虹”面貌著称，宗法新安画派，用笔轻灵、设色淡雅，画面留白多，尚未形成后来浑厚华滋的风格。"
  }
});
this.introManager.addIntro(introPanel);

// 2. 黄宾虹阶段（1930-1948 左右）
const introPanel2 = createPersonIntroduction(this.scene, {
  position: new THREE.Vector3(0, 5, 40),
  corridorLength: 80,
  corridorHeight: 12,
  panelWidth: 7,
  panelHeight: 8,
  content: {
    name: "黄宾虹",
    lifespan: "1930-1948",
    title: "盛期·黄宾虹",
    honor: "“黄宾虹”时期",
    detail: "由“白”入“黄”，积墨、破墨、渍墨并用，画面趋于苍润浑厚；提出“五笔七墨”说，山水浑厚华滋，形成标志性的“黄宾虹调”。"
  }
});
this.introManager.addIntro(introPanel2);

// 3. 黑宾虹阶段（1948-1955 晚年）
const introPanel3 = createPersonIntroduction(this.scene, {
  position: new THREE.Vector3(0, 5, 80),
  corridorLength: 80,
  corridorHeight: 12,
  panelWidth: 7,
  panelHeight: 8,
  content: {
    name: "黑宾虹",
    lifespan: "1948-1955",
    title: "晚期·黑宾虹",
    honor: "“黑宾虹”时期",
    detail: "以浓墨、焦墨、宿墨层层积染，画面黑中透亮，所谓“黑团团里墨团团”；用笔更趋自由，形成“浑厚华滋、黑密厚重”的终极风格。"
  }
});
this.introManager.addIntro(introPanel3);
      
    },

createAdaptivePainting(texture, index, isLeft = true) {
  const image = texture.image;
  const imgWidth = image.width;
  const imgHeight = image.height;

  const maxWidth = 4.0;
  const maxHeight = 5.0;

  let width, height;
  const aspectRatio = imgWidth / imgHeight;

  if (aspectRatio > 1) {
    width = Math.min(maxWidth, imgWidth / 200);
    height = width / aspectRatio;
    if (height > maxHeight) {
      height = maxHeight;
      width = height * aspectRatio;
    }
  } else {
    height = Math.min(maxHeight, imgHeight / 200);
    width = height * aspectRatio;
    if (width > maxWidth) {
      width = maxWidth;
      height = width / aspectRatio;
    }
  }

  this.paintingSizes[index] = { width, height, aspectRatio };

  const paintingMaterial = new THREE.MeshStandardMaterial({
    map: texture,
    side: THREE.DoubleSide,
    roughness: 0.8,
    metalness: 0.2
  });

  const frameMaterial = new THREE.MeshStandardMaterial({
    color: 0x8b4513,
    roughness: 0.4,
    metalness: 0.6
  });

  const painting = new THREE.Mesh(
    new THREE.PlaneGeometry(width, height),
    paintingMaterial
  );

  const frame = new THREE.Mesh(
    new THREE.BoxGeometry(
      width + 0.12,
      height + 0.12,
      this.paintingParams.thickness
    ),
    frameMaterial
  );

  const posZ = this.paintingParams.spacing * (index + 1);

  if (isLeft) {
    painting.position.set(
      -this.corridorParams.width / 2 + 0.5,
      this.paintingParams.offsetY + height / 2,
      posZ
    );
    painting.rotation.y = Math.PI / 2;

    frame.position.copy(painting.position);
    frame.position.x = painting.position.x - this.paintingParams.thickness / 2 - 0.01;
    frame.rotation.y = painting.rotation.y;
  } else {
    painting.position.set(
      this.corridorParams.width / 2 - 0.5,
      this.paintingParams.offsetY + height / 2,
      posZ
    );
    painting.rotation.y = -Math.PI / 2;

    frame.position.copy(painting.position);
    frame.position.x = painting.position.x + this.paintingParams.thickness / 2 + 0.01;
    frame.rotation.y = painting.rotation.y;
  }

  painting.castShadow = true;
  painting.receiveShadow = true;
  frame.castShadow = true;

  // ✅ 关键：把 id 挂到 mesh 上
  painting.userData.id = this.paintingImages[index].id;

  this.scene.add(painting);
  this.scene.add(frame);

  return { painting, frame, width, height };
},
loadAndCreatePaintings() {
  const textureLoader = new THREE.TextureLoader();
  let loadCount = 0;
  const totalImages = this.paintingImages.length * 2; // ✅ 修复：原来是 .url.length

  this.paintingImages.forEach((image, index) => {
    const imagePath = image.url;
    textureLoader.load(
      imagePath,
      (texture) => {
        texture.anisotropy = this.renderer.capabilities.getMaxAnisotropy();
        this.createAdaptivePainting(texture, index, true);
        this.createAdaptivePainting(texture, index, false);
        loadCount += 2;

        if (loadCount === totalImages) {
          console.log('所有画作加载完成');
        }
      },
      undefined,
      (error) => {
        console.error('图片加载失败:', error);
      }
    );
  });
},
initInteraction() {
  const raycaster = new THREE.Raycaster();
  const pointer = new THREE.Vector2();

  const onPointerDown = (event) => {
    const x = event.clientX ?? event.touches?.[0]?.clientX;
    const y = event.clientY ?? event.touches?.[0]?.clientY;

    if (x === undefined || y === undefined) return;

    pointer.x = (x / window.innerWidth) * 2 - 1;
    pointer.y = -(y / window.innerHeight) * 2 + 1;

    raycaster.setFromCamera(pointer, this.camera);
    const intersects = raycaster.intersectObjects(this.scene.children, false);

    for (let i = 0; i < intersects.length; i++) {
      const obj = intersects[i].object;
      if (obj.userData.id !== undefined) {
        window.location.href = `/work?id=${obj.userData.id}`;
        break;
      }
    }
  };

  window.addEventListener('click', onPointerDown);
  window.addEventListener('touchstart', onPointerDown);
},
    initControls() {
      this.controls = new OrbitControls(this.camera, this.renderer.domElement);
      this.controls.enableDamping = true;
      this.controls.dampingFactor = 0.05;
      this.controls.enablePan = true;
      this.controls.enableZoom = false;
      this.controls.enableRotate = true;
      this.controls.minPolarAngle = Math.PI / 2;
      this.controls.maxPolarAngle = Math.PI / 2;
      this.controls.minAzimuthAngle = -Infinity;
      this.controls.maxAzimuthAngle = Infinity;
    },

    initListeners() {
      document.addEventListener('keydown', this.handleKeyDown);
      document.addEventListener('keyup', this.handleKeyUp);
      window.addEventListener('resize', this.handleResize);
    },

    handleKeyDown(e) {
      const key = e.key;
      if (Object.prototype.hasOwnProperty.call(this.keys, key)) {
        this.keys[key] = true;
      }
    },

    handleKeyUp(e) {
      const key = e.key;
      if (Object.prototype.hasOwnProperty.call(this.keys, key)) {
        this.keys[key] = false;
      }
    },

    handleResize() {
      this.camera.aspect = window.innerWidth / window.innerHeight;
      this.camera.updateProjectionMatrix();
      this.renderer.setSize(window.innerWidth, window.innerHeight);
      this.labelRenderer.setSize(window.innerWidth, window.innerHeight);
    },

    updateCameraPosition() {
      // 修改了这里
      let direction = new THREE.Vector3();
      this.camera.getWorldDirection(direction);
      var directionZ=direction.z;
      if (directionZ<0) {
        directionZ=-1;
      }else{
        directionZ=1;
      }
      if (this.keys.w || this.keys.ArrowUp) {
        this.camera.position.z += this.moveSpeed*directionZ;
        this.camera.position.z = Math.min(this.camera.position.z, this.corridorParams.length - 20);
      }
      
      if (this.keys.s || this.keys.ArrowDown) {
        this.camera.position.z -= this.moveSpeed*directionZ;
        this.camera.position.z = Math.max(this.camera.position.z, 5);
      }
      
      const lookDirection = new THREE.Vector3(0, 0, 1);
      lookDirection.applyQuaternion(this.camera.quaternion);
      this.controls.target.copy(this.camera.position).add(lookDirection.multiplyScalar(-5));
    },

    closeIntro() {
      if (this.activeIntro && this.introManager) {
        this.introManager.hideIntro(this.activeIntro);
        this.activeIntro = null;
      }
    },

    animate() {
      this.animationId = requestAnimationFrame(this.animate);
      this.updateCameraPosition();
      this.controls.update();
      this.renderer.render(this.scene, this.camera);
      this.labelRenderer.render(this.scene, this.camera);
      
      if (this.introManager) {
        this.introManager.update();
      }
    },

    cleanup() {
      if (this.animationId) {
        cancelAnimationFrame(this.animationId);
      }
      
      document.removeEventListener('keydown', this.handleKeyDown);
      document.removeEventListener('keyup', this.handleKeyUp);
      window.removeEventListener('resize', this.handleResize);
      
      if (this.renderer) {
        this.renderer.dispose();
      }
      
      const css2dElement = document.getElementById('css2d-renderer');
      if (css2dElement && css2dElement.parentNode) {
        css2dElement.parentNode.removeChild(css2dElement);
      }
    }
  }
};
</script>

<style scoped>
.corridor-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

/* 美化加载页面 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: black;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  opacity: 1;
  transition: opacity 0.5s ease;
}

.loading-overlay.fade-out {
  opacity: 0;
}

.loading-content {
  text-align: center;
  color: white;
  max-width: 500px;
  padding: 40px;
}

/* 加载动画 */
.loader {
  margin-bottom: 40px;
}

.loader-spinner {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  border-top-color: #FfF5F0;
  animation: spin 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loader-text {
  font-size: 20px;
  color: #FfF5F0;
  margin-bottom: 10px;
  font-weight: bold;
}

/* 进度条 */
.progress-bar {
  width: 300px;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  margin: 20px auto;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffd700, #ff8c00);
  border-radius: 2px;
  width: 0%;
  transition: width 0.3s ease;
}

/* 加载提示 */
.loading-tip {
  background: rgba(255, 255, 255, 0.1);
  padding: 20px;
  border-radius: 10px;
  margin-top: 30px;
  backdrop-filter: blur(10px);
}

.loading-tip p {
  margin: 10px 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
}

.loading-tip p:first-child {
  font-size: 24px;
  color: #FfF5F0;
  font-weight: bold;
  margin-bottom: 15px;
}

/* 全局关闭按钮 */
.intro-close-btn {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  background: rgba(139, 69, 19, 0.9);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  cursor: pointer;
  z-index: 999;
  transition: all 0.3s;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.intro-close-btn:hover {
  background: rgba(139, 69, 19, 1);
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.4);
}

/* 操作提示 */
.controls-hint {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 30px;
  background: rgba(0, 0, 0, 0.7);
  padding: 12px 24px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  z-index: 998;
}

.hint-item {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.hint-item::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #ffd700;
  border-radius: 50%;
}

#scene-container {
  width: 100%;
  height: 100%;
  outline: none;
}

/* 全局CSS用于CSS2D标签 */
:deep(.person-intro) {
  position: relative !important;
  pointer-events: auto !important;
}

:deep(.intro-close-button) {
  position: absolute !important;
  top: 10px !important;
  right: 10px !important;
  width: 30px !important;
  height: 30px !important;
  background: rgba(139, 69, 19, 0.8) !important;
  color: white !important;
  border-radius: 50% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  font-size: 24px !important;
  cursor: pointer !important;
  z-index: 1000 !important;
  transition: all 0.3s !important;
}

:deep(.intro-close-button:hover) {
  background: rgba(139, 69, 19, 1) !important;
  transform: scale(1.1) !important;
}
</style>