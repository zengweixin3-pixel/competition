<template>
  <div class="space-y-6">
    <!-- 第一行：AI学习人格 + 今日学习概览 + AI今日建议 -->
    <div class="grid grid-cols-12 gap-6">
      <!-- AI学习人格 -->
      <div class="col-span-4 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">AI学习人格</h3>
          <el-icon class="text-text-muted"><ArrowRight /></el-icon>
        </div>
        <div class="flex items-center gap-6">
          <div class="w-32 h-32">
            <v-chart class="w-full h-full" :option="radarOption" autoresize />
          </div>
          <div class="flex-1">
            <div class="text-sm text-text-muted mb-1">你的学习人格类型</div>
            <div class="text-xl font-bold gradient-text mb-2">{{ userStore.learningDNA.type }}</div>
            <div class="inline-flex items-center gap-1 px-3 py-1 bg-accent-purple/20 text-accent-purple text-sm rounded-full mb-3">
              <el-icon><Moon /></el-icon>
              <span>夜间高效型</span>
            </div>
            <p class="text-sm text-text-secondary leading-relaxed">
              你擅长深入理解知识，喜欢探索原理，在安静的环境中效率更高，适合深度学习。
            </p>
            <el-button type="primary" class="mt-4" size="small">
              查看完整分析 <el-icon class="ml-1"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 今日学习概览 -->
      <div class="col-span-5 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">今日学习概览</h3>
        </div>
        <div class="grid grid-cols-4 gap-4 mb-4">
          <div class="text-center">
            <div class="text-2xl font-bold text-text-primary">{{ stats.studyTime }}<span class="text-sm font-normal text-text-muted">h</span></div>
            <div class="text-xs text-text-muted mt-1">学习时长</div>
            <div class="text-xs text-emerald-400 mt-1">↑ {{ stats.studyTimeChange }}%</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-text-primary">{{ stats.focusScore }}<span class="text-sm font-normal text-text-muted">分</span></div>
            <div class="text-xs text-text-muted mt-1">专注度</div>
            <div class="text-xs text-emerald-400 mt-1">↑ {{ stats.focusScoreChange }}%</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-text-primary">{{ stats.completedTasks }}/{{ stats.totalTasks }}</div>
            <div class="text-xs text-text-muted mt-1">完成任务</div>
            <div class="text-xs text-emerald-400 mt-1">↑ {{ stats.tasksChange }}%</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-text-primary">{{ stats.accuracy }}<span class="text-sm font-normal text-text-muted">%</span></div>
            <div class="text-xs text-text-muted mt-1">正确率</div>
            <div class="text-xs text-rose-400 mt-1">↓ {{ Math.abs(stats.accuracyChange) }}%</div>
          </div>
        </div>
        <div class="h-32">
          <v-chart class="w-full h-full" :option="focusChartOption" autoresize />
        </div>
      </div>

      <!-- AI今日建议 -->
      <div class="col-span-3 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">AI今日建议</h3>
          <el-icon class="text-text-muted"><More /></el-icon>
        </div>
        <div class="bg-gradient-to-br from-primary/10 to-accent-purple/10 rounded-xl p-4 border border-primary/20 mb-4">
          <div class="flex items-start gap-3">
            <div class="w-10 h-10 rounded-full bg-gradient-primary flex items-center justify-center flex-shrink-0">
              <el-icon class="text-white"><ChatDotRound /></el-icon>
            </div>
            <div>
              <p class="text-sm text-text-secondary leading-relaxed">
                晚上是你的高效学习期，建议安排难度较高的知识点学习哦！
              </p>
            </div>
          </div>
        </div>
        <div class="space-y-3">
          <div class="flex items-center gap-2 text-sm text-text-secondary">
            <el-icon class="text-primary"><CircleCheck /></el-icon>
            <span>建议学习：高数 重点章节 7.2</span>
          </div>
          <div class="flex items-center gap-2 text-sm text-text-secondary">
            <el-icon class="text-primary"><Clock /></el-icon>
            <span>最佳时间：19:30 - 21:30</span>
          </div>
          <div class="flex items-center gap-2 text-sm text-text-secondary">
            <el-icon class="text-primary"><Timer /></el-icon>
            <span>学习时长：90 分钟</span>
          </div>
        </div>
        <el-button type="primary" class="w-full mt-4">
          开始学习计划
        </el-button>
      </div>
    </div>

    <!-- 第二行：今日任务 + 错题本 + 学习数据 + 学习成就 -->
    <div class="grid grid-cols-12 gap-6">
      <!-- 今日任务 -->
      <div class="col-span-3 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">今日任务</h3>
          <span class="text-sm text-text-muted">{{ completedTasks }}/{{ totalTasks }} 已完成</span>
        </div>
        <el-progress :percentage="taskProgress" :stroke-width="8" class="mb-4" :color="['#6366f1', '#a855f7']" />
        <div class="space-y-3">
          <div v-for="task in tasks" :key="task.id" class="flex items-center gap-3 p-3 rounded-xl bg-dark-bg/50">
            <el-checkbox v-model="task.completed" size="large">
              <span :class="task.completed ? 'line-through text-text-muted' : 'text-text-primary'">{{ task.name }}</span>
            </el-checkbox>
            <span class="ml-auto text-xs px-2 py-1 rounded" :class="getSubjectClass(task.subject)">{{ task.subject }}</span>
            <span class="text-xs text-text-muted">{{ task.duration }}分钟</span>
          </div>
        </div>
        <div class="mt-4 text-center">
          <el-link type="primary" :underline="false">查看全部任务 →</el-link>
        </div>
      </div>

      <!-- 错题本 -->
      <div class="col-span-3 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">错题本</h3>
          <span class="text-sm text-accent-pink">12 题待复习</span>
        </div>
        <div class="space-y-3">
          <div v-for="(mistake, index) in mistakes" :key="index" class="p-3 rounded-xl bg-dark-bg/50 border-l-4" :class="mistake.borderColor">
            <div class="flex items-center justify-between mb-2">
              <span class="font-medium text-text-primary text-sm">{{ mistake.title }}</span>
              <span class="text-xs px-2 py-0.5 rounded" :class="mistake.tagClass">{{ mistake.tag }}</span>
            </div>
            <div class="flex items-center gap-4 text-xs text-text-muted">
              <span>{{ mistake.subject }}</span>
              <span>错误 {{ mistake.count }} 次</span>
            </div>
          </div>
        </div>
        <div class="mt-4 text-center">
          <el-link type="primary" :underline="false">查看全部错题 →</el-link>
        </div>
      </div>

      <!-- 学习数据 -->
      <div class="col-span-3 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">学习数据</h3>
          <el-dropdown>
            <span class="text-sm text-text-muted cursor-pointer">近 7 天 <el-icon><ArrowDown /></el-icon></span>
          </el-dropdown>
        </div>
        <div class="h-40">
          <v-chart class="w-full h-full" :option="studyDataOption" autoresize />
        </div>
      </div>

      <!-- 学习成就 -->
      <div class="col-span-3 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">学习成就</h3>
          <el-link type="primary" :underline="false">更多 ></el-link>
        </div>
        <div class="bg-gradient-to-br from-amber-500/20 to-orange-500/20 rounded-xl p-4 border border-amber-500/30 mb-4">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 rounded-full bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center">
              <el-icon class="text-white text-xl"><Trophy /></el-icon>
            </div>
            <div>
              <div class="font-bold text-text-primary">连续学习 7 天</div>
              <div class="text-sm text-text-secondary">坚持就是胜利！</div>
            </div>
          </div>
        </div>
        <div class="space-y-3">
          <div class="flex items-center gap-3 p-3 rounded-xl bg-dark-bg/50">
            <div class="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
              <el-icon class="text-primary"><View /></el-icon>
            </div>
            <div class="flex-1">
              <div class="text-sm text-text-primary">专注达人</div>
              <div class="text-xs text-text-muted">累计专注 20 小时</div>
            </div>
            <el-icon class="text-text-muted"><ArrowRight /></el-icon>
          </div>
          <div class="flex items-center gap-3 p-3 rounded-xl bg-dark-bg/50">
            <div class="w-10 h-10 rounded-full bg-accent-purple/20 flex items-center justify-center">
              <el-icon class="text-accent-purple"><DocumentChecked /></el-icon>
            </div>
            <div class="flex-1">
              <div class="text-sm text-text-primary">错题克星</div>
              <div class="text-xs text-text-muted">解决错题 50 道</div>
            </div>
            <el-icon class="text-text-muted"><ArrowRight /></el-icon>
          </div>
          <div class="flex items-center gap-3 p-3 rounded-xl bg-dark-bg/50">
            <div class="w-10 h-10 rounded-full bg-accent-cyan/20 flex items-center justify-center">
              <el-icon class="text-accent-cyan"><Calendar /></el-icon>
            </div>
            <div class="flex-1">
              <div class="text-sm text-text-primary">计划大师</div>
              <div class="text-xs text-text-muted">完成计划 10 次</div>
            </div>
            <el-icon class="text-text-muted"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 第三行：AI学习助手 + 情绪中心 -->
    <div class="grid grid-cols-12 gap-6">
      <!-- AI学习助手 -->
      <div class="col-span-8 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">AI学习助手</h3>
          <div class="flex gap-2">
            <el-tag v-for="tag in aiTags" :key="tag" size="small" effect="dark" class="cursor-pointer">{{ tag }}</el-tag>
          </div>
        </div>
        <div class="bg-dark-bg/50 rounded-xl p-4 mb-4 h-48 overflow-y-auto">
          <div v-for="(msg, index) in chatMessages" :key="index" class="flex gap-3 mb-4" :class="msg.isUser ? 'flex-row-reverse' : ''">
            <el-avatar v-if="!msg.isUser" :size="36" class="bg-gradient-primary">
              <el-icon class="text-white"><ChatDotRound /></el-icon>
            </el-avatar>
            <el-avatar v-else :size="36" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <div class="max-w-[70%]" :class="msg.isUser ? 'text-right' : ''">
              <div class="inline-block px-4 py-2 rounded-xl text-sm" :class="msg.isUser ? 'bg-primary text-white' : 'bg-dark-border text-text-primary'">
                {{ msg.content }}
              </div>
              <div class="text-xs text-text-muted mt-1">{{ msg.time }}</div>
            </div>
          </div>
        </div>
        <div class="flex gap-3">
          <el-input v-model="chatInput" placeholder="输入你的问题..." class="flex-1" @keyup.enter="sendMessage">
            <template #append>
              <el-button type="primary" :icon="Promotion" @click="sendMessage" />
            </template>
          </el-input>
        </div>
      </div>

      <!-- 情绪中心 -->
      <div class="col-span-4 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">情绪中心</h3>
          <span class="text-sm text-text-muted">记录心情，AI会更懂你哦 💜</span>
        </div>
        <div class="flex justify-between mb-6">
          <div v-for="emotion in emotions" :key="emotion.value" class="flex flex-col items-center gap-2 cursor-pointer group" @click="selectEmotion(emotion)">
            <div class="w-12 h-12 rounded-full flex items-center justify-center text-2xl transition-all duration-300" :class="selectedEmotion === emotion.value ? 'bg-primary/30 scale-110' : 'bg-dark-bg/50 group-hover:bg-primary/20'">
              {{ emotion.icon }}
            </div>
            <span class="text-xs text-text-muted">{{ emotion.label }}</span>
          </div>
        </div>
        <div class="mb-4">
          <div class="text-sm text-text-muted mb-2">今日心情记录</div>
          <el-input v-model="moodNote" type="textarea" :rows="3" placeholder="今天学习有点累，但是收获也很大！" />
        </div>
        <el-button type="primary" class="w-full" @click="saveMood">保存心情</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { RadarChart, LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, RadarComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { useUserStore } from '@/stores/user'
import {
  ArrowRight,
  Moon,
  ChatDotRound,
  CircleCheck,
  Clock,
  Timer,
  More,
  ArrowDown,
  Trophy,
  View,
  DocumentChecked,
  Calendar,
  Promotion,
} from '@element-plus/icons-vue'

use([CanvasRenderer, RadarChart, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, RadarComponent])

const userStore = useUserStore()

const stats = computed(() => userStore.todayStats)

const radarOption = computed(() => ({
  radar: {
    indicator: userStore.learningDNA.radarData.map(item => ({ name: item.name, max: 100 })),
    radius: '65%',
    axisName: {
      color: '#9ca3af',
      fontSize: 10,
    },
    splitArea: {
      areaStyle: {
        color: ['rgba(99, 102, 241, 0.05)', 'rgba(99, 102, 241, 0.1)'],
      },
    },
    axisLine: {
      lineStyle: { color: 'rgba(99, 102, 241, 0.3)' },
    },
    splitLine: {
      lineStyle: { color: 'rgba(99, 102, 241, 0.2)' },
    },
  },
  series: [{
    type: 'radar',
    data: [{
      value: userStore.learningDNA.radarData.map(item => item.value),
      areaStyle: {
        color: 'rgba(99, 102, 241, 0.3)',
      },
      lineStyle: {
        color: '#6366f1',
        width: 2,
      },
      itemStyle: {
        color: '#6366f1',
      },
    }],
  }],
}))

const focusChartOption = {
  grid: { top: 10, right: 10, bottom: 20, left: 30 },
  xAxis: {
    type: 'category',
    data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
    axisLine: { lineStyle: { color: '#374151' } },
    axisLabel: { color: '#6b7280', fontSize: 10 },
  },
  yAxis: {
    type: 'value',
    max: 100,
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#1f2937' } },
    axisLabel: { color: '#6b7280', fontSize: 10 },
  },
  series: [{
    data: [20, 15, 45, 60, 55, 85, 40],
    type: 'line',
    smooth: true,
    symbol: 'none',
    lineStyle: {
      color: '#6366f1',
      width: 3,
    },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(99, 102, 241, 0.4)' },
          { offset: 1, color: 'rgba(99, 102, 241, 0)' },
        ],
      },
    },
    markPoint: {
      data: [{ type: 'max', name: '最高', itemStyle: { color: '#a855f7' } }],
      label: { color: '#fff', fontSize: 10 },
    },
  }],
}

