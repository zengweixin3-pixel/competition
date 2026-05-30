<template>
  <div class="space-y-6" v-loading="loading">
    <div class="flex items-center justify-end gap-3">
      <el-button size="large" @click="showGenerateDialog = true">AI 生成计划</el-button>
      <el-button size="large" type="primary" @click="showCreateDialog = true">新增计划</el-button>
    </div>

    <section class="grid grid-cols-2 gap-6 xl:grid-cols-4">
      <div class="card-gradient rounded-2xl p-6 text-center">
        <p class="text-sm text-text-muted">今日总计划</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ todayTotal }}</p>
      </div>
      <div class="card-gradient rounded-2xl p-6 text-center">
        <p class="text-sm text-text-muted">今日已完成</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ todayCompleted }}</p>
      </div>
      <div class="card-gradient rounded-2xl p-6 text-center">
        <p class="text-sm text-text-muted">完成率</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ todayProgress }}%</p>
      </div>
      <div class="card-gradient rounded-2xl p-6 text-center">
        <p class="text-sm text-text-muted">月累计完成</p>
        <p class="mt-3 text-3xl font-bold text-text-primary">{{ reportCompletedTasks }}</p>
      </div>
    </section>

    <section class="grid grid-cols-1 gap-6 xl:grid-cols-3">
      <div class="card-gradient rounded-2xl p-6 xl:col-span-2">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-bold text-text-primary">今日计划列表</h3>
          </div>
          <el-tag type="primary">{{ todayCompleted }}/{{ todayTotal }}</el-tag>
        </div>
        <div class="mt-5 space-y-3">
          <div
            v-for="task in todayTasks"
            :key="task.id"
            class="rounded-2xl bg-dark-bg/50 p-4"
          >
            <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center gap-2">
                  <h4 class="truncate text-base font-bold text-text-primary">{{ task.name }}</h4>
                  <el-tag size="small">{{ task.subject }}</el-tag>
                </div>
                <p class="mt-3 text-sm leading-6 text-text-secondary">{{ task.goal || '未填写目标' }}</p>
                <div class="mt-3 flex flex-wrap gap-4 text-xs text-text-muted">
                  <span>学习日期 {{ formatDate(task.studyDate) }}</span>
                  <span>计划时长 {{ task.duration }} 分钟</span>
                  <span>开始时间 {{ formatTime(task.startTime) }}</span>
                </div>
              </div>
              <div class="flex items-center gap-3">
                <el-switch
                  :model-value="task.completed"
                  active-text="已完成"
                  inactive-text="待完成"
                  @change="toggleTask(task, $event)"
                />
              </div>
            </div>
          </div>
          <el-empty v-if="todayTasks.length === 0" description="今天还没有学习计划" />
        </div>
      </div>

      <div class="card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-bold text-text-primary">AI 计划建议</h3>
            <p class="text-sm text-text-muted">结合你的学习节奏给出安排建议</p>
          </div>
        </div>
        <div class="mt-5 space-y-4">
          <div class="rounded-2xl bg-dark-bg/50 p-4">
            <p class="text-xs text-text-muted">当前学习人格</p>
            <p class="mt-2 text-sm font-medium text-text-primary">{{ userStore.learningDNA.type || '暂未分析' }}</p>
          </div>
          <div class="rounded-2xl bg-dark-bg/50 p-4">
            <p class="text-xs text-text-muted">计划完成情况</p>
            <p class="mt-2 text-sm font-medium text-text-primary">
              今日 {{ todayCompleted }}/{{ todayTotal }}，月累计 {{ reportCompletedTasks }} 项
            </p>
          </div>
          <div class="rounded-2xl border border-primary/20 bg-primary/10 p-4">
            <p class="text-xs text-primary">建议</p>
            <p class="mt-2 text-sm leading-6 text-text-primary">{{ suggestionText }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-lg font-bold text-text-primary">近 7 天计划趋势</h3>
          <p class="text-sm text-text-muted">看看最近一周的完成节奏</p>
        </div>
      </div>
      <div class="mt-6 h-72">
        <v-chart class="h-full w-full" :option="trendOption" autoresize />
      </div>
    </section>

    <section class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-lg font-bold text-text-primary">本周计划排布</h3>
          <p class="text-sm text-text-muted">一周任务分布一目了然</p>
        </div>
      </div>
      <div class="mt-5 grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-7">
        <div v-for="day in weekBuckets" :key="day.date" class="rounded-2xl bg-dark-bg/50 p-4">
          <div class="flex items-center justify-between">
            <p class="text-sm font-medium text-text-primary">{{ day.label }}</p>
            <el-tag size="small">{{ day.tasks.length }}</el-tag>
          </div>
          <div class="mt-3 space-y-2">
            <div v-for="task in day.tasks" :key="task.id" class="rounded-xl bg-dark-border px-3 py-2 text-xs text-text-secondary">
              <p class="truncate">{{ task.name }}</p>
            </div>
            <p v-if="day.tasks.length === 0" class="text-xs text-text-muted">暂无计划</p>
          </div>
        </div>
      </div>
    </section>

    <el-dialog v-model="showCreateDialog" title="新增学习计划" width="560px" destroy-on-close>
      <div class="space-y-4">
        <el-input v-model="form.name" placeholder="计划名称" />
        <el-input v-model="form.goal" type="textarea" :rows="3" placeholder="计划目标" />
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
          <el-select v-model="form.subject" placeholder="选择学科">
            <el-option label="高等数学" value="高等数学" />
            <el-option label="线性代数" value="线性代数" />
            <el-option label="英语" value="英语" />
            <el-option label="专业课" value="专业课" />
          </el-select>
          <el-date-picker v-model="form.studyDate" type="date" class="w-full" placeholder="学习日期" value-format="YYYY-MM-DD" />
          <el-time-picker v-model="form.startTime" class="w-full" placeholder="开始时间" value-format="HH:mm:ss" />
          <el-input-number v-model="form.duration" :min="15" :step="15" class="w-full" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="createTask">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showGenerateDialog" title="AI 生成计划" width="560px" destroy-on-close>
      <div class="space-y-4">
        <el-select v-model="generateForm.focus" class="w-full" placeholder="学习重点">
          <el-option label="薄弱学科强化" value="薄弱学科强化" />
          <el-option label="综合复习" value="综合复习" />
          <el-option label="考前冲刺" value="考前冲刺" />
        </el-select>
        <el-input-number v-model="generateForm.dailyHours" :min="1" :max="12" class="w-full" />
        <el-date-picker v-model="generateForm.studyDate" type="date" class="w-full" placeholder="计划日期" value-format="YYYY-MM-DD" />
        <el-input v-model="generateForm.notes" type="textarea" :rows="4" placeholder="补充需求" />
      </div>
      <template #footer>
        <el-button @click="showGenerateDialog = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="generatePlan">生成并保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import { aiApi, studyApi } from '@/api'
import { useUserStore } from '@/stores/user'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent, LegendComponent])

