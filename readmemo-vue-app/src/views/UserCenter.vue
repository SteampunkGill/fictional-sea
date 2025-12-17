<template>
  <div class="user-center-layout">
    <div class="user-header">
      <h1 class="header-title">用户中心</h1>
      <div class="user-info">
        <img :src="userProfile.avatar" alt="用户头像" class="user-avatar" />
        <div class="user-details">
          <div class="user-name">{{ userProfile.nickname }}</div>
          <div class="user-email">{{ userProfile.email }}</div>
        </div>
      </div>
    </div>

    <div class="user-content">
      <aside class="user-nav">
        <div class="nav-title">导航菜单</div>
        <div class="nav-list">
          <div class="nav-item">
            <div class="nav-link" @click="showPage('dashboard')" :class="{ active: activePage === 'dashboard' }">
              📊 学习概览
            </div>
          </div>
          <div class="nav-item">
            <div class="nav-link" @click="showPage('profile')" :class="{ active: activePage === 'profile' }">
              👤 个人资料
            </div>
          </div>
          <div class="nav-item">
            <div class="nav-link" @click="showPage('security')" :class="{ active: activePage === 'security' }">
              🔒 账号安全
            </div>
          </div>
          <div class="nav-item">
            <div class="nav-link" @click="showPage('subscription')" :class="{ active: activePage === 'subscription' }">
              💎 订阅管理
            </div>
          </div>
          <div class="nav-item">
            <div class="nav-link" @click="showPage('notifications')" :class="{ active: activePage === 'notifications' }">
              🔔 通知设置
            </div>
          </div>
          <div class="nav-item">
            <div class="nav-link" @click="showPage('stats')" :class="{ active: activePage === 'stats' }">
              📈 学习统计
            </div>
          </div>
          <div class="nav-item">
            <div class="nav-link" @click="showPage('badges')" :class="{ active: activePage === 'badges' }">
              🏆 成就徽章
            </div>
          </div>
          <div class="nav-item">
            <div class="nav-link" @click="showPage('help')" :class="{ active: activePage === 'help' }">
              ❓ 帮助与反馈
            </div>
          </div>
          <div class="nav-item">
            <div class="nav-link" @click="showPage('about')" :class="{ active: activePage === 'about' }">
              ℹ️ 关于我们
            </div>
          </div>
        </div>
      </aside>

      <main class="user-main">
        <!-- 学习概览 -->
        <div v-if="activePage === 'dashboard'" class="user-page">
          <div class="page-title">学习数据摘要</div>
          
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon">📚</div>
              <div class="stat-value">{{ dashboardStats.documentsRead }}</div>
              <div class="stat-label">本周阅读文档</div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">⏱️</div>
              <div class="stat-value">{{ dashboardStats.readingHours }}</div>
              <div class="stat-label">本周阅读时长(小时)</div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📝</div>
              <div class="stat-value">{{ dashboardStats.vocabularyCount }}</div>
              <div class="stat-label">生词总数</div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📈</div>
              <div class="stat-value">{{ dashboardStats.vocabularyGrowth }}</div>
              <div class="stat-label">词汇量增长</div>
            </div>
          </div>

          <div class="recent-activity">
            <div class="section-title">最近活动</div>
            <div class="activity-list">
              <div v-for="(activity, index) in recentActivities" :key="index" class="activity-item">
                <div class="activity-icon">•</div>
                <div class="activity-text">{{ activity }}</div>
              </div>
            </div>
          </div>

          <div class="quick-actions">
            <div class="section-title">快速操作</div>
            <div class="action-buttons">
              <button class="btn btn-primary" @click="goToBookshelf">
                <span class="btn-icon">📖</span> 继续阅读
              </button>
              <button class="btn btn-secondary" @click="goToReview">
                <span class="btn-icon">🔄</span> 开始复习
              </button>
              <button class="btn btn-secondary" @click="goToUpload">
                <span class="btn-icon">📤</span> 上传文档
              </button>
              <button class="btn btn-secondary" @click="showPage('profile')">
                <span class="btn-icon">⚙️</span> 个人设置
              </button>
            </div>
          </div>
        </div>

        <!-- 个人资料 -->
        <div v-if="activePage === 'profile'" class="user-page">
          <div class="page-title">个人资料</div>
          <div class="profile-header">
            <img :src="userProfile.avatar" alt="Avatar" class="profile-avatar">
            <div class="profile-name-input">
              <input type="text" v-model="userProfile.nickname" placeholder="请输入昵称">
            </div>
          </div>
          <div class="form-group">
            <div class="form-label">电子邮箱</div>
            <input type="email" :value="userProfile.email" readonly>
          </div>
          <div class="form-group">
            <div class="form-label">个人简介</div>
            <textarea v-model="userProfile.bio" placeholder="介绍一下自己吧..."></textarea>
          </div>
          <button class="btn btn-primary" @click="saveProfile">保存修改</button>
        </div>

        <!-- 账户安全 -->
        <div v-if="activePage === 'security'" class="user-page">
          <div class="page-title">账户安全</div>
          <div class="section-title">修改密码</div>
          <div class="form-group">
            <div class="form-label">旧密码</div>
            <input type="password" v-model="passwords.old" placeholder="请输入当前密码">
          </div>
          <div class="form-group">
            <div class="form-label">新密码</div>
            <input type="password" v-model="passwords.new" placeholder="请输入新密码">
          </div>
          <div class="form-group">
            <div class="form-label">确认密码</div>
            <input type="password" v-model="passwords.confirm" placeholder="请再次输入新密码">
          </div>
          <button class="btn btn-primary" @click="updatePassword">更新密码</button>
          
          <div class="section-title" style="margin-top: 40px;">登录设备管理</div>
          <div class="device-list">
            <div v-for="(device, index) in loginDevices" :key="index" class="device-item">
              <div class="device-info">
                <div class="device-name">{{ device.device }}</div>
                <div class="device-details">{{ device.location }} - {{ device.time }}</div>
              </div>
              <button class="btn btn-secondary" @click="logoutDevice(index)">下线</button>
            </div>
          </div>
        </div>

        <!-- 订阅管理 -->
        <div v-if="activePage === 'subscription'" class="user-page">
          <div class="page-title">订阅管理</div>
          <div class="subscription-card">
            <div class="plan-name">{{ subscription.plan }}</div>
            <div class="plan-detail">到期日期: <span>{{ subscription.expiry }}</span></div>
            <div class="plan-detail">价格: <span>{{ subscription.price }}</span>/月</div>
            <div class="plan-actions">
              <button class="btn btn-primary" @click="switchPlan">切换计划</button>
              <button class="btn btn-danger" @click="cancelSubscription">取消订阅</button>
            </div>
          </div>
        </div>

        <!-- 通知设置 -->
        <div v-if="activePage === 'notifications'" class="user-page">
          <div class="page-title">通知设置</div>
          <div class="notification-list">
            <div class="notification-item">
              <div class="notification-label">邮件通知</div>
              <label class="switch">
                <input type="checkbox" v-model="notificationSettings.email">
                <span class="slider"></span>
              </label>
            </div>
            <div class="notification-item">
              <div class="notification-label">系统推送</div>
              <label class="switch">
                <input type="checkbox" v-model="notificationSettings.push">
                <span class="slider"></span>
              </label>
            </div>
            <div class="notification-item">
              <div class="notification-label">活动提醒</div>
              <label class="switch">
                <input type="checkbox" v-model="notificationSettings.activity">
                <span class="slider"></span>
              </label>
            </div>
          </div>
        </div>

        <!-- 学习统计 -->
        <div v-if="activePage === 'stats'" class="user-page">
          <div class="page-title">学习统计</div>
          <div class="stats-grid">
            <div v-for="(stat, index) in learningStats" :key="index" class="stat-card">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </div>

        <!-- 成就徽章 -->
        <div v-if="activePage === 'badges'" class="user-page">
          <div class="page-title">成就徽章</div>
          <div class="badge-grid">
            <div v-for="(badge, index) in achievementBadges" :key="index" 
                 :class="['badge-card', { locked: !badge.acquired }]">
              <img :src="badge.img" :alt="badge.name">
              <div class="badge-name">{{ badge.name }}</div>
            </div>
          </div>
        </div>

        <!-- 帮助与反馈 -->
        <div v-if="activePage === 'help'" class="user-page">
          <div class="page-title">帮助与反馈</div>
          <div class="feedback-form">
            <div class="form-group">
              <div class="form-label">问题类型</div>
              <select v-model="feedback.type">
                <option>功能建议</option>
                <option>Bug反馈</option>
                <option>内容错误</option>
                <option>其他</option>
              </select>
            </div>
            <div class="form-group">
              <div class="form-label">反馈内容</div>
              <textarea v-model="feedback.content" placeholder="请详细描述您的问题或建议..." required></textarea>
            </div>
            <button class="btn btn-primary" @click="submitFeedback">提交反馈</button>
          </div>
        </div>

        <!-- 关于我们 -->
        <div v-if="activePage === 'about'" class="user-page">
          <div class="page-title">关于我们</div>
          <div class="about-section">
            <img src="@/assets/logo.png" alt="App Logo" class="app-logo">
            <div class="app-name">ReadMemo</div>
            <div class="app-version">版本 V1.0.0</div>
            <div class="app-description">
              这是一款致力于帮助用户高效阅读和记忆的应用程序。通过智能文档解析和个性化学习计划，帮助您更好地掌握知识。
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const activePage = ref('dashboard');

