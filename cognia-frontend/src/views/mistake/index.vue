<template>
  <div class="space-y-6" v-loading="loading">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-text-primary">错题分析中心</h2>
        <p class="text-text-muted mt-1">AI智能分析错因，精准提升薄弱环节</p>
      </div>
      <div class="flex gap-3">
        <el-button type="primary" size="large" @click="showUploadDialog = true">
          <el-icon class="mr-2"><Upload /></el-icon>上传错题
        </el-button>
        <el-button size="large" @click="startReview">
          <el-icon class="mr-2"><VideoPlay /></el-icon>开始复习
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-6">
      <div class="card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-3xl font-bold text-text-primary">{{ stats.total }}</div>
            <div class="text-sm text-text-muted mt-1">总错题数</div>
          </div>
          <div class="w-14 h-14 rounded-xl bg-primary/20 flex items-center justify-center">
            <el-icon class="text-primary text-2xl"><Document /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2 text-sm">
          <span class="text-emerald-400">↓ 12%</span>
          <span class="text-text-muted">较上月</span>
        </div>
      </div>
      <div class="card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-3xl font-bold text-text-primary">{{ stats.pending }}</div>
            <div class="text-sm text-text-muted mt-1">待复习</div>
          </div>
          <div class="w-14 h-14 rounded-xl bg-amber-500/20 flex items-center justify-center">
            <el-icon class="text-amber-400 text-2xl"><Timer /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2 text-sm">
          <span class="text-rose-400">↑ 5%</span>
          <span class="text-text-muted">较上周</span>
        </div>
      </div>
      <div class="card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-3xl font-bold text-text-primary">{{ stats.mastered }}</div>
            <div class="text-sm text-text-muted mt-1">已掌握</div>
          </div>
          <div class="w-14 h-14 rounded-xl bg-emerald-500/20 flex items-center justify-center">
            <el-icon class="text-emerald-400 text-2xl"><CircleCheck /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2 text-sm">
          <span class="text-emerald-400">↑ 23%</span>
          <span class="text-text-muted">较上月</span>
        </div>
      </div>
      <div class="card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-3xl font-bold text-text-primary">{{ stats.accuracy }}%</div>
            <div class="text-sm text-text-muted mt-1">复习正确率</div>
          </div>
          <div class="w-14 h-14 rounded-xl bg-accent-purple/20 flex items-center justify-center">
            <el-icon class="text-accent-purple text-2xl"><TrendCharts /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2 text-sm">
          <span class="text-emerald-400">↑ 8%</span>
          <span class="text-text-muted">较上月</span>
        </div>
      </div>
    </div>

    <!-- 错因分析图表 -->
    <div class="grid grid-cols-12 gap-6">
      <div class="col-span-7 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">错因分布分析</h3>
          <el-radio-group v-model="analysisTimeRange" size="small">
            <el-radio-button label="本周">本周</el-radio-button>
            <el-radio-button label="本月">本月</el-radio-button>
            <el-radio-button label="全部">全部</el-radio-button>
          </el-radio-group>
        </div>
        <div class="h-64">
          <v-chart class="w-full h-full" :option="mistakeTypeOption" autoresize />
        </div>
      </div>
      <div class="col-span-5 card-gradient rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-text-primary">学科错题分布</h3>
        </div>
        <div class="h-64">
          <v-chart class="w-full h-full" :option="subjectOption" autoresize />
        </div>
      </div>
    </div>

    <!-- 错题列表 -->
    <div class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-4">
          <h3 class="font-bold text-text-primary">错题列表</h3>
          <el-input v-model="searchQuery" placeholder="搜索错题..." style="width: 240px" :prefix-icon="Search" />
        </div>
        <div class="flex items-center gap-3">
          <el-select v-model="filterSubject" placeholder="学科筛选" style="width: 120px" clearable>
            <el-option label="全部学科" value="" />
            <el-option label="高等数学" value="math" />
            <el-option label="线性代数" value="linear" />
            <el-option label="英语" value="english" />
            <el-option label="专业课" value="major" />
          </el-select>
          <el-select v-model="filterType" placeholder="错因筛选" style="width: 120px" clearable>
            <el-option label="全部错因" value="" />
            <el-option label="概念混淆" value="concept" />
            <el-option label="计算错误" value="calculation" />
            <el-option label="粗心大意" value="careless" />
            <el-option label="公式遗忘" value="formula" />
          </el-select>
          <el-select v-model="sortBy" placeholder="排序方式" style="width: 120px">
            <el-option label="最近添加" value="recent" />
            <el-option label="错误次数" value="count" />
            <el-option label="难度等级" value="difficulty" />
          </el-select>
        </div>
      </div>

      <div class="space-y-4">
        <div
          v-for="mistake in filteredMistakes"
          :key="mistake.id"
          class="bg-dark-bg/50 rounded-xl p-6 border-l-4 cursor-pointer transition-all hover:bg-dark-border/50"
          :class="mistake.borderColor"
          @click="viewMistakeDetail(mistake)"
        >
          <div class="flex items-start gap-4">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0" :class="mistake.iconBg">
              <el-icon :class="mistake.iconColor" class="text-xl"><component :is="mistake.icon" /></el-icon>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-3 mb-2">
                <h4 class="font-bold text-text-primary">{{ mistake.title }}</h4>
                <el-tag size="small" :type="mistake.tagType">{{ mistake.subject }}</el-tag>
                <el-tag size="small" effect="dark" :class="mistake.typeTagClass">{{ mistake.mistakeType }}</el-tag>
              </div>
              <p class="text-sm text-text-secondary mb-3 line-clamp-2">{{ mistake.content }}</p>
              <div class="flex items-center gap-6 text-sm">
                <span class="text-text-muted">错误 {{ mistake.errorCount }} 次</span>
                <span class="text-text-muted">难度：{{ mistake.difficulty }}</span>
                <span class="text-text-muted">添加于 {{ mistake.addTime }}</span>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <el-button type="primary" size="small" plain @click.stop="reviewMistake(mistake)">复习</el-button>
              <el-button size="small" @click.stop="viewAnalysis(mistake)">AI分析</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-6 flex justify-center">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalMistakes"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </div>
  </div>

  <!-- 上传对话框 -->
  <el-dialog v-model="showUploadDialog" title="上传错题" width="600px" destroy-on-close>
    <div class="space-y-6">
      <el-upload
        drag
        action="#"
        :auto-upload="false"
        class="w-full"
      >
        <el-icon class="text-4xl text-primary mb-4"><Upload /></el-icon>
        <div class="text-text-primary mb-2">拖拽文件到此处，或<em class="text-primary">点击上传</em></div>
        <div class="text-xs text-text-muted">支持图片、PDF、Word格式，单个文件不超过10MB</div>
      </el-upload>
      <div>
        <div class="text-sm text-text-primary mb-2">或手动输入</div>
        <el-input v-model="uploadTitle" placeholder="错题标题（可选）" class="mb-3" />
        <el-input v-model="uploadContent" type="textarea" :rows="4" placeholder="粘贴题目内容..." />
      </div>
      <div class="flex gap-4">
        <el-select v-model="uploadSubject" placeholder="选择学科" class="flex-1">
          <el-option label="高等数学" value="math" />
          <el-option label="线性代数" value="linear" />
          <el-option label="英语" value="english" />
          <el-option label="专业课" value="major" />
        </el-select>
        <el-select v-model="uploadDifficulty" placeholder="难度等级" class="flex-1">
          <el-option label="简单" value="easy" />
          <el-option label="中等" value="medium" />
          <el-option label="困难" value="hard" />
        </el-select>
      </div>
    </div>
    <template #footer>
      <el-button @click="showUploadDialog = false">取消</el-button>
      <el-button type="primary" @click="uploadMistake">上传并分析</el-button>
    </template>
  </el-dialog>

  <!-- AI分析详情对话框 -->
  <el-dialog v-model="showAnalysisDialog" title="AI错因分析" width="700px" destroy-on-close>
    <div v-if="selectedMistake" class="space-y-6">
      <div class="bg-dark-bg/50 rounded-xl p-4">
        <div class="text-sm text-gray-600 mb-2">题目</div>
        <div class="text-gray-900 font-medium">{{ selectedMistake.title }}</div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div class="bg-rose-500/10 rounded-xl p-4 border border-rose-500/30">
          <div class="flex items-center gap-2 mb-2">
            <el-icon class="text-rose-400"><CircleClose /></el-icon>
            <span class="font-medium text-rose-400">错误原因</span>
          </div>
          <div class="text-gray-800">{{ selectedMistake.analysis?.reason }}</div>
        </div>
        <div class="bg-amber-500/10 rounded-xl p-4 border border-amber-500/30">
          <div class="flex items-center gap-2 mb-2">
            <el-icon class="text-amber-400"><Warning /></el-icon>
            <span class="font-medium text-amber-400">根本问题</span>
          </div>
          <div class="text-gray-800">{{ selectedMistake.analysis?.rootCause }}</div>
        </div>
      </div>

      <div class="bg-emerald-500/10 rounded-xl p-4 border border-emerald-500/30">
        <div class="flex items-center gap-2 mb-3">
          <el-icon class="text-emerald-400"><CircleCheck /></el-icon>
          <span class="font-medium text-emerald-400">AI建议</span>
        </div>
        <ul class="space-y-2">
          <li v-for="(suggestion, idx) in selectedMistake.analysis?.suggestions" :key="idx" class="flex items-start gap-2 text-gray-800">
            <span class="text-emerald-400 mt-1">•</span>
            <span>{{ suggestion }}</span>
          </li>
        </ul>
      </div>

      <div class="bg-primary/10 rounded-xl p-4 border border-primary/30">
        <div class="flex items-center gap-2 mb-3">
          <el-icon class="text-primary"><Document /></el-icon>
          <span class="font-medium text-primary">推荐学习资源</span>
        </div>
        <div class="space-y-2">
          <div v-for="(resource, idx) in selectedMistake.analysis?.resources" :key="idx" class="flex items-center justify-between p-3 bg-dark-bg/50 rounded-lg">
            <div class="flex items-center gap-3">
              <el-icon class="text-primary"><VideoPlay /></el-icon>
              <span class="text-text-primary">{{ resource.title }}</span>
            </div>
            <el-button type="primary" size="small" plain>查看</el-button>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { mistakeApi } from '@/api'