type PlanTask = {
  id: number
  name: string
  goal: string
  subject: string
  duration: number
  studyDate: string
  startTime?: string
  endTime?: string
  completed: boolean
  raw: any
}

const userStore = useUserStore()
const loading = ref(true)
const saving = ref(false)
const generating = ref(false)
const showCreateDialog = ref(false)
const showGenerateDialog = ref(false)
const reportCompletedTasks = ref(0)
const todayTasks = ref<PlanTask[]>([])
const allTasks = ref<PlanTask[]>([])
const taskTrend = ref<Array<{ date: string; completed: number; pending: number }>>([])

const form = ref({
  name: '',
  goal: '',
  subject: '',
  studyDate: getLocalDateString(),
  startTime: '19:00:00',
  duration: 45,
})

const generateForm = ref({
  focus: '薄弱学科强化',
  dailyHours: 3,
  studyDate: getLocalDateString(),
  notes: '',
})

const todayTotal = computed(() => todayTasks.value.length)
const todayCompleted = computed(() => todayTasks.value.filter(item => item.completed).length)
const todayProgress = computed(() => todayTotal.value ? Math.round((todayCompleted.value / todayTotal.value) * 100) : 0)

const suggestionText = computed(() => {
  if (!todayTotal.value) return '今天还没有安排学习计划，建议先创建 1-2 个可完成的小任务。'
  if (todayCompleted.value === todayTotal.value) return '今天的计划已经全部完成，可以把剩余精力用来做错题复盘或总结。'
  if (todayCompleted.value === 0) return '建议先完成一项最容易启动的任务，建立今天的学习节奏。'
  return `今天还剩 ${todayTotal.value - todayCompleted.value} 项任务，优先处理耗时较长或与你的学习人格更匹配的内容。`
})

