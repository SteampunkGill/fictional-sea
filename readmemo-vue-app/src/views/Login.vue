<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="login-page">
    <div class="container">
      <!-- Logo 和标题 -->
      <div class="brand">
        <img src="@/assets/logo.png" alt="Logo" class="logo">
        <h1>欢迎回来</h1>
        <p>登录你的阅记星账户，继续你的阅读之旅</p>
      </div>

      <!-- 加载状态提示 -->
      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <p>正在登录中...</p>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleLogin" class="form">
        <div class="input-group">
          <label for="email">邮箱或用户名</label>
          <input
            id="email"
            type="text"
            v-model="form.email"
            placeholder="your@email.com"
            required
            :class="{ error: errors.email }"
          />
          <p v-if="errors.email" class="error-message">{{ errors.email }}</p>
        </div>

        <div class="input-group">
          <label for="password">密码</label>
          <input
            id="password"
            :type="showPassword ? 'text' : 'password'"
            v-model="form.password"
            placeholder="输入密码"
            required
            :class="{ error: errors.password }"
          />
          <button type="button" class="toggle-password" @click="showPassword = !showPassword">
            {{ showPassword ? '🙈' : '👁️' }}
          </button>
          <p v-if="errors.password" class="error-message">{{ errors.password }}</p>
        </div>

        <div class="options">
          <label class="checkbox">
            <input type="checkbox" v-model="form.remember">
            <span>记住我</span>
          </label>
          <router-link to="/forgot-password" class="link">忘记密码？</router-link>
        </div>

        <button type="submit" class="btn-primary btn-block" :disabled="loading">
          <span v-if="loading">登录中...</span>
          <span v-else>登录</span>
        </button>

        <!-- 第三方登录 -->
        <div class="social-login">
          <p class="divider">或使用第三方账号登录</p>
          <div class="social-buttons">
            <button type="button" class="social-btn google" @click="socialLogin('google')">
              <span class="icon">G</span> Google
            </button>
            <button type="button" class="social-btn wechat" @click="socialLogin('wechat')">
              <span class="icon">W</span> 微信
            </button>
            <button type="button" class="social-btn github" @click="socialLogin('github')">
              <span class="icon">G</span> GitHub
            </button>
          </div>
        </div>

        <!-- 注册链接 -->
        <div class="register-link">
          <p>还没有账户？ <router-link to="/register">注册新账号</router-link></p>
        </div>
      </form>
    </div>

    <!-- 登录成功/失败提示 -->
    <div v-if="message.show" class="message" :class="message.type">
      {{ message.text }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表单数据
const form = reactive({
  email: '',
  password: '',
  remember: false
})

const errors = reactive({
  email: '',
  password: ''
})

const showPassword = ref(false)
const loading = ref(false)

// 消息提示
const message = ref({
  show: false,
  text: '',
  type: 'success' // success, error, info
})

// 表单验证
const validate = () => {
  let valid = true
  errors.email = ''
  errors.password = ''

  if (!form.email) {
    errors.email = '请输入邮箱或用户名'
    valid = false
  } else if (!/\S+@\S+\.\S+/.test(form.email) && form.email.length < 3) {
    errors.email = '请输入有效的邮箱或用户名'
    valid = false
  }

  if (!form.password) {
    errors.password = '请输入密码'
    valid = false
  } else if (form.password.length < 6) {
    errors.password = '密码至少6位'
    valid = false
  }

  return valid
}

// 存储token到本地存储
const storeTokens = (accessToken, refreshToken, expiresIn) => {
  // 根据用户选择决定存储方式
  const storage = form.remember ? localStorage : sessionStorage
  
  storage.setItem('token', accessToken)
  storage.setItem('refreshToken', refreshToken)
  storage.setItem('expiresIn', expiresIn)
  storage.setItem('isAuthenticated', 'true')
  
  // 同时也在localStorage备份一份，用于跨页面访问
  localStorage.setItem('token', accessToken)
}

// 模拟登录（失败时回退使用）
const mockLogin = () => {
  // 模拟成功或失败（随机）
  const success = Math.random() > 0.3
  if (success) {
    message.value = {
      show: true,
      text: '登录成功！正在跳转...',
      type: 'success'
    }
    
    // 存储模拟token
    storeTokens('mock_access_token', 'mock_refresh_token', 3600)
    
    // 跳转到书架
    setTimeout(() => {
      router.push('/bookshelf')
    }, 1500)
  } else {
    message.value = {
      show: true,
      text: '登录失败：邮箱或密码错误',
      type: 'error'
    }
  }

  // 隐藏消息
  setTimeout(() => {
    message.value.show = false
  }, 3000)
}

// 真实登录请求
const handleLogin = async () => {
  if (!validate()) return

  loading.value = true
  message.value.show = false

  try {
    // 发送 API 请求到后端
    const response = await fetch('http://localhost:8080/api/v1/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: form.email,
        password: form.password
      })
    })

    const data = await response.json()

    if (data.success) {
      // 请求成功，使用后端返回的真实数据
      const loginData = data.data
      
      message.value = {
        show: true,
        text: data.message || '登录成功！正在跳转...',
        type: 'success'
      }
      
      // 存储 token 到本地存储
      storeTokens(
        loginData.accessToken,
        loginData.refreshToken,
        loginData.expiresIn
      )
      
      // 跳转到书架
      setTimeout(() => {
        router.push('/bookshelf')
      }, 1500)
    } else {
      // 后端返回失败，显示错误信息
      message.value = {
        show: true,
        text: data.message || '登录失败，请检查邮箱和密码',
        type: 'error'
      }
      
      // 如果后端返回特定错误码，可以在这里处理
      if (data.message && data.message.includes('邮箱/用户名或密码错误')) {
        errors.email = '邮箱或密码错误'
        errors.password = '邮箱或密码错误'
      }
    }
  } catch (error) {
    console.error('登录请求失败:', error)
    
    // 网络错误或其他错误，使用模拟数据
    message.value = {
      show: true,
      text: '网络错误，使用模拟数据登录',
      type: 'info'
    }
    
    // 回退到模拟登录
    mockLogin()
  } finally {
    loading.value = false
  }
}

