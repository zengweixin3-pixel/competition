<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-text-primary">学习计划</h2>
        <p class="text-text-muted mt-1">AI根据你的学习人格定制的专属计划</p>
      </div>
      <div class="flex gap-3">
        <el-button size="large" @click="showGenerateDialog = true">
          <el-icon class="mr-2"><MagicStick /></el-icon>AI生成计划
        </el-button>
        <el-button type="primary" size="large" @click="showCreateDialog = true">
          <el-icon class="mr-2"><Plus /></el-icon>创建计划
        </el-button>
      </div>
    </div>

    <!-- 今日计划概览 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h3 class="font-bold text-text-primary text-lg">今日学习计划</h3>
          <p class="text-sm text-text-muted">根据你的"理解驱动型"人格优化</p>
        </div>
        <div class="flex items-center gap-4">
          <div class="text-right">
            <div class="text-2xl font-bold text-text-primary">{{ todayProgress }}%</div>
            <div class="text-xs text-text-muted">已完成</div>
          </div>
          <el-progress type="circle" :percentage="todayProgress" :width="60" :stroke-width="6" :color="['#6366f1', '#a855f7']" />
        </div>
      </div>

      <div class="grid grid-cols-12 gap-4">
        <div class="col-span-8 space-y-3">
          <div
            v-for="task in todayTasks"
            :key="task.id"
            class="flex items-center gap-4 p-4 bg-dark-bg/50 rounded-xl border-l-4"
            :class="task.borderColor"
          >
            <el-checkbox v-model="task.completed" size="large" @change="updateProgress" />
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-1">
                <span class="font-medium" :class="task.completed ? 'line-through text-text-muted' : 'text-text-primary'">{{ task.name }}</span>
                <el-tag size="small" :class="task.tagClass">{{ task.subject }}</el-tag>
                <el-tag v-if="task.aiRecommended" size="small" effect="dark" class="bg-primary/30 border-primary/50">
                  <el-icon class="mr-1"><Star /></el-icon>AI推荐
                </el-tag>
              </div>
              <div class="flex items-center gap-4 text-sm text-text-muted">
                <span><el-icon class="mr-1"><Clock /></el-icon>{{ task.duration }}分钟</span>
                <span><el-icon class="mr-1"><Aim /></el-icon>{{ task.goal }}</span>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <el-button v-if="!task.completed" type="primary" size="small" plain @click="startTask(task)">开始</el-button>
              <el-button size="small" @click="editTask(task)">编辑</el-button>
            </div>
          </div>
        </div>
        <div class="col-span-4">
          <div class="bg-gradient-to-br from-primary/10 to-accent-purple/10 rounded-xl p-4 border border-primary/20 h-full">
            <div class="flex items-center gap-2 mb-4">
              <el-icon class="text-primary"><ChatDotRound /></el-icon>
              <span class="font-medium text-text-primary">AI学习建议</span>
            </div>
            <div class="space-y-3 text-sm">
              <div class="flex items-start gap-2">
                <el-icon class="text-primary mt-0.5"><CircleCheck /></el-icon>
                <span class="text-text-secondary">现在是你的高效时段，建议先完成高数定积分练习</span>
              </div>
              <div class="flex items-start gap-2">
                <el-icon class="text-primary mt-0.5"><Clock /></el-icon>
                <span class="text-text-secondary">每25分钟休息5分钟，保持最佳学习状态</span>
              </div>
              <div class="flex items-start gap-2">
                <el-icon class="text-primary mt-0.5"><Warning /></el-icon>
                <span class="text-text-secondary">注意：矩阵运算需要多检查，这是你的薄弱点</span>
              </div>
            </div>
            <el-button type="primary" class="w-full mt-4" size="small">
              <el-icon class="mr-1"><VideoPlay /></el-icon>开始专注模式
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 周计划视图 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-4">
          <h3 class="font-bold text-text-primary text-lg">本周计划</h3>
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="日视图">日视图</el-radio-button>
            <el-radio-button label="周视图">周视图</el-radio-button>
            <el-radio-button label="月视图">月视图</el-radio-button>
          </el-radio-group>
        </div>
        <div class="flex items-center gap-2">
          <el-button size="small" :icon="ArrowLeft" circle @click="prevWeek" />
          <span class="text-text-primary">{{ currentWeekRange }}</span>
          <el-button size="small" :icon="ArrowRight" circle @click="nextWeek" />
        </div>
      </div>

      <div class="grid grid-cols-7 gap-4">
        <div
          v-for="day in weekDays"
          :key="day.date"
          class="rounded-xl p-4 min-h-[200px]"
          :class="day.isToday ? 'bg-primary/10 border border-primary/30' : 'bg-dark-bg/50'"
        >
          <div class="flex items-center justify-between mb-3">
            <span class="text-sm" :class="day.isToday ? 'text-primary font-bold' : 'text-text-muted'">{{ day.name }}</span>
            <span class="text-xs" :class="day.isToday ? 'text-primary' : 'text-text-muted'">{{ day.date }}</span>
          </div>
          <div class="space-y-2">
            <div
              v-for="task in day.tasks"
              :key="task.id"
              class="text-xs p-2 rounded-lg cursor-pointer transition-all"
              :class="task.completed ? 'bg-emerald-500/20 text-emerald-400 line-through' : 'bg-dark-border text-text-secondary hover:bg-primary/20'"
            >
              <div class="flex items-center gap-1">
                <span class="w-1.5 h-1.5 rounded-full" :class="task.color"></span>
                <span class="truncate">{{ task.name }}</span>
              </div>
            </div>
          </div>
          <div v-if="day.tasks.length === 0" class="text-xs text-text-muted text-center py-4">
            暂无计划
          </div>
        </div>
      </div>
    </div>

    <!-- 学习统计 -->
    <div class="grid grid-cols-12 gap-6">
      <div class="col-span-8 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">计划完成情况趋势</h3>
          <el-radio-group v-model="statsTimeRange" size="small">
            <el-radio-button label="近7天">近7天</el-radio-button>
            <el-radio-button label="近30天">近30天</el-radio-button>
          </el-radio-group>
        </div>
        <div class="h-64">
          <v-chart class="w-full h-full" :option="completionTrendOption" autoresize />
        </div>
      </div>
      <div class="col-span-4 card-gradient rounded-2xl p-6">
        <h3 class="font-bold text-text-primary mb-4">学科时间分配</h3>
        <div class="h-64">
          <v-chart class="w-full h-full" :option="subjectTimeOption" autoresize />
        </div>
      </div>
    </div>

    <!-- 计划模板 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h3 class="font-bold text-text-primary text-lg">AI推荐计划模板</h3>
          <p class="text-sm text-text-muted">基于你的学习人格定制</p>
        </div>
        <el-link type="primary" :underline="false">查看更多 ></el-link>
      </div>
      <div class="grid grid-cols-3 gap-6">
        <div
          v-for="template in planTemplates"
          :key="template.id"
          class="bg-dark-bg/50 rounded-xl p-6 border border-transparent hover:border-primary/50 transition-all cursor-pointer group"
        >
          <div class="flex items-start justify-between mb-4">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center" :class="template.iconBg">
              <el-icon :class="template.iconColor" class="text-2xl"><component :is="template.icon" /></el-icon>
            </div>
            <el-tag v-if="template.forDNA" size="small" effect="dark" class="bg-primary/30 border-primary/50">{{ template.forDNA }}</el-tag>
          </div>
          <h4 class="font-bold text-text-primary mb-2 group-hover:text-primary transition-colors">{{ template.name }}</h4>
          <p class="text-sm text-text-secondary mb-4">{{ template.description }}</p>
          <div class="flex items-center gap-4 text-xs text-text-muted">
            <span><el-icon class="mr-1"><Clock /></el-icon>{{ template.duration }}</span>
            <span><el-icon class="mr-1"><Document /></el-icon>{{ template.taskCount }}个任务</span>
          </div>
          <el-button type="primary" class="w-full mt-4" plain>使用此模板</el-button>
        </div>
      </div>
    </div>
  </div>

  <!-- 创建计划对话框 -->
  <el-dialog v-model="showCreateDialog" title="创建学习计划" width="600px" destroy-on-close>
    <div class="space-y-4">
      <el-input v-model="newPlan.name" placeholder="计划名称" />
      <el-select v-model="newPlan.subject" placeholder="选择学科" class="w-full">
        <el-option label="高等数学" value="math" />
        <el-option label="线性代数" value="linear" />
        <el-option label="英语" value="english" />
        <el-option label="专业课" value="major" />
      </el-select>
      <el-date-picker v-model="newPlan.date" type="date" placeholder="选择日期" class="w-full" />
      <el-time-picker v-model="newPlan.startTime" placeholder="开始时间" class="w-full" />
      <el-input-number v-model="newPlan.duration" :min="15" :max="180" :step="15" class="w-full" placeholder="持续时间（分钟）" />
      <el-input v-model="newPlan.goal" type="textarea" :rows="3" placeholder="学习目标" />
    </div>
    <template #footer>
      <el-button @click="showCreateDialog = false">取消</el-button>
      <el-button type="primary" @click="createPlan">创建</el-button>
    </template>
  </el-dialog>

  <!-- AI生成计划对话框 -->
  <el-dialog v-model="showGenerateDialog" title="AI智能生成学习计划" width="600px" destroy-on-close>
    <div class="space-y-4">
      <div class="bg-primary/10 rounded-xl p-4 border border-primary/20">
        <div class="flex items-start gap-3">
          <el-icon class="text-primary text-xl"><ChatDotRound /></el-icon>
          <div>
            <div class="font-medium text-text-primary mb-1">AI将根据以下信息为你生成计划</div>
            <ul class="text-sm text-text-secondary space-y-1">
              <li>• 你的学习人格类型：理解驱动型</li>
              <li>• 近期错题分析结果</li>
              <li>• 你的高效学习时段</li>
              <li>• 当前学习目标和进度</li>
            </ul>
          </div>
        </div>
      </div>
      <el-select v-model="generateParams.period" placeholder="计划周期" class="w-full">
        <el-option label="今日计划" value="today" />
        <el-option label="本周计划" value="week" />
        <el-option label="本月计划" value="month" />
      </el-select>
      <el-select v-model="generateParams.focus" placeholder="学习重点" class="w-full">
        <el-option label="薄弱科目强化" value="weak" />
        <el-option label="全面复习" value="review" />
        <el-option label="新课预习" value="preview" />
        <el-option label="考前冲刺" value="exam" />
      </el-select>
      <el-input-number v-model="generateParams.dailyHours" :min="1" :max="12" class="w-full" placeholder="每日学习时长（小时）" />
      <el-input v-model="generateParams.notes" type="textarea" :rows="3" placeholder="特殊需求或备注（可选）" />
    </div>
    <template #footer>
      <el-button @click="showGenerateDialog = false">取消</el-button>
      <el-button type="primary" :loading="isGenerating" @click="generatePlan">
        <el-icon class="mr-1"><MagicStick /></el-icon>生成计划
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import {
  Plus,
  MagicStick,
  Clock,
  Aim,
  Star,
  ChatDotRound,
  CircleCheck,
  Warning,
  VideoPlay,
  ArrowLeft,
  ArrowRight,
  Document,
  Reading,
  Calendar,
} from '@element-plus/icons-vue'

