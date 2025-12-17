<template>
  <div class="vocabulary-container">
    <div class="header">
      <h1>Vocabulary Flashcards</h1>
      <div class="controls-main">
        <div class="study-mode">
          <label for="study-mode-selector">Study Mode:</label>
          <select id="study-mode-selector" v-model="studyMode">
            <option value="sequential">Sequential Review</option>
            <option value="spaced-repetition">Spaced Repetition</option>
          </select>
          <button @click="toggleQuizMode" class="quiz-toggle-btn">{{ quizMode ? 'Flashcard Mode' : 'Quiz Mode' }}</button>
        </div>
        <div class="progress-tracker">
          <span>Progress: {{ knownWordsCount }} / {{ filteredVocabularyList.length }}</span>
          <progress :value="knownWordsCount" :max="filteredVocabularyList.length"></progress>
        </div>
      </div>
      <div class="filter-bar">
          <label for="filter-difficulty">Filter by Difficulty:</label>
          <select id="filter-difficulty" v-model="filterDifficulty">
            <option value="all">All</option>
            <option value="Basic">Basic</option>
            <option value="Intermediate">Intermediate</option>
            <option value="Advanced">Advanced</option>
          </select>
          <label for="filter-status">Filter by Status:</label>
          <select id="filter-status" v-model="filterStatus">
            <option value="all">All</option>
            <option value="new">New</option>
            <option value="known">Known</option>
            <option value="reviewing">Reviewing</option>
          </select>
      </div>
    </div>

    <div v-if="!quizMode" class="flashcard-section">
      <div class="card" :class="{ flipped: isFlipped }" @click="flipCard">
        <div class="card-inner">
          <div class="card-front">
            <h2>{{ currentWord.word }}</h2>
            <p class="phonetic">{{ currentWord.phonetic }}</p>
            <span class="status-indicator" :class="currentWordStatus"></span>
          </div>
          <div class="card-back">
            <p><strong>Definition:</strong> {{ currentWord.definition }}</p>
            <p><strong>Part of Speech:</strong> {{ currentWord.partOfSpeech }}</p>
            <p><strong>Example:</strong> "{{ currentWord.exampleSentence }}"</p>
            <p><strong>Difficulty:</strong> {{ currentWord.difficulty }}</p>
          </div>
        </div>
      </div>

      <div class="controls-card">
        <button @click.stop="flipCard">Flip Card</button>
        <button @click.stop="markAsKnown">Mark as Known</button>
        <button @click.stop="markForReview">Mark for Review</button>
        <button @click.stop="showNextCard">Next Card</button>
      </div>
    </div>

    <div v-else class="quiz-section">
        <div v-if="currentQuizQuestion" class="quiz-card">
            <p class="question"><strong>Definition:</strong> {{ currentQuizQuestion.definition }}</p>
            <div class="options">
                <button 
                    v-for="(option, index) in currentQuizQuestion.options" 
                    :key="index"
                    @click="selectAnswer(option)"
                    :class="getOptionClass(option)"
                    :disabled="answerSelected">
                    {{ option.word }}
                </button>
            </div>
            <div v-if="answerSelected" class="feedback">
                <p v-if="isCorrect" class="correct">Correct!</p>
                <p v-else class="incorrect">Incorrect. The correct answer is <strong>{{ currentQuizQuestion.correctAnswer.word }}</strong>.</p>
                <button @click="nextQuestion">Next Question</button>
            </div>
        </div>
        <div v-else>
            <p>No more questions in this quiz session.</p>
        </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Vocabulary',
  data() {
    return {
      isFlipped: false,
      currentIndex: 0,
      studyMode: 'sequential', // 'sequential' or 'spaced-repetition'
      quizMode: false,
      currentQuizQuestion: null,
      answerSelected: false,
      selectedOption: null,
      isCorrect: false,
      filterDifficulty: 'all',
      filterStatus: 'all',
      vocabularyList: [
        // Basic
        { id: 1, word: 'eloquent', phonetic: '/ˈeləkwənt/', definition: 'Fluent or persuasive in speaking or writing.', exampleSentence: 'The president gave an eloquent speech.', partOfSpeech: 'adjective', difficulty: 'Basic' },
        { id: 2, word: 'benevolent', phonetic: '/bəˈnevələnt/', definition: 'Well meaning and kindly.', exampleSentence: 'A benevolent smile.', partOfSpeech: 'adjective', difficulty: 'Basic' },
        { id: 3, word: 'integrity', phonetic: '/ɪnˈteɡrəti/', definition: 'The quality of being honest and having strong moral principles.', exampleSentence: 'He is a man of great integrity.', partOfSpeech: 'noun', difficulty: 'Basic' },
        { id: 4, word: 'ambiguous', phonetic: '/æmˈbɪɡjuəs/', definition: 'Open to more than one interpretation; not having one obvious meaning.', exampleSentence: 'The ending of the movie was ambiguous.', partOfSpeech: 'adjective', difficulty: 'Basic' },
        { id: 5, word: 'empathy', phonetic: '/ˈempəθi/', definition: 'The ability to understand and share the feelings of another.', exampleSentence: 'He had a lot of empathy for others.', partOfSpeech: 'noun', difficulty: 'Basic' },
        // Intermediate
        { id: 6, word: 'ephemeral', phonetic: '/əˈfemərəl/', definition: 'Lasting for a very short time.', exampleSentence: 'The beauty of the cherry blossoms is ephemeral.', partOfSpeech: 'adjective', difficulty: 'Intermediate' },
        { id: 7, word: 'ubiquitous', phonetic: '/juːˈbɪkwɪtəs/', definition: 'Present, appearing, or found everywhere.', exampleSentence: 'Smartphones have become ubiquitous in modern society.', partOfSpeech: 'adjective', difficulty: 'Intermediate' },
        { id: 8, word: 'serendipity', phonetic: '/ˌserənˈdɪpəti/', definition: 'The occurrence and development of events by chance in a happy or beneficial way.', exampleSentence: 'Finding a twenty-dollar bill on the street was a moment of serendipity.', partOfSpeech: 'noun', difficulty: 'Intermediate' },
        { id: 9, word: 'resilience', phonetic: '/rɪˈzɪliəns/', definition: 'The capacity to recover quickly from difficulties; toughness.', exampleSentence: 'Her resilience in the face of adversity was admirable.', partOfSpeech: 'noun', difficulty: 'Intermediate' },
        { id: 10, word: 'pragmatic', phonetic: '/præɡˈmætɪk/', definition: 'Dealing with things sensibly and realistically in a way that is based on practical rather than theoretical considerations.', exampleSentence: 'A pragmatic approach to problem-solving.', partOfSpeech: 'adjective', difficulty: 'Intermediate' },
        { id: 11, word: 'gregarious', phonetic: '/ɡrɪˈɡeəriəs/', definition: 'Fond of company; sociable.', exampleSentence: 'He was a popular and gregarious man.', partOfSpeech: 'adjective', difficulty: 'Intermediate' },
        { id: 12, word: 'anomaly', phonetic: '/əˈnɒməli/', definition: 'Something that deviates from what is standard, normal, or expected.', exampleSentence: 'There are a number of anomalies in the present system.', partOfSpeech: 'noun', difficulty: 'Intermediate' },
        { id: 13, word: 'lethargic', phonetic: '/ləˈθɑːdʒɪk/', definition: 'Affected by lethargy; sluggish and apathetic.', exampleSentence: 'I felt too miserable and lethargic to get out of bed.', partOfSpeech: 'adjective', difficulty: 'Intermediate' },
        { id: 14, word: 'cogent', phonetic: '/ˈkəʊdʒənt/', definition: '(Of an argument or case) clear, logical, and convincing.', exampleSentence: 'She put forward some cogent reasons for abandoning the plan.', partOfSpeech: 'adjective', difficulty: 'Intermediate' },
        { id: 15, word: 'disparate', phonetic: '/ˈdɪspərət/', definition: 'Essentially different in kind; not allowing comparison.', exampleSentence: 'They inhabit disparate worlds of thought.', partOfSpeech: 'adjective', difficulty: 'Intermediate' },
        // Advanced
        { id: 16, word: 'mellifluous', phonetic: '/məˈlɪfluəs/', definition: 'Pleasingly smooth and musical to hear.', exampleSentence: 'She had a mellifluous voice that soothed the audience.', partOfSpeech: 'adjective', difficulty: 'Advanced' },
        { id: 17, word: 'pulchritudinous', phonetic: '/ˌpʌlkrɪˈtjuːdɪnəs/', definition: 'Having great physical beauty.', exampleSentence: 'The model was known for her pulchritudinous features.', partOfSpeech: 'adjective', difficulty: 'Advanced' },
        { id: 18, 'word': 'epiphany', 'phonetic': '/ɪˈpɪfəni/', 'definition': 'A moment of sudden and great revelation or realization.', 'exampleSentence': 'I had an epiphany and realized what I needed to do with my life.', 'partOfSpeech': 'noun', 'difficulty': 'Advanced' },
        { id: 19, 'word': 'facetious', 'phonetic': '/fəˈsiːʃəs/', 'definition': 'Treating serious issues with deliberately inappropriate humor; flippant.', 'exampleSentence': 'Stop being so facetious; this is a serious matter.', 'partOfSpeech': 'adjective', 'difficulty': 'Advanced' },
        { id: 20, 'word': 'magnanimous', 'phonetic': '/mæɡˈnænɪməs/', 'definition': 'Very generous or forgiving, especially toward a rival or someone less powerful than oneself.', 'exampleSentence': 'She was magnanimous in victory.', 'partOfSpeech': 'adjective', 'difficulty': 'Advanced' },
        { id: 21, word: 'sesquipedalian', phonetic: '/ˌseskwɪpɪˈdeɪliən/', definition: 'Characterized by long words; long-winded.', exampleSentence: 'His sesquipedalian prose was difficult to read.', partOfSpeech: 'adjective', difficulty: 'Advanced' },
        { id: 22, word: 'obfuscate', phonetic: '/ˈɒbfʌskeɪt/', definition: 'Render obscure, unclear, or unintelligible.', exampleSentence: 'The loan contract was designed to obfuscate the real cost of borrowing.', partOfSpeech: 'verb', difficulty: 'Advanced' },
        { id: 23, word: 'proclivity', phonetic: '/prəˈklɪvɪti/', definition: 'A tendency to choose or do something regularly; an inclination or predisposition toward a particular thing.', exampleSentence: 'A proclivity for hard work.', partOfSpeech: 'noun', difficulty: 'Advanced' },
        { id: 24, word: 'sycophant', phonetic: '/ˈsɪkəfænt/', definition: 'A person who acts obsequiously toward someone important in order to gain advantage.', exampleSentence: 'The king was surrounded by sycophants.', partOfSpeech: 'noun', difficulty: 'Advanced' },
        { id: 25, word: 'vicissitude', phonetic: '/vɪˈsɪsɪtjuːd/', definition: 'A change of circumstances or fortune, typically one that is unwelcome or unpleasant.', exampleSentence: 'The vicissitudes of the stock market.', partOfSpeech: 'noun', difficulty: 'Advanced' },
        { id: 26, word: "veracity", phonetic: "/vəˈræsəti/", definition: "Conformity to facts; accuracy.", exampleSentence: "The veracity of his story was questionable.", partOfSpeech: "noun", difficulty: "Advanced" },
        { id: 27, word: "winsome", phonetic: "/ˈwɪnsəm/", definition: "Attractive or appealing in appearance or character.", exampleSentence: "A winsome smile.", partOfSpeech: "adjective", difficulty: "Advanced" },
        { id: 28, word: "zeitgeist", phonetic: "/ˈzaɪtɡaɪst/", definition: "The defining spirit or mood of a particular period of history as shown by the ideas and beliefs of the time.", exampleSentence: "The zeitgeist of the 1960s was one of rebellion and social change.", partOfSpeech: "noun", difficulty: "Advanced" },
        { id: 29, word: "abnegation", phonetic: "/ˌæbnɪˈɡeɪʃən/", definition: "The action of renouncing or rejecting something.", exampleSentence: "She chose a life of abnegation, giving up all her worldly possessions.", partOfSpeech: "noun", difficulty: "Advanced" },
        { id: 30, word: "bilk", phonetic: "/bɪlk/", definition: "Obtain or withhold money from (someone) by deceit or without justification; cheat or defraud.", exampleSentence: "He bilked the elderly couple out of their life savings.", partOfSpeech: "verb", difficulty: "Advanced" },
        { id: 31, word: "catalyst", phonetic: "/ˈkætəlɪst/", definition: "A person or thing that precipitates an event.", exampleSentence: "The assassination of the archduke was the catalyst for World War I.", partOfSpeech: "noun", difficulty: "Intermediate" },
        { id: 32, word: "demagogue", phonetic: "/ˈdeməɡɒɡ/", definition: "A political leader who seeks support by appealing to popular desires and prejudices rather than by using rational argument.", exampleSentence: "The politician was a demagogue who preyed on the fears of the public.", partOfSpeech: "noun", difficulty: "Advanced" },
        { id: 33, word: "enervate", phonetic: "/ˈenəveɪt/", definition: "Cause (someone) to feel drained of energy or vitality; weaken.", exampleSentence: "The hot sun enervated the hikers.", partOfSpeech: "verb", difficulty: "Advanced" },
        { id: 34, word: "fatuous", phonetic: "/ˈfætjuəs/", definition: "Silly and pointless.", exampleSentence: "A fatuous remark.", partOfSpeech: "adjective", difficulty: "Advanced" },
        { id: 35, word: "garrulous", phonetic: "/ˈɡærələs/", definition: "Excessively talkative, especially on trivial matters.", exampleSentence: "He was a garrulous old man who would talk for hours.", partOfSpeech: "adjective", difficulty: "Advanced" },
        { id: 36, word: "hegemony", phonetic: "/hɪˈɡeməni/", definition: "Leadership or dominance, especially by one country or social group over others.", exampleSentence: "The hegemony of the United States has been challenged in recent years.", partOfSpeech: "noun", difficulty: "Advanced" },
        { id: 37, word: "iconoclast", phonetic: "/aɪˈkɒnəklæst/", definition: "A person who attacks cherished beliefs or institutions.", exampleSentence: "The artist was an iconoclast who challenged traditional notions of beauty.", partOfSpeech: "noun", difficulty: "Advanced" },
        { id: 38, word: "juxtaposition", phonetic: "/ˌdʒʌkstəpəˈzɪʃən/", definition: "The fact of two things being seen or placed close together with contrasting effect.", exampleSentence: "The juxtaposition of the old and new buildings was striking.", partOfSpeech: "noun", difficulty: "Intermediate" },
        { id: 39, word: "kowtow", phonetic: "/ˌkaʊˈtaʊ/", definition: "Act in an excessively subservient manner.", exampleSentence: "The employees were expected to kowtow to their boss.", partOfSpeech: "verb", difficulty: "Advanced" },
        { id: 40, word: "laconic", phonetic: "/ləˈkɒnɪk/", definition: "(Of a person, speech, or style of writing) using very few words.", exampleSentence: "His laconic reply suggested a lack of interest in the topic.", partOfSpeech: "adjective", difficulty: "Advanced" },
        { id: 41, word: "maudlin", phonetic: "/ˈmɔːdlɪn/", definition: "Self-pityingly or tearfully sentimental, often through drunkenness.", exampleSentence: "The play was a maudlin melodrama.", partOfSpeech: "adjective", difficulty: "Advanced" },
        { id: 42, word: "nadir", phonetic: "/ˈneɪdɪər/", definition: "The lowest point in the fortunes of a person or organization.", exampleSentence: "The company's nadir was in the early 1990s.", partOfSpeech: "noun", difficulty: "Advanced" },
        { id: 43, word: "obsequious", phonetic: "/əbˈsiːkwiəs/", definition: "Obedient or attentive to an excessive or servile degree.", exampleSentence: "The obsequious waiter was constantly hovering over their table.", partOfSpeech: "adjective", difficulty: "Advanced" },
        { id: 44, word: "palliative", phonetic: "/ˈpæliətɪv/", definition: "(Of a treatment or medicine) relieving pain or alleviating a problem without dealing with the underlying cause.", exampleSentence: "The doctor prescribed a palliative to ease the patient's suffering.", partOfSpeech: "adjective", difficulty: "Advanced" },
        { id: 45, word: "querulous", phonetic: "/ˈkwerʊləs/", definition: "Complaining in a petulant or whining manner.", exampleSentence: "She became querulous and demanding in her old age.", partOfSpeech: "adjective", difficulty: "Advanced" },
        { id: 46, word: "rancor", phonetic: "/ˈræŋkər/", definition: "Bitterness or resentfulness, especially when long-standing.", exampleSentence: "He felt a deep-seated rancor towards his former boss.", partOfSpeech: "noun", difficulty: "Advanced" },
        { id: 47, word: "salient", phonetic: "/ˈseɪliənt/", definition: "Most noticeable or important.", exampleSentence: "The salient points of the report.", partOfSpeech: "adjective", difficulty: "Intermediate" },
        { id: 48, word: "taciturn", phonetic: "/ˈtæsɪtɜːn/", definition: "(Of a person) reserved or uncommunicative in speech; saying little.", exampleSentence: "He was a taciturn man who rarely spoke.", partOfSpeech: "adjective", difficulty: "Advanced" },
        { id: 49, word: "unctuous", phonetic: "/ˈʌŋktʃuəs/", definition: "(Of a person) excessively or ingratiatingly flattering; oily.", exampleSentence: "He was an unctuous and insincere man.", partOfSpeech: "adjective", difficulty: "Advanced" },
        { id: 50, word: "vacillate", phonetic: "/ˈvæsəleɪt/", definition: "Alternate or waver between different opinions or actions; be indecisive.", exampleSentence: "He vacillated between accepting the job and turning it down.", partOfSpeech: "verb", difficulty: "Advanced" },
        { id: 51, word: 'alacrity', phonetic: '/əˈlækrɪti/', definition: 'Brisk and cheerful readiness.', exampleSentence: 'She accepted the invitation with alacrity.', partOfSpeech: 'noun', difficulty: 'Advanced' },
        { id: 52, word: 'conundrum', phonetic: '/kəˈnʌndrəm/', definition: 'A confusing and difficult problem or question.', exampleSentence: 'The team faced a logistical conundrum.', partOfSpeech: 'noun', difficulty: 'Intermediate' },
        { id: 53, word: 'diatribe', phonetic: '/ˈdaɪətraɪb/', definition: 'A forceful and bitter verbal attack against someone or something.', exampleSentence: 'He launched into a long diatribe against the government.', partOfSpeech: 'noun', difficulty: 'Advanced' },
        { id: 54, word: 'ebullient', phonetic: '/ɪˈbʌliənt/', definition: 'Cheerful and full of energy.', exampleSentence: 'She was in an ebullient mood.', partOfSpeech: 'adjective', difficulty: 'Intermediate' },
        { id: 55, word: 'fastidious', phonetic: '/fæˈstɪdiəs/', definition: 'Very attentive to and concerned about accuracy and detail.', exampleSentence: 'He is fastidious about his appearance.', partOfSpeech: 'adjective', difficulty: 'Intermediate' },
      ],
      learningStatus: {}, // { wordId: { status: 'new' | 'known' | 'reviewing', lastReviewed: null | Date } }
    };
  },
  computed: {
    filteredVocabularyList() {
      return this.vocabularyList.filter(word => {
        const difficultyMatch = this.filterDifficulty === 'all' || word.difficulty === this.filterDifficulty;
        const statusMatch = this.filterStatus === 'all' || this.getWordStatus(word.id) === this.filterStatus;
        return difficultyMatch && statusMatch;
      });
    },
    currentWord() {
      if (this.filteredVocabularyList.length === 0) return {};
      return this.filteredVocabularyList[this.currentIndex] || {};
    },
    currentWordStatus() {
      return this.getWordStatus(this.currentWord.id);
    },
    knownWordsCount() {
      return Object.values(this.learningStatus).filter(s => s.status === 'known').length;
    }
  },
  watch: {
    filterDifficulty() {
      this.currentIndex = 0;
    },
    filterStatus() {
      this.currentIndex = 0;
    }
  },
  methods: {
    getWordStatus(wordId) {
        const status = this.learningStatus[wordId];
        return status ? status.status : 'new';
    },
    flipCard() {
      this.isFlipped = !this.isFlipped;
    },
    markAsKnown() {
        if (!this.currentWord.id) return;
        this.updateStatus(this.currentWord.id, 'known');
        this.showNextCard();
    },
    markForReview() {
        if (!this.currentWord.id) return;
        this.updateStatus(this.currentWord.id, 'reviewing');
        this.showNextCard();
    },
    updateStatus(wordId, status) {
      const newStatus = { ...this.learningStatus[wordId], status, lastReviewed: new Date() };
      this.$set(this.learningStatus, wordId, newStatus);
      this.saveProgress();
    },
    showNextCard() {
      this.isFlipped = false;
      if (this.filteredVocabularyList.length <= 1) return;
      setTimeout(() => {
        if (this.studyMode === 'sequential') {
          this.currentIndex = (this.currentIndex + 1) % this.filteredVocabularyList.length;
        } else { // Spaced Repetition
          this.currentIndex = this.getNextCardIndexForSpacedRepetition();
        }
      }, 150);
    },
    getNextCardIndexForSpacedRepetition() {
      const reviewingWords = this.filteredVocabularyList.filter(word => this.getWordStatus(word.id) === 'reviewing');
      const newWords = this.filteredVocabularyList.filter(word => this.getWordStatus(word.id) === 'new');

      if (reviewingWords.length > 0) {
        reviewingWords.sort((a, b) => {
          const timeA = this.learningStatus[a.id]?.lastReviewed ? new Date(this.learningStatus[a.id].lastReviewed).getTime() : 0;
          const timeB = this.learningStatus[b.id]?.lastReviewed ? new Date(this.learningStatus[b.id].lastReviewed).getTime() : 0;
          return timeA - timeB;
        });
        return this.filteredVocabularyList.findIndex(w => w.id === reviewingWords.id);
      } else if (newWords.length > 0) {
        return this.filteredVocabularyList.findIndex(w => w.id === newWords.id);
      } else {
        // All words are known, cycle through them
        return (this.currentIndex + 1) % this.filteredVocabularyList.length;
      }
    },
    toggleQuizMode() {
        this.quizMode = !this.quizMode;
        if (this.quizMode) {
            this.generateQuizQuestion();
        }
    },
    generateQuizQuestion() {
        const wordsForQuiz = this.filteredVocabularyList.filter(w => this.getWordStatus(w.id) !== 'known');
        if (wordsForQuiz.length === 0) {
            this.currentQuizQuestion = null;
            return;
        }

        const questionWord = wordsForQuiz[Math.floor(Math.random() * wordsForQuiz.length)];
        const incorrectOptions = this.vocabularyList
            .filter(w => w.id !== questionWord.id)
            .sort(() => 0.5 - Math.random())
            .slice(0, 3);

        const options = [...incorrectOptions, questionWord].sort(() => 0.5 - Math.random());

        this.currentQuizQuestion = {
            definition: questionWord.definition,
            options: options,
            correctAnswer: questionWord
        };
        this.answerSelected = false;
        this.isCorrect = false;
        this.selectedOption = null;
    },
    selectAnswer(option) {
        this.answerSelected = true;
        this.selectedOption = option;
        if (option.id === this.currentQuizQuestion.correctAnswer.id) {
            this.isCorrect = true;
            this.updateStatus(option.id, 'known');
        } else {
            this.isCorrect = false;
            this.updateStatus(this.currentQuizQuestion.correctAnswer.id, 'reviewing');
        }
    },
    getOptionClass(option) {
        if (!this.answerSelected) return '';
        if (option.id === this.currentQuizQuestion.correctAnswer.id) return 'correct-answer';
        if (this.selectedOption && option.id === this.selectedOption.id) return 'incorrect-answer';
        return '';
    },
    nextQuestion() {
        this.generateQuizQuestion();
    },
    saveProgress() {
      localStorage.setItem('vocabularyProgress', JSON.stringify(this.learningStatus));
    },
    loadProgress() {
      const savedProgress = localStorage.getItem('vocabularyProgress');
      const loadedStatus = savedProgress ? JSON.parse(savedProgress) : {};
      
      this.vocabularyList.forEach(word => {
        if (!loadedStatus[word.id]) {
          loadedStatus[word.id] = { status: 'new', lastReviewed: null };
        }
      });
      this.learningStatus = loadedStatus;
    }
  },
  created() {
    this.loadProgress();
  },
  beforeUnmount() {
    // Log a session summary before the component is destroyed
    console.log("--- Study Session Summary ---");
    console.log(`Total words in list: ${this.vocabularyList.length}`);
    console.log(`Words you marked as 'known': ${this.knownWordsCount}`);
    const reviewingCount = Object.values(this.learningStatus).filter(s => s.status === 'reviewing').length;
    console.log(`Words for review: ${reviewingCount}`);
    console.log("-----------------------------");
  }
};
</script>

