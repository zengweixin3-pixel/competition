<template>
  <div class="space-y-6" v-loading="loading">
    <section class="grid grid-cols-1 gap-5 xl:grid-cols-[0.92fr_2.08fr]">
      <div class="card-gradient rounded-[28px] p-5">
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="text-sm uppercase tracking-[0.22em] text-text-muted">Learning DNA</p>
            <h2 class="mt-3 text-2xl font-bold text-text-primary">
              {{ userStore.learningDNA.type || '待分析' }}
            </h2>
            <p v-if="userStore.learningDNA.description" class="mt-3 text-sm leading-6 text-text-secondary">{{ userStore.learningDNA.description }}</p>
          </div>
          <el-button link type="primary" @click="router.push('/learning-dna')">
            查看详情
          </el-button>
        </div>

        <div class="mt-5 h-56">
          <v-chart class="h-full w-full" :option="radarOption" autoresize />
        </div>

        <div class="mt-4 flex flex-wrap gap-2">
          <el-tag
            v-for="tag in userStore.learningDNA.tags.slice(0, 4)"
            :key="tag"
            size="small"
            effect="plain"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>

      <div class="relative overflow-hidden rounded-[32px] border border-primary/20 bg-[#0d1324] p-5">
        <div class="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(99,102,241,0.22),transparent_32%),radial-gradient(circle_at_bottom_left,rgba(236,72,153,0.12),transparent_34%)]" />

        <div class="relative flex flex-col gap-6">
          <div class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <p class="text-sm uppercase tracking-[0.22em] text-text-muted">Today Snapshot</p>
              <h2 class="mt-3 text-2xl font-bold text-text-primary">今日学习概览</h2>
            </div>
            <div class="rounded-2xl border border-emerald-400/25 bg-emerald-400/10 px-4 py-2 text-xs text-emerald-300">
              Live
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3 xl:grid-cols-4">
            <div
              v-for="card in overviewCards"
              :key="card.label"
              class="rounded-3xl border border-white/6 bg-white/5 p-3.5 shadow-[0_18px_40px_rgba(10,14,26,0.26)] backdrop-blur"
            >
              <p class="text-xs uppercase tracking-[0.18em] text-text-muted">{{ card.label }}</p>
              <p class="mt-3 text-2xl font-bold text-text-primary">{{ card.value }}</p>
            </div>
          </div>

          <div class="rounded-[28px] border border-white/6 bg-[#10192f]/88 p-4">
            <div class="flex items-center justify-between gap-4">
              <div>
                <h3 class="text-lg font-bold text-text-primary">近 7 天学习趋势</h3>
              </div>
              <el-tag size="small" effect="plain">7D</el-tag>
            </div>
            <div class="mt-3 h-52">
              <v-chart class="h-full w-full" :option="studyTrendOption" autoresize />
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="grid grid-cols-1 gap-5 xl:grid-cols-[1.55fr_0.9fr]">
      <div class="relative overflow-hidden rounded-[32px] border border-primary/20 bg-[#0a111f]">
        <div class="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(99,102,241,0.18),transparent_28%),radial-gradient(circle_at_88%_10%,rgba(6,182,212,0.16),transparent_24%),radial-gradient(circle_at_80%_100%,rgba(236,72,153,0.12),transparent_30%)]" />

        <div class="relative flex h-full flex-col p-5">
          <div class="flex flex-col gap-4 border-b border-white/8 pb-5 lg:flex-row lg:items-center lg:justify-between">
            <div class="flex items-center gap-4">
              <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-primary shadow-glow">
                <span class="text-base font-bold text-white">AI</span>
              </div>
              <div>
                <div class="flex items-center gap-3">
                  <h3 class="text-xl font-bold text-text-primary">AI 学习助手</h3>
                  <span class="flex items-center gap-2 text-xs text-emerald-300">
                    <span class="h-2 w-2 rounded-full bg-emerald-400 shadow-[0_0_12px_rgba(52,211,153,0.9)]" />
                    在线
                  </span>
                </div>
              </div>
            </div>

            <div class="flex flex-wrap items-center gap-2">
              <el-tag v-for="tag in aiTags" :key="tag" size="small" effect="dark" class="!border-0 !bg-white/10">
                {{ tag }}
              </el-tag>
              <el-button type="primary" plain @click="router.push('/ai-chat')">
                深入咨询
                <el-icon class="ml-1"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>

          <div class="mt-4 h-[360px] space-y-3 overflow-y-auto pr-1">
            <div
              v-for="(message, index) in chatMessages"
              :key="index"
              class="flex gap-3"
              :class="message.isUser ? 'flex-row-reverse' : ''"
            >
              <div class="mt-1 flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl text-sm font-bold"
                :class="message.isUser ? 'bg-white/10 text-text-primary' : 'bg-gradient-primary text-white shadow-glow'">
                {{ message.isUser ? '我' : 'AI' }}
              </div>

              <div class="max-w-[84%]" :class="message.isUser ? 'text-right' : ''">
                <div class="mb-2 flex items-center gap-2 text-xs text-text-muted" :class="message.isUser ? 'flex-row-reverse' : ''">
                  <span>{{ message.isUser ? '你' : 'AI 学习助手' }}</span>
                  <span>{{ message.time }}</span>
                </div>
                <div
                  class="inline-block rounded-[24px] px-4 py-3 text-left text-sm leading-6"
                  :class="message.isUser ? 'bg-primary text-white shadow-[0_12px_24px_rgba(99,102,241,0.28)]' : 'border border-white/6 bg-white/6 text-text-primary'"
                >
                  <p class="whitespace-pre-wrap">{{ message.content }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-5 border-t border-white/8 pt-4">
            <div class="flex flex-wrap gap-2">
              <button
                v-for="prompt in quickPrompts"
                :key="prompt"
                type="button"
                class="rounded-full border border-white/10 bg-white/5 px-4 py-2 text-xs text-text-secondary transition hover:border-primary/40 hover:bg-primary/10 hover:text-text-primary"
                @click="fillQuickPrompt(prompt)"
              >
                {{ prompt }}
              </button>
            </div>

            <div class="mt-4 flex items-center gap-3 rounded-[24px] border border-white/10 bg-white/5 p-3">
              <el-input
                v-model="chatInput"
                class="flex-1"
                placeholder="输入你的学习问题..."
                @keyup.enter="sendMessage"
              />
              <el-button type="primary" :loading="sending" @click="sendMessage">
                发送
                <el-icon class="ml-1"><Promotion /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="card-gradient rounded-[30px] p-5">
        <div class="flex items-center justify-between gap-4">
          <div>
            <h3 class="text-lg font-bold text-text-primary">情绪中心</h3>
          </div>
          <el-button link type="primary" @click="router.push('/emotion')">查看记录</el-button>
        </div>

        <div class="mt-5 rounded-3xl border border-primary/15 bg-primary/8 px-4 py-3">
            <p class="text-xs uppercase tracking-[0.16em] text-text-muted">Mood</p>
          <p class="mt-2 text-base font-semibold text-text-primary">{{ currentEmotionLabel }}</p>
        </div>

        <div class="mt-4 grid grid-cols-2 gap-2.5">
          <button
            v-for="emotion in emotions"
            :key="emotion.value"
            type="button"
            class="rounded-3xl border px-3.5 py-3.5 text-left transition"
            :class="selectedEmotion === emotion.value ? 'border-primary bg-primary/12 shadow-[0_16px_30px_rgba(99,102,241,0.18)]' : 'border-white/8 bg-white/4 hover:border-white/16 hover:bg-white/8'"
            @click="selectedEmotion = emotion.value"
          >
            <div class="text-3xl">{{ emotion.icon }}</div>
            <p class="mt-3 text-sm font-semibold text-text-primary">{{ emotion.label }}</p>
            <p class="mt-1 text-xs text-text-muted">{{ emotion.desc }}</p>
          </button>
        </div>

        <el-input
          v-model="moodNote"
          class="mt-4"
          type="textarea"
          :rows="3"
          placeholder="写下当前状态..."
        />

        <el-button class="mt-4 w-full" type="primary" :loading="savingEmotion" @click="saveMood">
          保存当前情绪
        </el-button>
      </div>
    </section>

    <section class="grid grid-cols-1 gap-5 xl:grid-cols-2">
      <div class="card-gradient rounded-[30px] p-5">
        <div class="flex items-center justify-between gap-4">
          <div>
            <h3 class="text-lg font-bold text-text-primary">今日学习计划</h3>
          </div>
          <el-tag type="primary">{{ stats.completedTasks }}/{{ stats.totalTasks }}</el-tag>
        </div>

        <div class="mt-4 space-y-3">
          <div
            v-for="task in todayTasks"
            :key="task.id"
            class="rounded-3xl border border-white/8 bg-white/5 p-3.5"
          >
            <div class="flex items-start justify-between gap-4">
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <span class="inline-flex h-8 w-8 items-center justify-center rounded-2xl"
                    :class="task.completed ? 'bg-emerald-400/16 text-emerald-300' : 'bg-amber-400/12 text-amber-300'">
                    {{ task.completed ? '已' : '待' }}
                  </span>
                  <div class="min-w-0">
                    <p class="truncate text-sm font-semibold text-text-primary">{{ task.name }}</p>
                    <p class="mt-1 text-xs text-text-muted">{{ task.subject }} · {{ task.duration }} 分钟</p>
                  </div>
                </div>
                <p class="mt-3 text-sm leading-5 text-text-secondary line-clamp-2">{{ task.goal || '继续在学习计划页完善任务目标。' }}</p>
              </div>
              <el-tag :type="task.completed ? 'success' : 'warning'">
                {{ task.completed ? '已完成' : '待完成' }}
              </el-tag>
            </div>
          </div>

          <el-empty v-if="todayTasks.length === 0" description="今天还没有学习计划" />
        </div>
      </div>

      <div class="card-gradient rounded-[30px] p-5">
        <div class="flex items-center justify-between gap-4">
          <div>
            <h3 class="text-lg font-bold text-text-primary">待复习错题</h3>
          </div>
          <el-tag type="danger">{{ pendingMistakeCount }} 题待复习</el-tag>
        </div>

        <div class="mt-4 space-y-3">
          <div
            v-for="mistake in dashboardMistakes"
            :key="mistake.id"
            class="rounded-3xl border border-white/8 bg-white/5 p-3.5"
          >
            <div class="flex items-start justify-between gap-4">
              <div class="min-w-0">
                <p class="truncate text-sm font-semibold text-text-primary">{{ mistake.title }}</p>
                <div class="mt-2 flex flex-wrap gap-2 text-xs text-text-muted">
                  <span>{{ mistake.subject }}</span>
                  <span>·</span>
                  <span>{{ mistake.mistakeType }}</span>
                  <span>·</span>
                  <span>错误 {{ mistake.errorCount }} 次</span>
                </div>
              </div>
              <el-tag size="small" type="warning">{{ mistake.statusLabel }}</el-tag>
            </div>
          </div>

          <el-empty v-if="dashboardMistakes.length === 0" description="暂无错题数据" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { use } from 'echarts/core'
import { BarChart, RadarChart } from 'echarts/charts'
import { GridComponent, LegendComponent, RadarComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import { ArrowRight, Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { aiApi, emotionApi, mistakeApi, studyApi } from '@/api'
import { useUserStore } from '@/stores/user'

use([CanvasRenderer, RadarChart, BarChart, GridComponent, TooltipComponent, LegendComponent, RadarComponent])

type StudyTask = {
  id: number
  name: string
  goal: string
  subject: string
  duration: number
  completed: boolean
}

type MistakeSummary = {
  id: number
  title: string
  subject: string
  mistakeType: string
  errorCount: number
  statusLabel: string
}

type ChatBubble = {
  isUser: boolean
  content: string
  time: string
}

const userStore = useUserStore()
const router = useRouter()

const loading = ref(true)
const sending = ref(false)
const savingEmotion = ref(false)
const pendingMistakeCount = ref(0)
const todayTasks = ref<StudyTask[]>([])
const dashboardMistakes = ref<MistakeSummary[]>([])
const studyTrend = ref<Array<{ date: string; duration: number }>>([])
const chatMessages = ref<ChatBubble[]>([])
const chatInput = ref('')
const selectedEmotion = ref('')
const moodNote = ref('')

const aiTags = ['计划安排', '错题复盘', '高数', '英语', '效率优化']
const quickPrompts = ['帮我安排今晚学习顺序', '先复盘哪几道错题', '提高英语阅读效率', '把今天计划拆成可执行步骤']
const emotions = [
  { label: '超棒', value: 'great', icon: '😄', desc: '状态很好，适合推进主任务' },
  { label: '还不错', value: 'good', icon: '🙂', desc: '节奏稳定，适合稳步学习' },
  { label: '一般', value: 'normal', icon: '😐', desc: '状态普通，先完成小任务' },
  { label: '有点累', value: 'tired', icon: '😮‍💨', desc: '先降低任务难度，避免硬扛' },
  { label: '有点烦', value: 'frustrated', icon: '😣', desc: '先整理卡点，再继续推进' },
]

const stats = computed(() => userStore.todayStats)

const overviewCards = computed(() => [
  { label: '今日学习时长', value: `${stats.value.studyTime}h` },
  { label: '专注度', value: `${stats.value.focusScore}` },
  { label: '计划完成', value: `${stats.value.completedTasks}/${stats.value.totalTasks}` },
  { label: '练习正确率', value: `${stats.value.accuracy}%` },
])

const currentEmotionLabel = computed(() => {
  const current = emotions.find(item => item.value === userStore.userInfo.emotionState)
  return current?.label || '暂未设置'
})

const radarOption = computed(() => {
  const radarData = userStore.learningDNA.radarData.length
    ? userStore.learningDNA.radarData
    : [
        { name: '理解能力', value: 0 },
        { name: '记忆能力', value: 0 },
        { name: '专注持续', value: 0 },
        { name: '执行能力', value: 0 },
        { name: '情绪稳定', value: 0 },
        { name: '逻辑思维', value: 0 },
      ]

  return {
    radar: {
      indicator: radarData.map(item => ({ name: item.name, max: 100 })),
      radius: '62%',
      axisName: { color: '#9ca3af', fontSize: 11 },
      splitArea: {
        areaStyle: {
          color: ['rgba(99,102,241,0.04)', 'rgba(99,102,241,0.10)'],
        },
      },
      axisLine: { lineStyle: { color: 'rgba(99,102,241,0.25)' } },
      splitLine: { lineStyle: { color: 'rgba(99,102,241,0.16)' } },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: radarData.map(item => item.value),
            areaStyle: { color: 'rgba(99,102,241,0.28)' },
            lineStyle: { color: '#818cf8', width: 2 },
            itemStyle: { color: '#818cf8' },
          },
        ],
      },
    ],
  }
})

