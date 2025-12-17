<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="onboarding-page">
    <div class="header">
      <h1>发现阅记星的强大功能</h1>
      <p>滑动了解如何让阅读变得更轻松有趣</p>
    </div>

    <!-- 幻灯片容器 -->
    <div class="slides-container">
      <div class="slides" :style="{ transform: `translateX(-${currentIndex * 100}%)` }">
        <div v-for="(slide, index) in slides" :key="index" class="slide">
          <div class="slide-content">
            <div class="illustration">
              <div class="icon" :class="slide.icon" :style="{ backgroundColor: slide.color }">
                {{ slide.emoji }}
              </div>
            </div>
            <h2>{{ slide.title }}</h2>
            <p class="description">{{ slide.description }}</p>
            <ul v-if="slide.features" class="features">
              <li v-for="(feature, i) in slide.features" :key="i">{{ feature }}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页指示器 -->
    <div class="pagination">
      <button
        v-for="(slide, index) in slides"
        :key="index"
        class="dot"
        :class="{ active: currentIndex === index }"
        @click="currentIndex = index"
        :aria-label="`转到第 ${index + 1} 页`"
      ></button>
    </div>

    <!-- 导航按钮 -->
    <div class="navigation">
      <button class="btn-outline" @click="skip" v-if="currentIndex !== slides.length - 1">
        跳过
      </button>
      <div class="nav-buttons">
        <button class="btn-secondary" @click="prev" :disabled="currentIndex === 0">
          上一步
        </button>
        <button class="btn-primary" @click="next">
          {{ currentIndex === slides.length - 1 ? '立即体验' : '下一步' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 模拟幻灯片数据
const slides = ref([
  {
    icon: 'icon-ocr',
    emoji: '📸',
    color: '#FF9E9E',
    title: '智能 OCR 识别',
    description: '拍照或上传文档，瞬间将图片文字转换为可编辑文本',
    features: [
      '支持 PDF、图片、扫描件',
      '高精度文字识别',
      '多语言支持'
    ]
  },
  {
    icon: 'icon-lookup',
    emoji: '🔍',
    color: '#A3E4D7',
    title: '即点即查',
    description: '阅读时点击任意单词，立即显示释义、发音和例句',
    features: [
      '内置权威词典',
      '真人发音',
      '语境例句'
    ]
  },
  {
    icon: 'icon-vocab',
    emoji: '📚',
    color: '#FFD166',
    title: '智能生词本',
    description: '自动收集生词，按记忆曲线智能安排复习',
    features: [
      '自动分类整理',
      '多维度复习模式',
      '进度可视化'
    ]
  },
  {
    icon: 'icon-review',
    emoji: '🧠',
    color: '#8AC926',
    title: '科学复习系统',
    description: '基于艾宾浩斯遗忘曲线，帮你牢固记忆每一个单词',
    features: [
      '自适应复习计划',
      '多种练习题型',
      '学习报告分析'
    ]
  }
])

const currentIndex = ref(0)

const next = () => {
  if (currentIndex.value < slides.value.length - 1) {
    currentIndex.value++
  } else {
    // 跳转到登录页
    router.push('/login')
  }
}

const prev = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

const skip = () => {
  router.push('/login')
}
</script>

<style scoped>
.onboarding-page {
  min-height: 100vh;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #E3F4FF 0%, #FFF9F0 100%);
}

.header {
  text-align: center;
  margin-bottom: 2rem;
}

.header h1 {
  font-size: 3rem;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
}

.header p {
  font-size: 1.2rem;
  color: var(--color-text-light);
}

.slides-container {
  width: 100%;
  max-width: 900px;
  overflow: hidden;
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-hard);
  background-color: white;
  border: 5px solid var(--color-secondary);
}

.slides {
  display: flex;
  transition: transform 0.5s var(--transition-bounce);
}

.slide {
  min-width: 100%;
  padding: 3rem;
}

.slide-content {
  text-align: center;
}

.illustration {
  margin-bottom: 2rem;
}

.icon {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 4rem;
  box-shadow: var(--shadow-medium);
  margin-bottom: 1rem;
}

.slide h2 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: var(--color-text);
}

.description {
  font-size: 1.3rem;
  color: var(--color-text-light);
  margin-bottom: 2rem;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.features {
  list-style: none;
  padding: 0;
  margin: 2rem auto;
  max-width: 400px;
  text-align: left;
}

.features li {
  padding: 0.5rem 0;
  font-size: 1.1rem;
  position: relative;
  padding-left: 2rem;
}

.features li:before {
  content: '✓';
  position: absolute;
  left: 0;
  color: var(--color-success);
  font-weight: bold;
}

.pagination {
  display: flex;
  gap: 1rem;
  margin: 2rem 0;
}

.dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: var(--color-secondary);
  border: none;
  padding: 0;
  cursor: pointer;
  transition: all 0.3s var(--transition-smooth);
}

.dot.active {
  background-color: var(--color-primary);
  transform: scale(1.3);
}

.navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  max-width: 900px;
  margin-top: 2rem;
}

.nav-buttons {
  display: flex;
  gap: 1rem;
}

@media (max-width: 768px) {
  .onboarding-page {
    padding: 1rem;
  }
  
  .header h1 {
    font-size: 2.2rem;
  }
  
  .slide {
    padding: 2rem;
  }
  
  .icon {
    width: 100px;
    height: 100px;
    font-size: 3rem;
  }
  
  .slide h2 {
    font-size: 2rem;
  }
  
  .description {
    font-size: 1.1rem;
  }
  
  .navigation {
    flex-direction: column;
    gap: 1rem;
  }
}
</style>