import {
  Upload,
  VideoPlay,
  Document,
  Timer,
  CircleCheck,
  TrendCharts,
  Search,
  CircleClose,
  Warning,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

use([CanvasRenderer, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(true)

const stats = ref({
  total: 128,
  pending: 12,
  mastered: 89,
  accuracy: 76,
})

const analysisTimeRange = ref('本月')
const searchQuery = ref('')
const filterSubject = ref('')
const filterType = ref('')
const sortBy = ref('recent')
const currentPage = ref(1)
const pageSize = ref(10)
const totalMistakes = ref(128)

const showUploadDialog = ref(false)
const showAnalysisDialog = ref(false)
const selectedMistake = ref<any>(null)
const uploadSubject = ref('')
const uploadDifficulty = ref('')
const uploadContent = ref('')
const uploadTitle = ref('')

const mistakeTypeOption = {
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
    emphasis: {
      label: {
        show: true,
        fontSize: 14,
        fontWeight: 'bold',
        color: '#f9fafb',
      },
    },
    data: [
      { value: 35, name: '概念混淆', itemStyle: { color: '#6366f1' } },
      { value: 28, name: '计算错误', itemStyle: { color: '#a855f7' } },
      { value: 22, name: '粗心大意', itemStyle: { color: '#f59e0b' } },
      { value: 18, name: '公式遗忘', itemStyle: { color: '#10b981' } },
      { value: 15, name: '理解偏差', itemStyle: { color: '#06b6d4' } },
      { value: 10, name: '其他', itemStyle: { color: '#6b7280' } },
    ],
  }],
}

const subjectOption = {
  grid: { top: 20, right: 20, bottom: 30, left: 80 },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(17, 24, 39, 0.9)',
    borderColor: '#374151',
    textStyle: { color: '#f9fafb' },
  },
  xAxis: {
    type: 'value',
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#1f2937' } },
    axisLabel: { color: '#6b7280' },
  },
  yAxis: {
    type: 'category',
    data: ['专业课', '英语', '线性代数', '高等数学'],
    axisLine: { lineStyle: { color: '#374151' } },
    axisLabel: { color: '#9ca3af' },
  },
  series: [{
    type: 'bar',
    data: [
      { value: 25, itemStyle: { color: '#06b6d4', borderRadius: [0, 4, 4, 0] } },
      { value: 18, itemStyle: { color: '#f59e0b', borderRadius: [0, 4, 4, 0] } },
      { value: 32, itemStyle: { color: '#a855f7', borderRadius: [0, 4, 4, 0] } },
      { value: 53, itemStyle: { color: '#6366f1', borderRadius: [0, 4, 4, 0] } },
    ],
    barWidth: '60%',
  }],
}