// 第三方登录模拟
const socialLogin = (provider) => {
  message.value = {
    show: true,
    text: `正在通过 ${provider} 登录...`,
    type: 'info'
  }
  
  // 模拟第三方登录过程
  setTimeout(() => {
    // 存储模拟token
    storeTokens(
      `${provider}_access_token`,
      `${provider}_refresh_token`,
      3600
    )
    
    message.value = {
      show: true,
      text: `${provider}登录成功！正在跳转...`,
      type: 'success'
    }
    
    setTimeout(() => {
      router.push('/bookshelf')
    }, 1000)
  }, 1500)
}

// 自动填充测试数据（开发环境使用）
const fillTestData = () => {
  // 只在开发环境填充测试数据
  if (process.env.NODE_ENV === 'development') {
    form.email = 'test@example.com'
    form.password = 'password123'
  }
}

// 页面加载时填充测试数据
fillTestData()
</script>
<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, #FFE8E8 0%, #E3F4FF 100%);
}

.container {
  width: 100%;
  max-width: 500px;
  background-color: white;
  border-radius: var(--radius-large);
  padding: 3rem;
  box-shadow: var(--shadow-hard);
  border: 5px solid var(--color-primary);
}

.brand {
  text-align: center;
  margin-bottom: 2rem;
}

.logo {
  width: 80px;
  height: 80px;
  margin-bottom: 1rem;
}

.brand h1 {
  font-size: 2.5rem;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
}

.brand p {
  color: var(--color-text-light);
  font-size: 1.1rem;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.input-group {
  position: relative;
}

.input-group label {
  display: block;
  margin-bottom: 0.5rem;
}

.input-group input {
  padding-right: 50px;
}

.input-group input.error {
  border-color: var(--color-error);
}

.error-message {
  color: var(--color-error);
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.toggle-password {
  position: absolute;
  right: 12px;
  top: 42px;
  background: transparent;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox input {
  width: auto;
  transform: scale(1.2);
}

.link {
  color: var(--color-primary);
  font-weight: bold;
}

.btn-block {
  width: 100%;
  padding: 16px;
  font-size: 1.2rem;
}

.social-login {
  margin-top: 1rem;
}

.divider {
  text-align: center;
  position: relative;
  margin: 1.5rem 0;
  color: var(--color-text-light);
}

.divider:before,
.divider:after {
  content: '';
  position: absolute;
  top: 50%;
  width: 45%;
  height: 2px;
  background-color: var(--color-secondary);
}

.divider:before {
  left: 0;
}

.divider:after {
  right: 0;
}

.social-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.social-btn {
  flex: 1;
  padding: 12px;
  border-radius: var(--radius-medium);
  border: 3px solid;
  background-color: white;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.3s var(--transition-bounce);
}

.social-btn.google {
  border-color: #DB4437;
  color: #DB4437;
}

.social-btn.google:hover {
  background-color: #DB4437;
  color: white;
}

.social-btn.wechat {
  border-color: #09BB07;
  color: #09BB07;
}

.social-btn.wechat:hover {
  background-color: #09BB07;
  color: white;
}

.social-btn.github {
  border-color: #333;
  color: #333;
}

.social-btn.github:hover {
  background-color: #333;
  color: white;
}

.social-btn .icon {
  font-weight: bold;
  font-size: 1.2rem;
}

.register-link {
  text-align: center;
  margin-top: 2rem;
  color: var(--color-text-light);
}

.register-link a {
  font-weight: bold;
}

.message {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  padding: 1rem 2rem;
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-hard);
  z-index: 1000;
  animation: slideUp 0.5s var(--transition-bounce);
}

.message.success {
  background-color: var(--color-success);
  color: white;
  border: 3px solid #6daa2c;
}

.message.error {
  background-color: var(--color-error);
  color: white;
  border: 3px solid #cc474a;
}

.message.info {
  background-color: var(--color-info);
  color: white;
  border: 3px solid #0a6ebd;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

@media (max-width: 768px) {
  .container {
    padding: 2rem;
  }
  
  .brand h1 {
    font-size: 2rem;
  }
  
  .social-buttons {
    flex-direction: column;
  }
}
</style>