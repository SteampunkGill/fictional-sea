<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="reader-page">
    <!-- 顶部工具栏 -->
    <header class="toolbar" :class="{ hidden: toolbarHidden }">
      <div class="toolbar-left">
        <button class="toolbar-btn" @click="goBack">← 返回</button>
        <button class="toolbar-btn" @click="toggleSidebar('toc')">📖 目录</button>
        <button class="toolbar-btn" @click="toggleSidebar('notes')">📝 笔记</button>
      </div>
      <div class="toolbar-center">
        <h2 class="document-title">{{ document.title }}</h2>
        <div class="document-meta">
          <span>{{ document.author }}</span>
          <span>·</span>
          <span>进度 {{ progress }}%</span>
        </div>
      </div>
      <div class="toolbar-right">
        <button class="toolbar-btn" @click="toggleToolbar">🔧</button>
        <button class="toolbar-btn" @click="toggleSearch">🔍</button>
        <button class="toolbar-btn" @click="toggleSettings">⚙️</button>
        <button class="toolbar-btn" @click="toggleWordbook">📚</button>
      </div>
    </header>

    <!-- 搜索栏 -->
    <div class="search-bar" v-if="showSearch">
      <input
        type="text"
        v-model="searchQuery"
        placeholder="搜索文档内容..."
        @input="performSearch"
      />
      <button class="btn-close-search" @click="toggleSearch">✖</button>
      <div class="search-results" v-if="searchResults.length > 0">
        <div
          v-for="result in searchResults"
          :key="result.id"
          class="search-result"
          @click="jumpToResult(result)"
        >
          <span v-html="highlightText(result.text)"></span>
        </div>
      </div>
    </div>

    <div class="reader-container">
      <!-- 目录侧边栏 -->
      <aside class="sidebar sidebar-toc" :class="{ open: sidebarOpen.toc }">
        <div class="sidebar-header">
          <h3>目录</h3>
          <button class="btn-close-sidebar" @click="toggleSidebar('toc')">✖</button>
        </div>
        <div class="sidebar-content">
          <ul class="toc-list">
            <li
              v-for="chapter in toc"
              :key="chapter.id"
              :class="{ active: currentChapter === chapter.id }"
              @click="jumpToChapter(chapter.id)"
            >
              {{ chapter.title }}
              <span class="page-number">{{ chapter.page }}</span>
            </li>
          </ul>
        </div>
      </aside>

      <!-- 笔记侧边栏 -->
      <aside class="sidebar sidebar-notes" :class="{ open: sidebarOpen.notes }">
        <div class="sidebar-header">
          <h3>笔记与高亮</h3>
          <button class="btn-close-sidebar" @click="toggleSidebar('notes')">✖</button>
        </div>
        <div class="sidebar-content">
          <div class="notes-list">
            <div
              v-for="note in notes"
              :key="note.id"
              class="note-item"
              @click="jumpToNote(note)"
            >
              <div class="note-text">{{ note.text }}</div>
              <div class="note-meta">
                <span class="note-type" :class="note.type">{{ note.type === 'highlight' ? '高亮' : '笔记' }}</span>
                <span class="note-page">第 {{ note.page }} 页</span>
              </div>
            </div>
          </div>
          <button class="btn-add-note" @click="addNote">+ 添加笔记</button>
        </div>
      </aside>

      <!-- 主阅读区域 -->
      <main class="reading-area" @click="handleTextClick">
        <div class="content" ref="contentEl">
          <h1>{{ document.title }}</h1>
          <h3>作者：{{ document.author }}</h3>
          <div v-for="paragraph in paragraphs" :key="paragraph.id" class="paragraph">
            <p>{{ paragraph.text }}</p>
          </div>
        </div>

        <!-- 查词弹窗 -->
        <div v-if="wordPopup.show" class="word-popup" :style="wordPopup.style">
          <div class="popup-header">
            <strong>{{ wordPopup.word }}</strong>
            <button class="btn-close-popup" @click="closeWordPopup">✖</button>
          </div>
          <div class="popup-body">
            <div class="phonetic">/{{ wordPopup.phonetic }}/</div>
            <button class="btn-speak" @click="speakWord">🔊 发音</button>
            <div class="definition">
              <strong>{{ wordPopup.partOfSpeech }}</strong>
              {{ wordPopup.definition }}
            </div>
            <div class="example">{{ wordPopup.example }}</div>
            <button class="btn-add-to-vocab" @click="addToVocabulary">
              📚 添加到生词本
            </button>
          </div>
        </div>
      </main>
    </div>

    <!-- 底部进度条 -->
    <footer class="footer">
      <div class="progress-container">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
        </div>
        <div class="progress-info">
          <span>阅读进度 {{ progress }}%</span>
          <div class="footer-actions">
            <button class="btn-footer" @click="decreaseFontSize">A-</button>
            <button class="btn-footer" @click="increaseFontSize">A+</button>
            <button class="btn-footer" @click="toggleTheme">
              {{ theme === 'light' ? '🌙' : '☀️' }}
            </button>
          </div>
        </div>
      </div>
    </footer>

    <!-- 设置弹窗 -->
    <div v-if="showSettings" class="settings-modal">
      <div class="modal-content">
        <h3>阅读设置</h3>
        <div class="setting-group">
          <label>字体大小</label>
          <div class="font-size-control">
            <button @click="decreaseFontSize">A-</button>
            <span>{{ fontSize }}px</span>
            <button @click="increaseFontSize">A+</button>
          </div>
        </div>
        <div class="setting-group">
          <label>主题</label>
          <div class="theme-selector">
            <button
              v-for="t in themes"
              :key="t.id"
              :class="{ active: theme === t.id }"
              @click="theme = t.id"
            >
              {{ t.name }}
            </button>
          </div>
        </div>
        <div class="setting-group">
          <label>行高</label>
          <input type="range" min="1.2" max="2.5" step="0.1" v-model="lineHeight" />
          <span>{{ lineHeight }}</span>
        </div>
        <button class="btn-close-modal" @click="toggleSettings">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 文档数据
