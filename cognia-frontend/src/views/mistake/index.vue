<template>
  <div class="space-y-6" v-loading="loading">
    <div class="flex items-center justify-end gap-3">
      <el-button size="large" @click="reload">刷新数据</el-button>
      <el-button size="large" type="primary" @click="showCreateDialog = true">新增错题</el-button>
    </div>

    <section class="grid grid-cols-2 gap-6 xl:grid-cols-4">
      <div class="card-gradient rounded-[28px] p-6 text-center">
        <p class="text-sm text-text-muted">总错题数</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ stats.total }}</p>
      </div>
      <div class="card-gradient rounded-[28px] p-6 text-center">
        <p class="text-sm text-text-muted">待复习</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ stats.pending }}</p>
      </div>
      <div class="card-gradient rounded-[28px] p-6 text-center">
        <p class="text-sm text-text-muted">复习中</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ stats.reviewing }}</p>
      </div>
      <div class="card-gradient rounded-[28px] p-6 text-center">
        <p class="text-sm text-text-muted">已掌握</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ stats.mastered }}</p>
      </div>
    </section>

    <section class="grid grid-cols-1 gap-6 xl:grid-cols-2">
      <div class="card-gradient rounded-[28px] p-6">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-bold text-text-primary">错因分布</h3>
            <p class="text-sm text-text-muted">快速看清常见失分原因</p>
          </div>
        </div>
        <div class="mt-5 h-72">
          <v-chart class="h-full w-full" :option="mistakeTypeOption" autoresize />
        </div>
      </div>

      <div class="card-gradient rounded-[28px] p-6">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-bold text-text-primary">学科分布</h3>
            <p class="text-sm text-text-muted">看看问题更集中在哪些学科</p>
          </div>
          <el-tag type="success">改善率 {{ stats.improvement }}%</el-tag>
        </div>
        <div class="mt-5 h-72">
          <v-chart class="h-full w-full" :option="subjectOption" autoresize />
        </div>
      </div>
    </section>

    <section class="card-gradient rounded-[28px] p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="grid flex-1 grid-cols-1 gap-3 md:grid-cols-[1.4fr_0.8fr_0.8fr]">
          <el-input v-model="searchQuery" placeholder="搜索标题、内容或学科" clearable />
          <el-select v-model="filterSubject" clearable placeholder="筛选学科">
            <el-option label="高等数学" value="高等数学" />
            <el-option label="线性代数" value="线性代数" />
            <el-option label="英语" value="英语" />
            <el-option label="专业课" value="专业课" />
          </el-select>
          <el-select v-model="filterStatus" clearable placeholder="筛选状态">
            <el-option label="待复习" value="pending" />
            <el-option label="复习中" value="reviewing" />
            <el-option label="已掌握" value="mastered" />
          </el-select>
        </div>
      </div>

      <div class="mt-5 space-y-4">
        <div
          v-for="mistake in filteredMistakes"
          :key="mistake.id"
          class="rounded-[28px] border border-white/8 bg-dark-bg/40 p-5"
        >
          <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-2">
                <h4 class="text-base font-bold text-text-primary">{{ mistake.title }}</h4>
                <el-tag size="small">{{ mistake.subject }}</el-tag>
                <el-tag size="small" type="warning">{{ mistake.mistakeType }}</el-tag>
                <el-tag size="small" :type="statusTagType(mistake.status)">
                  {{ statusLabel(mistake.status) }}
                </el-tag>
              </div>

              <p class="mt-3 text-sm leading-6 text-text-secondary">{{ mistake.content }}</p>

              <div class="mt-3 flex flex-wrap gap-4 text-xs text-text-muted">
                <span>错误 {{ mistake.errorCount }} 次</span>
                <span>难度 {{ mistake.difficulty || '未标注' }}</span>
                <span>创建时间 {{ formatDate(mistake.createTime) }}</span>
              </div>

              <div v-if="mistake.aiAnalysis && expandedAnalysisIds.includes(mistake.id)" class="mt-4 rounded-3xl border border-primary/15 bg-primary/10 p-4">
                <div class="flex items-center justify-between gap-3">
                  <p class="text-xs uppercase tracking-[0.16em] text-primary">
                    {{ isAnalysisPendingText(mistake.aiAnalysis) ? 'AI 正在分析' : '最新 AI 分析' }}
                  </p>
                  <el-button link type="primary" @click="toggleAnalysis(mistake.id)">收起</el-button>
                </div>
                <pre class="mt-3 whitespace-pre-wrap text-sm leading-7 text-text-primary">{{ mistake.aiAnalysis }}</pre>
              </div>
            </div>

            <div class="flex shrink-0 gap-2">
              <el-button :loading="isAnalysisLoading(mistake.id)" @click="analyzeMistake(mistake)">
                {{ isAnalysisPending(mistake.id) ? '分析中...' : 'AI分析' }}
              </el-button>
              <el-button
                v-if="mistake.aiAnalysis"
                plain
                @click="toggleAnalysis(mistake.id)"
              >
                {{ expandedAnalysisIds.includes(mistake.id) ? '收起分析' : '展开分析' }}
              </el-button>
              <el-button type="primary" plain @click="openAnalysis(mistake)">查看详情</el-button>
            </div>
          </div>
        </div>

        <el-empty v-if="filteredMistakes.length === 0" description="暂无错题数据" />
      </div>
    </section>

    <el-dialog v-model="showCreateDialog" title="新增错题" width="560px" destroy-on-close>
      <div class="space-y-4">
        <el-input v-model="form.title" placeholder="错题标题" />
        <el-input v-model="form.content" type="textarea" :rows="5" placeholder="题目内容、错误步骤或错因描述" />
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
          <el-select v-model="form.subject" placeholder="选择学科">
            <el-option label="高等数学" value="高等数学" />
            <el-option label="线性代数" value="线性代数" />
            <el-option label="英语" value="英语" />
            <el-option label="专业课" value="专业课" />
          </el-select>
          <el-select v-model="form.mistakeType" placeholder="错误类型">
            <el-option label="概念混淆" value="概念混淆" />
            <el-option label="计算错误" value="计算错误" />
            <el-option label="理解偏差" value="理解偏差" />
            <el-option label="审题偏差" value="审题偏差" />
            <el-option label="方法不熟" value="方法不熟" />
          </el-select>
          <el-input-number v-model="form.errorCount" :min="1" class="w-full" />
          <el-select v-model="form.difficulty" placeholder="难度">
            <el-option label="简单" value="简单" />
            <el-option label="中等" value="中等" />
            <el-option label="困难" value="困难" />
          </el-select>
        </div>
      </div>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="createMistake">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAnalysisDialog" title="错题详情" width="760px" destroy-on-close>
      <div v-if="selectedMistake" class="space-y-5">
        <div class="rounded-3xl bg-dark-bg/50 p-4">
          <h4 class="text-base font-bold text-text-primary">{{ selectedMistake.title }}</h4>
          <p class="mt-3 text-sm leading-6 text-text-secondary">{{ selectedMistake.content }}</p>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="rounded-3xl bg-dark-bg/50 p-4">
            <p class="text-xs text-text-muted">学科</p>
            <p class="mt-2 text-sm text-text-primary">{{ selectedMistake.subject }}</p>
          </div>
          <div class="rounded-3xl bg-dark-bg/50 p-4">
            <p class="text-xs text-text-muted">状态</p>
            <p class="mt-2 text-sm text-text-primary">{{ statusLabel(selectedMistake.status) }}</p>
          </div>
        </div>

        <div class="rounded-3xl border border-primary/20 bg-primary/10 p-5">
          <p class="text-xs uppercase tracking-[0.16em] text-primary">AI 分析结果</p>
          <pre class="mt-3 whitespace-pre-wrap text-sm leading-7 text-text-primary">{{ selectedMistake.aiAnalysis || '还没有生成 AI 分析。' }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { use } from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import { mistakeApi } from '@/api'
import { useUserStore } from '@/stores/user'

use([CanvasRenderer, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

type MistakeItem = {
  id: number
  title: string
  content: string
  subject: string
  mistakeType: string
  errorCount: number
  difficulty?: string
  status: string
  aiAnalysis?: string
  createTime?: string
}

const ANALYSIS_PENDING_PREFIX = '[AI分析中]'
const ANALYSIS_PENDING_TEXT = '[AI分析中] 正在生成分析，请稍后查看。'
const ANALYSIS_FAILURE_PREFIX = '[AI服务异常]'
const ANALYSIS_POLL_INTERVAL = 2500
const ANALYSIS_POLL_MAX_COUNT = 24

const userStore = useUserStore()
const loading = ref(true)
const saving = ref(false)
const showCreateDialog = ref(false)
const showAnalysisDialog = ref(false)
const analyzingId = ref<number | null>(null)
const analysisPendingIds = ref<number[]>([])
const expandedAnalysisIds = ref<number[]>([])
const searchQuery = ref('')
const filterSubject = ref('')
const filterStatus = ref('')
const selectedMistake = ref<MistakeItem | null>(null)
const mistakes = ref<MistakeItem[]>([])
const pollingTimers = new Map<number, number>()
const stats = ref({
  total: 0,
  pending: 0,
  reviewing: 0,
  mastered: 0,
  improvement: 0,
  typeDistribution: [] as Array<{ mistakeType?: string; count?: number }>,
  subjectDistribution: [] as Array<{ subject?: string; count?: number }>,
})

const form = ref({
  title: '',
  content: '',
  subject: '',
  mistakeType: '概念混淆',
  errorCount: 1,
  difficulty: '中等',
})

const filteredMistakes = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  return mistakes.value.filter((item) => {
    const matchKeyword = !keyword || [item.title, item.content, item.subject].some(field => field?.toLowerCase().includes(keyword))
    const matchSubject = !filterSubject.value || item.subject === filterSubject.value
    const matchStatus = !filterStatus.value || item.status === filterStatus.value
    return matchKeyword && matchSubject && matchStatus
  })
})

const mistakeTypeOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { color: '#9ca3af' } },
  series: [
    {
      type: 'pie',
      radius: ['42%', '72%'],
      data: stats.value.typeDistribution.length
        ? stats.value.typeDistribution.map((item, index) => ({
            value: Number(item.count) || 0,
            name: item.mistakeType || '未分类',
            itemStyle: {
              color: ['#6366f1', '#8b5cf6', '#f59e0b', '#10b981', '#06b6d4'][index % 5],
            },
          }))
        : [{ value: 0, name: '暂无数据', itemStyle: { color: '#6b7280' } }],
      label: { color: '#e5e7eb' },
    },
  ],
}))

