import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({
    id: 1,
    username: '小明同学',
    avatar: '',
    learningType: '理解驱动型',
    emotionState: '专注',
    studyLevel: 12,
    continuousDays: 7,
    todayFocusTime: 4.2,
  })

  const learningDNA = ref({
    type: '理解驱动型',
    subType: '夜间高效型',
    tags: ['理解驱动型', '夜间高效型', '焦虑型学习者'],
    strengths: ['理解速度快', '图像记忆强'],
    weaknesses: ['容易拖延', '长时间学习效率下降'],
    suggestions: ['25分钟番茄钟', '晚间学习最佳', '先理解后刷题'],
    radarData: [
      { name: '理解能力', value: 85 },
      { name: '记忆能力', value: 72 },
      { name: '专注持久度', value: 65 },
      { name: '计划执行力', value: 58 },
      { name: '情绪稳定度', value: 70 },
      { name: '逻辑思维', value: 78 },
    ],
  })

  const todayStats = ref({
    studyTime: 4.2,
    studyTimeChange: 12,
    focusScore: 82,
    focusScoreChange: 5,
    completedTasks: 5,
    totalTasks: 8,
    tasksChange: 20,
    accuracy: 76,
    accuracyChange: -3,
  })

  const emotionOptions = [
    { label: '超棒', value: 'great', icon: '😄', color: '#10b981' },
    { label: '还不错', value: 'good', icon: '🙂', color: '#22c55e' },
    { label: '一般', value: 'normal', icon: '😐', color: '#f59e0b' },
    { label: '有点累', value: 'tired', icon: '😔', color: '#f97316' },
    { label: '很烦躁', value: 'frustrated', icon: '😫', color: '#ef4444' },
  ]

  return {
    userInfo,
    learningDNA,
    todayStats,
    emotionOptions,
  }
})
