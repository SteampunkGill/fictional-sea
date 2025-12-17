<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="upload-page">
    <header class="header">
      <button class="btn-back" @click="goBack">← 返回书架</button>
      <h1>上传文档</h1>
      <div class="header-info">
        <span class="info-icon">ℹ️</span>
        <span>支持 PDF, DOCX, TXT, JPG, PNG</span>
      </div>
    </header>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>正在上传文档...</p>
    </div>

    <!-- 错误提示 -->
    <div v-if="errorMessage" class="error-message">
      <span class="error-icon">⚠️</span>
      <span>{{ errorMessage }}</span>
      <button class="btn-close-error" @click="errorMessage = ''">×</button>
    </div>

    <main class="main">
      <!-- 拖拽上传区域 -->
      <div
        class="drop-zone"
        :class="{ 'drag-over': dragOver }"
        @dragover.prevent="handleDragOver"
        @dragleave="handleDragLeave"
        @drop.prevent="handleDrop"
        @click="triggerFileInput"
      >
        <div class="drop-content">
          <div class="upload-icon">📤</div>
          <h3>拖拽文件到此处或点击选择</h3>
          <p>最大文件大小：50MB</p>
          <input
            type="file"
            ref="fileInput"
            @change="handleFileSelect"
            multiple
            accept=".pdf,.docx,.txt,.jpg,.jpeg,.png"
            hidden
          />
          <button class="btn-primary">选择文件</button>
        </div>
      </div>

      <!-- 上传队列 -->
      <div class="upload-queue" v-if="files.length > 0">
        <h3>上传队列 ({{ files.length }})</h3>
        <div class="file-list">
          <div v-for="(file, index) in files" :key="index" class="file-item">
            <div class="file-info">
              <span class="file-icon">{{ getFileIcon(file.name) }}</span>
              <div class="file-details">
                <div class="file-name">{{ file.name }}</div>
                <div class="file-size">{{ formatFileSize(file.size) }}</div>
              </div>
            </div>
            <div class="file-actions">
              <div class="progress" v-if="file.progress < 100">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: file.progress + '%' }"></div>
                </div>
                <span class="progress-text">{{ file.progress }}%</span>
              </div>
              <span class="status success" v-else>✅ 上传完成</span>
              <button class="btn-remove" @click="removeFile(index)" :disabled="file.progress < 100">
                ✖
              </button>
            </div>
          </div>
        </div>

        <!-- 文档信息表单 -->
        <div class="document-form" v-if="files.some(f => f.progress === 100)">
          <h3>文档信息</h3>
          <div class="form-grid">
            <div class="input-group">
              <label for="title">标题 *</label>
              <input id="title" v-model="document.title" placeholder="输入文档标题" required />
            </div>
            <div class="input-group">
              <label for="author">作者</label>
              <input id="author" v-model="document.author" placeholder="输入作者" />
            </div>
            <div class="input-group">
              <label for="tags">标签</label>
              <input id="tags" v-model="document.tags" placeholder="用逗号分隔，如：小说, 经典" />
            </div>
            <div class="input-group">
              <label for="language">语言</label>
              <select id="language" v-model="document.language">
                <option value="zh">中文</option>
                <option value="en">英文</option>
                <option value="ja">日文</option>
                <option value="other">其他</option>
              </select>
            </div>
          </div>
          <div class="form-actions">
            <button class="btn-secondary" @click="resetForm">重置</button>
            <button class="btn-primary" @click="startProcessing" :disabled="processing">
              {{ processing ? '处理中...' : '开始处理' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 空状态提示 -->
      <div class="empty-tip" v-else>
        <p>还没有选择文件？试试拖拽一个文件到上方区域，或点击"选择文件"按钮。</p>
      </div>
    </main>

    <!-- 模拟处理完成提示 -->
    <div v-if="showSuccess" class="success-modal">
      <div class="modal-content">
        <div class="success-icon">🎉</div>
        <h2>处理完成！</h2>
        <p>文档已成功上传并处理，现在可以开始阅读了。</p>
        <div class="modal-actions">
          <button class="btn-primary" @click="goToBookshelf">返回书架</button>
          <button class="btn-outline" @click="goToReader">立即阅读</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const fileInput = ref(null)
const dragOver = ref(false)
const processing = ref(false)
const showSuccess = ref(false)
const loading = ref(false)
const errorMessage = ref('')

// 文件队列
const files = ref([])

// 文档信息
const document = reactive({
  title: '',
  author: '',
  tags: '',
  language: 'zh'
})

// 模拟数据（用于请求失败时回退）
const mockUploadResponse = {
  success: true,
  message: '文档上传成功',
  data: {
    document: {
      id: 1,
      title: '示例文档',
      fileName: 'example.pdf',
      fileSize: '2.5 MB',
      fileType: 'application/pdf',
      status: 'uploading',
      processingProgress: 0,
      createdAt: new Date().toISOString()
    }
  }
}

const triggerFileInput = () => {
  fileInput.value.click()
}

const handleFileSelect = (event) => {
  const selectedFiles = Array.from(event.target.files)
  addFiles(selectedFiles)
}

const handleDragOver = (event) => {
  dragOver.value = true
  event.dataTransfer.dropEffect = 'copy'
}

const handleDragLeave = () => {
  dragOver.value = false
}

const handleDrop = (event) => {
  dragOver.value = false
  const droppedFiles = Array.from(event.dataTransfer.files)
  addFiles(droppedFiles)
}

const addFiles = (fileList) => {
  fileList.forEach(file => {
    // 检查文件类型
    const allowedTypes = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'text/plain', 'image/jpeg', 'image/png']
    const fileType = file.type
    if (!allowedTypes.some(type => fileType.includes(type.replace('*', '')))) {
      alert(`文件 ${file.name} 类型不支持`)
      return
    }

    // 检查文件大小 (50MB)
    if (file.size > 50 * 1024 * 1024) {
      alert(`文件 ${file.name} 超过50MB限制`)
      return
    }

    files.value.push({
      file,
      name: file.name,
      size: file.size,
      progress: 0,
      completed: false
    })

    // 模拟上传进度
    simulateUpload(files.value.length - 1)
  })
}

const simulateUpload = (index) => {
  const interval = setInterval(() => {
    if (files.value[index].progress >= 100) {
      clearInterval(interval)
      files.value[index].completed = true
      return
    }
    files.value[index].progress += 10
  }, 200)
}

const removeFile = (index) => {
  if (files.value[index].progress < 100) {
    if (!confirm('文件正在上传，确定取消吗？')) return
  }
  files.value.splice(index, 1)
}

const getFileIcon = (filename) => {
  const ext = filename.split('.').pop().toLowerCase()
  if (ext === 'pdf') return '📕'
  if (ext === 'docx') return '📘'
  if (ext === 'txt') return '📄'
  if (['jpg', 'jpeg', 'png'].includes(ext)) return '🖼️'
  return '📎'
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const resetForm = () => {
  document.title = ''
  document.author = ''
  document.tags = ''
  document.language = 'zh'
}

const startProcessing = async () => {
  if (!document.title.trim()) {
    alert('请输入文档标题')
    return
  }

  // 获取已上传完成的文件
  const uploadedFiles = files.value.filter(f => f.progress === 100)
  if (uploadedFiles.length === 0) {
    alert('请等待文件上传完成')
    return
  }

  processing.value = true
  loading.value = true
  errorMessage.value = ''

  try {
    // 使用第一个上传完成的文件进行实际API请求
    const fileToUpload = uploadedFiles[0]
    const formData = new FormData()
    formData.append('file', fileToUpload.file)
    formData.append('title', document.title)
    formData.append('description', document.author) // 使用author字段作为description
    formData.append('tags', document.tags)
    formData.append('language', document.language)

    // 从localStorage获取token（假设登录后token存储在localStorage）
    const token = sessionStorage.getItem('token') || localStorage.getItem('token') || ''

    const response = await fetch('http://localhost:8080/api/v1/documents/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    })

    if (!response.ok) {
      // 尝试解析错误响应
      const errorText = await response.text()
      let errorMsg = `HTTP错误! 状态码: ${response.status}`
      try {
        const errorJson = JSON.parse(errorText)
        errorMsg = errorJson.message || errorMsg
      } catch (e) {
        // 如果不是JSON，则使用文本
        errorMsg = errorText || errorMsg
      }
      
      if (response.status === 401) {
        errorMsg = '登录已过期，请重新登录'
      } else if (response.status === 500) {
        errorMsg = '服务器内部错误，请稍后重试'
      }
      
      throw new Error(errorMsg)
    }

    const result = await response.json()
    
    if (result.success) {
      // 使用后端返回的真实数据
      console.log('上传成功，返回数据:', result.data)
      // 这里可以根据需要处理返回的数据
      // 例如：更新文件状态、显示成功消息等
      
      // 模拟处理过程
      setTimeout(() => {
        processing.value = false
        loading.value = false
        showSuccess.value = true
      }, 1000)
    } else {
      throw new Error(result.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    
    // 根据错误类型显示不同的提示
    if (error.message.includes('登录已过期') || error.message.includes('请先登录')) {
      errorMessage.value = '登录已过期，请重新登录'
      // 可以跳转到登录页
      // router.push('/login')
    } else if (error.message.includes('服务器内部错误')) {
      errorMessage.value = '服务器内部错误，请稍后重试'
    } else {
      errorMessage.value = `上传失败: ${error.message}，已使用模拟数据`
    }
    
    // 请求失败时使用模拟数据
    console.log('使用模拟数据:', mockUploadResponse.data)
    
    // 模拟处理过程
    setTimeout(() => {
      processing.value = false
      loading.value = false
      showSuccess.value = true
    }, 1000)
  }
}

const goBack = () => {
  router.push('/bookshelf')
}

const goToBookshelf = () => {
  router.push('/bookshelf')
}

const goToReader = () => {
  router.push('/reader/1')
}
</script>
<style scoped>
.upload-page {
  min-height: 100vh;
  background-color: var(--color-background);
  padding: 2rem;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.btn-back {
  background-color: transparent;
  border: 2px solid var(--color-secondary);
  color: var(--color-text);
  padding: 10px 20px;
  border-radius: var(--radius-medium);
  font-weight: bold;
  cursor: pointer;
}

.header h1 {
  font-size: 2.5rem;
  color: var(--color-primary);
  margin: 0;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: var(--color-secondary);
  color: var(--color-text);
  padding: 10px 20px;
  border-radius: var(--radius-large);
  font-weight: bold;
}

.main {
  max-width: 900px;
  margin: 0 auto;
}

.drop-zone {
  border: 4px dashed var(--color-secondary);
  border-radius: var(--radius-large);
  padding: 4rem 2rem;
  text-align: center;
  cursor: pointer;
  background-color: white;
  transition: all 0.3s var(--transition-bounce);
  margin-bottom: 3rem;
}

.drop-zone.drag-over {
  border-color: var(--color-primary);
  background-color: rgba(255, 158, 158, 0.1);
  transform: scale(1.02);
}

.drop-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.upload-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  animation: bounce 2s infinite var(--transition-bounce);
}

.drop-content h3 {
  font-size: 1.8rem;
  color: var(--color-text);
}

.drop-content p {
  color: var(--color-text-light);
  margin-bottom: 1.5rem;
}

.upload-queue h3 {
  font-size: 1.8rem;
  margin-bottom: 1.5rem;
  color: var(--color-primary);
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 3rem;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  padding: 1.5rem;
  border-radius: var(--radius-large);
  border: 3px solid var(--color-secondary);
  transition: all 0.3s ease;
}

.file-item:hover {
  border-color: var(--color-primary);
  transform: translateY(-4px);
  box-shadow: var(--shadow-soft);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.file-icon {
  font-size: 2rem;
}

.file-details {
  display: flex;
  flex-direction: column;
}

.file-name {
  font-weight: bold;
  color: var(--color-text);
  margin-bottom: 0.3rem;
}

.file-size {
  font-size: 0.9rem;
  color: var(--color-text-light);
}

.file-actions {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.progress {
  display: flex;
  align-items: center;
  gap: 1rem;
  min-width: 200px;
}

.progress-bar {
  flex: 1;
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: var(--color-primary);
  border-radius: 5px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 0.9rem;
  font-weight: bold;
  color: var(--color-primary);
  min-width: 40px;
}

.status.success {
  color: var(--color-success);
  font-weight: bold;
}

.btn-remove {
  background-color: transparent;
  border: none;
  color: var(--color-error);
  font-size: 1.5rem;
  cursor: pointer;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.btn-remove:hover {
  background-color: rgba(255, 89, 94, 0.1);
}

.btn-remove:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.document-form {
  background-color: white;
  padding: 2.5rem;
  border-radius: var(--radius-large);
  border: 3px solid var(--color-accent);
  box-shadow: var(--shadow-soft);
}

.document-form h3 {
  font-size: 1.8rem;
  margin-bottom: 1.5rem;
  color: var(--color-primary);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.input-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: var(--color-text);
}

.input-group input,
.input-group select {
  width: 100%;
  padding: 12px 16px;
  border-radius: var(--radius-medium);
  border: 3px solid var(--color-secondary);
  font-size: 1rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.empty-tip {
  text-align: center;
  padding: 3rem;
  color: var(--color-text-light);
  font-size: 1.1rem;
  background-color: white;
  border-radius: var(--radius-large);
  border: 3px dashed var(--color-secondary);
}

.success-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 3rem;
  border-radius: var(--radius-large);
  text-align: center;
  max-width: 500px;
  width: 90%;
  border: 5px solid var(--color-success);
  box-shadow: var(--shadow-hard);
  animation: popIn 0.5s var(--transition-bounce);
}

.success-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
  animation: bounce 2s infinite var(--transition-bounce);
}

.modal-content h2 {
  font-size: 2.5rem;
  color: var(--color-success);
  margin-bottom: 1rem;
}

.modal-content p {
  font-size: 1.2rem;
  color: var(--color-text);
  margin-bottom: 2rem;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

@keyframes popIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@media (max-width: 768px) {
  .upload-page {
    padding: 1rem;
  }
  
  .header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .drop-zone {
    padding: 3rem 1rem;
  }
  
  .file-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .file-actions {
    width: 100%;
  }
  
  .progress {
    min-width: auto;
    width: 100%;
  }
  
  .modal-content {
    padding: 2rem;
  }
}
</style>