const subjectOption = computed(() => ({
  grid: { top: 20, right: 20, bottom: 24, left: 56 },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'value',
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#1f2937' } },
    axisLabel: { color: '#6b7280' },
  },
  yAxis: {
    type: 'category',
    data: stats.value.subjectDistribution.length ? stats.value.subjectDistribution.map(item => item.subject || '未分类') : ['暂无数据'],
    axisLine: { lineStyle: { color: '#374151' } },
    axisLabel: { color: '#9ca3af' },
  },
  series: [
    {
      type: 'bar',
      data: stats.value.subjectDistribution.length
        ? stats.value.subjectDistribution.map((item, index) => ({
            value: Number(item.count) || 0,
            itemStyle: {
              color: ['#6366f1', '#10b981', '#f59e0b', '#8b5cf6'][index % 4],
              borderRadius: [0, 8, 8, 0],
            },
          }))
        : [{ value: 0, itemStyle: { color: '#6b7280' } }],
      barWidth: '56%',
    },
  ],
}))

onMounted(async () => {
  await Promise.all([userStore.loadDNA(), reload()])
  loading.value = false
})

onBeforeUnmount(() => {
  stopAllPolling()
})

const reload = async () => {
  const userId = getUserId()
  const [listResult, statsResult] = await Promise.all([
    mistakeApi.getList(userId, { pageNum: 1, pageSize: 100 }),
    mistakeApi.getStats(userId),
  ])

  mistakes.value = Array.isArray(listResult.list)
    ? listResult.list.map((item: any) => mapMistakeItem(item))
    : []

  stats.value = {
    total: Number(statsResult.total) || 0,
    pending: Number(statsResult.pending) || 0,
    reviewing: Number(statsResult.reviewing) || 0,
    mastered: Number(statsResult.mastered) || 0,
    improvement: Number(statsResult.improvement) || 0,
    typeDistribution: Array.isArray(statsResult.typeDistribution) ? statsResult.typeDistribution : [],
    subjectDistribution: Array.isArray(statsResult.subjectDistribution) ? statsResult.subjectDistribution : [],
  }

  syncPollingState()
}