use([CanvasRenderer, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

const viewMode = ref('周视图')
const statsTimeRange = ref('近7天')
const showCreateDialog = ref(false)
const showGenerateDialog = ref(false)
const isGenerating = ref(false)
const currentWeekRange = ref('1月15日 - 1月21日')

const todayTasks = ref([
  {
    id: 1,
    name: '高数：定积分应用练习',
    subject: '高等数学',
    duration: 60,
    goal: '完成5道定积分求面积题目',
    completed: true,
    aiRecommended: true,
    borderColor: 'border-primary',
    tagClass: 'bg-primary/20 text-primary border-primary/50',
  },
  {
    id: 2,
    name: '英语：阅读理解训练',
    subject: '英语',
    duration: 40,
    goal: '完成2篇阅读理解，总结主旨',
    completed: true,
    aiRecommended: false,
    borderColor: 'border-blue-500',
    tagClass: 'bg-blue-500/20 text-blue-400 border-blue-500/50',
  },
  {
    id: 3,
    name: '线代：矩阵运算专项',
    subject: '线性代数',
    duration: 45,
    goal: '复习矩阵乘法规则，做10道计算题',
    completed: false,
    aiRecommended: true,
    borderColor: 'border-accent-purple',
    tagClass: 'bg-accent-purple/20 text-accent-purple border-accent-purple/50',
  },
  {
    id: 4,
    name: '专业课：信号与系统',
    subject: '专业课',
    duration: 60,
    goal: '学习傅里叶变换基础概念',
    completed: false,
    aiRecommended: false,
    borderColor: 'border-emerald-500',
    tagClass: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/50',
  },
])

const todayProgress = computed(() => {
  const completed = todayTasks.value.filter(t => t.completed).length
  return Math.round((completed / todayTasks.value.length) * 100)
})

const weekDays = ref([
  { name: '周一', date: '1/15', isToday: false, tasks: [{ id: 1, name: '高数复习', completed: true, color: 'bg-primary' }] },
  { name: '周二', date: '1/16', isToday: false, tasks: [{ id: 2, name: '英语阅读', completed: true, color: 'bg-blue-500' }] },
  { name: '周三', date: '1/17', isToday: false, tasks: [{ id: 3, name: '线代练习', completed: false, color: 'bg-accent-purple' }] },
  { name: '周四', date: '1/18', isToday: false, tasks: [] },
  { name: '周五', date: '1/19', isToday: false, tasks: [{ id: 4, name: '专业课', completed: false, color: 'bg-emerald-500' }] },
  { name: '周六', date: '1/20', isToday: false, tasks: [{ id: 5, name: '错题复习', completed: false, color: 'bg-amber-500' }] },
  { name: '周日', date: '1/21', isToday: true, tasks: [{ id: 6, name: '高数练习', completed: true, color: 'bg-primary' }, { id: 7, name: '英语训练', completed: true, color: 'bg-blue-500' }, { id: 8, name: '线代专项', completed: false, color: 'bg-accent-purple' }] },
])

const completionTrendOption = {
  grid: { top: 20, right: 20, bottom: 40, left: 50 },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(17, 24, 39, 0.9)',
    borderColor: '#374151',
    textStyle: { color: '#f9fafb' },
  },
  legend: {
    data: ['计划完成率', '学习效率'],
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
    axisLabel: { color: '#6b7280', formatter: '{value}%' },
  },
  series: [
    {
      name: '计划完成率',
      type: 'line',
      smooth: true,
      data: [85, 90, 75, 80, 95, 70, 60],
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
      name: '学习效率',
      type: 'line',
      smooth: true,
      data: [80, 85, 70, 75, 90, 65, 55],
      lineStyle: { color: '#10b981', width: 3 },
      itemStyle: { color: '#10b981' },
    },
  ],
}

const subjectTimeOption = {
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
      { value: 35, name: '高等数学', itemStyle: { color: '#6366f1' } },
      { value: 25, name: '专业课', itemStyle: { color: '#10b981' } },
      { value: 20, name: '英语', itemStyle: { color: '#3b82f6' } },
      { value: 15, name: '线性代数', itemStyle: { color: '#a855f7' } },
      { value: 5, name: '其他', itemStyle: { color: '#6b7280' } },
    ],
  }],
}

