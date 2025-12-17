<template>
  <div id="app">
    <!-- 全局通知组件（用于模拟通知） -->
    <div v-if="notification.show" class="notification" :class="notification.type">
      <span>{{ notification.message }}</span>
      <button @click="notification.show = false" class="btn-close">×</button>
    </div>

    <!-- 顶部导航栏 -->
    <nav v-if="showNav" class="global-nav">
      <div class="nav-container">
        <router-link to="/" class="nav-logo">📚 阅记星</router-link>
        <div class="nav-links">
          <router-link to="/welcome">欢迎</router-link>
          <router-link to="/onboarding">引导</router-link>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
          <router-link to="/bookshelf">书架</router-link>
          <router-link to="/upload">上传</router-link>
          <router-link to="/reader">阅读器</router-link>
          <router-link to="/vocabulary">生词本</router-link>
          <router-link to="/review">复习</router-link>
          <router-link to="/user">用户中心</router-link>
          <router-link to="/settings">设置</router-link>
        </div>
      </div>
    </nav>

    <!-- 路由视图 -->
    <router-view />
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 模拟全局通知状态
const notification = ref({
  show: false,
  message: '',
  type: 'info' // info, success, warning, error
})

// 模拟显示通知的函数
const showNotification = (message, type = 'info') => {
  notification.value = { show: true, message, type }
  setTimeout(() => {
    notification.value.show = false
  }, 3000)
}

// 暴露给子组件（通过 provide/inject 或全局属性，这里简化）
window.$notify = showNotification


</script>

<style scoped>
#app {
  min-height: 100vh;
  background-color: var(--color-background);
}

.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  padding: 16px 24px;
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-hard);
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 300px;
  max-width: 400px;
  animation: slideIn 0.5s var(--transition-bounce);
  border: 3px solid;
}

.notification.info {
  background-color: var(--color-info);
  color: white;
  border-color: #0a6ebd;
}

.notification.success {
  background-color: var(--color-success);
  color: white;
  border-color: #6daa2c;
}

.notification.warning {
  background-color: var(--color-warning);
  color: var(--color-text);
  border-color: #e6b400;
}

.notification.error {
  background-color: var(--color-error);
  color: white;
  border-color: #cc474a;
}

.btn-close {
  background: transparent;
  color: inherit;
  border: none;
  font-size: 1.5rem;
  padding: 0;
  margin-left: 12px;
  cursor: pointer;
  transition: transform 0.2s;
}

.btn-close:hover {
  transform: scale(1.2);
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