const document = reactive({
  title: 'The Great Gatsby',
  author: 'F. Scott Fitzgerald',
  content: ''
})

// 工具栏状态
const toolbarHidden = ref(false)
const showSearch = ref(false)
const showSettings = ref(false)
const sidebarOpen = reactive({
  toc: false,
  notes: false
})

// 阅读设置
const fontSize = ref(16)
const theme = ref('light')
const lineHeight = ref(1.6)
const progress = ref(45)

// 搜索
const searchQuery = ref('')
const searchResults = ref([])

// 查词弹窗
const wordPopup = reactive({
  show: false,
  word: '',
  phonetic: '',
  partOfSpeech: '',
  definition: '',
  example: '',
  style: {}
})

// 模拟数据
const toc = ref([
  { id: 1, title: '第一章', page: 1 },
  { id: 2, title: '第二章', page: 10 },
  { id: 3, title: '第三章', page: 20 },
  { id: 4, title: '第四章', page: 30 },
  { id: 5, title: '第五章', page: 40 }
])

const notes = ref([
  { id: 1, text: '这是一个重要的段落', type: 'highlight', page: 12 },
  { id: 2, text: '需要复习的单词', type: 'note', page: 15 },
  { id: 3, text: '人物关系图', type: 'note', page: 22 }
])

const paragraphs = ref([
  { id: 1, text: 'In my younger and more vulnerable years my father gave me some advice that I’ve been turning over in my mind ever since.' },
  { id: 2, text: '“Whenever you feel like criticizing any one,” he told me, “just remember that all the people in this world haven’t had the advantages that you’ve had.”' },
  { id: 3, text: 'He didn’t say any more, but we’ve always been unusually communicative in a reserved way, and I understood that he meant a great deal more than that.' },
  { id: 4, text: 'In consequence, I’m inclined to reserve all judgments, a habit that has opened up many curious natures to me and also made me the victim of not a few veteran bores.' },
  { id: 5, text: 'The abnormal mind is quick to detect and attach itself to this quality when it appears in a normal person, and so it came about that in college I was unjustly accused of being a politician, because I was privy to the secret griefs of wild, unknown men.' }
])

const themes = [
  { id: 'light', name: '白天' },
  { id: 'dark', name: '夜间' },
  { id: 'sepia', name: '护眼' }
]

// 交互函数
const goBack = () => {
  router.push('/bookshelf')
}

const toggleToolbar = () => {
  toolbarHidden.value = !toolbarHidden.value
}

const toggleSearch = () => {
  showSearch.value = !showSearch.value
  if (!showSearch.value) {
    searchQuery.value = ''
    searchResults.value = []
  }
}

const toggleSettings = () => {
  showSettings.value = !showSettings.value
}

const toggleWordbook = () => {
  router.push('/vocabulary')
}

