<template>
  <div class="space-y-6" v-loading="loading">
    <div class="flex items-center justify-end">
      <el-button size="large" type="primary" @click="showRecordDialog = true">记录今日情绪</el-button>
    </div>

    <section class="grid grid-cols-1 gap-6 xl:grid-cols-3">
      <div class="card-gradient rounded-2xl p-6">
        <p class="text-sm text-text-muted">当前情绪</p>
        <div class="mt-6 text-center">
          <div class="text-7xl">{{ currentEmotion.icon }}</div>
          <h2 class="mt-4 text-3xl font-bold text-text-primary">{{ currentEmotion.label }}</h2>
          <p class="mt-3 text-sm leading-6 text-text-secondary">{{ currentEmotion.content }}</p>
        </div>
      </div>

      <div class="card-gradient rounded-2xl p-6 xl:col-span-2">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-bold text-text-primary">情绪趋势</h3>
            <p class="text-sm text-text-muted">看看最近一段时间的情绪波动</p>
          </div>
          <el-tag type="primary">{{ emotionStats.totalRecords }} 条记录</el-tag>
        </div>
        <div class="mt-6 h-72">
          <v-chart class="h-full w-full" :option="trendOption" autoresize />
        </div>
      </div>
    </section>

    <section class="grid grid-cols-2 gap-6 xl:grid-cols-4">
      <div class="card-gradient rounded-2xl p-6 text-center">
        <p class="text-sm text-text-muted">积极情绪</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ emotionStats.positive }}</p>
      </div>
      <div class="card-gradient rounded-2xl p-6 text-center">
        <p class="text-sm text-text-muted">平稳情绪</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ emotionStats.neutral }}</p>
      </div>
      <div class="card-gradient rounded-2xl p-6 text-center">
        <p class="text-sm text-text-muted">低落情绪</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ emotionStats.negative }}</p>
      </div>
      <div class="card-gradient rounded-2xl p-6 text-center">
        <p class="text-sm text-text-muted">稳定度</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ emotionStats.stability }}%</p>
      </div>
    </section>

    <section class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-lg font-bold text-text-primary">情绪日记</h3>
          <p class="text-sm text-text-muted">记录一下最近的状态和感受</p>
        </div>
        <el-button link type="primary" @click="reload">刷新</el-button>
      </div>
      <div class="mt-5 space-y-4">
        <div v-for="record in emotionRecords" :key="record.id" class="rounded-2xl bg-dark-bg/50 p-5">
          <div class="flex items-start gap-4">
            <div class="text-4xl">{{ emotionMeta(record.emotionType).icon }}</div>
            <div class="min-w-0 flex-1">
              <div class="flex items-center justify-between gap-4">
                <div>
                  <p class="text-sm font-medium text-text-primary">{{ record.emotionLabel }}</p>
                  <p class="mt-1 text-xs text-text-muted">{{ formatDate(record.recordDate || record.createTime) }}</p>
                </div>
                <el-tag size="small">{{ record.relatedActivity || '未填写活动' }}</el-tag>
              </div>
              <p class="mt-3 text-sm leading-6 text-text-secondary">{{ record.content || '未填写内容' }}</p>
              <div v-if="record.aiResponse" class="mt-4 rounded-2xl border border-primary/20 bg-primary/10 p-4">
                <p class="text-xs text-primary">AI 回应</p>
                <p class="mt-2 text-sm leading-6 text-text-primary">{{ record.aiResponse }}</p>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-if="emotionRecords.length === 0" description="暂无情绪记录" />
      </div>
    </section>

    <el-dialog v-model="showRecordDialog" title="记录今日情绪" width="520px" destroy-on-close>
      <div class="space-y-5">
        <div class="flex flex-wrap gap-3">
          <button
            v-for="emotion in emotions"
            :key="emotion.value"
            type="button"
            class="flex min-w-[90px] flex-col items-center rounded-2xl border px-4 py-3 transition"
            :class="selectedEmotion === emotion.value ? 'border-primary bg-primary/10' : 'border-dark-border bg-dark-bg/50'"
            @click="selectedEmotion = emotion.value"
          >
            <span class="text-3xl">{{ emotion.icon }}</span>
            <span class="mt-2 text-xs text-text-secondary">{{ emotion.label }}</span>
          </button>
        </div>
        <el-select v-model="relatedActivity" class="w-full" placeholder="关联学习活动">
          <el-option label="高等数学" value="高等数学" />
          <el-option label="线性代数" value="线性代数" />
          <el-option label="英语" value="英语" />
          <el-option label="专业课" value="专业课" />
          <el-option label="错题复盘" value="错题复盘" />
        </el-select>
        <el-input v-model="emotionNote" type="textarea" :rows="4" placeholder="写下你现在的状态和感受..." />
      </div>
      <template #footer>
        <el-button @click="showRecordDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEmotionRecord">保存记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import { emotionApi } from '@/api'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent])