const tasks = ref([
  { id: 1, name: '高数：定积分应用', completed: true, subject: '高数', duration: 60 },
  { id: 2, name: '英语：阅读理解训练', completed: true, subject: '英语', duration: 40 },
  { id: 3, name: '线代：矩阵运算', completed: false, subject: '线代', duration: 45 },
  { id: 4, name: '专业课：信号与系统', completed: true, subject: '专业课', duration: 60 },
])

const completedTasks = computed(() => tasks.value.filter(t => t.completed).length)
const totalTasks = computed(() => tasks.value.length)
const taskProgress = computed(() => Math.round((completedTasks.value / totalTasks.value) * 100))

const getSubjectClass = (subject: string) => {
  const map: Record<string, string> = {
    '高数': 'bg-emerald-500/20 text-emerald-400',
    '英语': 'bg-blue-500/20 text-blue-400',
    '线代': 'bg-amber-500/20 text-amber-400',
    '专业课': 'bg-purple-500/20 text-purple-400',
  }
  return map[subject] || 'bg-gray-500/20 text-gray-400'
}

const mistakes = ref([
  { title: '定积分求面积问题', subject: '高数', count: 2, tag: '概念理解错误', tagClass: 'bg-rose-500/20 text-rose-400', borderColor: 'border-rose-500' },
  { title: '矩阵乘法计算', subject: '线代', count: 1, tag: '计算错误', tagClass: 'bg-amber-500/20 text-amber-400', borderColor: 'border-amber-500' },
  { title: '英语阅读理解题', subject: '英语', count: 1, tag: '细节理解错误', tagClass: 'bg-blue-500/20 text-blue-400', borderColor: 'border-blue-500' },
])