const showPage = (page) => {
  activePage.value = page;
};

// 用户资料
const userProfile = ref({
  nickname: '小明同学',
  email: 'xiaoming@example.com',
  bio: '一名热爱阅读和编程的前端开发者。',
  avatar: 'https://i.pravatar.cc/150?u=a042581f4e29026704d'
});

const saveProfile = () => {
  alert('个人资料已保存');
};

// 学习概览数据
const dashboardStats = ref({
  documentsRead: 12,
  readingHours: 8.5,
  vocabularyCount: 42,
  vocabularyGrowth: '+15%'
});

const recentActivities = ref([
  '刚刚添加了单词 "serendipity" 到生词本',
  '2小时前完成了《傲慢与偏见》第3章阅读',
  '昨天复习了20个单词',
  '3天前上传了文档 "经济学原理.pdf"'
]);

// 账户安全
const passwords = ref({ old: '', new: '', confirm: '' });
const loginDevices = ref([
  { device: 'Chrome on Windows', location: '上海', time: '2025-12-16 10:30' },
  { device: 'iPhone 15 Pro', location: '北京', time: '2025-12-15 20:05' },
  { device: 'Safari on MacBook Pro', location: '上海', time: '2025-12-14 11:12' }
]);

const updatePassword = () => {
  passwords.value = { old: '', new: '', confirm: '' };
  alert('密码更新成功');
};

