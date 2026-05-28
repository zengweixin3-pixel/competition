<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-text-primary">学习报告</h2>
        <p class="text-text-muted mt-1">全面的学习数据分析，见证你的成长</p>
      </div>
      <div class="flex gap-3">
        <el-button size="large" @click="exportReport">
          <el-icon class="mr-2"><Download /></el-icon>导出报告
        </el-button>
        <el-button type="primary" size="large" @click="shareReport">
          <el-icon class="mr-2"><Share /></el-icon>分享
        </el-button>
      </div>
    </div>

    <!-- 核心数据概览 -->
    <div class="grid grid-cols-5 gap-6">
      <div class="card-gradient rounded-2xl p-6 text-center">
        <div class="text-4xl font-bold gradient-text mb-2">{{ overview.totalHours }}</div>
        <div class="text-sm text-text-muted">总学习时长(小时)</div>
        <div class="text-xs text-emerald-400 mt-2">↑ {{ overview.hoursChange }}% 较上月</div>
      </div>
      <div class="card-gradient rounded-2xl p-6 text-center">
        <div class="text-4xl font-bold text-primary mb-2">{{ overview.avgDaily }}</div>
        <div class="text-sm text-text-muted">日均学习(小时)</div>
        <div class="text-xs text-emerald-400 mt-2">↑ {{ overview.dailyChange }}% 较上月</div>
      </div>
      <div class="card-gradient rounded-2xl p-6 text-center">
        <div class="text-4xl font-bold text-accent-purple mb-2">{{ overview.completedTasks }}</div>
        <div class="text-sm text-text-muted">完成任务数</div>
        <div class="text-xs text-emerald-400 mt-2">↑ {{ overview.tasksChange }}% 较上月</div>
      </div>
      <div class="card-gradient rounded-2xl p-6 text-center">
        <div class="text-4xl font-bold text-emerald-400 mb-2">{{ overview.accuracy }}%</div>
        <div class="text-sm text-text-muted">平均正确率</div>
        <div class="text-xs text-emerald-400 mt-2">↑ {{ overview.accuracyChange }}% 较上月</div>
      </div>
      <div class="card-gradient rounded-2xl p-6 text-center">
        <div class="text-4xl font-bold text-accent-cyan mb-2">{{ overview.streak }}天</div>
        <div class="text-sm text-text-muted">连续学习</div>
        <div class="text-xs text-text-muted mt-2">继续保持！</div>
      </div>
    </div>

    <!-- 学习趋势分析 -->
    <div class="grid grid-cols-12 gap-6">
      <div class="col-span-8 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="font-bold text-text-primary text-lg">学习时长趋势</h3>
            <p class="text-sm text-text-muted">追踪你的每日学习投入</p>
          </div>
          <el-radio-group v-model="trendTimeRange" size="small">
            <el-radio-button label="近7天">近7天</el-radio-button>
            <el-radio-button label="近30天">近30天</el-radio-button>
            <el-radio-button label="近90天">近90天</el-radio-button>
          </el-radio-group>
        </div>
        <div class="h-72">
          <v-chart class="w-full h-full" :option="studyTrendOption" autoresize />
        </div>
      </div>
      <div class="col-span-4 card-gradient rounded-2xl p-6">
        <h3 class="font-bold text-text-primary mb-4">学习时段分布</h3>
        <div class="h-72">
          <v-chart class="w-full h-full" :option="timeDistributionOption" autoresize />
        </div>
      </div>
    </div>

    <!-- 学科分析与能力评估 -->
    <div class="grid grid-cols-12 gap-6">
      <div class="col-span-6 card-gradient rounded-2xl p-6">
        <h3 class="font-bold text-text-primary mb-4">学科表现对比</h3>
        <div class="h-72">
          <v-chart class="w-full h-full" :option="subjectCompareOption" autoresize />
        </div>
      </div>
      <div class="col-span-6 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">能力成长曲线</h3>
          <el-select v-model="abilitySubject" placeholder="选择学科" size="small" style="width: 120px">
            <el-option label="全部学科" value="all" />
            <el-option label="高等数学" value="math" />
            <el-option label="英语" value="english" />
          </el-select>
        </div>
        <div class="h-72">
          <v-chart class="w-full h-full" :option="abilityGrowthOption" autoresize />
        </div>
      </div>
    </div>

    <!-- 错题分析与改进 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h3 class="font-bold text-text-primary text-lg">错题改进追踪</h3>
          <p class="text-sm text-text-muted">见证你的错题攻克之路</p>
        </div>
        <el-link type="primary" :underline="false">查看详情 ></el-link>
      </div>
      <div class="grid grid-cols-4 gap-6">
        <div class="bg-dark-bg/50 rounded-xl p-6 text-center">
          <div class="w-16 h-16 rounded-full bg-primary/20 flex items-center justify-center mx-auto mb-4">
            <el-icon class="text-primary text-3xl"><DocumentDelete /></el-icon>
          </div>
          <div class="text-3xl font-bold text-text-primary mb-1">{{ mistakeStats.total }}</div>
          <div class="text-sm text-text-muted">累计错题</div>
        </div>
        <div class="bg-dark-bg/50 rounded-xl p-6 text-center">
          <div class="w-16 h-16 rounded-full bg-emerald-500/20 flex items-center justify-center mx-auto mb-4">
            <el-icon class="text-emerald-400 text-3xl"><CircleCheck /></el-icon>
          </div>
          <div class="text-3xl font-bold text-text-primary mb-1">{{ mistakeStats.mastered }}</div>
          <div class="text-sm text-text-muted">已掌握</div>
        </div>
        <div class="bg-dark-bg/50 rounded-xl p-6 text-center">
          <div class="w-16 h-16 rounded-full bg-amber-500/20 flex items-center justify-center mx-auto mb-4">
            <el-icon class="text-amber-400 text-3xl"><Timer /></el-icon>
          </div>
          <div class="text-3xl font-bold text-text-primary mb-1">{{ mistakeStats.reviewing }}</div>
          <div class="text-sm text-text-muted">复习中</div>
        </div>
        <div class="bg-dark-bg/50 rounded-xl p-6 text-center">
          <div class="w-16 h-16 rounded-full bg-accent-purple/20 flex items-center justify-center mx-auto mb-4">
            <el-icon class="text-accent-purple text-3xl"><TrendCharts /></el-icon>
          </div>
          <div class="text-3xl font-bold text-text-primary mb-1">{{ mistakeStats.improvement }}%</div>
          <div class="text-sm text-text-muted">改进率</div>
        </div>
      </div>
      <div class="mt-6 h-64">
        <v-chart class="w-full h-full" :option="mistakeImprovementOption" autoresize />
      </div>
    </div>

    <!-- 学习成就 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h3 class="font-bold text-text-primary text-lg">学习成就</h3>
          <p class="text-sm text-text-muted">你的每一个进步都值得被记录</p>
        </div>
        <el-link type="primary" :underline="false">查看全部 ></el-link>
      </div>
      <div class="grid grid-cols-4 gap-6">
        <div
          v-for="achievement in achievements"
          :key="achievement.id"
          class="bg-dark-bg/50 rounded-xl p-6 text-center border-2 transition-all"
          :class="achievement.unlocked ? 'border-amber-500/50' : 'border-transparent opacity-50'"
        >
          <div class="w-20 h-20 rounded-full mx-auto mb-4 flex items-center justify-center" :class="achievement.unlocked ? achievement.bgClass : 'bg-dark-border'">
            <el-icon :class="achievement.unlocked ? achievement.iconClass : 'text-text-muted'" class="text-4xl"><component :is="achievement.icon" /></el-icon>
          </div>
          <h4 class="font-bold text-text-primary mb-1">{{ achievement.name }}</h4>
          <p class="text-sm text-text-secondary mb-3">{{ achievement.desc }}</p>
          <el-progress :percentage="achievement.progress" :color="achievement.unlocked ? '#f59e0b' : '#6b7280'" :show-text="false" :stroke-width="6" />
          <div class="text-xs text-text-muted mt-2">{{ achievement.progress }}%</div>
        </div>
      </div>
    </div>

    <!-- AI学习总结 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-12 h-12 rounded-xl bg-gradient-primary flex items-center justify-center">
          <el-icon class="text-white text-2xl"><ChatDotRound /></el-icon>
        </div>
        <div>
          <h3 class="font-bold text-text-primary text-lg">AI学习总结</h3>
          <p class="text-sm text-text-muted">基于你的学习数据生成的个性化分析</p>
        </div>
      </div>
      <div class="grid grid-cols-2 gap-6">
        <div class="bg-dark-bg/50 rounded-xl p-6">
          <h4 class="font-bold text-text-primary mb-4 flex items-center gap-2">
            <el-icon class="text-emerald-400"><CircleCheck /></el-icon>
            本月亮点
          </h4>
          <ul class="space-y-3">
            <li v-for="(highlight, idx) in aiSummary.highlights" :key="idx" class="flex items-start gap-2 text-sm text-text-secondary">
              <span class="text-emerald-400 mt-1">✓</span>
              <span>{{ highlight }}</span>
            </li>
          </ul>
        </div>
        <div class="bg-dark-bg/50 rounded-xl p-6">
          <h4 class="font-bold text-text-primary mb-4 flex items-center gap-2">
            <el-icon class="text-amber-400"><Warning /></el-icon>
            改进建议
          </h4>
          <ul class="space-y-3">
            <li v-for="(suggestion, idx) in aiSummary.suggestions" :key="idx" class="flex items-start gap-2 text-sm text-text-secondary">
              <span class="text-amber-400 mt-1">•</span>
              <span>{{ suggestion }}</span>
            </li>
          </ul>
        </div>
      </div>
      <div class="mt-6 bg-gradient-to-r from-primary/10 to-accent-purple/10 rounded-xl p-6 border border-primary/20">
        <div class="flex items-start gap-4">
          <el-avatar :size="48" class="bg-gradient-primary">
            <el-icon class="text-white text-xl"><ChatDotRound /></el-icon>
          </el-avatar>
          <div>
            <div class="font-medium text-text-primary mb-2">AI学习教练说</div>
            <p class="text-text-secondary leading-relaxed">{{ aiSummary.coachMessage }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart, RadarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, RadarComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import {
  Download,
  Share,
  DocumentDelete,
  CircleCheck,
  Timer,
  TrendCharts,
  ChatDotRound,
  Warning,
  Trophy,
  Fire,
  Star,
  Medal,
  Target,
} from '@element-plus/icons-vue'

use([CanvasRenderer, LineChart, BarChart, PieChart, RadarChart, GridComponent, TooltipComponent, LegendComponent, RadarComponent])

const trendTimeRange = ref('近30天')
const abilitySubject = ref('all')

const overview = ref({
  totalHours: 126.5,
  hoursChange: 15,
  avgDaily: 4.2,
  dailyChange: 8,
  completedTasks: 156,
  tasksChange: 23,
  accuracy: 78,
  accuracyChange: 5,
  streak: 23,
})

const studyTrendOption = {
  grid: { top: 20, right: 20, bottom: 40, left: 50 },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(17, 24, 39, 0.9)',
    borderColor: '#374151',
    textStyle: { color: '#f9fafb' },
  },
  legend: {
    data: ['学习时长', '专注度'],
    bottom: 0,
    textStyle: { color: '#9ca3af' },
  },
  xAxis: {
    type: 'category',
    data: ['1日', '5日', '10日', '15日', '20日', '25日', '30日'],
    axisLine: { lineStyle: { color: '#374151' } },
    axisLabel: { color: '#6b7280' },
  },
  yAxis: [
    {
      type: 'value',
      name: '时长(小时)',
      nameTextStyle: { color: '#9ca3af' },
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#1f2937' } },
      axisLabel: { color: '#6b7280' },
    },
    {
      type: 'value',
      name: '专注度',
      max: 100,
      nameTextStyle: { color: '#9ca3af' },
      axisLine: { show: false },
      splitLine: { show: false },
      axisLabel: { color: '#6b7280', formatter: '{value}%' },
    },
  ],
  series: [
    {
      name: '学习时长',
      type: 'bar',
      data: [3.5, 4.2, 5.0, 4.5, 6.0, 4.8, 5.2],
      itemStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#6366f1' },
            { offset: 1, color: '#a855f7' },
          ],
        },
        borderRadius: [4, 4, 0, 0],
      },
      barWidth: '50%',
    },
    {
      name: '专注度',
      type: 'line',
      yAxisIndex: 1,
      smooth: true,
      data: [75, 82, 88, 85, 92, 80, 87],
      lineStyle: { color: '#10b981', width: 3 },
      itemStyle: { color: '#10b981' },
    },
  ],
}