<style scoped>
.vocabulary-container {
  max-width: 700px;
  margin: 20px auto;
  padding: 20px;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  color: #2c3e50;
  background-color: #f5f7fa;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.header {
  margin-bottom: 25px;
  text-align: center;
}

h1 {
  color: #1a2533;
}

.controls-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  margin-top: 15px;
}

.study-mode, .progress-tracker {
  display: flex;
  align-items: center;
  gap: 10px;
}

label {
  font-weight: bold;
}

select, progress {
  border-radius: 5px;
}

select {
  padding: 5px;
  border: 1px solid #ccc;
}

progress {
  width: 150px;
  height: 20px;
}

.filter-bar {
    margin-top: 15px;
    padding: 10px;
    background-color: #fff;
    border-radius: 8px;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 15px;
}

.flashcard-section, .quiz-section {
  perspective: 1000px;
  min-height: 350px;
}

.card {
  width: 100%;
  height: 280px;
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.7s cubic-bezier(0.4, 0.2, 0.2, 1);
  cursor: pointer;
}

.card.flipped {
  transform: rotateY(180deg);
}

.card-inner {
  position: absolute;
  width: 100%;
  height: 100%;
  text-align: center;
  transition: transform 0.7s;
  transform-style: preserve-3d;
  box-shadow: 0 6px 15px rgba(0,0,0,0.1);
  border-radius: 15px;
}

