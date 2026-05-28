import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: '',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue'),
        },
        {
          path: '/ai-chat',
          name: 'AIChat',
          component: () => import('@/views/ai-chat/index.vue'),
        },
        {
          path: '/learning-dna',
          name: 'LearningDNA',
          component: () => import('@/views/learning-dna/index.vue'),
        },
        {
          path: '/mistake',
          name: 'Mistake',
          component: () => import('@/views/mistake/index.vue'),
        },
        {
          path: '/emotion',
          name: 'Emotion',
          component: () => import('@/views/emotion/index.vue'),
        },
        {
          path: '/study-plan',
          name: 'StudyPlan',
          component: () => import('@/views/study-plan/index.vue'),
        },
        {
          path: '/report',
          name: 'Report',
          component: () => import('@/views/report/index.vue'),
        },
      ],
    },
  ],
})

export default router