const timeDistributionOption = {
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(17, 24, 39, 0.9)',
    borderColor: '#374151',
    textStyle: { color: '#f9fafb' },
  },
  legend: {
    orient: 'vertical',
    right: 10,
    top: 'center',
    textStyle: { color: '#9ca3af' },
  },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    center: ['35%', '50%'],
    avoidLabelOverlap: false,
    itemStyle: {
      borderRadius: 8,
      borderColor: '#0a0e1a',
      borderWidth: 2,
    },
    label: { show: false },
    data: [
      { value: 35, name: '晚上(18-24点)', itemStyle: { color: '#6366f1' } },
      { value: 25, name: '下午(14-18点)', itemStyle: { color: '#a855f7' } },
      { value: 20, name: '上午(8-12点)', itemStyle: { color: '#10b981' } },
      { value: 15, name: '深夜(0-2点)', itemStyle: { color: '#f59e0b' } },
      { value: 5, name: '其他时段', itemStyle: { color: '#6b7280' } },
    ],
  }],
}

const subjectCompareOption = {
  radar: {
    indicator: [
      { name: '学习时长', max: 100 },
      { name: '正确率', max: 100 },
      { name: '任务完成', max: 100 },
      { name: '知识掌握', max: 100 },
      { name: '进步速度', max: 100 },
    ],
    radius: '65%',
    axisName: { color: '#9ca3af', fontSize: 11 },
    splitArea: {
      areaStyle: {
        color: ['rgba(99, 102, 241, 0.05)', 'rgba(99, 102, 241, 0.1)'],
      },
    },
    axisLine: { lineStyle: { color: 'rgba(99, 102, 241, 0.3)' } },
    splitLine: { lineStyle: { color: 'rgba(99, 102, 241, 0.2)' } },
  },
  legend: {
    bottom: 0,
    textStyle: { color: '#9ca3af' },
  },
  series: [{
    type: 'radar',
    data: [
      {
        value: [85, 72, 80, 75, 88],
        name: '高等数学',
        areaStyle: { color: 'rgba(99, 102, 241, 0.3)' },
        lineStyle: { color: '#6366f1', width: 2 },
        itemStyle: { color: '#6366f1' },
      },
      {
        value: [70, 85, 75, 80, 70],
        name: '英语',
        areaStyle: { color: 'rgba(16, 185, 129, 0.3)' },
        lineStyle: { color: '#10b981', width: 2 },
        itemStyle: { color: '#10b981' },
      },
      {
        value: [60, 68, 65, 70, 75],
        name: '线性代数',
        areaStyle: { color: 'rgba(168, 85, 247, 0.3)' },
        lineStyle: { color: '#a855f7', width: 2 },
        itemStyle: { color: '#a855f7' },
      },
    ],
  }],
}