const trendOption = computed(() => ({
  grid: { top: 20, right: 20, bottom: 24, left: 36 },
  tooltip: { trigger: 'axis' },
  legend: {
    data: ['已完成', '待完成'],
    textStyle: { color: '#9ca3af' },
  },
  xAxis: {
    type: 'category',
    data: taskTrend.value.length ? taskTrend.value.map(item => item.date.slice(5)) : ['暂无数据'],
    axisLine: { lineStyle: { color: '#374151' } },
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
      name: '已完成',
      type: 'bar',
      data: taskTrend.value.length ? taskTrend.value.map(item => item.completed) : [0],
      itemStyle: { color: '#10b981', borderRadius: [6, 6, 0, 0] },
    },
    {
      name: '待完成',
      type: 'bar',
      data: taskTrend.value.length ? taskTrend.value.map(item => item.pending) : [0],
      itemStyle: { color: '#f59e0b', borderRadius: [6, 6, 0, 0] },
    },
  ],
}))

const weekBuckets = computed(() => {
  const start = startOfWeek(new Date())
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    const dateText = getLocalDateString(date)
    return {
      date: dateText,
      label: `${date.getMonth() + 1}/${date.getDate()}`,
      tasks: allTasks.value.filter(item => item.studyDate === dateText),
    }
  })
})

onMounted(async () => {
  await Promise.all([userStore.loadDNA(), reload()])
  loading.value = false
})

const reload = async () => {
  const userId = getUserId()
  const [statsResult, recordsResult] = await Promise.all([
    studyApi.getStats(userId),
    studyApi.getRecords(userId, 1, 200),
  ])

  reportCompletedTasks.value = Number(statsResult.reportCompletedTasks) || 0
  taskTrend.value = Array.isArray(statsResult.taskTrend)
    ? statsResult.taskTrend.map((item: any) => ({
        date: String(item.date || ''),
        completed: Number(item.completed) || 0,
        pending: Number(item.pending) || 0,
      }))
    : []

  allTasks.value = Array.isArray(recordsResult.list)
    ? recordsResult.list.map((record: any) => mapRecordToTask(record))
    : []

  todayTasks.value = allTasks.value.filter(item => item.studyDate === getLocalDateString())
}