const studyDataOption = {
  grid: { top: 10, right: 10, bottom: 20, left: 30 },
  xAxis: {
    type: 'category',
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '今天'],
    axisLine: { lineStyle: { color: '#374151' } },
    axisLabel: { color: '#6b7280', fontSize: 10 },
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#1f2937' } },
    axisLabel: { color: '#6b7280', fontSize: 10 },
  },
  series: [{
    data: [4, 5, 4.5, 6, 5.5, 4, 4.2],
    type: 'bar',
    barWidth: '60%',
    itemStyle: {
      borderRadius: [4, 4, 0, 0],
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: '#6366f1' },
          { offset: 1, color: '#a855f7' },
        ],
      },
    },
  }],
}

const aiTags = ref(['全部', '高数', '线代', '英语', '专业课', '其他'])

const chatMessages = ref([
  { isUser: true, content: '这个定积分题我还是不太理解，可以再讲一下吗？', time: '20:30' },
  { isUser: false, content: '当然可以！这道题的关键在于将图形分割为两个部分分别求面积，然后相加。我们一步一步来分析...', time: '20:31' },
])

const chatInput = ref('')

const sendMessage = () => {
  if (!chatInput.value.trim()) return
  chatMessages.value.push({
    isUser: true,
    content: chatInput.value,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })
  chatInput.value = ''
  setTimeout(() => {
    chatMessages.value.push({
      isUser: false,
      content: '好的，我来为你详细解答这个问题。首先我们需要理解定积分的几何意义...',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    })
  }, 1000)
}

const emotions = ref([
  { label: '超棒', value: 'great', icon: '😄' },
  { label: '还不错', value: 'good', icon: '🙂' },
  { label: '一般', value: 'normal', icon: '😐' },
  { label: '有点累', value: 'tired', icon: '😔' },
  { label: '很烦躁', value: 'frustrated', icon: '😫' },
])

const selectedEmotion = ref('')
const moodNote = ref('')

const selectEmotion = (emotion: any) => {
  selectedEmotion.value = emotion.value
}

const saveMood = () => {
  console.log('保存心情:', selectedEmotion.value, moodNote.value)
}
</script>
