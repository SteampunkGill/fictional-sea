/* eslint-disable */
<template>
  <div class="review-page">
    <header class="header">
      <h1>智能复习</h1>
      <div class="header-actions">
        <button class="btn-settings" @click="openSettings">⚙️ 设置</button>
        <button class="btn-stats" @click="openStats">📊 统计</button>
      </div>
    </header>

    <main class="main">
      <div class="review-progress">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
        </div>
        <div class="progress-text">
          今日进度: {{ completed }}/{{ total }} ({{ Math.round(progress) }}%)
        </div>
      </div>

      <div class="review-card" v-if="currentCard">
        <div class="card-front" v-show="!showAnswer">
          <div class="card-content">
            <h2 class="word">{{ currentCard.word }}</h2>
            <div class="hint" v-if="currentCard.hint">提示: {{ currentCard.hint }}</div>
            <button class="btn-flip" @click="flipCard">显示答案</button>
          </div>
        </div>
        <div class="card-back" v-show="showAnswer">
          <div class="card-content">
            <h2 class="word">{{ currentCard.word }}</h2>
            <div class="phonetic">{{ currentCard.phonetic }}</div>
            <div class="meaning">{{ currentCard.meaning }}</div>
            <div class="example" v-if="currentCard.example">
              <strong>例句:</strong> {{ currentCard.example }}
            </div>
            <div class="actions">
              <button class="btn-know" @click="markKnown">认识</button>
              <button class="btn-dont-know" @click="markUnknown">不认识</button>
              <button class="btn-skip" @click="skipCard">跳过</button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>今日复习已完成！🎉</p>
        <button class="btn-primary" @click="resetReview">重新开始</button>
      </div>

      <div class="review-controls">
        <button class="btn-control" @click="prevCard" :disabled="cardIndex === 0">上一张</button>
        <button class="btn-control" @click="nextCard" :disabled="cardIndex >= cards.length - 1">下一张</button>
        <button class="btn-control" @click="shuffleCards">🔀 打乱</button>
      </div>
    </main>

    <!-- 复习设置弹窗 -->
    <div class="modal" v-if="showSettings">
      <div class="modal-content">
        <h3>复习设置</h3>
        <div class="setting-item">
          <label>每日复习数量</label>
          <input type="number" v-model="dailyLimit" min="1" max="100" />
        </div>
        <div class="setting-item">
          <label>复习模式</label>
          <select v-model="reviewMode">
            <option value="normal">普通模式</option>
            <option value="spaced">间隔重复</option>
            <option value="test">测试模式</option>
          </select>
        </div>
        <div class="setting-item">
          <label>
            <input type="checkbox" v-model="autoPlayAudio" />
            自动播放发音
          </label>
        </div>
        <div class="modal-actions">
          <button class="btn-save" @click="saveSettings">保存</button>
          <button class="btn-cancel" @click="closeSettings">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/* eslint-disable vue/multi-word-component-names */
import { ref, computed } from 'vue'
import { defineOptions } from 'vue'

defineOptions({
  name: 'ReviewPage'
})

// 模拟数据
const cards = ref([
  { id: 1, word: 'serendipity', phonetic: '/ˌserənˈdɪpəti/', meaning: '意外发现珍奇事物的本领', example: 'Traveling is full of serendipity.', hint: '与幸运发现相关' },
  { id: 2, word: 'ephemeral', phonetic: '/ɪˈfemərəl/', meaning: '短暂的，瞬息的', example: 'The beauty of cherry blossoms is ephemeral.', hint: '形容短暂的事物' },
  { id: 3, word: 'ubiquitous', phonetic: '/juːˈbɪkwɪtəs/', meaning: '无所不在的', example: 'Smartphones are ubiquitous in modern life.', hint: '形容普遍存在' },
  { id: 4, word: 'melancholy', phonetic: '/ˈmelənkəli/', meaning: '忧郁，悲伤', example: 'The rainy day filled her with melancholy.', hint: '情绪相关' },
  { id: 5, word: 'eloquent', phonetic: '/ˈeləkwənt/', meaning: '雄辩的，有口才的', example: 'His eloquent speech moved the audience.', hint: '形容表达能力' },
])

const cardIndex = ref(0)
const showAnswer = ref(false)
const completed = ref(2)
const total = ref(cards.value.length)
const showSettings = ref(false)
const dailyLimit = ref(20)
const reviewMode = ref('normal')
const autoPlayAudio = ref(true)

const currentCard = computed(() => cards.value[cardIndex.value] || null)
const progress = computed(() => (completed.value / total.value) * 100)

const flipCard = () => {
  showAnswer.value = !showAnswer.value
}

const markKnown = () => {
  alert(`标记为认识: ${currentCard.value.word}`)
  completed.value++
  nextCard()
}

const markUnknown = () => {
  alert(`标记为不认识: ${currentCard.value.word}`)
  nextCard()
}

const skipCard = () => {
  alert(`跳过: ${currentCard.value.word}`)
  nextCard()
}

const nextCard = () => {
  showAnswer.value = false
  if (cardIndex.value < cards.value.length - 1) {
    cardIndex.value++
  } else {
    cardIndex.value = 0
  }
}

