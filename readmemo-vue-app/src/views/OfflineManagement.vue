<template>
  <div class="offline-management">
    <header class="header">
      <h1>📦 离线模式管理</h1>
      <p class="subtitle">管理已下载的文档，随时随地阅读</p>
    </header>

    <div class="content">
      <div class="stats">
        <div class="stat-card">
          <div class="stat-icon">📄</div>
          <div class="stat-info">
            <div class="stat-value">{{ downloadedCount }}</div>
            <div class="stat-label">已下载文档</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">💾</div>
          <div class="stat-info">
            <div class="stat-value">{{ usedStorage }} MB</div>
            <div class="stat-label">已用存储</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">📱</div>
          <div class="stat-info">
            <div class="stat-value">{{ availableSpace }} MB</div>
            <div class="stat-label">剩余空间</div>
          </div>
        </div>
      </div>

      <div class="controls">
        <button class="btn-primary" @click="openDownloadManager">
          ⬇️ 下载新文档
        </button>
        <button class="btn-secondary" @click="clearAllDownloads">
          🗑️ 清空所有下载
        </button>
        <button class="btn-secondary" @click="syncNow">
          🔄 立即同步
        </button>
      </div>

      <div class="document-list">
        <h2>已下载文档列表</h2>
        <div v-if="documents.length === 0" class="empty">
          <div class="empty-icon">📭</div>
          <p>暂无离线文档</p>
          <button class="btn-primary" @click="openDownloadManager">去下载</button>
        </div>
        <div v-else class="list">
          <div
            v-for="doc in documents"
            :key="doc.id"
            class="document-item"
            :class="{ expired: doc.isExpired }"
          >
            <div class="doc-icon">📄</div>
            <div class="doc-info">
              <h3>{{ doc.title }}</h3>
              <p>{{ doc.author }} · {{ doc.size }} MB · 下载于 {{ doc.downloadDate }}</p>
              <div class="doc-status">
                <span class="status" :class="doc.status">{{ doc.status }}</span>
                <span v-if="doc.isExpired" class="expired-label">已过期</span>
              </div>
            </div>
            <div class="doc-actions">
              <button class="btn-action" @click="openDocument(doc)">阅读</button>
              <button class="btn-action" @click="deleteDocument(doc)">删除</button>
              <button class="btn-action" @click="updateDocument(doc)">更新</button>
            </div>
          </div>
        </div>
      </div>

      <div class="settings">
        <h2>离线设置</h2>
        <div class="setting-group">
          <label class="setting-label">
            <input type="checkbox" v-model="autoDownloadUpdates" />
            <span>自动下载更新</span>
          </label>
          <label class="setting-label">
            <span>离线存储上限</span>
            <input type="range" min="100" max="5000" v-model="storageLimit" />
            <span class="value">{{ storageLimit }} MB</span>
          </label>
          <label class="setting-label">
            <span>文档过期时间</span>
            <select v-model="expirationDays">
              <option value="7">7天</option>
              <option value="30">30天</option>
              <option value="90">90天</option>
              <option value="never">永不过期</option>
            </select>
          </label>
        </div>
        <button class="btn-save" @click="saveSettings">保存设置</button>
      </div>
    </div>

    <!-- 下载管理器弹窗 -->
    <div class="modal" v-if="showDownloadManager">
      <div class="modal-content">
        <h3>下载新文档</h3>
        <div class="available-docs">
          <div
            v-for="doc in availableDocuments"
            :key="doc.id"
            class="available-doc"
          >
            <div class="doc-icon">📄</div>
            <div class="doc-info">
              <h4>{{ doc.title }}</h4>
              <p>{{ doc.author }} · {{ doc.size }} MB</p>
            </div>
            <button
              class="btn-download"
              @click="downloadDocument(doc)"
              :disabled="doc.downloading"
            >
              {{ doc.downloading ? '下载中...' : '下载' }}
            </button>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-close" @click="closeDownloadManager">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 模拟数据
