<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="welcome-page">
    <!-- 背景动画元素 -->
    <div class="bg-shapes">
      <div class="shape shape-1 float"></div>
      <div class="shape shape-2 bounce"></div>
      <div class="shape shape-3 float" style="animation-delay: 1s"></div>
      <div class="shape shape-4 bounce" style="animation-delay: 0.5s"></div>
      <div class="shape shape-5 float" style="animation-delay: 2s"></div>
    </div>

    <div class="content">
      <!-- Logo -->
      <div class="logo-container">
        <img src="@/assets/logo.png" alt="阅记星 Logo" class="logo bounce">
        <h1 class="app-name">阅记星</h1>
        <p class="tagline">让阅读更有趣，让单词更好记</p>
      </div>

      <!-- 标语 -->
      <div class="slogan">
        <h2>你的智能阅读伙伴</h2>
        <p>OCR 即点即查 · 生词本 · 智能复习</p>
      </div>

      <!-- 动作按钮 -->
      <div class="actions">
        <button class="btn-primary btn-lg" @click="goToOnboarding">
          开始探索
        </button>
        <button class="btn-outline btn-lg" @click="skipToLogin">
          跳过引导
        </button>
      </div>

      <!-- 倒计时提示 -->
      <div class="countdown">
        <p>{{ countdown }} 秒后自动进入</p>
        <div class="progress-bar">
          <div class="progress" :style="{ width: progressWidth }"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const countdown = ref(3)
const progressWidth = ref('0%')
let timer = null

const goToOnboarding = () => {
  clearInterval(timer)
  router.push('/onboarding')
}

const skipToLogin = () => {
  clearInterval(timer)
  // 模拟已登录状态，直接跳转到书架
  localStorage.setItem('isAuthenticated', 'true')
  router.push('/bookshelf')
}

onMounted(() => {
  // 启动倒计时
  let seconds = 3
  const updateProgress = () => {
    countdown.value = seconds
    progressWidth.value = `${(3 - seconds) * 33.33}%`
    if (seconds <= 0) {
      goToOnboarding()
      return
    }
    seconds--
  }
  updateProgress()
  timer = setInterval(updateProgress, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.welcome-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #FFE8E8 0%, #E3F4FF 100%);
}

.bg-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 0;
}

.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.3;
}

.shape-1 {
  width: 200px;
  height: 200px;
  background-color: var(--color-primary);
  top: 10%;
  left: 5%;
}

.shape-2 {
  width: 150px;
  height: 150px;
  background-color: var(--color-secondary);
  bottom: 15%;
  right: 10%;
}

.shape-3 {
  width: 100px;
  height: 100px;
  background-color: var(--color-accent);
  top: 50%;
  left: 80%;
}

.shape-4 {
  width: 120px;
  height: 120px;
  background-color: var(--color-success);
  bottom: 40%;
  left: 15%;
}

.shape-5 {
  width: 80px;
  height: 80px;
  background-color: var(--color-info);
  top: 70%;
  right: 20%;
}

.content {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 800px;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 3rem;
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-hard);
  border: 5px solid var(--color-primary);
}

.logo-container {
  margin-bottom: 2rem;
}

.logo {
  width: 120px;
  height: 120px;
  margin-bottom: 1rem;
}

.app-name {
  font-size: 4rem;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
  font-family: var(--font-handwriting);
}

.tagline {
  font-size: 1.5rem;
  color: var(--color-text-light);
  margin-bottom: 2rem;
}

.slogan {
  margin-bottom: 3rem;
}

.slogan h2 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: var(--color-secondary);
}

.slogan p {
  font-size: 1.3rem;
  color: var(--color-text);
}

.actions {
  display: flex;
  gap: 2rem;
  justify-content: center;
  margin-bottom: 3rem;
}

.btn-lg {
  padding: 18px 36px;
  font-size: 1.3rem;
  border-radius: var(--radius-large);
}

.countdown {
  margin-top: 2rem;
}

.countdown p {
  margin-bottom: 0.5rem;
  color: var(--color-text-light);
}

.progress-bar {
  width: 300px;
  height: 12px;
  background-color: var(--color-secondary);
  border-radius: 6px;
  margin: 0 auto;
  overflow: hidden;
}

.progress {
  height: 100%;
  background-color: var(--color-primary);
  border-radius: 6px;
  transition: width 1s linear;
}

@media (max-width: 768px) {
  .content {
    padding: 2rem;
  }
  
  .app-name {
    font-size: 3rem;
  }
  
  .slogan h2 {
    font-size: 2rem;
  }
  
  .actions {
    flex-direction: column;
    gap: 1rem;
  }
  
  .progress-bar {
    width: 200px;
  }
}
</style>