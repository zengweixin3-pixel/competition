<template>
  <div class="space-y-6" v-loading="loading">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-text-primary">学习人格分析</h2>
        <p class="text-text-muted mt-1">深入了解你的学习DNA，发现最适合你的学习方式</p>
      </div>
      <el-button type="primary" size="large" class="gradient-primary border-0" @click="startAssessment">
        <el-icon class="mr-2"><Refresh /></el-icon>{{ showQuestionnaire ? '测评中...' : '重新测评' }}
      </el-button>
    </div>

    <!-- 测评问卷 -->
    <div v-if="showQuestionnaire" class="card-gradient rounded-2xl p-6">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h3 class="font-bold text-text-primary text-lg">学习人格测评问卷</h3>
          <p class="text-sm text-text-muted">完成 10 道题目，AI 将分析你的学习人格</p>
        </div>
        <div class="flex items-center gap-4">
          <span class="text-sm text-text-muted">{{ questionnaireProgress }}% 完成</span>
          <el-progress :percentage="questionnaireProgress" :stroke-width="6" :color="['#6366f1', '#a855f7']" class="!w-32" :show-text="false" />
        </div>
      </div>

      <div class="mb-6">
        <div class="flex gap-1 mb-4">
          <div
            v-for="(_, idx) in questions"
            :key="idx"
            class="h-1.5 flex-1 rounded-full cursor-pointer transition-all"
            :class="idx === currentQuestion ? 'bg-primary' : answers[idx] > 0 ? 'bg-emerald-500/60' : 'bg-dark-border'"
            @click="currentQuestion = idx"
          ></div>
        </div>

        <div class="bg-dark-bg/50 rounded-xl p-6">
          <div class="flex items-center gap-3 mb-4">
            <span class="w-8 h-8 rounded-lg bg-primary/20 flex items-center justify-center text-sm font-bold text-primary">{{ currentQuestion + 1 }}</span>
            <span class="text-text-secondary text-sm">第 {{ currentQuestion + 1 }} / {{ questions.length }} 题</span>
          </div>
          <h4 class="text-lg font-bold text-text-primary mb-6">{{ questions[currentQuestion].q }}</h4>
          <div class="space-y-3">
            <div
              v-for="(opt, oi) in questions[currentQuestion].options"
              :key="oi"
              class="p-4 rounded-xl cursor-pointer border-2 transition-all"
              :class="answers[currentQuestion] === oi + 1 ? 'bg-primary/20 border-primary' : 'bg-dark-bg/30 border-transparent hover:bg-dark-border/50 hover:border-dark-border'"
              @click="selectAnswer(currentQuestion, oi)"
            >
              <div class="flex items-center gap-3">
                <span class="w-6 h-6 rounded-full border-2 flex items-center justify-center text-xs" :class="answers[currentQuestion] === oi + 1 ? 'border-primary bg-primary text-white' : 'border-text-muted text-text-muted'">{{ ['A','B','C','D','E'][oi] }}</span>
                <span class="text-text-primary">{{ opt }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="flex items-center justify-between">
        <el-button :disabled="currentQuestion === 0" @click="prevQuestion">
          <el-icon class="mr-1"><ArrowLeft /></el-icon>上一题
        </el-button>
        <div class="flex gap-3">
          <el-button v-if="isAllAnswered" type="success" @click="applyAssessment">
            <el-icon class="mr-1"><CircleCheck /></el-icon>查看测评结果
          </el-button>
          <span v-if="!isAllAnswered" class="text-sm text-text-muted self-center">还有 {{ questions.length - answers.filter(a => a > 0).length }} 题未答</span>
        </div>
      </div>
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
          <div class="mt-4 flex items-center gap-2 text-primary text-sm cursor-pointer" @click.stop="ElMessage.info(`${strategy.title}的详细说明将在后续版本中提供`)">
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
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
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
  ArrowLeft,
  Clock,
  Reading,
  Aim,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

use([CanvasRenderer, RadarChart, LineChart, GridComponent, TooltipComponent, LegendComponent, RadarComponent])

const loading = ref(true)
const store = useUserStore()
const radarTimeRange = ref('本周')
const trendTimeRange = ref('近7天')
const compareSubject = ref('all')
const showQuestionnaire = ref(false)
const currentQuestion = ref(0)
const answers = ref<number[]>(new Array(10).fill(0))

const questions = [
  {
    q: '学习新知识时，你更喜欢哪种方式？',
    options: ['看视频讲解，直观易懂', '读教材文本，系统学习', '直接做练习题，在实践中掌握', '画思维导图，梳理知识结构', '动手实验或做项目'],
    dims: ['理解能力', '逻辑思维'],
  },
  {
    q: '遇到难题卡住时，你的第一反应是？',
    options: ['深入钻研，不弄明白不罢休', '请教老师或同学', '先标记跳过，回头再看', '上网查资料或看视频', '感到焦虑，担心学不会'],
    dims: ['情绪稳定度', '理解能力'],
  },
  {
    q: '你感觉学习效率最高的时段是？',
    options: ['清晨(6-9点)，头脑清醒', '上午(9-12点)，精力充沛', '下午(14-18点)，状态回升', '晚上(19-22点)，安静专注', '深夜(22点后)，无人打扰'],
    dims: ['专注持久度'],
  },
  {
    q: '连续学习45分钟后，你通常的状态是？',
    options: ['精神饱满，还能继续学很久', '稍有疲惫但还能坚持', '注意力明显下降', '需要起身活动和休息', '完全无法集中注意力'],
    dims: ['专注持久度', '情绪稳定度'],
  },
  {
    q: '做学习计划时，你的执行情况是？',
    options: ['严格执行计划，按部就班完成', '基本能完成，偶尔调整', '经常根据实际情况修改计划', '很少做详细计划，随性学习', '做了计划但很难坚持完成'],
    dims: ['计划执行力'],
  },
  {
    q: '临近考试时，你通常的状态是？',
    options: ['胸有成竹，按计划复习', '有点紧张，但能正常备考', '突击复习，临时抱佛脚', '非常焦虑，影响复习效率', '随缘，不太在意考试'],
    dims: ['情绪稳定度', '计划执行力'],
  },
  {
    q: '学习环境的偏好是？',
    options: ['需要绝对安静，一点声音都会分心', '有点背景白噪音更好', '喜欢图书馆/自习室的氛围', '对环境要求不高，哪都能学', '喜欢咖啡厅等轻松的环境'],
    dims: ['专注持久度'],
  },
  {
    q: '做笔记的习惯是怎样的？',
    options: ['条理清晰，分类整理得很系统', '简明扼要，只记关键要点', '侧重于记重点公式和结论', '喜欢画图、做思维导图', '很少记笔记，主要靠理解记忆'],
    dims: ['理解能力', '记忆能力'],
  },
  {
    q: '学习一个新概念后，你能做到？',
    options: ['能用自己的话清晰地向别人解释', '大致理解，能回答相关问题', '需要再看一遍才能完全理解', '要通过做题才能验证是否掌握', '概念比较抽象，过几天就忘了'],
    dims: ['理解能力', '逻辑思维'],
  },
  {
    q: '面对大量学习任务时，你的做法是？',
    options: ['按优先级排序，逐一攻克', '先做简单的建立信心', '从最难的开始，先啃硬骨头', '随机开始，做到哪算哪', '感到压力大，容易拖延'],
    dims: ['计划执行力', '情绪稳定度', '逻辑思维'],
  },
]

const dimensionScores = computed(() => {
  if (answers.value.every(a => a === 0)) {
    return {
      理解能力: dna.value.radarData[0].value,
      记忆能力: dna.value.radarData[1].value,
      专注持久度: dna.value.radarData[2].value,
      计划执行力: dna.value.radarData[3].value,
      情绪稳定度: dna.value.radarData[4].value,
      逻辑思维: dna.value.radarData[5].value,
    }
  }
  const dimTotals: Record<string, number> = {}
  const dimCounts: Record<string, number> = {}
  questions.forEach((q, qi) => {
    const score = answers.value[qi]
    if (score === 0) return
    q.dims.forEach(dim => {
      dimTotals[dim] = (dimTotals[dim] || 0) + score
      dimCounts[dim] = (dimCounts[dim] || 0) + 1
    })
  })
  const result: Record<string, number> = {}
  for (const dim of Object.keys(dimTotals)) {
    result[dim] = Math.round((dimTotals[dim] / (dimCounts[dim] * 5)) * 100)
  }
  return result
})

const questionnaireProgress = computed(() => {
  const answered = answers.value.filter(a => a > 0).length
  return Math.round((answered / questions.length) * 100)
})

const startAssessment = () => {
  showQuestionnaire.value = true
  currentQuestion.value = 0
  answers.value = new Array(10).fill(0)
}

const selectAnswer = (qIndex: number, optionIndex: number) => {
  answers.value[qIndex] = optionIndex + 1
  if (qIndex < questions.length - 1) {
    currentQuestion.value = qIndex + 1
  }
}

const prevQuestion = () => {
  if (currentQuestion.value > 0) {
    currentQuestion.value = currentQuestion.value - 1
  }
}

const isAllAnswered = computed(() => answers.value.every(a => a > 0))

const applyAssessment = () => {
  const scores = dimensionScores.value
  dna.value.radarData = [
    { name: '理解能力', value: scores['理解能力'] },
    { name: '记忆能力', value: scores['记忆能力'] },
    { name: '专注持久度', value: scores['专注持久度'] },
    { name: '计划执行力', value: scores['计划执行力'] },
    { name: '情绪稳定度', value: scores['情绪稳定度'] },
    { name: '逻辑思维', value: scores['逻辑思维'] },
  ]
  const maxDim = Object.entries(scores).sort((a, b) => b[1] - a[1])[0]
  const typeMap: Record<string, { type: string; desc: string; strengths: string; weaknesses: string; suggestions: string }> = {
    '理解能力': { type: '理解驱动型', desc: '你擅长深入理解知识，喜欢探索原理，在安静的环境中效率更高，适合深度学习。你对抽象概念的理解能力很强，但有时候会因为追求完美而拖延。', strengths: '理解速度快,图像记忆强,深度思考能力', weaknesses: '容易拖延,长时间学习效率下降,完美主义倾向', suggestions: '25分钟番茄钟,晚间学习最佳,先理解后刷题' },
    '记忆能力': { type: '记忆强化型', desc: '你拥有出色的记忆力，能够快速记住大量信息。适合通过反复记忆和归纳总结来巩固知识。', strengths: '记忆速度快,归纳总结能力强,知识网络构建好', weaknesses: '理解深度需加强,应用能力有待提升', suggestions: '加强概念理解,多做应用练习,定期复习巩固' },
    '专注持久度': { type: '专注深潜型', desc: '你能长时间保持高度专注，适合进行深度学习和大块时间的知识攻坚。', strengths: '专注力强,深度学习,效率高', weaknesses: '灵活性不足,容易忽略时间,社交学习少', suggestions: '设定时间提醒,穿插不同学科,适当社交学习' },
    '计划执行力': { type: '执行先锋型', desc: '你善于制定计划并严格执行，学习有条不紊，进度把控能力强。', strengths: '自律性强,计划周密,执行力好', weaknesses: '过度计划可能忽略灵活调整,压力管理需注意', suggestions: '保持灵活性,定期回顾调整,注意劳逸结合' },
    '情绪稳定度': { type: '从容稳进型', desc: '你的情绪管理能力出色，面对学习压力和挑战时能保持冷静，稳步前进。', strengths: '情绪稳定,抗压能力强,心态好', weaknesses: '有时过于佛系,需要增强紧迫感', suggestions: '设定有挑战性的目标,适当给自己一些压力,保持动力' },
    '逻辑思维': { type: '逻辑推理型', desc: '你的逻辑思维能力突出，善于分析和推理，适合理科和需要严密推理的学科。', strengths: '逻辑严密,分析力强,推理能力好', weaknesses: '直觉判断需加强,创新思维有待提升', suggestions: '多接触跨学科知识,锻炼发散思维,参加辩论或头脑风暴' },
  }
  const personality = typeMap[maxDim[0]] || typeMap['理解能力']
  dna.value.type = personality.type
  dna.value.tags = [personality.type, 'AI测评生成']
  dna.value.description = personality.desc
  dna.value.strengths = [
    { title: personality.strengths.split(',')[0], desc: `这是你的核心优势之一` },
    { title: personality.strengths.split(',')[1] || '学习能力强', desc: `这是你的重要优势` },
    { title: personality.strengths.split(',')[2] || '潜力巨大', desc: `继续保持和发挥` },
  ]
  dna.value.weaknesses = [
    { title: personality.weaknesses.split(',')[0], desc: `需要关注和改进的方面` },
    { title: personality.weaknesses.split(',')[1] || '部分能力需加强', desc: `有提升空间` },
    { title: personality.weaknesses.split(',')[2] || '注意学习习惯', desc: `可以通过练习改善` },
  ]
  dna.value.strategies = [
    { title: personality.suggestions.split(',')[0] || '番茄工作法', desc: '采用25分钟专注+5分钟休息的循环，保持高效学习状态', icon: 'Clock', bgClass: 'bg-primary/20', iconClass: 'text-primary' },
    { title: personality.suggestions.split(',')[1] || '晚间深度学习', desc: '利用高效时段进行需要深度思考的学习内容', icon: 'Reading', bgClass: 'bg-accent-purple/20', iconClass: 'text-accent-purple' },
    { title: personality.suggestions.split(',')[2] || '先理解后刷题', desc: '先通过视频/讲解理解概念，再进行练习巩固', icon: 'Aim', bgClass: 'bg-accent-cyan/20', iconClass: 'text-accent-cyan' },
  ]
  showQuestionnaire.value = false
  ElMessage.success('测评完成！你的学习人格已更新')
}

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

onMounted(async () => {
  try {
    await Promise.all([store.loadUser(), store.loadDNA()])
  } catch { /* ignore */ }
  finally { loading.value = false }
})
</script>