const downloadedCount = ref(5)
const usedStorage = ref(245)
const availableSpace = ref(3755)

const documents = ref([
  {
    id: 1,
    title: '傲慢与偏见',
    author: '简·奥斯汀',
    size: 12,
    downloadDate: '2025-12-10',
    status: '已下载',
    isExpired: false
  },
  {
    id: 2,
    title: '经济学原理',
    author: '曼昆',
    size: 45,
    downloadDate: '2025-12-05',
    status: '已下载',
    isExpired: false
  },
  {
    id: 3,
    title: '科学革命的结构',
    author: '托马斯·库恩',
    size: 28,
    downloadDate: '2025-11-20',
    status: '已过期',
    isExpired: true
  },
  {
    id: 4,
    title: '人类简史',
    author: '尤瓦尔·赫拉利',
    size: 36,
    downloadDate: '2025-12-12',
    status: '已下载',
    isExpired: false
  },
  {
    id: 5,
    title: '代码大全',
    author: 'Steve McConnell',
    size: 52,
    downloadDate: '2025-12-01',
    status: '已下载',
    isExpired: false
  }
])

const availableDocuments = ref([
  { id: 6, title: '设计心理学', author: '唐纳德·诺曼', size: 18, downloading: false },
  { id: 7, title: '算法导论', author: 'Thomas H. Cormen', size: 67, downloading: false },
  { id: 8, title: '百年孤独', author: '加西亚·马尔克斯', size: 22, downloading: false }
])

const showDownloadManager = ref(false)
const autoDownloadUpdates = ref(true)
const storageLimit = ref(2000)
const expirationDays = ref('30')

const openDownloadManager = () => {
  showDownloadManager.value = true
}

const closeDownloadManager = () => {
  showDownloadManager.value = false
}

const downloadDocument = (doc) => {
  doc.downloading = true
  // 模拟下载
  setTimeout(() => {
    doc.downloading = false
    documents.value.push({
      ...doc,
      id: Date.now(),
      downloadDate: new Date().toISOString().split('T')[0],
      status: '已下载',
      isExpired: false
    })
    downloadedCount.value++
    usedStorage.value += doc.size
    availableSpace.value -= doc.size
    alert(`已下载: ${doc.title}`)
  }, 1500)
}

const deleteDocument = (doc) => {
  if (confirm(`确定删除 "${doc.title}" 吗？`)) {
    const index = documents.value.findIndex(d => d.id === doc.id)
    if (index > -1) {
      usedStorage.value -= doc.size
      availableSpace.value += doc.size
      documents.value.splice(index, 1)
      downloadedCount.value--
    }
  }
}

const updateDocument = (doc) => {
  alert(`检查更新: ${doc.title}`)
}

const openDocument = (doc) => {
  alert(`打开文档: ${doc.title}`)
  // 实际中应跳转到阅读器
}

const clearAllDownloads = () => {
  if (confirm('确定清空所有下载的文档吗？此操作不可恢复。')) {
    documents.value = []
    downloadedCount.value = 0
    usedStorage.value = 0
    availableSpace.value = 5000
  }
}

const syncNow = () => {
  alert('正在同步离线文档...')
}

const saveSettings = () => {
  alert(`设置已保存: 自动下载 ${autoDownloadUpdates.value ? '开启' : '关闭'}, 存储上限 ${storageLimit.value} MB, 过期 ${expirationDays.value} 天`)
}
</script>

<style scoped>
.offline-management {
  min-height: 100vh;
  background-color: var(--color-background);
  padding: 2rem;
}

.header {
  margin-bottom: 2rem;
}

.header h1 {
  font-size: 3rem;
  color: var(--color-primary);
}

.subtitle {
  font-size: 1.5rem;
  color: var(--color-text-light);
}