const toggleSidebar = (type) => {
  sidebarOpen[type] = !sidebarOpen[type]
  // 关闭另一个侧边栏
  const other = type === 'toc' ? 'notes' : 'toc'
  sidebarOpen[other] = false
}

const performSearch = () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  // 模拟搜索
  searchResults.value = [
    { id: 1, text: '...my father gave me some advice that I’ve been turning over...', page: 1 },
    { id: 2, text: '...all the people in this world haven’t had the advantages...', page: 2 }
  ]
}

const highlightText = (text) => {
  const query = searchQuery.value
  if (!query) return text
  const regex = new RegExp(`(${query})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

const jumpToResult = (result) => {
  // 模拟跳转
  alert(`跳转到第 ${result.page} 页`)
}

const jumpToChapter = (chapterId) => {
  currentChapter.value = chapterId
  // 模拟跳转
  alert(`跳转到第 ${chapterId} 章`)
}

const jumpToNote = (note) => {
  alert(`跳转到第 ${note.page} 页的笔记`)
}

const addNote = () => {
  const text = prompt('输入笔记内容')
  if (text) {
    notes.value.push({
      id: notes.value.length + 1,
      text,
      type: 'note',
      page: Math.floor(Math.random() * 50) + 1
    })
  }
}

// 处理文本点击（查词）
const handleTextClick = (event) => {
  // 简单模拟：点击单词弹出查词
  if (event.target.tagName === 'P' || event.target.tagName === 'SPAN') {
    const words = ['father', 'advice', 'criticizing', 'advantages', 'communicative', 'judgments']
    const randomWord = words[Math.floor(Math.random() * words.length)]
    
    wordPopup.word = randomWord
    wordPopup.phonetic = 'ˈfɑːðər'
    wordPopup.partOfSpeech = 'n.'
    wordPopup.definition = '父亲；祖先；创始人'
    wordPopup.example = 'My father gave me some advice.'
    wordPopup.style = {
      left: event.clientX + 'px',
      top: event.clientY + 'px'
    }
    wordPopup.show = true
  }
}

const closeWordPopup = () => {
  wordPopup.show = false
}

const speakWord = () => {
  // 模拟发音
  alert(`发音：${wordPopup.word}`)
}

const addToVocabulary = () => {
  alert(`已添加 "${wordPopup.word}" 到生词本`)
  closeWordPopup()
}

const increaseFontSize = () => {
  fontSize.value = Math.min(fontSize.value + 2, 30)
}

const decreaseFontSize = () => {
  fontSize.value = Math.max(fontSize.value - 2, 12)
}

const toggleTheme = () => {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
}

const currentChapter = ref(1)

onMounted(() => {
  // 模拟加载文档
  console.log('Loading document ID:', route.params.id)
})
</script>

<style scoped>
.reader-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-background);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: white;
  box-shadow: var(--shadow-soft);
  border-bottom: 5px solid var(--color-primary);
  transition: transform 0.3s ease;
}

.toolbar.hidden {
  transform: translateY(-100%);
}

.toolbar-btn {
  background-color: transparent;
  border: 2px solid var(--color-secondary);
  border-radius: var(--radius-medium);
  padding: 8px 16px;
  margin: 0 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background-color: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.toolbar-center {
  text-align: center;
}

.document-title {
  font-size: 1.5rem;
  color: var(--color-primary);
  margin-bottom: 0.3rem;
}

.document-meta {
  color: var(--color-text-light);
  font-size: 0.9rem;
}

.search-bar {
  padding: 1rem 2rem;
  background-color: white;
  border-bottom: 3px solid var(--color-secondary);
  position: relative;
}

.search-bar input {
  width: 100%;
  padding: 12px 20px;
  border-radius: var(--radius-large);
  border: 3px solid var(--color-primary);
  font-size: 1rem;
}

.btn-close-search {
  position: absolute;
  right: 2.5rem;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 2rem;
  right: 2rem;
  background-color: white;
  border-radius: var(--radius-medium);
  box-shadow: var(--shadow-hard);
  max-height: 300px;
  overflow-y: auto;
  z-index: 100;
}

.search-result {
  padding: 1rem;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}

.search-result:hover {
  background-color: #f9f9f9;
}

.reader-container {
  display: flex;
  flex: 1;
  overflow: hidden;
  position: relative;
}

.sidebar {
  position: fixed;
  top: 0;
  bottom: 0;
  width: 300px;
  background-color: white;
  box-shadow: var(--shadow-hard);
  z-index: 200;
  transition: transform 0.3s var(--transition-bounce);
  border-left: 5px solid var(--color-secondary);
}

.sidebar-toc {
  left: 0;
  transform: translateX(-100%);
}

.sidebar-notes {
  right: 0;
  transform: translateX(100%);
}

.sidebar.open {
  transform: translateX(0);
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 3px solid var(--color-secondary);
}

.sidebar-header h3 {
  font-size: 1.5rem;
  color: var(--color-primary);
}

.btn-close-sidebar {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}

.sidebar-content {
  padding: 1.5rem;
  height: calc(100% - 70px);
  overflow-y: auto;
}

.toc-list {
  list-style: none;
  padding: 0;
}

.toc-list li {
  padding: 1rem;
  border-radius: var(--radius-medium);
  margin-bottom: 0.5rem;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  border: 2px solid transparent;
}

.toc-list li:hover {
  background-color: var(--color-secondary);
}

.toc-list li.active {
  background-color: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.notes-list {
  margin-bottom: 2rem;
}

.note-item {
  padding: 1rem;
  border-radius: var(--radius-medium);
  border: 2px solid var(--color-secondary);
  margin-bottom: 1rem;
  cursor: pointer;
}

.note-item:hover {
  border-color: var(--color-primary);
}

.note-text {
  margin-bottom: 0.5rem;
}

.note-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: var(--color-text-light);
}

.note-type.highlight {
  color: var(--color-accent);
  font-weight: bold;
}

.note-type.note {
  color: var(--color-info);
  font-weight: bold;
}

.btn-add-note {
  width: 100%;
  padding: 12px;
  background-color: var(--color-secondary);
  color: var(--color-text);
  border-radius: var(--radius-large);
  font-weight: bold;
}

.reading-area {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  position: relative;
}

.content {
  max-width: 800px;
  margin: 0 auto;
  line-height: v-bind(lineHeight);
  font-size: v-bind(fontSize + 'px');
}

.paragraph {
  margin-bottom: 1.5rem;
}

.word-popup {
  position: fixed;
  background-color: white;
  border-radius: var(--radius-large);
  box-shadow: var(--shadow-hard);
  border: 5px solid var(--color-primary);
  padding: 1.5rem;
  min-width: 300px;
  max-width: 400px;
  z-index: 300;
  animation: popIn 0.3s var(--transition-bounce);
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  font-size: 1.5rem;
}

.btn-close-popup {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}

.popup-body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.phonetic {
  font-style: italic;
  color: var(--color-text-light);
}

.btn-speak, .btn-add-to-vocab {
  padding: 8px 16px;
  border-radius: var(--radius-medium);
  font-weight: bold;
  cursor: pointer;
}

.btn-speak {
  background-color: var(--color-secondary);
  color: var(--color-text);
}

.btn-add-to-vocab {
  background-color: var(--color-primary);
  color: white;
}

.footer {
  padding: 1rem 2rem;
  background-color: white;
  border-top: 5px solid var(--color-secondary);
}

.progress-container {
  max-width: 800px;
  margin: 0 auto;
}

.progress-bar {
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  margin-bottom: 0.5rem;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: var(--color-primary);
  border-radius: 5px;
  transition: width 0.5s ease;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-footer {
  background-color: var(--color-secondary);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  font-weight: bold;
}

.settings-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 400;
}

.modal-content {
  background-color: white;
  padding: 2.5rem;
  border-radius: var(--radius-large);
  max-width: 500px;
  width: 90%;
  border: 5px solid var(--color-accent);
}

.modal-content h3 {
  font-size: 1.8rem;
  margin-bottom: 1.5rem;
  color: var(--color-primary);
}

.setting-group {
  margin-bottom: 1.5rem;
}

.setting-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.font-size-control {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.font-size-control button {
  background-color: var(--color-secondary);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-medium);
  cursor: pointer;
  font-weight: bold;
}

.theme-selector {
  display: flex;
  gap: 0.5rem;
}

.theme-selector button {
  flex: 1;
  padding: 10px;
  border: 2px solid var(--color-secondary);
  background-color: white;
  border-radius: var(--radius-medium);
  cursor: pointer;
}

.theme-selector button.active {
  background-color: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.btn-close-modal {
  width: 100%;
  padding: 12px;
  background-color: var(--color-secondary);
  color: var(--color-text);
  border-radius: var(--radius-large);
  font-weight: bold;
  margin-top: 1rem;
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

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .reading-area {
    padding: 1rem;
  }
}
</style>