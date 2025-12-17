<template>
  <div class="dictionary-page">
    <header class="header">
      <button class="btn-back" @click="goBack">← 返回</button>
      <h1>词典详情</h1>
      <div class="header-actions">
        <button class="btn-add" @click="addToVocabulary">📚 添加到生词本</button>
        <button class="btn-speak" @click="speakWord">🔊 发音</button>
      </div>
    </header>

    <main class="main">
      <!-- 单词标题 -->
      <div class="word-header">
        <h2 class="word">{{ word }}</h2>
        <div class="phonetic">/{{ phonetic }}/</div>
        <div class="tags">
          <span class="tag level">{{ level }}</span>
          <span class="tag part-of-speech">{{ partOfSpeech }}</span>
          <span class="tag frequency">常用度: {{ frequency }}</span>
        </div>
      </div>

      <!-- 释义 -->
      <section class="section">
        <h3>释义</h3>
        <div class="definitions">
          <div v-for="(def, idx) in definitions" :key="idx" class="definition-item">
            <div class="def-header">
              <span class="def-index">{{ idx + 1 }}.</span>
              <span class="def-pos">{{ def.pos }}</span>
            </div>
            <p class="def-meaning">{{ def.meaning }}</p>
            <div class="def-examples">
              <div v-for="(ex, exIdx) in def.examples" :key="exIdx" class="example">
                <span class="example-text">{{ ex.text }}</span>
                <span class="example-translation">{{ ex.translation }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 同义词/反义词 -->
      <section class="section">
        <h3>同义词与反义词</h3>
        <div class="syn-ant-grid">
          <div class="synonyms">
            <h4>同义词</h4>
            <div class="word-list">
              <span v-for="syn in synonyms" :key="syn" class="word-chip" @click="lookupWord(syn)">{{ syn }}</span>
            </div>
          </div>
          <div class="antonyms">
            <h4>反义词</h4>
            <div class="word-list">
              <span v-for="ant in antonyms" :key="ant" class="word-chip" @click="lookupWord(ant)">{{ ant }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 例句 -->
      <section class="section">
        <h3>例句</h3>
        <div class="examples">
          <div v-for="(ex, idx) in exampleSentences" :key="idx" class="example-sentence">
            <div class="sentence-text">{{ ex.text }}</div>
            <div class="sentence-translation">{{ ex.translation }}</div>
            <div class="sentence-source">来源: {{ ex.source }}</div>
          </div>
        </div>
      </section>

      <!-- 词源 -->
      <section class="section" v-if="etymology">
        <h3>词源</h3>
        <p class="etymology">{{ etymology }}</p>
      </section>

      <!-- 相关词汇 -->
      <section class="section">
        <h3>相关词汇</h3>
        <div class="related-words">
          <span v-for="rel in relatedWords" :key="rel.word" class="related-chip" @click="lookupWord(rel.word)">
            {{ rel.word }} <span class="rel-pos">({{ rel.pos }})</span>
          </span>
        </div>
      </section>
    </main>

    <!-- 底部操作 -->
    <footer class="footer">
      <div class="footer-actions">
        <button class="btn-footer" @click="startReview">开始复习</button>
        <button class="btn-footer" @click="toggleFavorite">
          {{ isFavorite ? '❤️ 已收藏' : '🤍 收藏' }}
        </button>
        <button class="btn-footer" @click="shareWord">分享</button>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 模拟数据
const word = ref('')
const phonetic = ref('')
const level = ref('CET-6')
const partOfSpeech = ref('n.')
const frequency = ref('高频')
const definitions = ref([
  {
    pos: 'n.',
    meaning: '父亲；祖先；创始人',
    examples: [
      { text: 'My father gave me some advice.', translation: '我父亲给了我一些建议。' },
      { text: 'He is the father of modern physics.', translation: '他是现代物理学之父。' }
    ]
  },
  {
    pos: 'v.',
    meaning: '成为…的父亲；创立',
    examples: [
      { text: 'He fathered three children.', translation: '他生了三个孩子。' }
    ]
  }
])
const synonyms = ref(['dad', 'parent', 'progenitor', 'sire'])
const antonyms = ref(['mother', 'child'])
const exampleSentences = ref([
  { text: 'The father of the bride gave a touching speech.', translation: '新娘的父亲发表了感人的演讲。', source: 'The Great Gatsby' },
  { text: 'He is like a father to me.', translation: '他对我来说就像父亲一样。', source: 'Personal' }
])
const etymology = ref('来自古英语 fæder，源自原始日耳曼语 *fadēr，源自原始印欧语 *ph₂tḗr。')
const relatedWords = ref([
  { word: 'fatherly', pos: 'adj.' },
  { word: 'fatherhood', pos: 'n.' },
  { word: 'fatherland', pos: 'n.' },
  { word: 'grandfather', pos: 'n.' }
])
const isFavorite = ref(false)

onMounted(() => {
  // 模拟从路由参数获取单词
  const wordParam = route.params.word || 'father'
  word.value = wordParam
  phonetic.value = 'ˈfɑːðər'
  // 模拟加载数据
  console.log('加载单词详情:', wordParam)
})

const goBack = () => {
  router.back()
}

const addToVocabulary = () => {
  alert(`已添加 "${word.value}" 到生词本`)
}

const speakWord = () => {
  // 模拟发音
  const utterance = new SpeechSynthesisUtterance(word.value)
  utterance.lang = 'en-US'
  window.speechSynthesis.speak(utterance)
}

const lookupWord = (w) => {
  router.push(`/dictionary/${w}`)
}

const startReview = () => {
  router.push('/review')
}

const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value
}

const shareWord = () => {
  alert(`分享单词: ${word.value}`)
}
</script>

<style scoped>
.dictionary-page {
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

.header-actions {
  display: flex;
  gap: 1rem;
}

.btn-add, .btn-speak {
  padding: 10px 20px;
  border-radius: var(--radius-medium);
  font-weight: bold;
  cursor: pointer;
}

.btn-add {
  background-color: var(--color-primary);
  color: white;
  border: none;
}

.btn-speak {
  background-color: var(--color-secondary);
  color: var(--color-text);
  border: 2px solid var(--color-secondary);
}

.main {
  max-width: 900px;
  margin: 0 auto;
}

.word-header {
  text-align: center;
  margin-bottom: 3rem;
  padding: 2rem;
  background-color: white;
  border-radius: var(--radius-large);
  border: 5px solid var(--color-primary);
}

.word {
  font-size: 4rem;
  color: var(--color-primary);
  margin-bottom: 0.5rem;
}

.phonetic {
  font-size: 1.8rem;
  color: var(--color-text-light);
  font-style: italic;
  margin-bottom: 1rem;
}

.tags {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.tag {
  padding: 8px 16px;
  border-radius: var(--radius-medium);
  font-weight: bold;
  font-size: 0.9rem;
}

.tag.level {
  background-color: var(--color-accent);
  color: white;
}

.tag.part-of-speech {
  background-color: var(--color-info);
  color: white;
}

.tag.frequency {
  background-color: var(--color-success);
  color: white;
}

.section {
  background-color: white;
  padding: 2rem;
  border-radius: var(--radius-large);
  margin-bottom: 2rem;
  border: 3px solid var(--color-secondary);
}

.section h3 {
  font-size: 1.8rem;
  color: var(--color-primary);
  margin-bottom: 1.5rem;
  border-bottom: 3px solid var(--color-secondary);
  padding-bottom: 0.5rem;
}

.definitions {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.definition-item {
  padding: 1.5rem;
  border-radius: var(--radius-medium);
  background-color: #f9f9f9;
  border-left: 5px solid var(--color-primary);
}

.def-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.def-index {
  font-weight: bold;
  color: var(--color-primary);
  font-size: 1.2rem;
}

.def-pos {
  background-color: var(--color-info);
  color: white;
  padding: 4px 12px;
  border-radius: var(--radius-small);
  font-size: 0.9rem;
}

.def-meaning {
  font-size: 1.2rem;
  margin-bottom: 1rem;
}

.def-examples {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.example {
  padding: 0.8rem;
  background-color: white;
  border-radius: var(--radius-medium);
  border-left: 3px solid var(--color-secondary);
}

.example-text {
  font-style: italic;
  color: var(--color-text);
}

.example-translation {
  display: block;
  color: var(--color-text-light);
  font-size: 0.9rem;
  margin-top: 0.3rem;
}

.syn-ant-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

@media (max-width: 768px) {
  .syn-ant-grid {
    grid-template-columns: 1fr;
  }
}

.synonyms h4, .antonyms h4 {
  font-size: 1.5rem;
  color: var(--color-primary);
  margin-bottom: 1rem;
}

.word-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
}

.word-chip {
  background-color: var(--color-secondary);
  color: var(--color-text);
  padding: 8px 16px;
  border-radius: var(--radius-medium);
  cursor: pointer;
  transition: all 0.2s;
}

.word-chip:hover {
  background-color: var(--color-primary);
  color: white;
  transform: translateY(-2px);
}

.examples {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.example-sentence {
  padding: 1.5rem;
  border-radius: var(--radius-medium);
  background-color: #f9f9f9;
  border: 2px solid var(--color-secondary);
}

.sentence-text {
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
}

.sentence-translation {
  color: var(--color-text-light);
  margin-bottom: 0.5rem;
}

.sentence-source {
  font-size: 0.9rem;
  color: var(--color-info);
  text-align: right;
}

.etymology {
  font-size: 1.1rem;
  line-height: 1.6;
  color: var(--color-text);
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: var(--radius-medium);
}

.related-words {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.related-chip {
  background-color: var(--color-accent);
  color: white;
  padding: 10px 20px;
  border-radius: var(--radius-medium);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.related-chip:hover {
  background-color: var(--color-primary);
}

.rel-pos {
  font-size: 0.8rem;
  opacity: 0.9;
}

.footer {
  margin-top: 3rem;
  padding: 2rem;
  background-color: white;
  border-radius: var(--radius-large);
  border-top: 5px solid var(--color-secondary);
}

.footer-actions {
  display: flex;
  justify-content: center;
  gap: 2rem;
}

.btn-footer {
  padding: 15px 30px;
  border-radius: var(--radius-large);
  font-weight: bold;
  font-size: 1.1rem;
  cursor: pointer;
  border: none;
  min-width: 150px;
}

.btn-footer:first-child {
  background-color: var(--color-primary);
  color: white;
}

.btn-footer:nth-child(2) {
  background-color: var(--color-secondary);
  color: var(--color-text);
}

.btn-footer:last-child {
  background-color: var(--color-accent);
  color: white;
}
</style>