.stats {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 200px;
  background-color: white;
  border-radius: var(--radius-large);
  padding: 1.5rem;
  border: 3px solid var(--color-secondary);
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.stat-icon {
  font-size: 3rem;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: bold;
  color: var(--color-primary);
}

.stat-label {
  font-size: 1.2rem;
  color: var(--color-text-light);
}

.controls {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}

.btn-primary, .btn-secondary {
  padding: 15px 30px;
  border-radius: var(--radius-large);
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
  border: 3px solid;
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.btn-secondary {
  background-color: white;
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.document-list {
  background-color: white;
  border-radius: var(--radius-large);
  padding: 2rem;
  border: 3px solid var(--color-primary);
  margin-bottom: 2rem;
}

.document-list h2 {
  font-size: 2rem;
  color: var(--color-primary);
  margin-bottom: 1.5rem;
  border-bottom: 3px solid var(--color-secondary);
  padding-bottom: 10px;
}

.empty {
  text-align: center;
  padding: 4rem;
  color: var(--color-text-light);
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 1rem;
}

.empty p {
  font-size: 1.5rem;
  margin-bottom: 2rem;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.document-item {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem;
  border-radius: var(--radius-large);
  border: 3px solid var(--color-secondary);
  transition: all 0.3s;
}

.document-item:hover {
  border-color: var(--color-primary);
  background-color: #f9f9f9;
}

.document-item.expired {
  opacity: 0.7;
  border-color: #ccc;
}

.doc-icon {
  font-size: 3rem;
}

.doc-info {
  flex: 1;
}

.doc-info h3 {
  font-size: 1.5rem;
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.doc-info p {
  color: var(--color-text-light);
  margin-bottom: 0.5rem;
}

.doc-status {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.status {
  padding: 5px 15px;
  border-radius: var(--radius-medium);
  font-weight: bold;
  background-color: var(--color-success);
  color: white;
}

.status.已过期 {
  background-color: var(--color-danger);
}

.expired-label {
  color: var(--color-danger);
  font-weight: bold;
}

.doc-actions {
  display: flex;
  gap: 1rem;
}

.btn-action {
  padding: 10px 20px;
  border-radius: var(--radius-medium);
  border: 2px solid var(--color-secondary);
  background-color: white;
  font-weight: bold;
  cursor: pointer;
}

.settings {
  background-color: white;
  border-radius: var(--radius-large);
  padding: 2rem;
  border: 3px solid var(--color-primary);
}

.settings h2 {
  font-size: 2rem;
  color: var(--color-primary);
  margin-bottom: 1.5rem;
  border-bottom: 3px solid var(--color-secondary);
  padding-bottom: 10px;
}

.setting-group {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.setting-label {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 1.2rem;
}

.setting-label span:first-child {
  min-width: 150px;
}

.setting-label input[type="range"] {
  flex: 1;
}

.value {
  min-width: 50px;
  text-align: right;
}

.btn-save {
  padding: 15px 40px;
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-large);
  font-size: 1.2rem;
  font-weight: bold;
  cursor: pointer;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 3rem;
  border-radius: var(--radius-large);
  border: 5px solid var(--color-primary);
  max-width: 800px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h3 {
  font-size: 2rem;
  color: var(--color-primary);
  margin-bottom: 2rem;
}

.available-docs {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.available-doc {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem;
  border-radius: var(--radius-large);
  border: 3px solid var(--color-secondary);
}

.available-doc .doc-icon {
  font-size: 2.5rem;
}

.available-doc .doc-info {
  flex: 1;
}

.available-doc .doc-info h4 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.btn-download {
  padding: 10px 20px;
  border-radius: var(--radius-medium);
  background-color: var(--color-primary);
  color: white;
  border: none;
  font-weight: bold;
  cursor: pointer;
}

.btn-download:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-actions {
  text-align: right;
}

.btn-close {
  padding: 12px 24px;
  border-radius: var(--radius-medium);
  background-color: var(--color-secondary);
  color: white;
  border: none;
  font-weight: bold;
  cursor: pointer;
}
</style>