const logoutDevice = (index) => {
  loginDevices.value.splice(index, 1);
  alert('设备已下线');
};

// 订阅管理
const subscription = ref({
  plan: '高级会员',
  expiry: '2026-12-31',
  price: '¥25'
});

const switchPlan = () => alert('切换计划功能暂未开放。');
const cancelSubscription = () => alert('您已取消订阅。');

// 通知设置
const notificationSettings = ref({
  email: true,
  push: true,
  activity: false
});

// 学习统计
const learningStats = ref([
  { label: '累计学习时长', value: '128 小时' },
  { label: '完成课程数', value: '32 门' },
  { label: '连续学习天数', value: '78 天' },
  { label: '阅读文档数', value: '156 篇' }
]);

// 成就徽章
const achievementBadges = ref([
  { name: '初学者', img: 'https://img.icons8.com/color/96/000000/laurel-wreath.png', acquired: true },
  { name: '阅读达人', img: 'https://img.icons8.com/color/96/000000/medal2.png', acquired: true },
  { name: '学霸', img: 'https://img.icons8.com/color/96/000000/trophy.png', acquired: true },
  { name: '评论家', img: 'https://img.icons8.com/color/96/000000/filled-star.png', acquired: true },
  { name: '夜猫子', img: 'https://img.icons8.com/color/96/000000/crescent-moon.png', acquired: false },
  { name: '全勤奖', img: 'https://img.icons8.com/color/96/000000/calendar-plus.png', acquired: false }
]);