const studyTrendOption = computed(() => ({
  grid: { top: 20, right: 16, bottom: 24, left: 40 },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: studyTrend.value.length ? studyTrend.value.map(item => item.date.slice(5)) : ['暂无数据'],
    axisLine: { lineStyle: { color: '#334155' } },
    axisLabel: { color: '#6b7280' },
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#1f2937' } },
    axisLabel: { color: '#6b7280' },
  },
  series: [
    {
      type: 'bar',
      data: studyTrend.value.length ? studyTrend.value.map(item => Math.round((item.duration / 60) * 10) / 10) : [0],
      barWidth: '52%',
      itemStyle: {
        borderRadius: [10, 10, 0, 0],
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: '#8b5cf6' },
            { offset: 1, color: '#3b82f6' },
          ],
        },
      },
    },
  ],
}))

onMounted(async () => {
  try {
    await Promise.all([userStore.loadUser(), userStore.loadDNA(), loadDashboard()])
    selectedEmotion.value = userStore.userInfo.emotionState || ''
    chatMessages.value = buildStarterMessages()
  } finally {
    loading.value = false
  }
})

const loadDashboard = async () => {
  const userId = getUserId()
  const [studyStats, studyRecords, mistakeList, mistakeStats] = await Promise.all([
    studyApi.getStats(userId),
    studyApi.getRecords(userId, 1, 200),
    mistakeApi.getList(userId, { pageNum: 1, pageSize: 50 }),
    mistakeApi.getStats(userId),
  ])

  Object.assign(userStore.todayStats, {
    studyTime: Number(studyStats.studyTime) || 0,
    studyTimeChange: Number(studyStats.studyTimeChange) || 0,
    focusScore: Number(studyStats.focusScore) || 0,
    focusScoreChange: Number(studyStats.focusScoreChange) || 0,
    completedTasks: Number(studyStats.completedTasks) || 0,
    totalTasks: Number(studyStats.totalTasks) || 0,
    tasksChange: Number(studyStats.tasksChange) || 0,
    accuracy: Number(studyStats.accuracy) || 0,
    accuracyChange: Number(studyStats.accuracyChange) || 0,
  })

  studyTrend.value = Array.isArray(studyStats.weekTrend)
    ? studyStats.weekTrend.map((item: any) => ({
        date: String(item.date || ''),
        duration: Number(item.duration) || 0,
      }))
    : []

  const localToday = getLocalDateString()
  todayTasks.value = Array.isArray(studyRecords.list)
    ? studyRecords.list
        .filter((item: any) => String(item.studyDate || '').slice(0, 10) === localToday)
        .map((item: any) => mapStudyRecordToTask(item))
        .slice(0, 6)
    : []

  pendingMistakeCount.value = Number(mistakeStats.pending) || 0
  dashboardMistakes.value = Array.isArray(mistakeList.list)
    ? mistakeList.list
        .filter((item: any) => item.status === 'pending' || item.status === 'reviewing')
        .slice(0, 5)
        .map((item: any) => ({
          id: Number(item.id) || Date.now(),
          title: item.title || '未命名错题',
          subject: item.subject || '未分类',
          mistakeType: item.mistakeType || '未分类',
          errorCount: Number(item.errorCount) || 1,
          statusLabel: item.status === 'reviewing' ? '复习中' : '待复习',
        }))
    : []
}

