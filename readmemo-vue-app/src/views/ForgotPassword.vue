<template>
  <div class="forgot-password-page">
    <div class="container">
      <!-- 品牌 -->
      <div class="brand">
        <img src="@/assets/logo.png" alt="Logo" class="logo">
        <h1>重置密码</h1>
        <p v-if="!showVerification && !showResetForm">输入你的邮箱，我们将发送验证码</p>
        <p v-if="showVerification">请输入验证码验证邮箱</p>
        <p v-if="showResetForm">设置新密码</p>
      </div>

      <!-- 邮箱输入表单 -->
      <form @submit.prevent="handleSubmit" class="form" v-if="!showVerification && !showResetForm">
        <div class="input-group">
          <label for="email">邮箱地址</label>
          <input
            id="email"
            type="email"
            v-model="email"
            placeholder="your@email.com"
            required
            :class="{ error: error }"
          />
          <p v-if="error" class="error-message">{{ error }}</p>
        </div>

        <button type="submit" class="btn-primary btn-block" :disabled="loading">
          <span v-if="loading">发送中...</span>
          <span v-else>发送验证码</span>
        </button>

        <!-- 返回登录 -->
        <div class="back-link">
          <router-link to="/login">← 返回登录</router-link>
        </div>
      </form>

      <!-- 验证码输入 -->
      <div class="verification-form" v-if="showVerification && !showResetForm">
        <div class="success-icon">📧</div>
        <h2>验证邮箱</h2>
        <p>我们已向 <strong>{{ email }}</strong> 发送了验证码。</p>
        <p>请输入您收到的验证码：</p>
        
        <div class="input-group">
          <label for="verification-code">验证码</label>
          <input
            id="verification-code"
            type="text"
            v-model="verificationCode"
            placeholder="请输入验证码"
            maxlength="6"
            required
            :class="{ error: verificationError }"
          />
          <p v-if="verificationError" class="error-message">{{ verificationError }}</p>
        </div>
        
        <div class="actions">
          <button class="btn-primary" @click="verifyCode" :disabled="verifying">
            <span v-if="verifying">验证中...</span>
            <span v-else>验证</span>
          </button>
          <button class="btn-outline" @click="resendCode" :disabled="resendCooldown > 0">
            <span v-if="resendCooldown > 0">{{ resendCooldown }}秒后重发</span>
            <span v-else>重新发送</span>
          </button>
          <button class="btn-text" @click="resetForm">返回修改邮箱</button>
        </div>
        
        <p class="tip">没有收到验证码？请检查邮箱地址是否正确，或稍后重试。</p>
      </div>

      <!-- 重置密码表单 -->
      <form @submit.prevent="handleResetPassword" class="form" v-if="showResetForm">
        <div class="success-icon">✅</div>
        <h2>设置新密码</h2>
        <p>邮箱验证已通过，请设置您的新密码。</p>
        
        <div class="input-group">
          <label for="new-password">新密码</label>
          <input
            id="new-password"
            type="password"
            v-model="newPassword"
            placeholder="请输入新密码"
            required
            :class="{ error: resetError }"
          />
          <p class="input-hint">密码长度至少6位</p>
        </div>
        
        <div class="input-group">
          <label for="confirm-password">确认密码</label>
          <input
            id="confirm-password"
            type="password"
            v-model="confirmPassword"
            placeholder="请再次输入新密码"
            required
            :class="{ error: resetError }"
          />
        </div>
        
        <p v-if="resetError" class="error-message">{{ resetError }}</p>
        <p v-if="resetSuccess" class="success-message">{{ resetSuccess }}</p>
        
        <div class="actions">
          <button type="submit" class="btn-primary" :disabled="resetting">
            <span v-if="resetting">重置中...</span>
            <span v-else>重置密码</span>
          </button>
          <button type="button" class="btn-outline" @click="goToLogin">返回登录</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表单数据
const email = ref('')
const verificationCode = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const resetToken = ref('') // 存储验证成功后后端返回的token

// 状态控制
const error = ref('')
const verificationError = ref('')
const resetError = ref('')
const resetSuccess = ref('')
const loading = ref(false)
const verifying = ref(false)
const resetting = ref(false)
const showVerification = ref(false)
const showResetForm = ref(false)
const resendCooldown = ref(0)
let resendTimer = null

// 清理定时器
onUnmounted(() => {
  if (resendTimer) {
    clearInterval(resendTimer)
  }
})

// 发送验证码请求
const handleSubmit = async () => {
  if (!email.value) {
    error.value = '请输入邮箱地址'
    return
  }
  if (!/\S+@\S+\.\S+/.test(email.value)) {
    error.value = '邮箱格式不正确'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await fetch('http://localhost:8080/api/v1/auth/forgot-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: email.value
      })
    })
    
    const data = await response.json()
    
    if (response.ok && data.success) {
      showVerification.value = true
      // 开始重发倒计时
      startResendCooldown()
    } else {
      error.value = data.message || '发送失败，请重试'
    }
    
  } catch (err) {
    console.error('请求失败:', err)
    error.value = '网络请求失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

// 验证验证码
const verifyCode = async () => {
  if (!verificationCode.value) {
    verificationError.value = '请输入验证码'
    return
  }

  verifying.value = true
  verificationError.value = ''

  try {
    // 这里应该调用验证码验证接口
    // 假设验证成功后后端会返回一个重置token
    const response = await fetch('http://localhost:8080/api/v1/auth/verify-code', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: email.value,
        code: verificationCode.value
      })
    })
    
    const data = await response.json()
    
    if (response.ok && data.success) {
      // 保存重置token（根据您的后端实际返回字段调整）
      resetToken.value = data.token || verificationCode.value
      showVerification.value = false
      showResetForm.value = true
    } else {
      verificationError.value = data.message || '验证码错误，请重试'
    }
    
  } catch (err) {
    console.error('验证失败:', err)
    verificationError.value = '网络请求失败，请重试'
  } finally {
    verifying.value = false
  }
}

