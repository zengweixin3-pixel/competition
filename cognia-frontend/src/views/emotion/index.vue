<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-text-primary">情绪中心</h2>
        <p class="text-text-muted mt-1">记录心情，AI会更懂你哦 💜</p>
      </div>
      <el-button type="primary" size="large" @click="showRecordDialog = true">
        <el-icon class="mr-2"><Plus /></el-icon>记录今日心情
      </el-button>
    </div>

    <!-- 情绪概览 -->
    <div class="grid grid-cols-12 gap-6">
      <!-- 当前情绪状态 -->
      <div class="col-span-4 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-6">
          <h3 class="font-bold text-text-primary">当前情绪状态</h3>
          <el-tag effect="dark" :class="currentEmotion.tagClass">{{ currentEmotion.label }}</el-tag>
        </div>
        <div class="text-center py-8">
          <div class="text-8xl mb-4">{{ currentEmotion.icon }}</div>
          <div class="text-2xl font-bold text-text-primary mb-2">{{ currentEmotion.title }}</div>
          <p class="text-text-secondary">{{ currentEmotion.description }}</p>
        </div>
        <div class="bg-dark-bg/50 rounded-xl p-4">
          <div class="text-sm text-text-muted mb-2">AI建议</div>
          <p class="text-sm text-text-primary">{{ currentEmotion.suggestion }}</p>
        </div>
      </div>

      <!-- 情绪趋势 -->
      <div class="col-span-8 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">情绪变化趋势</h3>
          <el-radio-group v-model="emotionTimeRange" size="small">
            <el-radio-button label="近7天">近7天</el-radio-button>
            <el-radio-button label="近30天">近30天</el-radio-button>
            <el-radio-button label="近90天">近90天</el-radio-button>
          </el-radio-group>
        </div>
        <div class="h-64">
          <v-chart class="w-full h-full" :option="emotionTrendOption" autoresize />
        </div>
      </div>
    </div>

    <!-- 情绪统计 -->
    <div class="grid grid-cols-4 gap-6">
      <div class="card-gradient rounded-2xl p-6">
        <div class="flex items-center gap-4">
          <div class="w-14 h-14 rounded-xl bg-emerald-500/20 flex items-center justify-center">
            <span class="text-3xl">😄</span>
          </div>
          <div>
            <div class="text-2xl font-bold text-text-primary">{{ emotionStats.positive }}天</div>
            <div class="text-sm text-text-muted">积极情绪</div>
          </div>
        </div>
      </div>
      <div class="card-gradient rounded-2xl p-6">
        <div class="flex items-center gap-4">
          <div class="w-14 h-14 rounded-xl bg-amber-500/20 flex items-center justify-center">
            <span class="text-3xl">😐</span>
          </div>
          <div>
            <div class="text-2xl font-bold text-text-primary">{{ emotionStats.neutral }}天</div>
            <div class="text-sm text-text-muted">平静情绪</div>
          </div>
        </div>
      </div>
      <div class="card-gradient rounded-2xl p-6">
        <div class="flex items-center gap-4">
          <div class="w-14 h-14 rounded-xl bg-rose-500/20 flex items-center justify-center">
            <span class="text-3xl">😫</span>
          </div>
          <div>
            <div class="text-2xl font-bold text-text-primary">{{ emotionStats.negative }}天</div>
            <div class="text-sm text-text-muted">消极情绪</div>
          </div>
        </div>
      </div>
      <div class="card-gradient rounded-2xl p-6">
        <div class="flex items-center gap-4">
          <div class="w-14 h-14 rounded-xl bg-primary/20 flex items-center justify-center">
            <el-icon class="text-primary text-2xl"><TrendCharts /></el-icon>
          </div>
          <div>
            <div class="text-2xl font-bold text-text-primary">{{ emotionStats.stability }}%</div>
            <div class="text-sm text-text-muted">情绪稳定度</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 情绪与学习关联分析 -->
    <div class="grid grid-cols-12 gap-6">
      <div class="col-span-6 card-gradient rounded-2xl p-6">
        <h3 class="font-bold text-text-primary mb-4">情绪与学习效率关联</h3>
        <div class="h-64">
          <v-chart class="w-full h-full" :option="correlationOption" autoresize />
        </div>
      </div>
      <div class="col-span-6 card-gradient rounded-2xl p-6">
        <h3 class="font-bold text-text-primary mb-4">不同时段情绪分布</h3>
        <div class="h-64">
          <v-chart class="w-full h-full" :option="timeDistributionOption" autoresize />
        </div>
      </div>
    </div>

    <!-- 情绪日记 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between mb-6">
        <h3 class="font-bold text-text-primary">情绪日记</h3>
        <el-radio-group v-model="diaryTimeRange" size="small">
          <el-radio-button label="全部">全部</el-radio-button>
          <el-radio-button label="本月">本月</el-radio-button>
          <el-radio-button label="上月">上月</el-radio-button>
        </el-radio-group>
      </div>
      <div class="space-y-4">
        <div v-for="record in emotionRecords" :key="record.id" class="flex gap-4 p-4 bg-dark-bg/50 rounded-xl">
          <div class="w-16 text-center">
            <div class="text-3xl mb-1">{{ record.icon }}</div>
            <div class="text-xs text-text-muted">{{ record.time }}</div>
          </div>
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2">
              <span class="font-medium text-text-primary">{{ record.title }}</span>
              <el-tag size="small" :class="record.tagClass">{{ record.label }}</el-tag>
            </div>
            <p class="text-sm text-text-secondary mb-3">{{ record.content }}</p>
            <div v-if="record.aiResponse" class="bg-primary/10 rounded-lg p-3 border border-primary/20">
              <div class="flex items-start gap-2">
                <el-icon class="text-primary mt-0.5"><ChatDotRound /></el-icon>
                <p class="text-sm text-text-primary">{{ record.aiResponse }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 情绪调节建议 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-12 h-12 rounded-xl bg-gradient-primary flex items-center justify-center">
          <el-icon class="text-white text-2xl"><MagicStick /></el-icon>
        </div>
        <div>
          <h3 class="font-bold text-text-primary text-lg">AI情绪调节建议</h3>
          <p class="text-sm text-text-muted">基于你的情绪历史，为你定制的调节方案</p>
        </div>
      </div>
      <div class="grid grid-cols-3 gap-6">
        <div class="bg-dark-bg/50 rounded-xl p-6">
          <div class="w-12 h-12 rounded-xl bg-emerald-500/20 flex items-center justify-center mb-4">
            <el-icon class="text-emerald-400 text-2xl"><Sunny /></el-icon>
          </div>
          <h4 class="font-bold text-text-primary mb-2">当感到焦虑时</h4>
          <ul class="space-y-2 text-sm text-text-secondary">
            <li>• 尝试5分钟深呼吸练习</li>
            <li>• 将大任务分解为小目标</li>
            <li>• 听轻音乐放松心情</li>
            <li>• 适当休息，不要强迫自己</li>
          </ul>
        </div>
        <div class="bg-dark-bg/50 rounded-xl p-6">
          <div class="w-12 h-12 rounded-xl bg-primary/20 flex items-center justify-center mb-4">
            <el-icon class="text-primary text-2xl"><Moon /></el-icon>
          </div>
          <h4 class="font-bold text-text-primary mb-2">当感到疲惫时</h4>
          <ul class="space-y-2 text-sm text-text-secondary">
            <li>• 进行15分钟小憩</li>
            <li>• 做一些轻度拉伸运动</li>
            <li>• 切换到轻松的学习内容</li>
            <li>• 喝一杯温水或茶</li>
          </ul>
        </div>
        <div class="bg-dark-bg/50 rounded-xl p-6">
          <div class="w-12 h-12 rounded-xl bg-accent-purple/20 flex items-center justify-center mb-4">
            <el-icon class="text-accent-purple text-2xl"><Star /></el-icon>
          </div>
          <h4 class="font-bold text-text-primary mb-2">当感到烦躁时</h4>
          <ul class="space-y-2 text-sm text-text-secondary">
            <li>• 暂停学习，出去走走</li>
            <li>• 与朋友或家人聊聊天</li>
            <li>• 写下让你烦躁的事情</li>
            <li>• 换个环境继续学习</li>
          </ul>
        </div>
      </div>
    </div>
  </div>

  <!-- 记录情绪对话框 -->
  <el-dialog v-model="showRecordDialog" title="记录今日心情" width="500px" destroy-on-close>
    <div class="space-y-6">
      <div>
        <div class="text-sm text-text-primary mb-4">你现在感觉如何？</div>
        <div class="flex justify-between">
          <div
            v-for="emotion in emotions"
            :key="emotion.value"
            class="flex flex-col items-center gap-2 cursor-pointer p-3 rounded-xl transition-all"
            :class="selectedEmotion === emotion.value ? 'bg-primary/20 border border-primary/50' : 'hover:bg-dark-border/50 border border-transparent'"
            @click="selectedEmotion = emotion.value"
          >
            <div class="text-4xl">{{ emotion.icon }}</div>
            <span class="text-sm" :class="selectedEmotion === emotion.value ? 'text-primary' : 'text-text-muted'">{{ emotion.label }}</span>
          </div>
        </div>
      </div>
      <div>
        <div class="text-sm text-text-primary mb-2">详细描述（可选）</div>
        <el-input
          v-model="emotionNote"
          type="textarea"
          :rows="4"
          placeholder="描述一下你现在的状态，比如：今天学习有点累，但是收获也很大..."
        />
      </div>
      <div>
        <div class="text-sm text-text-primary mb-2">关联的学习活动</div>
        <el-select v-model="relatedActivity" placeholder="选择相关活动" class="w-full">
          <el-option label="高数学习" value="math" />
          <el-option label="英语学习" value="english" />
          <el-option label="专业课学习" value="major" />
          <el-option label="复习错题" value="review" />
          <el-option label="其他" value="other" />
        </el-select>
      </div>
    </div>
    <template #footer>
      <el-button @click="showRecordDialog = false">取消</el-button>
      <el-button type="primary" @click="saveEmotionRecord">保存记录</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, ScatterChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import {
  Plus,
  TrendCharts,
  MagicStick,
  ChatDotRound,
  Sunny,
  Moon,
  Star,
} from '@element-plus/icons-vue'

use([CanvasRenderer, LineChart, ScatterChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

const emotionTimeRange = ref('近7天')
const diaryTimeRange = ref('全部')
const showRecordDialog = ref(false)
const selectedEmotion = ref('')
const emotionNote = ref('')
const relatedActivity = ref('')

const currentEmotion = ref({
  label: '还不错',
  icon: '🙂',
  title: '状态良好',
  description: '你的情绪状态不错，适合继续学习',
  suggestion: '保持当前的学习节奏，可以尝试一些稍微有挑战性的内容',
  tagClass: 'bg-emerald-500/30 border-emerald-500/50 text-emerald-400',
})

const emotionStats = ref({
  positive: 18,
  neutral: 7,
  negative: 5,
  stability: 78,
})

const emotions = [
  { label: '超棒', value: 'great', icon: '😄' },
  { label: '还不错', value: 'good', icon: '🙂' },
  { label: '一般', value: 'normal', icon: '😐' },
  { label: '有点累', value: 'tired', icon: '😔' },
  { label: '很烦躁', value: 'frustrated', icon: '😫' },
]

const emotionTrendOption = {
  grid: { top: 20, right: 20, bottom: 40, left: 50 },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(17, 24, 39, 0.9)',
    borderColor: '#374151',
    textStyle: { color: '#f9fafb' },
    formatter: (params: any) => {
      const emotionMap: Record<number, string> = { 5: '😄', 4: '🙂', 3: '😐', 2: '😔', 1: '😫' }
      return `${params[0].axisValue}<br/>情绪指数: ${params[0].value} ${emotionMap[params[0].value]}`
    },
  },
  xAxis: {
    type: 'category',
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    axisLine: { lineStyle: { color: '#374151' } },
    axisLabel: { color: '#6b7280' },
  },
  yAxis: {
    type: 'value',
    min: 1,
    max: 5,
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#1f2937' } },
    axisLabel: {
      color: '#6b7280',
      formatter: (value: number) => {
        const map: Record<number, string> = { 5: '😄', 4: '🙂', 3: '😐', 2: '😔', 1: '😫' }
        return map[value] || ''
      },
    },
  },
  series: [{
    data: [4, 3, 4, 5, 4, 3, 4],
    type: 'line',
    smooth: true,
    symbol: 'circle',
    symbolSize: 10,
    lineStyle: {
      color: '#6366f1',
      width: 3,
    },
    itemStyle: {
      color: '#6366f1',
      borderWidth: 2,
      borderColor: '#fff',
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
  }],
}

const correlationOption = {
  grid: { top: 20, right: 20, bottom: 40, left: 50 },
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(17, 24, 39, 0.9)',
    borderColor: '#374151',
    textStyle: { color: '#f9fafb' },
  },
  xAxis: {
    type: 'value',
    name: '情绪指数',
    nameTextStyle: { color: '#9ca3af' },
    axisLine: { lineStyle: { color: '#374151' } },
    splitLine: { lineStyle: { color: '#1f2937' } },
    axisLabel: { color: '#6b7280' },
  },
  yAxis: {
    type: 'value',
    name: '学习效率',
    nameTextStyle: { color: '#9ca3af' },
    axisLine: { lineStyle: { color: '#374151' } },
    splitLine: { lineStyle: { color: '#1f2937' } },
    axisLabel: { color: '#6b7280' },
  },
  series: [{
    type: 'scatter',
    data: [
      [5, 92], [4, 85], [4, 88], [3, 72], [4, 80],
      [3, 68], [5, 95], [2, 55], [4, 82], [3, 70],
      [5, 90], [4, 86], [2, 50], [3, 75], [4, 84],
    ],
    symbolSize: 12,
    itemStyle: {
      color: {
        type: 'radial',
        x: 0.5, y: 0.5, r: 0.5,
        colorStops: [
          { offset: 0, color: '#a855f7' },
          { offset: 1, color: '#6366f1' },
        ],
      },
      shadowBlur: 10,
      shadowColor: 'rgba(99, 102, 241, 0.5)',
    },
  }],
}

const timeDistributionOption = {
  grid: { top: 20, right: 20, bottom: 40, left: 50 },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(17, 24, 39, 0.9)',
    borderColor: '#374151',
    textStyle: { color: '#f9fafb' },
  },
  legend: {
    data: ['积极', '平静', '消极'],
    bottom: 0,
    textStyle: { color: '#9ca3af' },
  },
  xAxis: {
    type: 'category',
    data: ['早晨', '上午', '中午', '下午', '傍晚', '晚上', '深夜'],
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
      name: '积极',
      type: 'bar',
      stack: 'total',
      data: [3, 5, 2, 4, 6, 8, 4],
      itemStyle: { color: '#10b981' },
    },
    {
      name: '平静',
      type: 'bar',
      stack: 'total',
      data: [4, 3, 5, 4, 3, 2, 3],
      itemStyle: { color: '#6366f1' },
    },
    {
      name: '消极',
      type: 'bar',
      stack: 'total',
      data: [1, 2, 1, 2, 1, 2, 3],
      itemStyle: { color: '#f43f5e' },
    },
  ],
}