const mistakes = ref([
  {
    id: 1,
    title: '定积分求面积问题：计算曲线y=x²与y=x围成的区域面积',
    content: '在计算定积分时，没有正确找出两个函数的交点，导致积分上下限设置错误...',
    subject: '高等数学',
    mistakeType: '概念混淆',
    errorCount: 2,
    difficulty: '中等',
    addTime: '2024-01-15',
    icon: 'Document',
    iconBg: 'bg-primary/20',
    iconColor: 'text-primary',
    borderColor: 'border-primary',
    tagType: 'primary',
    typeTagClass: 'bg-primary/30 border-primary/50',
    analysis: {
      reason: '没有理解定积分的几何意义，不清楚如何确定积分上下限',
      rootCause: '对函数图像的交点求解不熟练，缺乏数形结合的思维',
      suggestions: [
        '重新学习定积分的几何意义，理解"面积=积分"的本质',
        '练习画函数图像，培养数形结合能力',
        '多做求交点的专项练习',
        '使用AI学习助手进行针对性训练',
      ],
      resources: [
        { title: '定积分几何意义详解视频', type: 'video' },
        { title: '函数图像与交点求解专题', type: 'doc' },
        { title: '定积分求面积练习题集', type: 'exercise' },
      ],
    },
  },
  {
    id: 2,
    title: '矩阵乘法计算：求AB的乘积，其中A为3×2矩阵，B为2×4矩阵',
    content: '在进行矩阵乘法时，行与列的对应元素相乘后相加的计算过程中出现错误...',
    subject: '线性代数',
    mistakeType: '计算错误',
    errorCount: 3,
    difficulty: '简单',
    addTime: '2024-01-14',
    icon: 'Grid',
    iconBg: 'bg-accent-purple/20',
    iconColor: 'text-accent-purple',
    borderColor: 'border-accent-purple',
    tagType: 'success',
    typeTagClass: 'bg-accent-purple/30 border-accent-purple/50',
    analysis: {
      reason: '矩阵乘法计算过程中粗心，没有仔细核对每个元素的计算',
      rootCause: '计算习惯不好，缺乏检查步骤',
      suggestions: [
        '养成逐步计算并标注的习惯',
        '计算完成后进行验算',
        '多做基础计算练习，提高准确率',
      ],
      resources: [
        { title: '矩阵乘法计算技巧', type: 'video' },
        { title: '线性代数计算题专项训练', type: 'exercise' },
      ],
    },
  },
  {
    id: 3,
    title: '英语阅读理解：关于气候变化的科普文章',
    content: '在阅读理解题中，对文章主旨的把握出现偏差，导致主旨题选择错误...',
    subject: '英语',
    mistakeType: '理解偏差',
    errorCount: 1,
    difficulty: '困难',
    addTime: '2024-01-13',
    icon: 'Reading',
    iconBg: 'bg-amber-500/20',
    iconColor: 'text-amber-400',
    borderColor: 'border-amber-500',
    tagType: 'warning',
    typeTagClass: 'bg-amber-500/30 border-amber-500/50',
    analysis: {
      reason: '过度关注细节，忽略了文章的整体结构和主旨',
      rootCause: '阅读策略不当，没有先把握文章框架再读细节',
      suggestions: [
        '先读首尾段把握主旨，再读细节',
        '练习快速浏览skimming技巧',
        '多做主旨大意题专项训练',
      ],
      resources: [
        { title: '英语阅读技巧：如何快速把握主旨', type: 'video' },
        { title: '阅读理解主旨题专项练习', type: 'exercise' },
      ],
    },
  },
])