const createMistake = async () => {
  if (!form.value.title.trim() || !form.value.content.trim() || !form.value.subject) {
    ElMessage.warning('请填写完整的错题信息')
    return
  }

  saving.value = true
  try {
    await mistakeApi.addMistake({
      title: form.value.title.trim(),
      content: form.value.content.trim(),
      subject: form.value.subject,
      mistakeType: form.value.mistakeType,
      errorCount: form.value.errorCount,
      difficulty: form.value.difficulty,
      status: 'pending',
    })

    showCreateDialog.value = false
    form.value = {
      title: '',
      content: '',
      subject: '',
      mistakeType: '概念混淆',
      errorCount: 1,
      difficulty: '中等',
    }
    await reload()
    ElMessage.success('错题已保存')
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '错题保存失败，请稍后重试'))
  } finally {
    saving.value = false
  }
}

const analyzeMistake = async (mistake: MistakeItem) => {
  analyzingId.value = mistake.id

  try {
    const dna = userStore.learningDNA.type
      ? `${userStore.learningDNA.type} ${userStore.learningDNA.radarData.map(item => `${item.name}${item.value}`).join(' ')}`
      : ''

    const result = await mistakeApi.analyzeMistake(mistake.id, dna)

    const target = mistakes.value.find(item => item.id === mistake.id)
    if (target) {
      target.aiAnalysis = ANALYSIS_PENDING_TEXT
    }

    if (!expandedAnalysisIds.value.includes(mistake.id)) {
      expandedAnalysisIds.value.push(mistake.id)
    }

    if (selectedMistake.value?.id === mistake.id) {
      selectedMistake.value = { ...selectedMistake.value, aiAnalysis: ANALYSIS_PENDING_TEXT }
    }

    startAnalysisPolling(mistake.id)
    ElMessage.info(result.message || '已提交 AI 分析请求，请稍后查看结果')
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, 'AI 服务异常，请稍后重试'))
  } finally {
    analyzingId.value = null
  }
}

