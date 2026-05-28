<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-text-primary">学习人格分析</h2>
        <p class="text-text-muted mt-1">深入了解你的学习DNA，发现最适合你的学习方式</p>
      </div>
      <el-button type="primary" size="large" class="gradient-primary border-0">
        <el-icon class="mr-2"><Refresh /></el-icon>重新测评
      </el-button>
    </div>

    <!-- 核心人格卡片 -->
    <div class="grid grid-cols-12 gap-6">
      <!-- 人格类型主卡片 -->
      <div class="col-span-5 card-gradient rounded-2xl p-8 relative overflow-hidden">
        <div class="absolute top-0 right-0 w-64 h-64 bg-gradient-to-br from-primary/20 to-accent-purple/20 rounded-full blur-3xl -mr-32 -mt-32"></div>
        <div class="relative z-10">
          <div class="flex items-center gap-4 mb-6">
            <div class="w-20 h-20 rounded-2xl bg-gradient-primary flex items-center justify-center glow-effect">
              <el-icon class="text-white text-4xl"><User /></el-icon>
            </div>
            <div>
              <div class="text-sm text-text-muted mb-1">你的学习人格</div>
              <div class="text-3xl font-bold gradient-text">{{ dna.type }}</div>
              <div class="flex gap-2 mt-2">
                <el-tag v-for="tag in dna.tags" :key="tag" effect="dark" size="small" class="bg-primary/30 border-primary/50">{{ tag }}</el-tag>
              </div>
            </div>
          </div>
          <p class="text-text-secondary leading-relaxed mb-6">{{ dna.description }}</p>
          <div class="grid grid-cols-2 gap-4">
            <div class="bg-dark-bg/50 rounded-xl p-4">
              <div class="text-2xl font-bold text-emerald-400">{{ dna.strengthScore }}%</div>
              <div class="text-sm text-text-muted">优势匹配度</div>
            </div>
            <div class="bg-dark-bg/50 rounded-xl p-4">
              <div class="text-2xl font-bold text-primary">{{ dna.learningEfficiency }}%</div>
              <div class="text-sm text-text-muted">学习效率</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 能力雷达图 -->
      <div class="col-span-7 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">能力维度分析</h3>
          <el-radio-group v-model="radarTimeRange" size="small">
            <el-radio-button label="本周">本周</el-radio-button>
            <el-radio-button label="本月">本月</el-radio-button>
            <el-radio-button label="全部">全部</el-radio-button>
          </el-radio-group>
        </div>
        <div class="flex items-center gap-8">
          <div class="w-80 h-80">
            <v-chart class="w-full h-full" :option="radarOption" autoresize />
          </div>
          <div class="flex-1 space-y-4">
            <div v-for="item in dna.radarData" :key="item.name" class="flex items-center gap-4">
              <div class="w-20 text-sm text-text-secondary">{{ item.name }}</div>
              <el-progress :percentage="item.value" :color="getProgressColor(item.value)" class="flex-1" :stroke-width="8" />
              <div class="w-12 text-right font-bold" :class="getScoreColor(item.value)">{{ item.value }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 优势与风险分析 -->
    <div class="grid grid-cols-2 gap-6">
      <!-- 优势分析 -->
      <div class="card-gradient rounded-2xl p-6 border-l-4 border-emerald-500">
        <div class="flex items-center gap-3 mb-6">
          <div class="w-12 h-12 rounded-xl bg-emerald-500/20 flex items-center justify-center">
            <el-icon class="text-emerald-400 text-2xl"><CircleCheck /></el-icon>
          </div>
          <div>
            <h3 class="font-bold text-text-primary text-lg">核心优势</h3>
            <p class="text-sm text-text-muted">你的学习超能力</p>
          </div>
        </div>
        <div class="space-y-4">
          <div v-for="(strength, index) in dna.strengths" :key="index" class="flex items-start gap-4 p-4 bg-dark-bg/50 rounded-xl">
            <div class="w-8 h-8 rounded-lg bg-emerald-500/20 flex items-center justify-center flex-shrink-0">
              <span class="text-emerald-400 font-bold">{{ index + 1 }}</span>
            </div>
            <div>
              <div class="font-medium text-text-primary mb-1">{{ strength.title }}</div>
              <p class="text-sm text-text-secondary">{{ strength.desc }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 风险提示 -->
      <div class="card-gradient rounded-2xl p-6 border-l-4 border-amber-500">
        <div class="flex items-center gap-3 mb-6">
          <div class="w-12 h-12 rounded-xl bg-amber-500/20 flex items-center justify-center">
            <el-icon class="text-amber-400 text-2xl"><Warning /></el-icon>
          </div>
          <div>
            <h3 class="font-bold text-text-primary text-lg">潜在风险</h3>
            <p class="text-sm text-text-muted">需要注意的学习陷阱</p>
          </div>
        </div>
        <div class="space-y-4">
          <div v-for="(weakness, index) in dna.weaknesses" :key="index" class="flex items-start gap-4 p-4 bg-dark-bg/50 rounded-xl">
            <div class="w-8 h-8 rounded-lg bg-amber-500/20 flex items-center justify-center flex-shrink-0">
              <span class="text-amber-400 font-bold">{{ index + 1 }}</span>
            </div>
            <div>
              <div class="font-medium text-text-primary mb-1">{{ weakness.title }}</div>
              <p class="text-sm text-text-secondary">{{ weakness.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- AI个性化建议 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center gap-3 mb-6">
        <div class="w-12 h-12 rounded-xl bg-gradient-primary flex items-center justify-center">
          <el-icon class="text-white text-2xl"><MagicStick /></el-icon>
        </div>
        <div>
          <h3 class="font-bold text-text-primary text-lg">AI个性化学习策略</h3>
          <p class="text-sm text-text-muted">基于你的学习人格定制的专属方案</p>
        </div>
      </div>
      <div class="grid grid-cols-3 gap-6">
        <div v-for="(strategy, index) in dna.strategies" :key="index" class="bg-dark-bg/50 rounded-xl p-6 hover:bg-dark-border/50 transition-all duration-300 cursor-pointer group">
          <div class="w-14 h-14 rounded-xl mb-4 flex items-center justify-center" :class="strategy.bgClass">
            <el-icon class="text-2xl" :class="strategy.iconClass"><component :is="strategy.icon" /></el-icon>
          </div>
          <h4 class="font-bold text-text-primary mb-2 group-hover:text-primary transition-colors">{{ strategy.title }}</h4>
          <p class="text-sm text-text-secondary leading-relaxed">{{ strategy.desc }}</p>
          <div class="mt-4 flex items-center gap-2 text-primary text-sm">
            <span>查看详情</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 学习历史趋势 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h3 class="font-bold text-text-primary text-lg">学习人格演变趋势</h3>
          <p class="text-sm text-text-muted">追踪你的学习风格变化</p>
        </div>
        <el-radio-group v-model="trendTimeRange" size="small">
          <el-radio-button label="近7天">近7天</el-radio-button>
          <el-radio-button label="近30天">近30天</el-radio-button>
          <el-radio-button label="近90天">近90天</el-radio-button>
        </el-radio-group>
      </div>
      <div class="h-64">
        <v-chart class="w-full h-full" :option="trendOption" autoresize />
      </div>
    </div>

    <!-- 人格对比 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h3 class="font-bold text-text-primary text-lg">与同类学习者对比</h3>
          <p class="text-sm text-text-muted">了解你在同类学习者中的位置</p>
        </div>
        <el-select v-model="compareSubject" placeholder="选择学科" size="small" style="width: 120px">
          <el-option label="全部学科" value="all" />
          <el-option label="高等数学" value="math" />
          <el-option label="英语" value="english" />
          <el-option label="专业课" value="major" />
        </el-select>
      </div>
      <div class="grid grid-cols-4 gap-6">
        <div v-for="(compare, index) in compareData" :key="index" class="text-center p-6 bg-dark-bg/50 rounded-xl">
          <div class="text-sm text-text-muted mb-2">{{ compare.label }}</div>
          <div class="text-3xl font-bold mb-2" :class="compare.color">{{ compare.value }}</div>
          <div class="text-xs text-text-secondary">超过 {{ compare.percent }}% 的同类学习者</div>
          <el-progress :percentage="compare.percent" :color="compare.progressColor" class="mt-3" :show-text="false" :stroke-width="6" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { RadarChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, RadarComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import {
  User,
  Refresh,
  CircleCheck,
  Warning,
  MagicStick,
  ArrowRight,
  Clock,
  Reading,
  Aim,
} from '@element-plus/icons-vue'

use([CanvasRenderer, RadarChart, LineChart, GridComponent, TooltipComponent, LegendComponent, RadarComponent])

const radarTimeRange = ref('本周')
const trendTimeRange = ref('近7天')
const compareSubject = ref('all')

const dna = ref({
  type: '理解驱动型',
  tags: ['理解驱动型', '夜间高效型', '焦虑型学习者'],
  description: '你擅长深入理解知识，喜欢探索原理，在安静的环境中效率更高，适合深度学习。你对抽象概念的理解能力很强，但有时候会因为追求完美而拖延。',
  strengthScore: 87,
  learningEfficiency: 82,
  radarData: [
    { name: '理解能力', value: 85 },
    { name: '记忆能力', value: 72 },
    { name: '专注持久度', value: 65 },
    { name: '计划执行力', value: 58 },
    { name: '情绪稳定度', value: 70 },
    { name: '逻辑思维', value: 78 },
  ],
  strengths: [
    { title: '理解速度快', desc: '你能够快速抓住知识的核心概念，善于建立知识体系' },
    { title: '图像记忆强', desc: '对图表、流程图等视觉化内容记忆深刻' },
    { title: '深度思考能力', desc: '不满足于表面理解，喜欢探究原理和本质' },
  ],
  weaknesses: [
    { title: '容易拖延', desc: '面对大型任务时容易感到压力，导致拖延行为' },
    { title: '长时间学习效率下降', desc: '持续学习超过1小时后，注意力和效率明显降低' },
    { title: '完美主义倾向', desc: '过度追求细节完美，影响整体学习进度' },
  ],
  strategies: [
    { title: '番茄工作法', desc: '采用25分钟专注+5分钟休息的循环，保持高效学习状态', icon: 'Clock', bgClass: 'bg-primary/20', iconClass: 'text-primary' },
    { title: '晚间深度学习', desc: '利用夜间安静时段进行需要深度思考的学习内容', icon: 'Reading', bgClass: 'bg-accent-purple/20', iconClass: 'text-accent-purple' },
    { title: '先理解后刷题', desc: '先通过视频/讲解理解概念，再进行练习巩固', icon: 'Aim', bgClass: 'bg-accent-cyan/20', iconClass: 'text-accent-cyan' },
  ],
})

const radarOption = {
  radar: {
    indicator: dna.value.radarData.map(item => ({ name: item.name, max: 100 })),
    radius: '70%',
    axisName: {
      color: '#9ca3af',
      fontSize: 12,
    },
    splitArea: {
      areaStyle: {
        color: ['rgba(99, 102, 241, 0.05)', 'rgba(99, 102, 241, 0.1)', 'rgba(99, 102, 241, 0.15)'],
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
      value: dna.value.radarData.map(item => item.value),
      name: '当前能力',
      areaStyle: {
        color: 'rgba(99, 102, 241, 0.3)',
      },
      lineStyle: {
        color: '#6366f1',
        width: 3,
      },
      itemStyle: {
        color: '#6366f1',
        borderWidth: 2,
        borderColor: '#fff',
      },
    }, {
      value: [70, 65, 55, 50, 60, 65],
      name: '上周平均',
      areaStyle: {
        color: 'rgba(168, 85, 247, 0.15)',
      },
      lineStyle: {
        color: '#a855f7',
        width: 2,
        type: 'dashed',
      },
      itemStyle: {
        color: '#a855f7',
      },
    }],
  }],
  legend: {
    bottom: 0,
    textStyle: { color: '#9ca3af' },
  },
}

const trendOption = {
  grid: { top: 20, right: 20, bottom: 40, left: 50 },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(17, 24, 39, 0.9)',
    borderColor: '#374151',
    textStyle: { color: '#f9fafb' },
  },
  legend: {
    data: ['理解能力', '记忆能力', '专注持久度'],
    bottom: 0,
    textStyle: { color: '#9ca3af' },
  },
  xAxis: {
    type: 'category',
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
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
      name: '理解能力',
      type: 'line',
      smooth: true,
      data: [80, 82, 81, 83, 85, 84, 85],
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
      name: '记忆能力',
      type: 'line',
      smooth: true,
      data: [68, 70, 69, 71, 72, 71, 72],
      lineStyle: { color: '#a855f7', width: 3 },
      itemStyle: { color: '#a855f7' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(168, 85, 247, 0.4)' },
            { offset: 1, color: 'rgba(168, 85, 247, 0)' },
          ],
        },
      },
    },
    {
      name: '专注持久度',
      type: 'line',
      smooth: true,
      data: [60, 62, 61, 63, 65, 64, 65],
      lineStyle: { color: '#06b6d4', width: 3 },
      itemStyle: { color: '#06b6d4' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(6, 182, 212, 0.4)' },
            { offset: 1, color: 'rgba(6, 182, 212, 0)' },
          ],
        },
      },
    },
  ],
}

const compareData = [
  { label: '学习效率', value: '82%', percent: 78, color: 'text-primary', progressColor: '#6366f1' },
  { label: '知识掌握', value: '76%', percent: 65, color: 'text-accent-purple', progressColor: '#a855f7' },
  { label: '任务完成', value: '85%', percent: 82, color: 'text-emerald-400', progressColor: '#10b981' },
  { label: '专注时长', value: '4.2h', percent: 70, color: 'text-accent-cyan', progressColor: '#06b6d4' },
]

const getProgressColor = (value: number) => {
  if (value >= 80) return '#10b981'
  if (value >= 60) return '#6366f1'
  return '#f59e0b'
}

const getScoreColor = (value: number) => {
  if (value >= 80) return 'text-emerald-400'
  if (value >= 60) return 'text-primary'
  return 'text-amber-400'
}
</script>
