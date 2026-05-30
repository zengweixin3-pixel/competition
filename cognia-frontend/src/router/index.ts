import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { guest: true },
    },
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
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
        {
          path: '/knowledge',
          name: 'Knowledge',
          component: () => import('@/views/knowledge/index.vue'),
        },
        {
          path: '/achievements',
          name: 'Achievements',
          component: () => import('@/views/achievements/index.vue'),
        },
        {
          path: '/settings',
          name: 'Settings',
          component: () => import('@/views/settings/index.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const isLoggedIn = !!localStorage.getItem('cognia-token')

  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.guest && isLoggedIn) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