const openAnalysis = (mistake: MistakeItem) => {
  selectedMistake.value = { ...mistake }
  showAnalysisDialog.value = true
}

const toggleAnalysis = (id: number) => {
  if (expandedAnalysisIds.value.includes(id)) {
    expandedAnalysisIds.value = expandedAnalysisIds.value.filter(item => item !== id)
  } else {
    expandedAnalysisIds.value.push(id)
  }
}

const mapMistakeItem = (item: any): MistakeItem => ({
  id: Number(item.id) || Date.now(),
  title: item.title || '未命名错题',
  content: item.content || '',
  subject: item.subject || '未分类',
  mistakeType: item.mistakeType || '未分类',
  errorCount: Number(item.errorCount) || 1,
  difficulty: item.difficulty || '',
  status: item.status || 'pending',
  aiAnalysis: item.aiAnalysis || '',
  createTime: item.createTime || '',
})

const syncPollingState = () => {
  const pendingIds = mistakes.value
    .filter(item => isAnalysisPendingText(item.aiAnalysis))
    .map(item => item.id)

  pendingIds.forEach(id => startAnalysisPolling(id))

  Array.from(pollingTimers.keys()).forEach((id) => {
    if (!pendingIds.includes(id)) {
      stopAnalysisPolling(id)
    }
  })
}

