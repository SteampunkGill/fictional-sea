<template>
  <div class="search-results">
    <header class="header">
      <div class="search-bar">
        <input
          type="text"
          v-model="query"
          placeholder="搜索文档、生词、笔记..."
          @keyup.enter="performSearch"
        />
        <button class="btn-search" @click="performSearch">🔍</button>
      </div>
      <div class="filters">
        <button
          v-for="filter in filterOptions"
          :key="filter.value"
          :class="{ active: activeFilter === filter.value }"
          @click="setFilter(filter.value)"
        >
          {{ filter.label }}
        </button>
      </div>
    </header>

    <div class="results-container">
      <div v-if="loading" class="loading">搜索中...</div>
      <div v-else-if="results.length === 0" class="no-results">
        <div class="illustration">🔍</div>
        <h3>未找到结果</h3>
        <p>尝试其他关键词或调整筛选条件</p>
      </div>
      <div v-else>
        <section class="result-section" v-if="filteredDocuments.length > 0">
          <h2>📄 文档 ({{ filteredDocuments.length }})</h2>
          <div class="document-list">
            <div
              v-for="doc in filteredDocuments"
              :key="doc.id"
              class="document-card"
              @click="openDocument(doc)"
            >
              <div class="doc-icon">📄</div>
              <div class="doc-info">
                <h4>{{ doc.title }}</h4>
                <p>{{ doc.author }} · {{ doc.date }}</p>
                <p class="snippet">{{ doc.snippet }}</p>
              </div>
              <div class="doc-status" :class="doc.status">{{ doc.status }}</div>
            </div>
          </div>
        </section>

        <section class="result-section" v-if="filteredWords.length > 0">
          <h2>📝 生词 ({{ filteredWords.length }})</h2>
          <div class="word-list">
            <div
              v-for="word in filteredWords"
              :key="word.id"
              class="word-card"
              @click="openWord(word)"
            >
              <div class="word">{{ word.word }}</div>
              <div class="phonetic">/{{ word.phonetic }}/</div>
              <div class="meaning">{{ word.meaning }}</div>
              <div class="source">来自: {{ word.source }}</div>
            </div>
          </div>
        </section>

        <section class="result-section" v-if="filteredNotes.length > 0">
          <h2>📒 笔记 ({{ filteredNotes.length }})</h2>
          <div class="note-list">
            <div
              v-for="note in filteredNotes"
              :key="note.id"
              class="note-card"
              @click="openNote(note)"
            >
              <div class="note-content">
                <p>{{ note.content }}</p>
                <div class="note-meta">
                  <span>📌 {{ note.document }}</span>
                  <span>📅 {{ note.date }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
/* eslint-disable vue/multi-word-component-names */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const query = ref('')
const activeFilter = ref('all')
const loading = ref(false)

const filterOptions = [
  { label: '全部', value: 'all' },
  { label: '文档', value: 'documents' },
  { label: '生词', value: 'words' },
  { label: '笔记', value: 'notes' }
]

// 模拟搜索结果
const mockResults = {
  documents: [
    {
      id: 1,
      title: '傲慢与偏见',
      author: '简·奥斯汀',
      date: '2025-12-10',
      snippet: '这是一部关于爱情与阶级的经典小说...',
      status: '已读完'
    },
    {
      id: 2,
      title: '经济学原理',
      author: '曼昆',
      date: '2025-12-05',
      snippet: '介绍了微观经济学和宏观经济学的基本概念...',
      status: '阅读中'
    }
  ],
  words: [
    {
      id: 1,
      word: 'serendipity',
      phonetic: 'ˌserənˈdɪpəti',
      meaning: '意外发现珍奇事物的本领',
      source: '傲慢与偏见'
    },
    {
      id: 2,
      word: 'ubiquitous',
      phonetic: 'juːˈbɪkwɪtəs',
      meaning: '无所不在的',
      source: '经济学原理'
    }
  ],
  notes: [
    {
      id: 1,
      content: '主角伊丽莎白·班纳特的独立性格反映了当时女性意识的觉醒。',
      document: '傲慢与偏见',
      date: '2025-12-12'
    },
    {
      id: 2,
      content: '供需曲线是经济学分析的核心工具，价格由市场均衡决定。',
      document: '经济学原理',
      date: '2025-12-08'
    }
  ]
}

const results = ref(mockResults)

const filteredDocuments = computed(() => {
  if (activeFilter.value !== 'all' && activeFilter.value !== 'documents') return []
  return results.value.documents.filter(doc =>
    doc.title.toLowerCase().includes(query.value.toLowerCase()) ||
    doc.snippet.toLowerCase().includes(query.value.toLowerCase())
  )
})

const filteredWords = computed(() => {
  if (activeFilter.value !== 'all' && activeFilter.value !== 'words') return []
  return results.value.words.filter(word =>
    word.word.toLowerCase().includes(query.value.toLowerCase()) ||
    word.meaning.toLowerCase().includes(query.value.toLowerCase())
  )
})

const filteredNotes = computed(() => {
  if (activeFilter.value !== 'all' && activeFilter.value !== 'notes') return []
  return results.value.notes.filter(note =>
    note.content.toLowerCase().includes(query.value.toLowerCase()) ||
    note.document.toLowerCase().includes(query.value.toLowerCase())
  )
})

const performSearch = () => {
  loading.value = true
  // 模拟搜索延迟
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const setFilter = (filter) => {
  activeFilter.value = filter
}

const openDocument = (doc) => {
  router.push(`/reader/${doc.id}`)
}

const openWord = (word) => {
  router.push(`/dictionary/${word.word}`)
}

const openNote = (note) => {
  // 跳转到笔记所在文档
  router.push(`/reader/1?note=${note.id}`)
}
</script>

<style scoped>
.search-results {
  min-height: 100vh;
  background-color: var(--color-background);
  padding: 2rem;
}

.header {
  margin-bottom: 2rem;
}

.search-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.search-bar input {
  flex: 1;
  padding: 15px 20px;
  border-radius: var(--radius-large);
  border: 3px solid var(--color-primary);
  font-size: 1.2rem;
  outline: none;
}

.btn-search {
  padding: 15px 30px;
  border-radius: var(--radius-large);
  background-color: var(--color-primary);
  color: white;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}

.filters {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.filters button {
  padding: 10px 20px;
  border-radius: var(--radius-medium);
  border: 3px solid var(--color-secondary);
  background-color: white;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.filters button.active {
  background-color: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.results-container {
  background-color: white;
  border-radius: var(--radius-large);
  padding: 2rem;
  border: 3px solid var(--color-primary);
}

.loading,
.no-results {
  text-align: center;
  padding: 4rem;
  font-size: 1.5rem;
  color: var(--color-text-light);
}

.illustration {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.result-section {
  margin-bottom: 3rem;
}

.result-section h2 {
  font-size: 2rem;
  color: var(--color-primary);
  margin-bottom: 1.5rem;
  border-bottom: 3px solid var(--color-secondary);
  padding-bottom: 10px;
}

.document-list,
.word-list,
.note-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.document-card,
.word-card,
.note-card {
  padding: 1.5rem;
  border-radius: var(--radius-large);
  border: 3px solid var(--color-secondary);
  cursor: pointer;
  transition: all 0.3s;
}

.document-card:hover,
.word-card:hover,
.note-card:hover {
  border-color: var(--color-primary);
  background-color: #f9f9f9;
}

.document-card {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.doc-icon {
  font-size: 3rem;
}

.doc-info {
  flex: 1;
}

.doc-info h4 {
  font-size: 1.5rem;
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.doc-info p {
  color: var(--color-text-light);
  margin-bottom: 0.5rem;
}

.snippet {
  font-style: italic;
}

.doc-status {
  padding: 5px 15px;
  border-radius: var(--radius-medium);
  font-weight: bold;
  background-color: var(--color-accent);
  color: white;
}

.word-card {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.word {
  font-size: 2rem;
  font-weight: bold;
  color: var(--color-primary);
  min-width: 150px;
}

.phonetic {
  font-size: 1.2rem;
  color: var(--color-text-light);
  min-width: 150px;
}

.meaning {
  flex: 1;
  font-size: 1.2rem;
}

.source {
  font-size: 1rem;
  color: var(--color-text-light);
}

.note-content p {
  font-size: 1.2rem;
  margin-bottom: 1rem;
}

.note-meta {
  display: flex;
  justify-content: space-between;
  color: var(--color-text-light);
}
</style>