// 重置密码
const handleResetPassword = async () => {
  // 验证密码
  if (!newPassword.value || !confirmPassword.value) {
    resetError.value = '请输入新密码和确认密码'
    return
  }
  
  if (newPassword.value.length < 6) {
    resetError.value = '密码长度不能少于6位'
    return
  }
  
  if (newPassword.value !== confirmPassword.value) {
    resetError.value = '两次输入的密码不一致'
    return
  }
  
  resetting.value = true
  resetError.value = ''
  resetSuccess.value = ''

  try {
    const response = await fetch('http://localhost:8080/api/v1/auth/reset-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        token: resetToken.value,
        email: email.value,
        password: newPassword.value,
        password_confirmation: confirmPassword.value
      })
    })
    
    const data = await response.json()
    
    if (response.ok && data.success) {
      resetSuccess.value = data.message || '密码重置成功！'
      // 3秒后跳转到登录页
      setTimeout(() => {
        goToLogin()
      }, 3000)
    } else {
      resetError.value = data.message || '密码重置失败，请重试'
    }
    
  } catch (err) {
    console.error('重置密码失败:', err)
    resetError.value = '网络请求失败，请检查网络连接'
  } finally {
    resetting.value = false
  }
}

// 重新发送验证码
const resendCode = async () => {
  if (resendCooldown.value > 0) return
  
  loading.value = true
  try {
    const response = await fetch('http://localhost:8080/api/v1/auth/forgot-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email: email.value
      })
    })
    
    const data = await response.json()
    
    if (response.ok && data.success) {
      // 重新开始倒计时
      startResendCooldown()
      verificationError.value = '验证码已重新发送'
    } else {
      verificationError.value = data.message || '发送失败，请重试'
    }
    
  } catch (err) {
    console.error('重发验证码失败:', err)
    verificationError.value = '网络请求失败，请重试'
  } finally {
    loading.value = false
  }
}

// 开始重发倒计时
const startResendCooldown = () => {
  resendCooldown.value = 60
  resendTimer = setInterval(() => {
    resendCooldown.value--
    if (resendCooldown.value <= 0) {
      clearInterval(resendTimer)
    }
  }, 1000)
}

// 返回修改邮箱
const resetForm = () => {
  showVerification.value = false
  showResetForm.value = false
  verificationCode.value = ''
  verificationError.value = ''
  resetToken.value = ''
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, #FFF9F0 0%, #FFE8E8 100%);
}

.container {
  width: 100%;
  max-width: 500px;
  background-color: white;
  border-radius: var(--radius-large);
  padding: 3rem;
  box-shadow: var(--shadow-hard);
  border: 5px solid var(--color-accent);
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

.form, .verification-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.input-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: var(--color-text);
}

.input-group input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-medium);
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.input-group input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.input-group input.error {
  border-color: var(--color-error);
}

.input-hint {
  font-size: 0.85rem;
  color: var(--color-text-light);
  margin-top: 0.25rem;
}

.error-message {
  color: var(--color-error);
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.success-message {
  color: var(--color-success);
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: var(--radius-medium);
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--color-primary-dark);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-outline {
  background-color: transparent;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
  padding: 10px 22px;
  border-radius: var(--radius-medium);
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-outline:hover:not(:disabled) {
  background-color: var(--color-primary);
  color: white;
}

.btn-outline:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-text {
  background: none;
  border: none;
  color: var(--color-text-light);
  font-size: 0.9rem;
  cursor: pointer;
  padding: 8px;
}

.btn-text:hover {
  color: var(--color-primary);
  text-decoration: underline;
}

.btn-block {
  width: 100%;
  padding: 16px;
  font-size: 1.2rem;
}

.back-link {
  text-align: center;
  margin-top: 1.5rem;
}

.back-link a {
  color: var(--color-primary);
  font-weight: bold;
  font-size: 1.1rem;
  text-decoration: none;
}

.back-link a:hover {
  text-decoration: underline;
}

.success-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
  text-align: center;
  animation: bounce 2s infinite var(--transition-bounce);
}

.verification-form h2,
.form h2 {
  text-align: center;
  font-size: 2rem;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
}

.verification-form p,
.form p {
  text-align: center;
  color: var(--color-text);
  margin-bottom: 1rem;
  line-height: 1.6;
}

.verification-form strong {
  color: var(--color-primary);
}

.actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin: 2rem 0;
}

.tip {
  font-size: 0.9rem;
  color: var(--color-text-light);
  margin-top: 2rem;
  text-align: center;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@media (max-width: 768px) {
  .container {
    padding: 2rem;
  }
  
  .brand h1 {
    font-size: 2rem;
  }
  
  .actions {
    flex-direction: column;
  }
  
  .btn-primary,
  .btn-outline {
    width: 100%;
  }
}
</style>