const startAnalysisPolling = (id: number) => {
  if (pollingTimers.has(id)) {
    addPendingId(id)
    return
  }

  addPendingId(id)
  scheduleAnalysisPoll(id, 0)
}

const scheduleAnalysisPoll = (id: number, attempt: number) => {
  const timer = window.setTimeout(async () => {
    try {
      const detail = await mistakeApi.getDetail(id)
      applyMistakeDetail(detail)

      if (isAnalysisPendingText(detail.aiAnalysis)) {
        if (attempt + 1 >= ANALYSIS_POLL_MAX_COUNT) {
          stopAnalysisPolling(id)
          ElMessage.warning('AI 分析仍在处理中，请稍后刷新查看')
          return
        }
        scheduleAnalysisPoll(id, attempt + 1)
        return
      }

      stopAnalysisPolling(id)

      if (looksLikeAiFailure(detail.aiAnalysis)) {
        ElMessage.error('AI 服务异常，请稍后重试')
        return
      }

      if (detail.aiAnalysis && !expandedAnalysisIds.value.includes(id)) {
        expandedAnalysisIds.value.push(id)
      }

      ElMessage.success('AI 分析完成')
    } catch (error) {
      stopAnalysisPolling(id)
      ElMessage.error(resolveErrorMessage(error, '分析结果获取失败，请稍后重试'))
    }
  }, ANALYSIS_POLL_INTERVAL)

  pollingTimers.set(id, timer)
}

const stopAnalysisPolling = (id: number) => {
  const timer = pollingTimers.get(id)
  if (timer) {
    window.clearTimeout(timer)
    pollingTimers.delete(id)
  }
  analysisPendingIds.value = analysisPendingIds.value.filter(item => item !== id)
}

const stopAllPolling = () => {
  Array.from(pollingTimers.keys()).forEach(id => stopAnalysisPolling(id))
}

const applyMistakeDetail = (detail: any) => {
  const mapped = mapMistakeItem(detail)
  const index = mistakes.value.findIndex(item => item.id === mapped.id)
  if (index >= 0) {
    mistakes.value[index] = mapped
  } else {
    mistakes.value.unshift(mapped)
  }

  if (selectedMistake.value?.id === mapped.id) {
    selectedMistake.value = { ...selectedMistake.value, ...mapped }
  }
}

const addPendingId = (id: number) => {
  if (!analysisPendingIds.value.includes(id)) {
    analysisPendingIds.value = [...analysisPendingIds.value, id]
  }
}

const isAnalysisPending = (id: number) => {
  return analysisPendingIds.value.includes(id)
}

const isAnalysisLoading = (id: number) => {
  return analyzingId.value === id || isAnalysisPending(id)
}

const isAnalysisPendingText = (value?: string) => {
  return value?.trim().startsWith(ANALYSIS_PENDING_PREFIX) || false
}

const looksLikeAiFailure = (value?: string) => {
  const text = value?.trim()
  return !text || text.startsWith(ANALYSIS_FAILURE_PREFIX) || text.includes('AI服务暂时不可用')
}

const resolveErrorMessage = (error: unknown, fallback: string) => {
  return error instanceof Error && error.message ? error.message : fallback
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

const statusLabel = (status?: string) => {
  if (status === 'mastered') return '已掌握'
  if (status === 'reviewing') return '复习中'
  return '待复习'
}

const statusTagType = (status?: string) => {
  if (status === 'mastered') return 'success'
  if (status === 'reviewing') return 'warning'
  return 'info'
}

const formatDate = (value?: string) => {
  if (!value) return '未知时间'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>