const filteredMistakes = computed(() => {
  let result = [...mistakes.value]
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    result = result.filter(m =>
      m.title.toLowerCase().includes(q) ||
      m.content.toLowerCase().includes(q) ||
      m.subject.toLowerCase().includes(q)
    )
  }
  if (filterSubject.value) {
    const subjectMap: Record<string, string> = {
      'math': '高等数学',
      'linear': '线性代数',
      'english': '英语',
      'major': '专业课',
    }
    const subjectName = subjectMap[filterSubject.value] || filterSubject.value
    result = result.filter(m => m.subject === subjectName)
  }
  if (filterType.value) {
    const typeMap: Record<string, string> = {
      'concept': '概念混淆',
      'calculation': '计算错误',
      'careless': '粗心大意',
      'formula': '公式遗忘',
    }
    const typeName = typeMap[filterType.value] || filterType.value
    result = result.filter(m => m.mistakeType === typeName)
  }
  if (sortBy.value === 'count') {
    result.sort((a, b) => b.errorCount - a.errorCount)
  } else if (sortBy.value === 'difficulty') {
    const diffOrder: Record<string, number> = { '困难': 3, '中等': 2, '简单': 1 }
    result.sort((a, b) => (diffOrder[b.difficulty] || 0) - (diffOrder[a.difficulty] || 0))
  }
  return result
})