const emotionRecords = ref([
  {
    id: 1,
    icon: '🙂',
    time: '今天 20:30',
    title: '学习状态不错',
    label: '还不错',
    tagClass: 'bg-emerald-500/30 border-emerald-500/50 text-emerald-400',
    content: '今天学习有点累，但是收获也很大！完成了定积分的复习，感觉对几何意义的理解更深了。',
    aiResponse: '太棒了！你的努力正在积累成果。建议你现在可以稍微休息一下，让大脑消化今天学到的知识。',
  },
  {
    id: 2,
    icon: '😔',
    time: '昨天 15:20',
    title: '有点疲惫',
    label: '有点累',
    tagClass: 'bg-amber-500/30 border-amber-500/50 text-amber-400',
    content: '连续学了3个小时，感觉有点疲惫，注意力不太集中了。',
    aiResponse: '学习疲劳是正常的！根据你的学习人格，建议采用番茄工作法，每25分钟休息5分钟。现在可以起来活动一下，喝杯水。',
  },
  {
    id: 3,
    icon: '😄',
    time: '前天 21:00',
    title: '攻克难题的喜悦',
    label: '超棒',
    tagClass: 'bg-primary/30 border-primary/50 text-primary',
    content: '终于搞懂了矩阵乘法的原理！原来一直搞混了行列的对应关系，现在豁然开朗。',
    aiResponse: '恭喜你！这种顿悟的感觉是学习中最美妙的时刻。建议你现在趁热打铁，做几道相关练习题巩固一下。',
  },
])

const saveEmotionRecord = () => {
  showRecordDialog.value = false
  selectedEmotion.value = ''
  emotionNote.value = ''
  relatedActivity.value = ''
}
</script>