const createTask = async () => {
  if (!form.value.name.trim() || !form.value.subject || !form.value.studyDate) {
    ElMessage.warning('请填写完整的计划信息')
    return
  }
  saving.value = true
  try {
    await studyApi.addRecord(buildStudyRecordPayload({
      name: form.value.name.trim(),
      goal: form.value.goal.trim(),
      subject: form.value.subject,
      duration: form.value.duration,
      studyDate: form.value.studyDate,
      startTime: form.value.startTime,
    }))
    showCreateDialog.value = false
    form.value = {
      name: '',
      goal: '',
      subject: '',
      studyDate: getLocalDateString(),
      startTime: '19:00:00',
      duration: 45,
    }
    await reload()
    ElMessage.success('学习计划已保存')
  } catch {
    ElMessage.error('学习计划保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const toggleTask = async (task: PlanTask, completed: boolean | string | number) => {
  const isCompleted = Boolean(completed)
  try {
    await studyApi.updateRecord({
      id: task.id,
      subject: task.subject,
      duration: task.duration,
      content: JSON.stringify({ name: task.name, goal: task.goal }),
      studyDate: task.studyDate,
      startTime: task.raw.startTime,
      endTime: isCompleted ? new Date().toISOString().slice(0, 19) : null,
      focusLevel: task.raw.focusLevel,
      score: task.raw.score,
    })
    await reload()
    ElMessage.success(isCompleted ? '任务已标记为完成' : '任务已恢复为待完成')
  } catch {
    ElMessage.error('任务状态更新失败，请稍后重试')
  }
}

const generatePlan = async () => {
  generating.value = true
  try {
    const dna = userStore.learningDNA.type
      ? `${userStore.learningDNA.type} ${userStore.learningDNA.radarData.map(item => `${item.name}${item.value}`).join(' ')}`
      : '常规学习计划'
    const result = await aiApi.generatePlan({
      userDNA: dna,
      focus: generateForm.value.focus,
      dailyHours: generateForm.value.dailyHours,
      notes: generateForm.value.notes,
    })
    const tasks = parseGeneratedPlan(result.plan)
    if (!tasks.length) {
      ElMessage.warning('AI 返回内容无法解析成计划，请调整条件后重试')
      return
    }
    for (const task of tasks) {
      await studyApi.addRecord(buildStudyRecordPayload({
        ...task,
        studyDate: generateForm.value.studyDate,
        startTime: '19:00:00',
      }))
    }
    showGenerateDialog.value = false
    await reload()
    ElMessage.success('AI 计划已生成并保存')
  } catch {
    ElMessage.error('AI 计划生成失败，请稍后重试')
  } finally {
    generating.value = false
  }
}

const buildStudyRecordPayload = (task: {
  name: string
  goal: string
  subject: string
  duration: number
  studyDate: string
  startTime?: string
}) => {
  const datePart = task.studyDate
  const timePart = task.startTime || '19:00:00'
  return {
    subject: task.subject,
    duration: task.duration,
    content: JSON.stringify({ name: task.name, goal: task.goal }),
    studyDate: datePart,
    startTime: `${datePart}T${timePart}`,
    endTime: null,
  }
}

const mapRecordToTask = (record: any): PlanTask => {
  const parsed = parseContent(record.content)
  return {
    id: Number(record.id) || Date.now(),
    name: parsed.name,
    goal: parsed.goal,
    subject: record.subject || '未分类',
    duration: Number(record.duration) || 0,
    studyDate: (record.studyDate || '').slice(0, 10),
    startTime: record.startTime || '',
    endTime: record.endTime || '',
    completed: Boolean(record.endTime),
    raw: record,
  }
}

const parseContent = (content?: string) => {
  if (!content) return { name: '学习任务', goal: '' }
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

const parseGeneratedPlan = (planText: string) => {
  return planText
    .split('\n')
    .map(line => line.replace(/^[\d\-\*\.\s]+/, '').trim())
    .filter(line => line.length >= 4)
    .slice(0, 5)
    .map((line, index) => ({
      name: line.slice(0, 20),
      goal: line,
      subject: ['高等数学', '线性代数', '英语', '专业课'][index % 4],
      duration: Math.max(30, generateForm.value.dailyHours * 15),
    }))
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

const formatDate = (value?: string) => {
  if (!value) return '未设置'
  return value.slice(0, 10)
}

const formatTime = (value?: string) => {
  if (!value) return '未设置'
  if (value.includes('T')) return value.split('T')[1].slice(0, 5)
  return value.slice(11, 16) || value.slice(0, 5)
}

const startOfWeek = (date: Date) => {
  const current = new Date(date)
  const day = current.getDay() || 7
  current.setDate(current.getDate() - day + 1)
  current.setHours(0, 0, 0, 0)
  return current
}

function getLocalDateString(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
</script>