type EmotionRecordItem = {
  id?: number
  emotionType: string
  emotionLabel: string
  content?: string
  relatedActivity?: string
  aiResponse?: string
  recordDate?: string
  createTime?: string
  emotionScore?: number
}

const loading = ref(true)
const saving = ref(false)
const showRecordDialog = ref(false)
const selectedEmotion = ref('')
const emotionNote = ref('')
const relatedActivity = ref('')
const emotionRecords = ref<EmotionRecordItem[]>([])
const emotionStats = ref({
  totalRecords: 0,
  positive: 0,
  neutral: 0,
  negative: 0,
  stability: 0,
})

const emotions = [
  { label: '超棒', value: 'great', icon: '😄', score: 5 },
  { label: '还不错', value: 'good', icon: '🙂', score: 4 },
  { label: '一般', value: 'normal', icon: '😐', score: 3 },
  { label: '有点累', value: 'tired', icon: '😔', score: 2 },
  { label: '很烦躁', value: 'frustrated', icon: '😫', score: 1 },
]

const currentEmotion = computed(() => {
  const latest = emotionRecords.value[0]
  if (!latest) {
    return {
      label: '暂无记录',
      icon: '🫥',
      content: '记录一次真实情绪后，这里会显示你的最新状态。',
    }
  }
  return {
    label: latest.emotionLabel,
    icon: emotionMeta(latest.emotionType).icon,
    content: latest.content || '已记录情绪',
  }
})

const trendOption = computed(() => ({
  grid: { top: 20, right: 20, bottom: 24, left: 36 },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: emotionRecords.value.length
      ? [...emotionRecords.value].reverse().map(item => formatDate(item.recordDate || item.createTime, true))
      : ['暂无数据'],
    axisLine: { lineStyle: { color: '#374151' } },
    axisLabel: { color: '#6b7280' },
  },
  yAxis: {
    type: 'value',
    min: 1,
    max: 5,
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#1f2937' } },
    axisLabel: { color: '#6b7280' },
  },
  series: [
    {
      type: 'line',
      smooth: true,
      data: emotionRecords.value.length
        ? [...emotionRecords.value].reverse().map(item => Number(item.emotionScore) || emotionMeta(item.emotionType).score)
        : [0],
      lineStyle: { color: '#6366f1', width: 3 },
      itemStyle: { color: '#6366f1' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(99,102,241,0.35)' },
            { offset: 1, color: 'rgba(99,102,241,0.02)' },
          ],
        },
      },
    },
  ],
}))

onMounted(async () => {
  await reload()
  loading.value = false
})

const reload = async () => {
  const userId = getUserId()
  const start = getLocalDateString(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000))
  const end = getLocalDateString()
  const [records, stats] = await Promise.all([
    emotionApi.getRecords(userId, start, end),
    emotionApi.getStats(userId),
  ])
  emotionRecords.value = Array.isArray(records) ? records : []
  emotionStats.value = {
    totalRecords: Number(stats.totalRecords) || 0,
    positive: Number(stats.positive) || 0,
    neutral: Number(stats.neutral) || 0,
    negative: Number(stats.negative) || 0,
    stability: Number(stats.stability) || 0,
  }
}

const saveEmotionRecord = async () => {
  if (!selectedEmotion.value) {
    ElMessage.warning('请先选择当前情绪')
    return
  }
  const emotion = emotions.find(item => item.value === selectedEmotion.value)
  saving.value = true
  try {
    await emotionApi.addRecord({
      emotionType: selectedEmotion.value,
      emotionLabel: emotion?.label || '一般',
      content: emotionNote.value || '今日情绪记录',
      relatedActivity: relatedActivity.value,
      emotionScore: emotion?.score || 3,
    })
    await reload()
    showRecordDialog.value = false
    selectedEmotion.value = ''
    emotionNote.value = ''
    relatedActivity.value = ''
    ElMessage.success('情绪记录已保存')
  } catch {
    ElMessage.error('情绪保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const emotionMeta = (emotionType?: string) => {
  return emotions.find(item => item.value === emotionType) || emotions[2]
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

const formatDate = (value?: string, compact = false) => {
  if (!value) return compact ? '--' : '未知时间'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return compact
    ? `${date.getMonth() + 1}/${date.getDate()}`
    : `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
const getLocalDateString = (date = new Date()) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
</script>
