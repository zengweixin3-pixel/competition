<template>
  <div class="min-h-screen bg-dark-bg flex">
    <!-- 侧边栏 -->
    <aside class="w-64 bg-dark-card border-r border-dark-border flex flex-col">
      <!-- Logo -->
      <div class="p-6 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-gradient-primary flex items-center justify-center">
          <el-icon class="text-white text-xl"><Brain /></el-icon>
        </div>
        <div>
          <h1 class="font-bold text-lg text-text-primary">Cognia</h1>
          <p class="text-xs text-text-muted">AI学习人格教练</p>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="flex-1 px-4 py-4">
        <div class="space-y-1">
          <router-link
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300"
            :class="$route.path === item.path ? 'bg-primary/20 text-primary' : 'text-text-secondary hover:bg-dark-border hover:text-text-primary'"
          >
            <el-icon class="text-lg"><component :is="item.icon" /></el-icon>
            <span class="font-medium">{{ item.name }}</span>
            <span v-if="item.badge" class="ml-auto px-2 py-0.5 text-xs bg-accent-pink text-white rounded-full">{{ item.badge }}</span>
          </router-link>
        </div>
      </nav>

      <!-- 底部统计 -->
      <div class="p-4 border-t border-dark-border">
        <div class="bg-gradient-to-br from-primary/10 to-accent-purple/10 rounded-xl p-4 border border-primary/20">
          <div class="flex items-center gap-2 mb-2">
            <el-icon class="text-primary"><Timer /></el-icon>
            <span class="text-sm text-text-secondary">今日专注时间</span>
          </div>
          <div class="text-2xl font-bold gradient-text">{{ userStore.userInfo.todayFocusTime }}h</div>
          <div class="text-xs text-text-muted mt-1">连续学习 {{ userStore.userInfo.continuousDays }} 天</div>
        </div>
      </div>

      <!-- 用户信息 -->
      <div class="p-4 border-t border-dark-border">
        <div class="flex items-center gap-3">
          <el-avatar :size="40" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          <div class="flex-1 min-w-0">
            <div class="font-medium text-text-primary truncate">{{ userStore.userInfo.username }}</div>
            <div class="text-xs text-text-muted truncate">高数期末冲刺中...</div>
          </div>
          <el-icon class="text-text-muted cursor-pointer hover:text-text-primary"><ArrowRight /></el-icon>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="flex-1 flex flex-col min-h-screen overflow-hidden">
      <!-- 顶部栏 -->
      <header class="h-16 bg-dark-card/50 backdrop-blur-xl border-b border-dark-border flex items-center justify-between px-8">
        <div>
          <h2 class="text-xl font-bold text-text-primary">{{ pageTitle }}</h2>
          <p class="text-sm text-text-muted">{{ pageSubtitle }}</p>
        </div>
        <div class="flex items-center gap-4">
          <div class="relative">
            <el-input
              v-model="searchQuery"
              placeholder="搜索知识点、问题或教材"
              class="w-80"
              :prefix-icon="Search"
            />
          </div>
          <el-badge :value="3" class="cursor-pointer">
            <el-icon class="text-xl text-text-secondary hover:text-text-primary"><Bell /></el-icon>
          </el-badge>
          <el-avatar :size="36" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
        </div>
      </header>

      <!-- 页面内容 -->
      <div class="flex-1 overflow-auto p-8">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  HomeFilled,
  ChatDotRound,
  User,
  DocumentDelete,
  Collection,
  Calendar,
  DataLine,
  Medal,
  Setting,
  Timer,
  ArrowRight,
  Bell,
  Search,
} from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
const searchQuery = ref('')

const menuItems = [
  { name: '首页', path: '/', icon: 'HomeFilled' },
  { name: 'AI学习助手', path: '/ai-chat', icon: 'ChatDotRound' },
  { name: '学习人格', path: '/learning-dna', icon: 'User', badge: 'NEW' },
  { name: '错题分析', path: '/mistake', icon: 'DocumentDelete' },
  { name: '知识库', path: '/knowledge', icon: 'Collection' },
  { name: '情绪中心', path: '/emotion', icon: 'DataLine' },
  { name: '学习计划', path: '/study-plan', icon: 'Calendar' },
  { name: '学习报告', path: '/report', icon: 'DataLine' },
  { name: '成就中心', path: '/achievements', icon: 'Medal' },
  { name: '设置中心', path: '/settings', icon: 'Setting' },
]

const pageTitles: Record<string, { title: string; subtitle: string }> = {
  '/': { title: '晚上好，小明同学 👋', subtitle: '今天是你改变学习习惯的 第 23 天' },
  '/ai-chat': { title: 'AI学习助手', subtitle: '随时为你解答学习问题' },
  '/learning-dna': { title: '学习人格', subtitle: '了解你的学习DNA' },
  '/mistake': { title: '错题分析', subtitle: '分析错题，精准提升' },
  '/emotion': { title: '情绪中心', subtitle: '记录心情，AI会更懂你哦 💜' },
  '/study-plan': { title: '学习计划', subtitle: '制定你的专属学习计划' },
  '/report': { title: '学习报告', subtitle: '查看你的学习数据分析' },
}

const pageTitle = computed(() => pageTitles[route.path]?.title || 'Cognia')
const pageSubtitle = computed(() => pageTitles[route.path]?.subtitle || '')
</script>
