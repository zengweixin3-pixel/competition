import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

  const avatarUrl = ref(localStorage.getItem('cognia-avatar') || DEFAULT_AVATAR)
  const loading = ref(false)

  const setAvatar = (url: string) => {
    avatarUrl.value = url
    localStorage.setItem('cognia-avatar', url)
  }

  const userInfo = ref({
    id: 1,
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
    strengths: [] as string[],
    weaknesses: [] as string[],
    suggestions: [] as string[],
    radarData: [
      { name: '理解能力', value: 0 },
      { name: '记忆能力', value: 0 },
      { name: '专注持久度', value: 0 },
      { name: '计划执行力', value: 0 },
      { name: '情绪稳定度', value: 0 },
      { name: '逻辑思维', value: 0 },
    ],
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
    { label: '有点累', value: 'tired', icon: '😔', color: '#f97316' },
    { label: '很烦躁', value: 'frustrated', icon: '😫', color: '#ef4444' },
  ]

  const loadUser = async () => {
    loading.value = true
    try {
      const user = await userApi.getUserInfo()
      userInfo.value = {
        id: user.id || 1,
        username: user.username || '同学',
        avatar: user.avatar || '',
        learningType: user.learningType || '',
        emotionState: user.emotionState || '',
        studyLevel: user.studyLevel || 0,
        continuousDays: user.continuousDays || 0,
        todayFocusTime: user.todayFocusTime || 0,
      }
    } catch {
      console.warn('用户信息加载失败，使用默认值')
    } finally {
      loading.value = false
    }
  }

  const loadDNA = async () => {
    try {
      const dna = await userApi.getLearningDNA()
      learningDNA.value = {
        type: dna.dnaType || '',
        subType: dna.subType || '',
        tags: dna.tags ? dna.tags.split(',') : [],
        strengths: dna.strengths ? dna.strengths.split(',') : [],
        weaknesses: dna.weaknesses ? dna.weaknesses.split(',') : [],
        suggestions: dna.suggestions ? dna.suggestions.split(',') : [],
        radarData: [
          { name: '理解能力', value: dna.understanding || 0 },
          { name: '记忆能力', value: dna.memory || 0 },
          { name: '专注持久度', value: dna.focus || 0 },
          { name: '计划执行力', value: dna.execution || 0 },
          { name: '情绪稳定度', value: dna.emotion || 0 },
          { name: '逻辑思维', value: dna.logic || 0 },
        ],
      }
    } catch {
      console.warn('学习DNA加载失败，使用默认值')
    }
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