const abilityGrowthOption = {
  grid: { top: 20, right: 20, bottom: 40, left: 50 },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(17, 24, 39, 0.9)',
    borderColor: '#374151',
    textStyle: { color: '#f9fafb' },
  },
  legend: {
    data: ['知识掌握度', '解题能力', '理解深度'],
    bottom: 0,
    textStyle: { color: '#9ca3af' },
  },
  xAxis: {
    type: 'category',
    data: ['第1周', '第2周', '第3周', '第4周'],
    axisLine: { lineStyle: { color: '#374151' } },
    axisLabel: { color: '#6b7280' },
  },
  yAxis: {
    type: 'value',
    max: 100,
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#1f2937' } },
    axisLabel: { color: '#6b7280' },
  },
  series: [
    {
      name: '知识掌握度',
      type: 'line',
      smooth: true,
      data: [45, 58, 68, 78],
      lineStyle: { color: '#6366f1', width: 3 },
      itemStyle: { color: '#6366f1' },
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
    },
    {
      name: '解题能力',
      type: 'line',
      smooth: true,
      data: [40, 52, 62, 72],
      lineStyle: { color: '#10b981', width: 3 },
      itemStyle: { color: '#10b981' },
    },
    {
      name: '理解深度',
      type: 'line',
      smooth: true,
      data: [50, 60, 72, 82],
      lineStyle: { color: '#a855f7', width: 3 },
      itemStyle: { color: '#a855f7' },
    },
  ],
}