const planTemplates = ref([
  {
    id: 1,
    name: '理解驱动型专属计划',
    description: '针对理解驱动型学习者设计，强调概念理解后再练习，适合深度学习',
    icon: 'Reading',
    iconBg: 'bg-primary/20',
    iconColor: 'text-primary',
    forDNA: '理解驱动型',
    duration: '每日4小时',
    taskCount: 6,
  },
  {
    id: 2,
    name: '考前冲刺计划',
    description: '针对考试周的密集复习计划，重点突破薄弱环节',
    icon: 'Calendar',
    iconBg: 'bg-rose-500/20',
    iconColor: 'text-rose-400',
    forDNA: '',
    duration: '每日6小时',
    taskCount: 8,
  },
  {
    id: 3,
    name: '错题攻克计划',
    description: '基于你的错题本智能生成，针对性强化薄弱知识点',
    icon: 'Document',
    iconBg: 'bg-accent-purple/20',
    iconColor: 'text-accent-purple',
    forDNA: '',
    duration: '每日3小时',
    taskCount: 5,
  },
])

const newPlan = ref({
  name: '',
  subject: '',
  date: '',
  startTime: '',
  duration: 45,
  goal: '',
})

const generateParams = ref({
  period: 'today',
  focus: 'weak',
  dailyHours: 4,
  notes: '',
})

const updateProgress = () => {}

const startTask = (task: any) => {
  console.log('开始任务:', task)
}

const editTask = (task: any) => {
  console.log('编辑任务:', task)
}

const prevWeek = () => {}
const nextWeek = () => {}

const createPlan = () => {
  showCreateDialog.value = false
}

const generatePlan = () => {
  isGenerating.value = true
  setTimeout(() => {
    isGenerating.value = false
    showGenerateDialog.value = false
  }, 2000)
}
</script>