// 帮助与反馈
const feedback = ref({ type: '功能建议', content: '' });
const submitFeedback = () => {
  feedback.value = { type: '功能建议', content: '' };
  alert('反馈已提交，感谢您的支持！');
};

// 导航函数
const goToBookshelf = () => router.push('/bookshelf');
const goToReview = () => router.push('/review');
const goToUpload = () => router.push('/upload');
</script>

<style scoped>
/* 定义CSS变量，方便统一管理颜色和圆角 */
:root {
  --color-primary: #007bff;
  --color-secondary: #6c757d;
  --color-accent: #17a2b8;
  --color-danger: #dc3545;
  --color-background: #f8f9fa;
  --color-text: #343a40;
  --color-text-light: #6c757d;
  --radius-medium: 0.375rem;
  --radius-large: 0.75rem;
}

.user-center-layout {
  font-family: 'Arial', sans-serif;
  background-color: var(--color-background);
  min-height: 100vh;
  padding: 2rem;
  display: flex;
  flex-direction: column;
}

.user-header {
  background-color: white;
  padding: 2rem;
  border-radius: var(--radius-large);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.header-title {
  font-size: 2.5rem;
  color: var(--color-primary);
  margin-bottom: 1.5rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--color-primary);
}

.user-details {
  text-align: left;
}

.user-name {
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--color-text);
}

.user-email {
  font-size: 1.2rem;
  color: var(--color-text-light);
}

.user-content {
  display: flex;
  gap: 2rem;
  flex-grow: 1;
}

.user-nav {
  width: 250px;
  background-color: white;
  border-radius: var(--radius-large);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
}

.nav-title {
  font-size: 1.4rem;
  font-weight: bold;
  color: var(--color-primary);
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid var(--color-secondary);
}

.nav-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex-grow: 1;
}

.nav-item {
  margin-bottom: 0.5rem;
}

.nav-link {
  padding: 12px 15px;
  border-radius: var(--radius-medium);
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1.1rem;
  color: var(--color-text-light);
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.nav-link:hover {
  background-color: #e9ecef;
  color: var(--color-primary);
}

.nav-link.active {
  background-color: var(--color-primary);
  color: white;
  font-weight: bold;
}

.user-main {
  flex: 1;
  background-color: white;
  border-radius: var(--radius-large);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 2.5rem;
}

.user-page {
  display: flex;
  flex-direction: column;
}

.page-title {
  font-size: 2rem;
  font-weight: bold;
  color: var(--color-primary);
  margin-bottom: 2rem;
  padding-bottom: 10px;
  border-bottom: 3px solid var(--color-secondary);
}

.section-title {
  font-size: 1.6rem;
  font-weight: bold;
  color: var(--color-text);
  margin-bottom: 1.5rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2.5rem;
}

.stat-card {
  background-color: var(--color-background);
  padding: 1.5rem;
  border-radius: var(--radius-large);
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  font-size: 2.5rem;
  margin-bottom: 0.8rem;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
}

.stat-label {
  font-size: 1.1rem;
  color: var(--color-text-light);
}

.recent-activity {
  margin-bottom: 2.5rem;
}

.activity-list {
  list-style: none;
  padding: 0;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 0.8rem 0;
  border-bottom: 1px solid #eee;
  font-size: 1.1rem;
  color: var(--color-text);
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  margin-right: 0.8rem;
  color: var(--color-accent);
  font-size: 1.2rem;
}

.quick-actions {
  margin-bottom: 2.5rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.btn {
  padding: 12px 25px;
  border-radius: var(--radius-medium);
  font-weight: bold;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 0.7rem;
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
}

.btn-primary:hover {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #f8f9fa;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}

.btn-secondary:hover {
  background-color: var(--color-primary);
  color: white;
}

.btn-danger {
  background-color: var(--color-danger);
  color: white;
}

.btn-danger:hover {
  background-color: #c82333;
}

.btn-icon {
  font-size: 1.3rem;
}

/* Profile Page */
.profile-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.profile-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--color-primary);
}

.profile-name-input input {
  font-size: 1.8rem;
  font-weight: bold;
  border: none;
  border-bottom: 2px solid var(--color-secondary);
  padding-bottom: 5px;
  width: 300px;
  outline: none;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-label {
  font-size: 1.2rem;
  color: var(--color-text);
  margin-bottom: 0.8rem;
  font-weight: bold;
}

.form-group input[type="email"],
.form-group input[type="password"],
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 12px 15px;
  border-radius: var(--radius-medium);
  border: 2px solid var(--color-secondary);
  font-size: 1.1rem;
  outline: none;
  box-sizing: border-box; /* Ensures padding doesn't affect width */
}

.form-group input[readonly] {
  background-color: #e9ecef;
  color: var(--color-text-light);
}

.form-group textarea {
  min-height: 120px;
  resize: vertical;
}

/* Security Page */
.device-list {
  margin-top: 1.5rem;
  border: 2px solid var(--color-secondary);
  border-radius: var(--radius-large);
  padding: 1.5rem;
}

.device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid #eee;
}