const mistakeStats = ref({
  total: 128,
  mastered: 89,
  reviewing: 27,
  improvement: 76,
})

const mistakeImprovementOption = {
  grid: { top: 20, right: 20, bottom: 40, left: 50 },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(17, 24, 39, 0.9)',
    borderColor: '#374151',
    textStyle: { color: '#f9fafb' },
  },
  legend: {
    data: ['新增错题', '已掌握', '复习中'],
    bottom: 0,
    textStyle: { color: '#9ca3af' },
  },
  xAxis: {
    type: 'category',
    data: ['第1周', '第2周', '第3周', '第4周'],
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
      name: '新增错题',
      type: 'bar',
      data: [15, 12, 10, 8],
      itemStyle: { color: '#f43f5e', borderRadius: [4, 4, 0, 0] },
      barWidth: '25%',
    },
    {
      name: '已掌握',
      type: 'bar',
      data: [8, 15, 20, 25],
      itemStyle: { color: '#10b981', borderRadius: [4, 4, 0, 0] },
      barWidth: '25%',
    },
    {
      name: '复习中',
      type: 'bar',
      data: [7, 4, 5, 3],
      itemStyle: { color: '#f59e0b', borderRadius: [4, 4, 0, 0] },
      barWidth: '25%',
    },
  ],
}