const sendMessage = async () => {
  const content = chatInput.value.trim()
  if (!content || sending.value) return

  chatMessages.value.push({
    isUser: true,
    content,
    time: getCurrentTimeLabel(),
  })
  chatInput.value = ''
  sending.value = true

  try {
    const result = await aiApi.chat({
      message: content,
      userDNA: userStore.learningDNA.type || userStore.userInfo.learningType,
      emotion: userStore.userInfo.emotionState,
      context: '',
      source: 'dashboard',
      forcedAgent: 'auto',
    })

    chatMessages.value.push({
      isUser: false,
      content: result.response || 'AI 暂时没有返回内容，请稍后再试。',
      time: getCurrentTimeLabel(),
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : 'AI 服务异常，请稍后重试'
    ElMessage.error(message)
    chatMessages.value.push({
      isUser: false,
      content: 'AI 服务暂时不可用，稍后再试；你也可以先进入完整 AI 助手页继续咨询。',
      time: getCurrentTimeLabel(),
    })
  } finally {
    sending.value = false
  }
}

const fillQuickPrompt = (prompt: string) => {
  chatInput.value = prompt
}

const saveMood = async () => {
  if (!selectedEmotion.value) {
    ElMessage.warning('请先选择当前情绪')
    return
  }

  const emotion = emotions.find(item => item.value === selectedEmotion.value)
  savingEmotion.value = true

  try {
    await emotionApi.addRecord({
      emotionType: selectedEmotion.value,
      emotionLabel: emotion?.label || '一般',
      content: moodNote.value.trim() || '首页情绪打卡',
      relatedActivity: '首页情绪中心',
    })

    await userStore.loadUser()
    moodNote.value = ''
    ElMessage.success('情绪已保存')
  } catch (error) {
    const message = error instanceof Error ? error.message : '情绪保存失败，请稍后重试'
    ElMessage.error(message)
  } finally {
    savingEmotion.value = false
  }
}

const buildStarterMessages = (): ChatBubble[] => {
  const firstTask = todayTasks.value[0]
  const taskTip = firstTask
    ? `我已经看到你今天排了 ${stats.value.totalTasks} 个计划，建议先从「${firstTask.name}」开场。`
    : '你今天还没有开始学习计划，我可以先帮你拆一个容易启动的小任务。'

  return [
    {
      isUser: false,
      content: '晚上好，我是你的首页版 AI 学习助手。这里会先陪你看计划、盯错题、稳节奏，再把深入咨询交给完整 AI 页面。',
      time: '19:30',
    },
    {
      isUser: true,
      content: '今晚我想先把计划和错题复盘顺序理清楚。',
      time: '19:31',
    },
    {
      isUser: false,
      content: `${taskTip}\n另外你现在还有 ${pendingMistakeCount.value} 道待复习错题，建议先做 1 个主任务，再集中复盘 2 道最常错的题。`,
      time: '19:31',
    },
  ]
}

const mapStudyRecordToTask = (record: any): StudyTask => {
  const parsed = parseStudyContent(record.content)
  return {
    id: Number(record.id) || Date.now(),
    name: parsed.name,
    goal: parsed.goal,
    subject: record.subject || '未分类',
    duration: Number(record.duration) || 0,
    completed: Boolean(record.endTime),
  }
}

const parseStudyContent = (content?: string) => {
  if (!content) {
    return { name: '学习任务', goal: '' }
  }

  try {
    const parsed = JSON.parse(content)
    return {
      name: parsed.name || parsed.goal || '学习任务',
      goal: parsed.goal || '',
    }
  } catch {
    return { name: content, goal: '' }
  }
}

const getCurrentTimeLabel = () => {
  return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const getUserId = () => {
  try {
    const stored = localStorage.getItem('cognia-user')
    const parsed = stored ? JSON.parse(stored) : null
    return Number(parsed?.id) || 1
  } catch {
    return 1
  }
}

const getLocalDateString = (date = new Date()) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
</script>
