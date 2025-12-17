<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="register-page">
    <div class="container">
      <!-- 品牌 -->
      <div class="brand">
        <img src="@/assets/logo.png" alt="Logo" class="logo">
        <h1>加入阅记星</h1>
        <p>开启你的智能阅读之旅</p>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleRegister" class="form">
        <div class="input-group">
          <label for="username">用户名</label>
          <input
            id="username"
            type="text"
            v-model="form.username"
            placeholder="输入用户名"
            required
            :class="{ error: errors.username }"
          />
          <p v-if="errors.username" class="error-message">{{ errors.username }}</p>
        </div>

        <div class="input-group">
          <label for="email">邮箱</label>
          <input
            id="email"
            type="email"
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
            placeholder="至少6位字符"
            required
            @input="updatePasswordStrength"
            :class="{ error: errors.password }"
          />
          <button type="button" class="toggle-password" @click="showPassword = !showPassword">
            {{ showPassword ? '🙈' : '👁️' }}
          </button>
          <!-- 密码强度指示器 -->
          <div class="password-strength">
            <div class="strength-bar" :style="{ width: strength.width }" :class="strength.class"></div>
            <span class="strength-text">{{ strength.text }}</span>
          </div>
          <p v-if="errors.password" class="error-message">{{ errors.password }}</p>
        </div>

        <div class="input-group">
          <label for="confirmPassword">确认密码</label>
          <input
            id="confirmPassword"
            :type="showConfirmPassword ? 'text' : 'password'"
            v-model="form.confirmPassword"
            placeholder="再次输入密码"
            required
            :class="{ error: errors.confirmPassword }"
          />
          <button type="button" class="toggle-password" @click="showConfirmPassword = !showConfirmPassword">
            {{ showConfirmPassword ? '🙈' : '👁️' }}
          </button>
          <p v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</p>
        </div>

        <div class="input-group checkbox-group">
          <label class="checkbox">
            <input type="checkbox" v-model="form.agreeTerms" required>
            <span>我已阅读并同意 <a href="#" @click.prevent>服务条款</a> 和 <a href="#" @click.prevent>隐私政策</a></span>
          </label>
          <p v-if="errors.agreeTerms" class="error-message">{{ errors.agreeTerms }}</p>
        </div>

        <button type="submit" class="btn-primary btn-block" :disabled="loading">
          <span v-if="loading">注册中...</span>
          <span v-else>注册</span>
        </button>

        <!-- 已有账号 -->
        <div class="login-link">
          <p>已有账户？ <router-link to="/login">去登录</router-link></p>
        </div>
      </form>
    </div>

    <!-- 模拟注册成功提示 -->
    <div v-if="message.show" class="message" :class="message.type">
      {{ message.text }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

// 表单数据
const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false
})

const errors = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeTerms: ''
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const loading = ref(false)

// 密码强度
const strength = reactive({
  width: '0%',
  class: 'weak',
  text: '密码强度'
})

// 消息提示
const message = ref({
  show: false,
  text: '',
  type: 'success'
})

// 计算密码强度
const updatePasswordStrength = () => {
  const pass = form.password
  let score = 0
  if (pass.length >= 6) score++
  if (pass.length >= 8) score++
  if (/[A-Z]/.test(pass)) score++
  if (/[0-9]/.test(pass)) score++
  if (/[^A-Za-z0-9]/.test(pass)) score++

  let width = '0%'
  let cls = 'weak'
  let text = '弱'

  if (score <= 1) {
    width = '25%'
    cls = 'weak'
    text = '弱'
  } else if (score <= 3) {
    width = '50%'
    cls = 'medium'
    text = '中'
  } else if (score <= 4) {
    width = '75%'
    cls = 'good'
    text = '强'
  } else {
    width = '100%'
    cls = 'excellent'
    text = '非常强'
  }

  strength.width = width
  strength.class = cls
  strength.text = text
}

// 表单验证
const validate = () => {
  let valid = true
  errors.username = ''
  errors.email = ''
  errors.password = ''
  errors.confirmPassword = ''
  errors.agreeTerms = ''

  if (!form.username) {
    errors.username = '请输入用户名'
    valid = false
  } else if (form.username.length < 3) {
    errors.username = '用户名至少3位'
    valid = false
  }

  if (!form.email) {
    errors.email = '请输入邮箱'
    valid = false
  } else if (!/\S+@\S+\.\S+/.test(form.email)) {
    errors.email = '邮箱格式不正确'
    valid = false
  }

  if (!form.password) {
    errors.password = '请输入密码'
    valid = false
  } else if (form.password.length < 6) {
    errors.password = '密码至少6位'
    valid = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    valid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次密码不一致'
    valid = false
  }

  if (!form.agreeTerms) {
    errors.agreeTerms = '请同意服务条款和隐私政策'
    valid = false
  }

  return valid
}

// 模拟注册改为真实 API 调用
const handleRegister = async () => {
  if (!validate()) return

  loading.value = true

  try {
    // 准备请求数据
    const registerData = {
      email: form.email,
      password: form.password,
      username: form.username,
      nickname: form.username // 如果没有昵称字段，使用用户名
    }

    // 发送注册请求到后端
    const response = await fetch('http://localhost:8080/api/v1/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(registerData)
    })

    // 解析响应数据
    const result = await response.json()

    // 根据响应状态处理
    if (response.ok && result.success) {
      // 注册成功
      message.value = {
        show: true,
        text: result.message || '注册成功！正在跳转到登录页...',
        type: 'success'
      }

      // 3秒后跳转到登录页
      setTimeout(() => {
        window.location.href = '/login'
      }, 3000)

    } else {
      // 注册失败
      message.value = {
        show: true,
        text: result.message || '注册失败，请稍后重试',
        type: 'error'
      }
    }

  } catch (error) {
    // 网络错误或其他异常
    console.error('注册请求失败:', error)
    message.value = {
      show: true,
      text: '网络连接失败，请检查网络后重试',
      type: 'error'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, #E3F4FF 0%, #FFF9F0 100%);
}

.container {
  width: 100%;
  max-width: 500px;
  background-color: white;
  border-radius: var(--radius-large);
  padding: 3rem;
  box-shadow: var(--shadow-hard);
  border: 5px solid var(--color-secondary);
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

.password-strength {
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.strength-bar {
  height: 8px;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.strength-bar.weak {
  background-color: var(--color-error);
}

.strength-bar.medium {
  background-color: var(--color-warning);
}

.strength-bar.good {
  background-color: var(--color-success);
}

.strength-bar.excellent {
  background-color: var(--color-info);
}

.strength-text {
  font-size: 0.9rem;
  font-weight: bold;
}

.checkbox-group {
  margin-top: 1rem;
}

.checkbox {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox input {
  width: auto;
  transform: scale(1.2);
  margin-top: 0.2rem;
}

.checkbox span {
  line-height: 1.4;
}

.checkbox a {
  color: var(--color-primary);
  text-decoration: underline;
}

.btn-block {
  width: 100%;
  padding: 16px;
  font-size: 1.2rem;
  margin-top: 1rem;
}

.login-link {
  text-align: center;
  margin-top: 2rem;
  color: var(--color-text-light);
}

.login-link a {
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
}
</style>