.card-front, .card-back {
  box-sizing: border-box;
  position: absolute;
  width: 100%;
  height: 100%;
  -webkit-backface-visibility: hidden;
  backface-visibility: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 25px;
  border-radius: 15px;
}

.card-front {
  background: linear-gradient(135deg, #ffffff, #eef2f7);
}

.card-back {
  background-color: #e8f4f8;
  transform: rotateY(180deg);
  text-align: left;
  align-items: flex-start;
  font-size: 1.1em;
}

.card-front h2 {
  font-size: 3em;
  margin: 0;
}

.phonetic {
  color: #555;
  font-size: 1.3em;
  margin-top: 10px;
}

.status-indicator {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 15px;
  height: 15px;
  border-radius: 50%;
}

.status-indicator.new { background-color: #3498db; }
.status-indicator.known { background-color: #2ecc71; }
.status-indicator.reviewing { background-color: #f1c40f; }

.controls-card {
  margin-top: 25px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

button {
  padding: 12px 25px;
  font-size: 1em;
  cursor: pointer;
  border: none;
  border-radius: 8px;
  background-color: #3498db;
  color: white;
  transition: background-color 0.3s, transform 0.2s;
  -webkit-tap-highlight-color: transparent;
}

button:hover:not(:disabled) {
  background-color: #2980b9;
  transform: translateY(-2px);
}

button:disabled {
    cursor: not-allowed;
    opacity: 0.7;
}

.quiz-toggle-btn {
    background-color: #9b59b6;
}
.quiz-toggle-btn:hover {
    background-color: #8e44ad;
}

.quiz-section {
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
}
.quiz-card .question {
    font-size: 1.2em;
    margin-bottom: 20px;
}
.options {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
}
.options button {
    width: 100%;
    background-color: #ecf0f1;
    color: #34495e;
    border: 2px solid transparent;
    text-align: left;
    padding: 15px;
}
.options button:hover:not(:disabled) {
    background-color: #bdc3c7;
}
.options button.correct-answer {
    background-color: #2ecc71;
    color: white;
    border-color: #27ae60;
}
.options button.incorrect-answer {
    background-color: #e74c3c;
    color: white;
    border-color: #c0392b;
}
.feedback {
    margin-top: 20px;
    text-align: center;
}
.feedback button {
    margin-top: 15px;
}
.feedback .correct {
    color: #2ecc71;
    font-weight: bold;
}
.feedback .incorrect {
    color: #e74c3c;
    font-weight: bold;
}
</style>