const prevCard = () => {
  showAnswer.value = false
  if (cardIndex.value > 0) {
    cardIndex.value--
  }
}

const shuffleCards = () => {
  // 简单打乱
  cards.value = [...cards.value].sort(() => Math.random() - 0.5)
  cardIndex.value = 0
  alert('卡片已打乱')
}

const resetReview = () => {
  completed.value = 0
  cardIndex.value = 0
  showAnswer.value = false
}

const openSettings = () => {
  showSettings.value = true
}

const closeSettings = () => {
  showSettings.value = false
}

const saveSettings = () => {
  alert(`设置已保存: 每日 ${dailyLimit.value} 个, 模式 ${reviewMode.value}`)
  closeSettings()
}

const openStats = () => {
  alert('打开统计页面')
}
</script>

<style scoped>
.review-page {
  min-height: 100vh;
  background-color: var(--color-background);
  padding: 2rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header h1 {
  font-size: 3rem;
  color: var(--color-primary);
}

.header-actions {
  display: flex;
  gap: 1rem;
}

.btn-settings, .btn-stats {
  padding: 10px 20px;
  border-radius: var(--radius-medium);
  border: 2px solid var(--color-secondary);
  background-color: white;
  font-weight: bold;
  cursor: pointer;
}

.main {
  max-width: 800px;
  margin: 0 auto;
}

.review-progress {
  margin-bottom: 2rem;
}

.progress-bar {
  height: 20px;
  background-color: #eee;
  border-radius: var(--radius-large);
  overflow: hidden;
  border: 3px solid var(--color-secondary);
}

.progress-fill {
  height: 100%;
  background-color: var(--color-success);
  transition: width 0.3s;
}

.progress-text {
  text-align: center;
  margin-top: 0.5rem;
  font-weight: bold;
  color: var(--color-text);
}

.review-card {
  background-color: white;
  border-radius: var(--radius-large);
  padding: 3rem;
  border: 5px solid var(--color-primary);
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2rem;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.card-content {
  text-align: center;
}

.word {
  font-size: 4rem;
  color: var(--color-primary);
  margin-bottom: 1rem;
}

.phonetic {
  font-size: 1.8rem;
  color: var(--color-text-light);
  font-style: italic;
  margin-bottom: 1rem;
}

.meaning {
  font-size: 2rem;
  margin-bottom: 2rem;
  color: var(--color-text);
}

.example {
  font-size: 1.2rem;
  color: var(--color-text-light);
  margin-bottom: 2rem;
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: var(--radius-medium);
}

.hint {
  font-size: 1.2rem;
  color: var(--color-info);
  margin-bottom: 2rem;
}

.btn-flip {
  padding: 15px 30px;
  background-color: var(--color-accent);
  color: white;
  border: none;
  border-radius: var(--radius-large);
  font-size: 1.2rem;
  cursor: pointer;
}

.actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 2rem;
}

.btn-know, .btn-dont-know, .btn-skip {
  padding: 15px 30px;
  border-radius: var(--radius-large);
  border: none;
  font-weight: bold;
  font-size: 1.1rem;
  cursor: pointer;
  min-width: 120px;
}

.btn-know {
  background-color: var(--color-success);
  color: white;
}

.btn-dont-know {
  background-color: var(--color-danger);
  color: white;
}

.btn-skip {
  background-color: var(--color-secondary);
  color: var(--color-text);
}

.empty-state {
  text-align: center;
  padding: 4rem;
  background-color: white;
  border-radius: var(--radius-large);
  border: 5px dashed var(--color-secondary);
}

.empty-state p {
  font-size: 2rem;
  color: var(--color-text-light);
  margin-bottom: 2rem;
}

.btn-primary {
  padding: 15px 40px;
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-large);
  font-size: 1.5rem;
  cursor: pointer;
}

.review-controls {
  display: flex;
  justify-content: center;
  gap: 2rem;
  margin-top: 2rem;
}

.btn-control {
  padding: 12px 24px;
  border-radius: var(--radius-medium);
  border: 2px solid var(--color-secondary);
  background-color: white;
  font-weight: bold;
  cursor: pointer;
}

.btn-control:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
  max-width: 500px;
  width: 90%;
}

.modal-content h3 {
  font-size: 2rem;
  color: var(--color-primary);
  margin-bottom: 2rem;
}

.setting-item {
  margin-bottom: 1.5rem;
}

.setting-item label {
  display: block;
  font-weight: bold;
  margin-bottom: 0.5rem;
  color: var(--color-text);
}

.setting-item input, .setting-item select {
  width: 100%;
  padding: 10px;
  border-radius: var(--radius-medium);
  border: 2px solid var(--color-secondary);
  font-size: 1rem;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.btn-save, .btn-cancel {
  padding: 10px 20px;
  border-radius: var(--radius-medium);
  border: none;
  font-weight: bold;
  cursor: pointer;
}

.btn-save {
  background-color: var(--color-primary);
  color: white;
}

.btn-cancel {
  background-color: var(--color-secondary);
  color: var(--color-text);
}
</style>