const viewMistakeDetail = (mistake: any) => {
  selectedMistake.value = mistake
  showAnalysisDialog.value = true
}

const reviewMistake = (mistake: any) => {
  ElMessage.success(`开始复习：${mistake.title}，AI将为你生成针对性练习`)
}

const viewAnalysis = (mistake: any) => {
  selectedMistake.value = mistake
  showAnalysisDialog.value = true
}

const uploadMistake = () => {
  const content = uploadContent.value.trim()
  if (!content) {
    ElMessage.warning('请输入错题内容')
    return
  }
  if (!uploadSubject.value) {
    ElMessage.warning('请选择学科')
    return
  }
  const subjectMap: Record<string, string> = {
    'math': '高等数学', 'linear': '线性代数', 'english': '英语', 'major': '专业课',
  }
  const diffMap: Record<string, string> = {
    'easy': '简单', 'medium': '中等', 'hard': '困难',
  }
  const subjectName = subjectMap[uploadSubject.value] || uploadSubject.value
  const difficultyName = diffMap[uploadDifficulty.value] || '中等'
  const newMistake = {
    id: Date.now(),
    title: uploadTitle.value.trim() || content.slice(0, 30) + (content.length > 30 ? '...' : ''),
    content,
    subject: subjectName,
    mistakeType: '概念混淆',
    errorCount: 1,
    difficulty: difficultyName,
    addTime: new Date().toISOString().slice(0, 10),
    icon: 'Document',
    iconBg: 'bg-primary/20',
    iconColor: 'text-primary',
    borderColor: 'border-primary',
    tagType: 'primary',
    typeTagClass: 'bg-primary/30 border-primary/50',
    analysis: null,
  }
  mistakes.value.unshift(newMistake)
  stats.value.total++
  stats.value.pending++
  showUploadDialog.value = false
  uploadContent.value = ''
  uploadTitle.value = ''
  uploadSubject.value = ''
  uploadDifficulty.value = ''
  ElMessage.success('错题已上传，AI正在分析中...')
}

const startReview = () => {
  ElMessage.info('复习模式已启动，请选择待复习的错题')
}

onMounted(async () => {
  try {
    const data = await mistakeApi.getList(1, { pageNum: 1, pageSize: 50 })
    if (data && data.list) {
      mistakes.value = data.list.map((m: any) => ({
        id: m.id || Date.now(),
        title: m.title || '',
        content: m.content || '',
        subject: m.subject || '',
        mistakeType: m.mistakeType || '概念混淆',
        errorCount: m.errorCount || 1,
        difficulty: m.difficulty || '中等',
        addTime: (m.createTime || '').slice(0, 10),
        icon: 'Document',
        iconBg: 'bg-primary/20',
        iconColor: 'text-primary',
        borderColor: 'border-primary',
        tagType: 'primary',
        typeTagClass: 'bg-primary/30 border-primary/50',
        analysis: null,
      }))
      totalMistakes.value = data.total || mistakes.value.length
    }
    const apiStats = await mistakeApi.getStats()
    if (apiStats) {
      stats.value = {
        total: Number(apiStats.total) || 0,
        pending: Number(apiStats.pending) || 0,
        mastered: Number(apiStats.mastered) || 0,
        accuracy: Number(apiStats.accuracy) || 0,
      }
    }
  } catch {
    // 后端不可用，保持默认数据
  } finally {
    loading.value = false
  }
})
</script>
