import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '@/api'
import type { User } from '@/types'

export const useUserStore = defineStore('user', () => {
  const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

  const avatarUrl = ref(localStorage.getItem('cognia-avatar') || DEFAULT_AVATAR)
  const loading = ref(false)

  const setAvatar = (url: string) => {
    avatarUrl.value = url
    localStorage.setItem('cognia-avatar', url)
  }

  const userInfo = ref<User>({
    id: 0,
    username: '加载中...',
    avatar: '',
    learningType: '',
    emotionState: '',
    studyLevel: 0,
    continuousDays: 0,
    todayFocusTime: 0,
  })

  const learningDNA = ref({
    type: '',
    subType: '',
    tags: [] as string[],
    strengths: [] as { title: string; desc: string }[],
    weaknesses: [] as { title: string; desc: string }[],
    suggestions: [] as string[],
    radarData: [
      { name: '理解能力', value: 0 },
      { name: '记忆能力', value: 0 },
      { name: '专注持续', value: 0 },
      { name: '执行能力', value: 0 },
      { name: '情绪稳定', value: 0 },
      { name: '逻辑思维', value: 0 },
    ],
    description: '',
    strengthScore: 0,
    learningEfficiency: 0,
    strategies: [] as { title: string; desc: string; icon: string; bgClass: string; iconClass: string }[],
  })

  const todayStats = ref({
    studyTime: 0,
    studyTimeChange: 0,
    focusScore: 0,
    focusScoreChange: 0,
    completedTasks: 0,
    totalTasks: 0,
    tasksChange: 0,
    accuracy: 0,
    accuracyChange: 0,
  })

  const emotionOptions = [
    { label: '超棒', value: 'great', icon: '😄', color: '#10b981' },
    { label: '还不错', value: 'good', icon: '🙂', color: '#22c55e' },
    { label: '一般', value: 'normal', icon: '😐', color: '#f59e0b' },
    { label: '有点累', value: 'tired', icon: '😮‍💨', color: '#f97316' },
    { label: '有点烦', value: 'frustrated', icon: '😣', color: '#ef4444' },
  ]

  const getUserId = (): number => {
    const stored = localStorage.getItem('cognia-user')
    if (stored) {
      try {
        const user = JSON.parse(stored)
        return user.id || 1
      } catch {
        return 1
      }
    }
    return 1
  }

  const loadUser = async () => {
    loading.value = true
    try {
      const user = await userApi.getUserInfo(getUserId())
      if (user) {
        userInfo.value = {
          id: user.id || getUserId(),
          username: user.username || '同学',
          avatar: user.avatar || '',
          learningType: user.learningType || '',
          emotionState: user.emotionState || '',
          studyLevel: user.studyLevel || 0,
          continuousDays: user.continuousDays || 0,
          todayFocusTime: user.todayFocusTime || 0,
        }
      }
    } catch {
      console.warn('用户信息加载失败')
    } finally {
      loading.value = false
    }
  }

  const loadDNA = async () => {
    try {
      const dna = await userApi.getLearningDNA(getUserId())
      if (dna) {
        learningDNA.value = {
          type: dna.dnaType || '',
          subType: dna.subType || '',
          tags: dna.tags ? dna.tags.split(',').map(item => item.trim()).filter(Boolean) : [],
          strengths: dna.strengths
            ? dna.strengths.split(',').map((item, index) => ({
                title: item.trim(),
                desc: index === 0 ? '这是你最稳定的优势之一' : index === 1 ? '继续保持会让效率更高' : '适合在计划中继续放大',
              }))
            : [],
          weaknesses: dna.weaknesses
            ? dna.weaknesses.split(',').map((item, index) => ({
                title: item.trim(),
                desc: index === 0 ? '这是当前最值得优先修补的短板' : index === 1 ? '通过复盘和节奏控制可以改善' : '需要持续关注学习习惯',
              }))
            : [],
          suggestions: dna.suggestions ? dna.suggestions.split(',').map(item => item.trim()).filter(Boolean) : [],
          radarData: [
            { name: '理解能力', value: dna.understanding || 0 },
            { name: '记忆能力', value: dna.memory || 0 },
            { name: '专注持续', value: dna.focus || 0 },
            { name: '执行能力', value: dna.execution || 0 },
            { name: '情绪稳定', value: dna.emotion || 0 },
            { name: '逻辑思维', value: dna.logic || 0 },
          ],
          description: buildDnaDescription(dna.dnaType, dna.subType),
          strengthScore: Math.round(
            (dna.understanding + dna.memory + dna.focus + dna.execution + dna.emotion + dna.logic) / 6
          ) || 0,
          learningEfficiency: dna.focus || 0,
          strategies: [],
        }
      }
    } catch {
      console.warn('学习DNA加载失败')
    }
  }

  const buildDnaDescription = (dnaType?: string, subType?: string) => {
    if (!dnaType) {
      return ''
    }

    const fragments = [`你当前的学习风格偏向「${dnaType}」`]
    if (subType) {
      fragments.push(`，子类型更接近「${subType}」`)
    }
    fragments.push('。建议保持“先理解，再练习，再复盘”的节奏，把优势真正转成稳定输出。')
    return fragments.join('')
  }

  return {
    avatarUrl,
    setAvatar,
    loading,
    loadUser,
    loadDNA,
    userInfo,
    learningDNA,
    todayStats,
    emotionOptions,
  }
})