const achievements = ref([
  { id: 1, name: '学习达人', desc: '累计学习100小时', icon: 'Trophy', unlocked: true, progress: 100, bgClass: 'bg-amber-500/20', iconClass: 'text-amber-400' },
  { id: 2, name: '连续打卡', desc: '连续学习21天', icon: 'Fire', unlocked: true, progress: 100, bgClass: 'bg-orange-500/20', iconClass: 'text-orange-400' },
  { id: 3, name: '错题克星', desc: '掌握100道错题', icon: 'Target', unlocked: false, progress: 89, bgClass: 'bg-primary/20', iconClass: 'text-primary' },
  { id: 4, name: '全能学霸', desc: '所有学科正确率超80%', icon: 'Medal', unlocked: false, progress: 75, bgClass: 'bg-accent-purple/20', iconClass: 'text-accent-purple' },
])

const aiSummary = ref({
  highlights: [
    '本月学习时长达到126.5小时，较上月增长15%，表现优异',
    '连续23天坚持学习，养成了良好的学习习惯',
    '高数成绩提升明显，正确率从65%提升到78%',
    '错题改进率达到76%，说明复习方法有效',
  ],
  suggestions: [
    '线性代数学习时间偏少，建议增加投入',
    '上午时段学习效率较低，可以尝试调整学习计划',
    '概念混淆类错题较多，建议加强基础概念理解',
    '深夜学习时间占比过高，注意保证充足睡眠',
  ],
  coachMessage: '小明同学，这个月你的学习表现非常出色！特别是连续23天的坚持，让我看到了你的毅力。高数的进步也很明显，继续保持！不过要注意平衡各科的学习时间，线性代数还需要加强。记住，学习是一场马拉松，保持节奏比冲刺更重要。加油！',
})

const exportReport = () => {}
const shareReport = () => {}
</script>