.device-item:last-child {
  border-bottom: none;
}

.device-name {
  font-size: 1.2rem;
  font-weight: bold;
  color: var(--color-text);
}

.device-details {
  font-size: 1rem;
  color: var(--color-text-light);
}

/* Subscription Page */
.subscription-card {
  background-color: var(--color-primary);
  color: white;
  padding: 2rem;
  border-radius: var(--radius-large);
  text-align: center;
  box-shadow: 0 4px 15px rgba(0, 123, 255, 0.3);
}

.plan-name {
  font-size: 2.2rem;
  font-weight: bold;
  margin-bottom: 1rem;
}

.plan-detail {
  font-size: 1.3rem;
  margin-bottom: 0.7rem;
}

.plan-detail span {
  font-weight: bold;
}

.plan-actions {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.btn-danger {
  background-color: var(--color-danger);
  color: white;
  border: 2px solid var(--color-danger);
}

.btn-danger:hover {
  background-color: #c82333;
}

/* Notification Settings */
.notification-list {
  margin-top: 1rem;
}

.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid #eee;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-label {
  font-size: 1.2rem;
  color: var(--color-text);
}

.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 34px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

input:checked + .slider {
  background-color: var(--color-primary);
}

input:checked + .slider:before {
  transform: translateX(26px);
}

/* Badges Page */
.badge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 1.5rem;
}

.badge-card {
  text-align: center;
  padding: 1rem;
  border-radius: var(--radius-large);
  background-color: #f0f0f0;
  border: 3px solid var(--color-secondary);
  transition: all 0.3s ease;
}

.badge-card.locked {
  background-color: #e0e0e0;
  filter: grayscale(80%);
  border-color: #ccc;
}

.badge-card img {
  width: 60px;
  height: 60px;
  margin-bottom: 0.5rem;
}

.badge-name {
  font-size: 1rem;
  color: var(--color-text);
  font-weight: bold;
}

.badge-card.locked .badge-name {
  color: var(--color-text-light);
}

/* Help & Feedback Page */
.feedback-form {
  max-width: 600px;
  margin: 0 auto;
  padding: 2rem;
  background-color: var(--color-background);
  border-radius: var(--radius-large);
  border: 2px solid var(--color-secondary);
}

.feedback-form .form-group {
  margin-bottom: 1.5rem;
}

.feedback-form textarea {
  min-height: 150px;
}

/* About Us Page */
.about-section {
  text-align: center;
  padding: 2rem;
  background-color: var(--color-background);
  border-radius: var(--radius-large);
  border: 2px solid var(--color-secondary);
}

.app-logo {
  width: 100px;
  height: 100px;
  margin-bottom: 1rem;
}

.app-name {
  font-size: 2.5rem;
  font-weight: bold;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
}

.app-version {
  font-size: 1.2rem;
  color: var(--color-text-light);
  margin-bottom: 1.5rem;
}

.app-description {
  font-size: 1.1rem;
  line-height: 1.6;
  color: var(--color-text);
  max-width: 600px;
  margin: 0 auto;
}
</style>